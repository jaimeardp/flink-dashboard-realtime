package com.dataflow.main.model;

import com.dataflow.main.serialization.GenericRecordSerializationSchema;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.dataflow.main.serialization.GenericRecordSerializationSchema;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultVolumen {

    //private static final Logger LOG = LoggerFactory.getLogger(GenericRecordSerializationSchema.class);
    private String asset;
    private long value;
    private long window_timestamp;

    public ResultVolumen() {

    }

    // Parameterized constructor with JsonCreator annotation
    public ResultVolumen(
            @JsonProperty("asset") String asset,
            @JsonProperty("value") long value,
            @JsonProperty("window_timestamp") long window_timestamp
    ) {
        this.asset = asset;
        this.value = value;
        this.window_timestamp = window_timestamp;
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public long getWindowTimestamp() {
        return window_timestamp;
    }

    public void setWndowTimestamp(long window_timestamp) {
        this.window_timestamp = window_timestamp;
    }

    @Override
    public String toString() {
        // Convert the transaction to a JSON string or any other format needed for Kinesis
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert transaction to string", e);
        }
    }
}

