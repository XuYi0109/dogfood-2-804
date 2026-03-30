package com.sklearn.dto;

public class ModelValidationResponse {
    private boolean valid;
    private String modelName;
    private String modelType;
    private int inputFeatures;
    private int outputClasses;
    private String message;

    public ModelValidationResponse() {}

    public static ModelValidationResponse valid(String modelName, String modelType, 
                                                 int inputFeatures, int outputClasses) {
        ModelValidationResponse response = new ModelValidationResponse();
        response.valid = true;
        response.modelName = modelName;
        response.modelType = modelType;
        response.inputFeatures = inputFeatures;
        response.outputClasses = outputClasses;
        response.message = "Model is valid and ready for predictions";
        return response;
    }

    public static ModelValidationResponse invalid(String message) {
        ModelValidationResponse response = new ModelValidationResponse();
        response.valid = false;
        response.message = message;
        return response;
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

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public int getInputFeatures() {
        return inputFeatures;
    }

    public void setInputFeatures(int inputFeatures) {
        this.inputFeatures = inputFeatures;
    }

    public int getOutputClasses() {
        return outputClasses;
    }

    public void setOutputClasses(int outputClasses) {
        this.outputClasses = outputClasses;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
