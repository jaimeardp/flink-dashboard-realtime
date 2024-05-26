package com.dataflow.main.serialization;

import com.dataflow.main.model.ResultVolumen;
import com.dataflow.main.model.Transaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.java.tuple.Tuple3;

import java.nio.charset.StandardCharsets;

// Define the serialization schema
//public class GenericTupleSerializationSchema implements SerializationSchema<Tuple3<String, Long, Long>> {
public class GenericTupleSerializationSchema implements SerializationSchema<ResultVolumen> {

//    @Override
//    public byte[] serialize(Tuple3<String, Long, Long> element) {
//        return element.toString().getBytes(StandardCharsets.UTF_8);
//    }
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(ResultVolumen element) {
        //LOG.debug("Application elements: Serializable 1- {}", element.toString());
        try {
            return objectMapper.writeValueAsString(element).getBytes();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize GenericRecord" + element.toString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize GenericRecord" + element.toString(), e);
        }
    }


}