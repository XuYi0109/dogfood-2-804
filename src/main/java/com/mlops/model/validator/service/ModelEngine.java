package com.mlops.model.validator.service;

import com.mlops.model.validator.entity.ModelMetadata;
import com.mlops.model.validator.entity.PredictionRequest;
import com.mlops.model.validator.entity.PredictionResponse;
import com.mlops.model.validator.entity.ValidationResult;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ModelEngine {

    String getEngineType();

    boolean supportsModelType(String modelType);

    ModelMetadata loadModel(String modelName, InputStream modelStream) throws Exception;

    ModelMetadata loadModel(String modelName, String modelPath) throws Exception;

    Optional<ModelMetadata> getModelMetadata(String modelName);

    List<String> listLoadedModels();

    boolean unloadModel(String modelName);

    PredictionResponse predict(String modelName, PredictionRequest request);

    ValidationResult validate(String modelName);

    boolean isHealthy();

    Map<String, Object> getEngineStats();
}
