package com.mlops.model.validator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class ValidationResult {

    @JsonProperty("valid")
    private boolean valid;

    @JsonProperty("model_name")
    private String modelName;

    @JsonProperty("checks")
    private Map<String, Boolean> checks;

    @JsonProperty("errors")
    private List<String> errors;

    @JsonProperty("warnings")
    private List<String> warnings;

    @JsonProperty("timestamp")
    private Instant timestamp;

    @JsonProperty("validation_duration_ms")
    private long validationDurationMs;

    public ValidationResult() {
        this.timestamp = Instant.now();
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Map<String, Boolean> getChecks() {
        return checks;
    }

    public void setChecks(Map<String, Boolean> checks) {
        this.checks = checks;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public long getValidationDurationMs() {
        return validationDurationMs;
    }

    public void setValidationDurationMs(long validationDurationMs) {
        this.validationDurationMs = validationDurationMs;
    }

    public static ValidationResult success(String modelName, Map<String, Boolean> checks) {
        ValidationResult result = new ValidationResult();
        result.setValid(true);
        result.setModelName(modelName);
        result.setChecks(checks);
        return result;
    }

    public static ValidationResult failure(String modelName, List<String> errors, Map<String, Boolean> checks) {
        ValidationResult result = new ValidationResult();
        result.setValid(false);
        result.setModelName(modelName);
        result.setErrors(errors);
        result.setChecks(checks);
        return result;
    }
}
