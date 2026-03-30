package org.acme;

import java.util.List;

public class PredictionRequest {
    private List<Double> features;

    public PredictionRequest() {
    }

    public PredictionRequest(List<Double> features) {
        this.features = features;
    }

    public List<Double> getFeatures() {
        return features;
    }

    public void setFeatures(List<Double> features) {
        this.features = features;
    }
}
