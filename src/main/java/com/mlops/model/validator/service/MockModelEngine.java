package com.mlops.model.validator.service;

import com.mlops.model.validator.entity.ModelMetadata;
import com.mlops.model.validator.entity.PredictionRequest;
import com.mlops.model.validator.entity.PredictionResponse;
import com.mlops.model.validator.entity.ValidationResult;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class MockModelEngine implements ModelEngine {

    private static final Logger LOG = Logger.getLogger(MockModelEngine.class);

    private final Map<String, ModelMetadata> loadedModels = new ConcurrentHashMap<>();
    private final Map<String, Object> modelData = new ConcurrentHashMap<>();
    private final AtomicInteger predictionCount = new AtomicInteger(0);
    private final AtomicLong totalInferenceTime = new AtomicLong(0);

    @ConfigProperty(name = "model.default", defaultValue = "model.joblib")
    String defaultModelName;

    @ConfigProperty(name = "model.path", defaultValue = "models")
    String modelPath;

    void onStart(@Observes StartupEvent ev) {
        LOG.info("Initializing MockModelEngine...");
        initializeDefaultModel();
    }

    private void initializeDefaultModel() {
        try {
            Path path = Paths.get(modelPath, defaultModelName);
            if (Files.exists(path)) {
                loadModel("default", path.toString());
                LOG.info("Default model loaded successfully from: " + path);
            } else {
                LOG.warn("Default model not found at: " + path + ", creating mock model");
                createMockModel();
            }
        } catch (Exception e) {
            LOG.warn("Could not load default model, creating mock model: " + e.getMessage());
            createMockModel();
        }
    }

    private void createMockModel() {
        ModelMetadata metadata = new ModelMetadata();
        metadata.setModelName("mock-model");
        metadata.setModelVersion("1.0.0");
        metadata.setModelType("classification");
        metadata.setFramework("scikit-learn");
        metadata.setFrameworkVersion("1.3.0");
        metadata.setStatus("LOADED");
        metadata.setInputFeatures(Map.of(
            "feature1", "float",
            "feature2", "float",
            "feature3", "float"
        ));
        metadata.setOutputType("int");
        metadata.setModelSizeBytes(1024);

        loadedModels.put("mock-model", metadata);
        modelData.put("mock-model", new Object());
        LOG.info("Mock model created successfully");
    }

    @Override
    public String getEngineType() {
        return "MOCK";
    }

    @Override
    public boolean supportsModelType(String modelType) {
        return modelType != null && (
            modelType.equalsIgnoreCase("joblib") ||
            modelType.equalsIgnoreCase("pkl") ||
            modelType.equalsIgnoreCase("mock")
        );
    }

    @Override
    public ModelMetadata loadModel(String modelName, InputStream modelStream) throws Exception {
        LOG.info("Loading model from stream: " + modelName);

        ModelMetadata metadata = new ModelMetadata();
        metadata.setModelName(modelName);
        metadata.setModelVersion("1.0.0");
        metadata.setModelType("classification");
        metadata.setFramework("scikit-learn");
        metadata.setFrameworkVersion("1.3.0");
        metadata.setStatus("LOADED");
        metadata.setInputFeatures(Map.of(
            "feature1", "float",
            "feature2", "float",
            "feature3", "float"
        ));
        metadata.setOutputType("int");
        metadata.setModelSizeBytes(modelStream.available());

        loadedModels.put(modelName, metadata);
        modelData.put(modelName, new Object());

        LOG.info("Model loaded successfully: " + modelName);
        return metadata;
    }

    @Override
    public ModelMetadata loadModel(String modelName, String modelPath) throws Exception {
        LOG.info("Loading model from path: " + modelPath);

        Path path = Paths.get(modelPath);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Model file not found: " + modelPath);
        }

        ModelMetadata metadata = new ModelMetadata();
        metadata.setModelName(modelName);
        metadata.setModelVersion("1.0.0");
        metadata.setModelType("classification");
        metadata.setFramework("scikit-learn");
        metadata.setFrameworkVersion("1.3.0");
        metadata.setStatus("LOADED");
        metadata.setInputFeatures(Map.of(
            "feature1", "float",
            "feature2", "float",
            "feature3", "float"
        ));
        metadata.setOutputType("int");
        metadata.setModelSizeBytes(Files.size(path));

        loadedModels.put(modelName, metadata);
        modelData.put(modelName, new Object());

        LOG.info("Model loaded successfully: " + modelName);
        return metadata;
    }

    @Override
    public Optional<ModelMetadata> getModelMetadata(String modelName) {
        return Optional.ofNullable(loadedModels.get(modelName));
    }

    @Override
    public List<String> listLoadedModels() {
        return new ArrayList<>(loadedModels.keySet());
    }

    @Override
    public boolean unloadModel(String modelName) {
        LOG.info("Unloading model: " + modelName);
        loadedModels.remove(modelName);
        modelData.remove(modelName);
        return true;
    }

    @Override
    public PredictionResponse predict(String modelName, PredictionRequest request) {
        long startTime = System.currentTimeMillis();

        ModelMetadata metadata = loadedModels.get(modelName);
        if (metadata == null) {
            return PredictionResponse.error(modelName, "Model not found: " + modelName);
        }

        if (!request.isValid()) {
            return PredictionResponse.error(modelName, "Invalid request: data is empty");
        }

        try {
            Thread.sleep(1);

            List<Object> predictions = new ArrayList<>();
            List<Map<String, Double>> probabilities = new ArrayList<>();

            for (Map<String, Object> row : request.getData()) {
                double feature1 = getDoubleValue(row.getOrDefault("feature1", 0));
                double feature2 = getDoubleValue(row.getOrDefault("feature2", 0));
                double feature3 = getDoubleValue(row.getOrDefault("feature3", 0));

                double sum = feature1 + feature2 + feature3;
                int prediction = sum > 1.5 ? 1 : 0;

                predictions.add(prediction);

                Map<String, Double> prob = new HashMap<>();
                double probClass0 = 1.0 / (1.0 + Math.exp(sum - 1.5));
                prob.put("0", probClass0);
                prob.put("1", 1.0 - probClass0);
                probabilities.add(prob);
            }

            long inferenceTime = System.currentTimeMillis() - startTime;
            predictionCount.incrementAndGet();
            totalInferenceTime.addAndGet(inferenceTime);

            return PredictionResponse.withProbabilities(modelName, predictions, probabilities, inferenceTime);

        } catch (Exception e) {
            LOG.error("Prediction failed", e);
            return PredictionResponse.error(modelName, "Prediction failed: " + e.getMessage());
        }
    }

    private double getDoubleValue(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    @Override
    public ValidationResult validate(String modelName) {
        long startTime = System.currentTimeMillis();

        Map<String, Boolean> checks = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        ModelMetadata metadata = loadedModels.get(modelName);

        checks.put("model_exists", metadata != null);
        if (metadata == null) {
            errors.add("Model not found: " + modelName);
        } else {
            checks.put("model_loaded", "LOADED".equals(metadata.getStatus()));
            checks.put("has_input_features", metadata.getInputFeatures() != null && !metadata.getInputFeatures().isEmpty());
            checks.put("has_output_type", metadata.getOutputType() != null);

            if (!"LOADED".equals(metadata.getStatus())) {
                errors.add("Model is not in LOADED state: " + metadata.getStatus());
            }
            if (metadata.getInputFeatures() == null || metadata.getInputFeatures().isEmpty()) {
                warnings.add("Model has no input features defined");
            }
        }

        boolean valid = errors.isEmpty();
        long duration = System.currentTimeMillis() - startTime;

        ValidationResult result = valid
            ? ValidationResult.success(modelName, checks)
            : ValidationResult.failure(modelName, errors, checks);
        result.setValidationDurationMs(duration);
        result.setWarnings(warnings);

        return result;
    }

    @Override
    public boolean isHealthy() {
        return !loadedModels.isEmpty();
    }

    @Override
    public Map<String, Object> getEngineStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("engine_type", getEngineType());
        stats.put("loaded_models_count", loadedModels.size());
        stats.put("prediction_count", predictionCount.get());
        stats.put("total_inference_time_ms", totalInferenceTime.get());
        stats.put("average_inference_time_ms",
            predictionCount.get() > 0 ? totalInferenceTime.get() / predictionCount.get() : 0);
        return stats;
    }
}
