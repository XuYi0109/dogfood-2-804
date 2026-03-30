package com.mlops.model.validator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;

public class ModelMetadata {

    @JsonProperty("model_name")
    private String modelName;

    @JsonProperty("model_version")
    private String modelVersion;

    @JsonProperty("model_type")
    private String modelType;

    @JsonProperty("framework")
    private String framework;

    @JsonProperty("framework_version")
    private String frameworkVersion;

    @JsonProperty("input_features")
    private Map<String, String> inputFeatures;

    @JsonProperty("output_type")
    private String outputType;

    @JsonProperty("model_size_bytes")
    private long modelSizeBytes;

    @JsonProperty("loaded_at")
    private Instant loadedAt;

    @JsonProperty("status")
    private String status;

    @JsonProperty("error_message")
    private String errorMessage;

    public ModelMetadata() {
        this.loadedAt = Instant.now();
        this.status = "UNKNOWN";
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public String getFramework() {
        return framework;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    public String getFrameworkVersion() {
        return frameworkVersion;
    }

    public void setFrameworkVersion(String frameworkVersion) {
        this.frameworkVersion = frameworkVersion;
    }

    public Map<String, String> getInputFeatures() {
        return inputFeatures;
    }

    public void setInputFeatures(Map<String, String> inputFeatures) {
        this.inputFeatures = inputFeatures;
    }

    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    public long getModelSizeBytes() {
        return modelSizeBytes;
    }

    public void setModelSizeBytes(long modelSizeBytes) {
        this.modelSizeBytes = modelSizeBytes;
    }

    public Instant getLoadedAt() {
        return loadedAt;
    }

    public void setLoadedAt(Instant loadedAt) {
        this.loadedAt = loadedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public static ModelMetadata success(String modelName, String modelType) {
        ModelMetadata metadata = new ModelMetadata();
        metadata.setModelName(modelName);
        metadata.setModelType(modelType);
        metadata.setStatus("LOADED");
        return metadata;
    }

    public static ModelMetadata error(String modelName, String errorMessage) {
        ModelMetadata metadata = new ModelMetadata();
        metadata.setModelName(modelName);
        metadata.setStatus("ERROR");
        metadata.setErrorMessage(errorMessage);
        return metadata;
    }
}
