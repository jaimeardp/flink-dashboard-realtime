package com.dataflow.main;

import com.amazonaws.services.kinesisanalytics.runtime.KinesisAnalyticsRuntime;
import com.dataflow.main.model.ResultVolumen;
import com.dataflow.main.model.Transaction;
import com.dataflow.main.process.HighValueTransactionProcessFunction;
import com.dataflow.main.serialization.GenericRecordSerializationSchema;
import com.dataflow.main.serialization.GenericTupleSerializationSchema;
import com.dataflow.main.util.TimestampExtractor;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kinesis.sink.KinesisStreamsSink;
import org.apache.flink.connector.firehose.sink.KinesisFirehoseSink;
import org.apache.flink.formats.avro.AvroDeserializationSchema;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kinesis.FlinkKinesisConsumer;
import org.apache.flink.streaming.connectors.kinesis.config.AWSConfigConstants;
import org.apache.flink.streaming.connectors.kinesis.config.ConsumerConfigConstants;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.Properties;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.flink.streaming.connectors.kinesis.config.ConsumerConfigConstants.RecordPublisherType.EFO;

public class StreamDataJob {
    private static final Logger LOG = LoggerFactory.getLogger(StreamDataJob.class);

    private static final String DEFAULT_SOURCE_STREAM = "example-stream";
    private static final String DEFAULT_SINK_FIREHOSE_STREAM = "my-stream-batch-ingest";
    private static final String DEFAULT_PUBLISHER_TYPE = ConsumerConfigConstants.RecordPublisherType.POLLING.name(); // "POLLING" for standard consumer, "EFO" for Enhanced Fan-Out
    private static final String DEFAULT_EFO_CONSUMER_NAME = "sample-efo-flink-consumer";
    private static final String DEFAULT_SINK_STREAM = "example-stream-enriched";
    private static final String DEFAULT_AWS_REGION = "us-east-1";

    public static void main(String[] args) throws Exception {
        // set up the streaming execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        final ParameterTool applicationProperties = loadApplicationParameters(args, env);

        LOG.warn("Application properties: {}", applicationProperties.toMap());

        Schema avroSchema = new Schema.Parser().parse(StreamDataJob.class.getResourceAsStream("/transaction.avsc"));

        DataStream<Transaction> input = createKinesisSource(env, applicationProperties);

        WatermarkStrategy<Transaction> watermarkStrategy = WatermarkStrategy
                .<Transaction>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                .withTimestampAssigner((event, timestamp) -> event.getBlockTimestamp());

        DataStream<Transaction> transactions = input
                .assignTimestampsAndWatermarks(watermarkStrategy);

        transactions.print();

        DataStream<ResultVolumen> volumeByAsset = transactions
                .keyBy(transaction -> transaction.getAsset())
                .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
                .aggregate(new AggregateFunction<Transaction, Long, Long>() {
                    @Override
                    public Long createAccumulator() { return 0L; }
                    @Override
                    public Long add(Transaction transaction, Long accumulator) {
                        return accumulator + transaction.getValue();
                    }
                    @Override
                    public Long getResult(Long accumulator) {
                        return accumulator;
                    }
                    @Override
                    public Long merge(Long a, Long b) {
                        return a + b;
                    }
                }, new ProcessWindowFunction<Long, ResultVolumen, String, TimeWindow>() {
                    @Override
                    public void process(String asset, Context context, Iterable<Long> elements, Collector<ResultVolumen> out) {
                        long volume = elements.iterator().next();
                        long timestamp = context.window().getEnd();

                        out.collect(new ResultVolumen(asset, volume, timestamp));

                    }
                });

        volumeByAsset
                .sinkTo( createKinesisSink(applicationProperties) )
                .setParallelism(1);

        env.execute("Flink Kinesis Source and Sink examples");
    }

    private static ParameterTool loadApplicationParameters(String[] args, StreamExecutionEnvironment env) throws IOException {
        Properties properties = new Properties();
        if (env instanceof LocalStreamEnvironment) {
            return ParameterTool.fromArgs(args);
        } else {
            Map<String, Properties> applicationProperties = KinesisAnalyticsRuntime.getApplicationProperties();

            if(applicationProperties == null) {

                InputStream input = StreamDataJob.class.getClassLoader().getResourceAsStream("application.properties");
                properties.load(input);

                Properties flinkProperties = new Properties();
                properties.stringPropertyNames().stream()
                        .filter(key -> key.startsWith("FlinkApplicationProperties"))
                        .forEach(key -> flinkProperties.setProperty(key, properties.getProperty(key)));

                Map<String, String> map = flinkProperties.entrySet().stream()
                        .collect(Collectors.toMap(
                                e -> String.valueOf(e.getKey()),
                                e -> String.valueOf(e.getValue())
                        ));
                return ParameterTool.fromMap(map);


            }else{
                Properties flinkProperties = applicationProperties.get("FlinkApplicationProperties");
                Map<String, String> map = new HashMap<>(flinkProperties.size());
                flinkProperties.forEach((k, v) -> map.put((String) k, (String) v));
                return ParameterTool.fromMap(map);
            }


        }
    }

    private static DataStream<Transaction> createKinesisSource(final StreamExecutionEnvironment env, ParameterTool applicationProperties) {
        Properties inputProperties = new Properties();
        inputProperties.put(AWSConfigConstants.AWS_REGION, applicationProperties.get("kinesis.region", DEFAULT_AWS_REGION));
        inputProperties.put(ConsumerConfigConstants.STREAM_INITIAL_POSITION, applicationProperties.get("kine sis.initial.position", "LATEST"));
        inputProperties.put(ConsumerConfigConstants.RECORD_PUBLISHER_TYPE, applicationProperties.get("kinesis.publisher.type", DEFAULT_PUBLISHER_TYPE));
        if (DEFAULT_PUBLISHER_TYPE.equals(EFO.name())) {
            inputProperties.put(ConsumerConfigConstants.EFO_CONSUMER_NAME, applicationProperties.get("kinesis.efo.consumer.name", DEFAULT_EFO_CONSUMER_NAME));
        }
        return env.addSource( new FlinkKinesisConsumer<>(
                applicationProperties.get("kinesis.source.stream", DEFAULT_SOURCE_STREAM),
                new GenericRecordSerializationSchema(),
                inputProperties)
        ).setParallelism(1);
    }

    private static KinesisStreamsSink<ResultVolumen> createKinesisSink(ParameterTool applicationProperties) {

        try{
            Properties sinkProperties = new Properties();
            sinkProperties.put(AWSConfigConstants.AWS_REGION, applicationProperties.get("kinesis.region", DEFAULT_AWS_REGION));

            LOG.debug("Application elements: Setting Sink 1- {}", applicationProperties);

            return KinesisStreamsSink.<ResultVolumen>builder()
                    .setKinesisClientProperties(sinkProperties)
                    .setSerializationSchema(new GenericTupleSerializationSchema())
                    .setPartitionKeyGenerator(element -> String.valueOf(element.getWindowTimestamp()))
                    .setStreamName(applicationProperties.get("kinesis.sink.stream", DEFAULT_SINK_STREAM))
                    .build();
        }catch (Exception e){

            throw new RuntimeException("Failed to create Sink" + applicationProperties, e);

        }
    }
}
