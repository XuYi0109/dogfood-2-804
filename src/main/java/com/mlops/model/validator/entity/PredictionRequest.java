package com.mlops.model.validator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class PredictionRequest {

    @JsonProperty("model_name")
    private String modelName;

    @JsonProperty("data")
    private List<Map<String, Object>> data;

    @JsonProperty("format")
    private String format = "map";

    @JsonProperty("batch_size")
    private Integer batchSize;

    public PredictionRequest() {
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public boolean isValid() {
        return data != null && !data.isEmpty();
    }
}
