package com.dataflow.main.process;

import com.dataflow.main.model.Transaction;
import com.dataflow.main.serialization.GenericRecordSerializationSchema;
import org.apache.avro.generic.GenericRecord;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HighValueTransactionProcessFunction extends ProcessWindowFunction<Transaction, Transaction, String, TimeWindow> {

    private static final Logger LOG = LoggerFactory.getLogger(HighValueTransactionProcessFunction.class);

    private static final double HIGH_VALUE_THRESHOLD = 1000000.0; // Example threshold for high-value transactions

    @Override
    public void process(String key, Context context, Iterable<Transaction> elements, Collector<Transaction> out) {

        try{
            LOG.debug("Application elements: Transactions to iterate {}");

            for (Transaction transaction : elements) {

                LOG.debug("Application elements: Transactions {}", transaction.toString());

                double value = (double) transaction.getValue();

                if (value > HIGH_VALUE_THRESHOLD) {
                    // Process high-value transaction
                    LOG.debug("Application elements: Transactions greater 1mill {}", transaction.toString());

                    out.collect(transaction);
                }
            }

        } catch (Exception e){

            LOG.error("Error processing window function", e);

            throw new RuntimeException("Failed to process Windows transactions" , e);


        }
    }
}
