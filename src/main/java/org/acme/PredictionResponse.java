package org.acme;

import java.util.List;

public class PredictionResponse {
    private List<Double> predictions;
    private String status;
    private long processingTimeMs;

    public PredictionResponse() {
    }

    public PredictionResponse(List<Double> predictions, String status, long processingTimeMs) {
        this.predictions = predictions;
        this.status = status;
        this.processingTimeMs = processingTimeMs;
    }

    public List<Double> getPredictions() {
        return predictions;
    }

    public void setPredictions(List<Double> predictions) {
        this.predictions = predictions;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
}
