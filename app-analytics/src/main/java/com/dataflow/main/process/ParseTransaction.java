package com.dataflow.main.process;

import com.dataflow.main.StreamDataJob;
import com.dataflow.main.model.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.functions.MapFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParseTransaction implements MapFunction<String, Transaction> {

    private static final Logger LOG = LoggerFactory.getLogger(StreamDataJob.class);

    @Override
    public Transaction map(String value) throws Exception {
        // Assuming JSON format for transaction data
        ObjectMapper objectMapper = new ObjectMapper();

        LOG.warn("Application elements: ParseTransaction {}", value);

        String jsonString = objectMapper.writeValueAsString(value);

        LOG.warn("Application elements: ParseTransaction {}", jsonString);

        return objectMapper.readValue(jsonString, Transaction.class);
    }
}
