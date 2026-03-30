package com.mlops.model.validator;

import com.mlops.model.validator.entity.PredictionRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasKey;

@QuarkusTest
public class ModelResourceTest {

    @Test
    public void testListModels() {
        given()
            .when()
            .get("/api/v1/models")
            .then()
            .statusCode(200)
            .body("models", notNullValue())
            .body("count", greaterThanOrEqualTo(0));
    }

    @Test
    public void testGetModelMetadata() {
        given()
            .when()
            .get("/api/v1/models/mock-model")
            .then()
            .statusCode(200)
            .body("model_name", equalTo("mock-model"))
            .body("status", equalTo("LOADED"))
            .body("framework", equalTo("scikit-learn"));
    }

    @Test
    public void testGetModelMetadataNotFound() {
        given()
            .when()
            .get("/api/v1/models/non-existent-model")
            .then()
            .statusCode(404)
            .body("error", containsString("not found"));
    }

    @Test
    public void testPredict() {
        PredictionRequest request = new PredictionRequest();
        request.setModelName("mock-model");
        request.setData(List.of(
            Map.of("feature1", 1.0, "feature2", 2.0, "feature3", 3.0),
            Map.of("feature1", 0.5, "feature2", 0.5, "feature3", 0.5)
        ));

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/models/mock-model/predict")
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("model_name", equalTo("mock-model"))
            .body("predictions", notNullValue())
            .body("predictions.size()", equalTo(2))
            .body("batch_size", equalTo(2))
            .body("inference_time_ms", greaterThanOrEqualTo(0));
    }

    @Test
    public void testPredictWithProbabilities() {
        PredictionRequest request = new PredictionRequest();
        request.setModelName("mock-model");
        request.setData(List.of(
            Map.of("feature1", 1.0, "feature2", 2.0, "feature3", 3.0)
        ));

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/models/mock-model/predict")
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("probabilities", notNullValue())
            .body("probabilities[0]", hasKey("0"))
            .body("probabilities[0]", hasKey("1"));
    }

    @Test
    public void testPredictInvalidRequest() {
        PredictionRequest request = new PredictionRequest();
        request.setModelName("mock-model");
        request.setData(List.of());

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/models/mock-model/predict")
            .then()
            .statusCode(400)
            .body("success", equalTo(false));
    }

    @Test
    public void testPredictModelNotFound() {
        PredictionRequest request = new PredictionRequest();
        request.setModelName("non-existent-model");
        request.setData(List.of(Map.of("feature1", 1.0)));

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/models/non-existent-model/predict")
            .then()
            .statusCode(400)
            .body("success", equalTo(false))
            .body("error_message", containsString("not found"));
    }

    @Test
    public void testValidateModel() {
        given()
            .when()
            .post("/api/v1/models/mock-model/validate")
            .then()
            .statusCode(200)
            .body("valid", equalTo(true))
            .body("model_name", equalTo("mock-model"))
            .body("checks", notNullValue());
    }

    @Test
    public void testValidateModelNotFound() {
        given()
            .when()
            .post("/api/v1/models/non-existent-model/validate")
            .then()
            .statusCode(400)
            .body("valid", equalTo(false))
            .body("errors", notNullValue());
    }

    @Test
    public void testValidateAllModels() {
        given()
            .when()
            .post("/api/v1/models/validate-all")
            .then()
            .statusCode(200)
            .body("valid", equalTo(true))
            .body("checks", notNullValue());
    }

    @Test
    public void testGetStats() {
        given()
            .when()
            .get("/api/v1/models/stats")
            .then()
            .statusCode(200)
            .body("engine_type", notNullValue())
            .body("loaded_models_count", greaterThanOrEqualTo(0));
    }

    @Test
    public void testUnloadModel() {
        given()
            .when()
            .post("/api/v1/models/mock-model/unload")
            .then()
            .statusCode(200)
            .body("message", containsString("unloaded successfully"));

        given()
            .when()
            .get("/api/v1/models/mock-model")
            .then()
            .statusCode(404);
    }
}
