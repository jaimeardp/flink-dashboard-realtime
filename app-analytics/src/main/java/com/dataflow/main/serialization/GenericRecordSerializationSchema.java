package com.dataflow.main.serialization;

import com.dataflow.main.model.Transaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GenericRecordSerializationSchema implements DeserializationSchema<Transaction>, SerializationSchema<Transaction> {

    private static final Logger LOG = LoggerFactory.getLogger(GenericRecordSerializationSchema.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Transaction deserialize(byte[] message) throws IOException {

        try {
            LOG.debug("Application elements: 1- {}", message);
            LOG.debug("Application elements: 2- {}", message.getClass().getName());

            LOG.debug("Application elements: Object- {}", objectMapper.readValue(message, Transaction.class));


            Transaction trans = objectMapper.readValue(message, Transaction.class);

            return trans;

        } catch (JsonProcessingException e) {
            LOG.debug("Application elements: 2- {}", e);
            throw new RuntimeException("Failed to serialize GenericRecord" + message, e);
        }

    }

    @Override
    public byte[] serialize(Transaction element) {
        LOG.debug("Application elements: Serializable 1- {}", element.toString());

        try {
            return objectMapper.writeValueAsString(element).getBytes();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize GenericRecord" + element.toString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize GenericRecord" + element.toString(), e);
        }
    }

    @Override
    public boolean isEndOfStream(Transaction nextElement){
        return false;
    }

    @Override
    public TypeInformation<Transaction> getProducedType(){
        return TypeInformation.of(Transaction.class);
    }
}
