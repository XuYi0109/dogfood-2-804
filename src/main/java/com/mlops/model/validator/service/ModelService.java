package com.mlops.model.validator.service;

import com.mlops.model.validator.entity.ModelMetadata;
import com.mlops.model.validator.entity.PredictionRequest;
import com.mlops.model.validator.entity.PredictionResponse;
import com.mlops.model.validator.entity.ValidationResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class ModelService {

    private static final Logger LOG = Logger.getLogger(ModelService.class);

    @Inject
    ModelEngine modelEngine;

    public ModelMetadata loadModel(String modelName, InputStream modelStream) throws Exception {
        LOG.info("Loading model: " + modelName);
        return modelEngine.loadModel(modelName, modelStream);
    }

    public ModelMetadata loadModel(String modelName, String modelPath) throws Exception {
        LOG.info("Loading model from path: " + modelPath);
        return modelEngine.loadModel(modelName, modelPath);
    }

    public Optional<ModelMetadata> getModelMetadata(String modelName) {
        return modelEngine.getModelMetadata(modelName);
    }

    public List<String> listLoadedModels() {
        return modelEngine.listLoadedModels();
    }

    public boolean unloadModel(String modelName) {
        LOG.info("Unloading model: " + modelName);
        return modelEngine.unloadModel(modelName);
    }

    public PredictionResponse predict(String modelName, PredictionRequest request) {
        LOG.debug("Prediction request for model: " + modelName);
        return modelEngine.predict(modelName, request);
    }

    public ValidationResult validateModel(String modelName) {
        LOG.info("Validating model: " + modelName);
        return modelEngine.validate(modelName);
    }

    public ValidationResult validateAllModels() {
        LOG.info("Validating all loaded models");
        List<String> models = modelEngine.listLoadedModels();

        if (models.isEmpty()) {
            ValidationResult result = new ValidationResult();
            result.setValid(false);
            result.setModelName("ALL");
            result.setErrors(List.of("No models loaded"));
            return result;
        }

        boolean allValid = true;
        List<String> errors = new java.util.ArrayList<>();

        for (String modelName : models) {
            ValidationResult result = modelEngine.validate(modelName);
            if (!result.isValid()) {
                allValid = false;
                errors.addAll(result.getErrors());
            }
        }

        ValidationResult summary = new ValidationResult();
        summary.setValid(allValid);
        summary.setModelName("ALL");
        summary.setErrors(errors);
        Map<String, Boolean> checks = new java.util.HashMap<>();
        checks.put("models_checked", true);
        checks.put("all_valid", allValid);
        summary.setChecks(checks);

        return summary;
    }

    public boolean isHealthy() {
        return modelEngine.isHealthy();
    }

    public Map<String, Object> getServiceStats() {
        return modelEngine.getEngineStats();
    }
}
