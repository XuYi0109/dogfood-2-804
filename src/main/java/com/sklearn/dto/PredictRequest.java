package com.sklearn.dto;

import java.util.List;

public class PredictRequest {
    private List<Double> features;

    public PredictRequest() {}

    public PredictRequest(List<Double> features) {
        this.features = features;
    }

    public List<Double> getFeatures() {
        return features;
    }

    public void setFeatures(List<Double> features) {
        this.features = features;
    }

    public float[] getFeaturesAsFloatArray() {
        if (features == null || features.isEmpty()) {
            return new float[0];
        }
        float[] result = new float[features.size()];
        for (int i = 0; i < features.size(); i++) {
            result[i] = features.get(i).floatValue();
        }
        return result;
    }
}
