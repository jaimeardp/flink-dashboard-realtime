package com.dataflow.main.model;

import com.dataflow.main.serialization.GenericRecordSerializationSchema;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Transaction {

    private static final Logger LOG = LoggerFactory.getLogger(GenericRecordSerializationSchema.class);

    private String from_address;
    private String to_address;
    private long value;
    private long block_timestamp;
    private String transaction_type;
    private String asset;
    private String feed;

    public Transaction() {

    }

    // Parameterized constructor with JsonCreator annotation
    public Transaction(
            @JsonProperty("from_address") String from_address,
            @JsonProperty("to_address") String to_address,
            @JsonProperty("value") long value,
            @JsonProperty("block_timestamp") long block_timestamp,
            @JsonProperty("transaction_type") String transaction_type,
            @JsonProperty("asset") String asset
    ) {
        this.from_address = from_address;
        this.to_address = to_address;
        this.value = value;
        this.block_timestamp = block_timestamp;
        this.transaction_type = transaction_type;
        this.asset = asset;
    }
    

    // Getters and setters
    public String getFromAddress() {

        LOG.debug("Application elements: Get FromAddress 1- {}", from_address);

        return from_address;
    }

    public void setFromAddress(String from_address) {
        this.from_address = from_address;
    }

    public String getToAddress() {
        return to_address;
    }

    public void setToAddress(String to_address) {
        this.to_address = to_address;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public long getBlockTimestamp() {
        return block_timestamp;
    }

    public void setBlockTimestamp(long block_timestamp) {
        this.block_timestamp = block_timestamp;
    }

    public String getTransactionType() {
        return transaction_type;
    }

    public void setTransactionType(String transaction_type) {
        this.transaction_type = transaction_type;
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
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
