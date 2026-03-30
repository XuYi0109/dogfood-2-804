package com.mlops.model.validator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class PredictionResponse {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("model_name")
    private String modelName;

    @JsonProperty("predictions")
    private List<Object> predictions;

    @JsonProperty("probabilities")
    private List<Map<String, Double>> probabilities;

    @JsonProperty("inference_time_ms")
    private long inferenceTimeMs;

    @JsonProperty("timestamp")
    private Instant timestamp;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("batch_size")
    private int batchSize;

    public PredictionResponse() {
        this.timestamp = Instant.now();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public List<Object> getPredictions() {
        return predictions;
    }

    public void setPredictions(List<Object> predictions) {
        this.predictions = predictions;
    }

    public List<Map<String, Double>> getProbabilities() {
        return probabilities;
    }

    public void setProbabilities(List<Map<String, Double>> probabilities) {
        this.probabilities = probabilities;
    }

    public long getInferenceTimeMs() {
        return inferenceTimeMs;
    }

    public void setInferenceTimeMs(long inferenceTimeMs) {
        this.inferenceTimeMs = inferenceTimeMs;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public static PredictionResponse success(String modelName, List<Object> predictions, long inferenceTimeMs) {
        PredictionResponse response = new PredictionResponse();
        response.setSuccess(true);
        response.setModelName(modelName);
        response.setPredictions(predictions);
        response.setInferenceTimeMs(inferenceTimeMs);
        response.setBatchSize(predictions != null ? predictions.size() : 0);
        return response;
    }

    public static PredictionResponse error(String modelName, String errorMessage) {
        PredictionResponse response = new PredictionResponse();
        response.setSuccess(false);
        response.setModelName(modelName);
        response.setErrorMessage(errorMessage);
        return response;
    }

    public static PredictionResponse withProbabilities(String modelName, List<Object> predictions,
                                                        List<Map<String, Double>> probabilities, long inferenceTimeMs) {
        PredictionResponse response = success(modelName, predictions, inferenceTimeMs);
        response.setProbabilities(probabilities);
        return response;
    }
}
