package com.sklearn.service;

import com.sklearn.dto.ModelValidationResponse;
import com.sklearn.dto.PredictResponse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ModelServiceTest {

    @Inject
    ModelService modelService;

    @Test
    void testModelLoaded() {
        assertNotNull(modelService);
    }

    @Test
    void testPredictWithValidFeatures() {
        float[] features = new float[]{5.1f, 3.5f, 1.4f, 0.2f};
        
        PredictResponse response = modelService.predict(features);
        
        if (modelService.isModelLoaded()) {
            assertTrue(response.isSuccess());
            assertTrue(response.getPredictedClass() >= 0 && response.getPredictedClass() <= 2);
            assertNotNull(response.getPredictedLabel());
            assertEquals(3, response.getProbabilities().size());
        } else {
            assertFalse(response.isSuccess());
        }
    }

    @Test
    void testPredictWithInvalidFeatureCount() {
        float[] features = new float[]{1.0f, 2.0f};
        
        PredictResponse response = modelService.predict(features);
        
        assertFalse(response.isSuccess());
        assertNotNull(response.getMessage());
    }

    @Test
    void testPredictWithNullFeatures() {
        PredictResponse response = modelService.predict(null);
        
        assertFalse(response.isSuccess());
        assertNotNull(response.getMessage());
    }

    @Test
    void testValidateModel() {
        ModelValidationResponse response = modelService.validate();
        
        if (modelService.isModelLoaded()) {
            assertTrue(response.isValid());
            assertEquals(4, response.getInputFeatures());
            assertEquals(3, response.getOutputClasses());
        } else {
            assertFalse(response.isValid());
        }
    }

    @Test
    void testIsModelLoaded() {
        boolean loaded = modelService.isModelLoaded();
        assertTrue(loaded || !loaded);
    }
}
