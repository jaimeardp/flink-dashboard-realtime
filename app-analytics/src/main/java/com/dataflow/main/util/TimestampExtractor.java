package com.dataflow.main.util;

import com.dataflow.main.StreamDataJob;
import com.dataflow.main.model.Transaction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.eventtime.TimestampAssigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimestampExtractor {
    private static final Logger LOG = LoggerFactory.getLogger(StreamDataJob.class);

    public static long extractTimestamp(Transaction element) {
        try {
            LOG.warn("Application elements: {}", element);

            LOG.debug("Application elements: {}", element.getClass().getName());

            LOG.debug("Application elements: Timestamp {}", element.getBlockTimestamp());


            return element.getBlockTimestamp();
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract timestamp from JSON", e);
        }
    }
}
