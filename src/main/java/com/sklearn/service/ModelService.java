package com.sklearn.service;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtSession.Result;
import com.sklearn.dto.ModelValidationResponse;
import com.sklearn.dto.PredictResponse;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class ModelService {

    private static final Logger LOG = Logger.getLogger(ModelService.class);
    private static final List<String> IRIS_LABELS = Arrays.asList("setosa", "versicolor", "virginica");

    @ConfigProperty(name = "model.path", defaultValue = "models/iris_classifier.onnx")
    String modelPath;

    @ConfigProperty(name = "model.input.features", defaultValue = "4")
    int inputFeatures;

    private OrtEnvironment env;
    private OrtSession session;
    private boolean modelLoaded = false;

    void onStart(@Observes StartupEvent ev) {
        loadModel();
    }

    @PreDestroy
    void cleanup() {
        try {
            if (session != null) {
                session.close();
            }
            if (env != null) {
                env.close();
            }
            LOG.info("Model resources cleaned up");
        } catch (Exception e) {
            LOG.error("Error cleaning up model resources", e);
        }
    }

    private void loadModel() {
        try {
            env = OrtEnvironment.getEnvironment();
            
            Path onnxPath = resolveModelPath();
            
            if (onnxPath == null || !Files.exists(onnxPath)) {
                LOG.warnf("Model file not found at: %s. Service will start without model.", modelPath);
                LOG.info("To generate the model, run: cd scripts && pip install -r requirements.txt && python train_model.py");
                return;
            }

            session = env.createSession(onnxPath.toString());
            modelLoaded = true;
            LOG.infof("ONNX model loaded successfully from: %s", onnxPath);
            LOG.infof("Model input names: %s", session.getInputNames());
            LOG.infof("Model output names: %s", session.getOutputNames());
            
        } catch (OrtException e) {
            LOG.error("Failed to load ONNX model", e);
            modelLoaded = false;
        }
    }

    private Path resolveModelPath() {
        Path directPath = Path.of(modelPath);
        if (Files.exists(directPath)) {
            return directPath;
        }

        Path targetPath = Path.of("target/classes").resolve(modelPath);
        if (Files.exists(targetPath)) {
            return targetPath;
        }

        try (InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(modelPath)) {
            if (is != null) {
                Path tempFile = Files.createTempFile("model", ".onnx");
                Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
                tempFile.toFile().deleteOnExit();
                return tempFile;
            }
        } catch (Exception e) {
            LOG.debug("Could not load model from classpath", e);
        }

        return null;
    }

    public PredictResponse predict(float[] features) {
        if (!modelLoaded) {
            return PredictResponse.error("Model not loaded. Please check model file exists.");
        }

        if (features == null || features.length != inputFeatures) {
            return PredictResponse.error(
                String.format("Invalid input: expected %d features, got %d", 
                    inputFeatures, features == null ? 0 : features.length));
        }

        try {
            OnnxTensor inputTensor = OnnxTensor.createTensor(env, new float[][]{features});
            
            String inputName = session.getInputNames().iterator().next();
            java.util.Map<String, OnnxTensor> inputs = java.util.Collections.singletonMap(inputName, inputTensor);
            
            try (Result result = session.run(inputs)) {
                float[][] probabilities = (float[][]) result.get(0).getValue();
                
                int predictedClass = argMax(probabilities[0]);
                String predictedLabel = IRIS_LABELS.get(predictedClass);
                List<Double> probs = toDoubleList(probabilities[0]);
                
                return new PredictResponse(predictedClass, predictedLabel, probs);
            } finally {
                inputTensor.close();
            }
            
        } catch (OrtException e) {
            LOG.error("Prediction failed", e);
            return PredictResponse.error("Prediction failed: " + e.getMessage());
        }
    }

    public ModelValidationResponse validate() {
        if (!modelLoaded) {
            return ModelValidationResponse.invalid("Model not loaded");
        }

        try {
            String modelName = session.getInputNames().iterator().next();
            int outputCount = session.getOutputNames().size();
            
            return ModelValidationResponse.valid(
                "iris_classifier",
                "RandomForestClassifier",
                inputFeatures,
                IRIS_LABELS.size()
            );
        } catch (Exception e) {
            return ModelValidationResponse.invalid("Validation failed: " + e.getMessage());
        }
    }

    public boolean isModelLoaded() {
        return modelLoaded;
    }

    private int argMax(float[] array) {
        int maxIndex = 0;
        float maxValue = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxValue) {
                maxValue = array[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private List<Double> toDoubleList(float[] array) {
        List<Double> list = new ArrayList<>(array.length);
        for (float v : array) {
            list.add((double) v);
        }
        return list;
    }
}
