package org.acme;

public class ModelValidationResponse {
    private boolean valid;
    private String modelType;
    private String version;
    private String message;

    public ModelValidationResponse() {
    }

    public ModelValidationResponse(boolean valid, String modelType, String version, String message) {
        this.valid = valid;
        this.modelType = modelType;
        this.version = version;
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
