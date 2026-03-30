package com.sklearn.dto;

import java.util.List;

public class PredictResponse {
    private int predictedClass;
    private String predictedLabel;
    private List<Double> probabilities;
    private boolean success;
    private String message;

    public PredictResponse() {}

    public PredictResponse(int predictedClass, String predictedLabel, List<Double> probabilities) {
        this.predictedClass = predictedClass;
        this.predictedLabel = predictedLabel;
        this.probabilities = probabilities;
        this.success = true;
    }

    public static PredictResponse error(String message) {
        PredictResponse response = new PredictResponse();
        response.success = false;
        response.message = message;
        return response;
    }

    public int getPredictedClass() {
        return predictedClass;
    }

    public void setPredictedClass(int predictedClass) {
        this.predictedClass = predictedClass;
    }

    public String getPredictedLabel() {
        return predictedLabel;
    }

    public void setPredictedLabel(String predictedLabel) {
        this.predictedLabel = predictedLabel;
    }

    public List<Double> getProbabilities() {
        return probabilities;
    }

    public void setProbabilities(List<Double> probabilities) {
        this.probabilities = probabilities;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
