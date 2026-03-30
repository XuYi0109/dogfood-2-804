package org.acme;

import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@ApplicationScoped
@Startup
public class ModelService {
    private static final Logger LOG = Logger.getLogger(ModelService.class);

    @ConfigProperty(name = "model.path")
    String modelPath;

    @ConfigProperty(name = "model.enabled")
    boolean modelEnabled;

    private boolean modelLoaded = false;
    private String modelType = "MockScikitLearnModel";
    private String modelVersion = "1.0.0";

    @PostConstruct
    void init() {
        loadModel();
    }

    public void loadModel() {
        try {
            LOG.info("Attempting to load model from: " + modelPath);

            if (modelEnabled) {
                File modelFile = new File(modelPath);
                if (modelFile.exists()) {
                    LOG.info("Model file found. Loading...");
                    modelLoaded = true;
                    LOG.info("Model loaded successfully.");
                } else {
                    LOG.warn("Model file not found at: " + modelPath);
                    LOG.info("Using mock model for demonstration purposes.");
                    modelLoaded = true;
                }
            } else {
                LOG.info("Model loading disabled. Using mock model.");
                modelLoaded = true;
            }
        } catch (Exception e) {
            LOG.error("Failed to load model", e);
            modelLoaded = false;
        }
    }

    public PredictionResponse predict(PredictionRequest request) {
        long startTime = System.currentTimeMillis();

        List<Double> predictions = new ArrayList<>();

        if (modelLoaded) {
            List<Double> features = request.getFeatures();
            if (features != null && !features.isEmpty()) {
                for (int i = 0; i < features.size(); i++) {
                    double prediction = features.get(i) * 2.5 + ThreadLocalRandom.current().nextDouble(-0.5, 0.5);
                    predictions.add(Math.round(prediction * 1000.0) / 1000.0);
                }
            } else {
                predictions.add(0.0);
            }
        }

        long processingTime = System.currentTimeMillis() - startTime;
        return new PredictionResponse(predictions, modelLoaded ? "success" : "error", processingTime);
    }

    public ModelValidationResponse validateModel() {
        if (modelLoaded) {
            return new ModelValidationResponse(
                true,
                modelType,
                modelVersion,
                "Model is valid and ready for predictions"
            );
        } else {
            return new ModelValidationResponse(
                false,
                modelType,
                modelVersion,
                "Model failed to load"
            );
        }
    }

    public boolean isModelLoaded() {
        return modelLoaded;
    }
}
