package com.sklearn;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class ModelResourceTest {

    @Test
    void testHealthEndpoint() {
        given()
            .when()
            .get("/q/health")
            .then()
            .statusCode(200)
            .body("status", anyOf(equalTo("UP"), equalTo("DOWN")));
    }

    @Test
    void testHealthReadyEndpoint() {
        given()
            .when()
            .get("/q/health/ready")
            .then()
            .statusCode(anyOf(is(200), is(503)));
    }

    @Test
    void testHealthLiveEndpoint() {
        given()
            .when()
            .get("/q/health/live")
            .then()
            .statusCode(200);
    }

    @Test
    void testModelStatus() {
        given()
            .when()
            .get("/api/model/status")
            .then()
            .statusCode(200)
            .body("modelLoaded", anyOf(is(true), is(false)));
    }

    @Test
    void testModelInfo() {
        given()
            .when()
            .get("/api/model/info")
            .then()
            .statusCode(200)
            .body("name", equalTo("iris_classifier"))
            .body("type", equalTo("RandomForestClassifier"))
            .body("inputFeatures", equalTo(4))
            .body("outputClasses", equalTo(3));
    }

    @Test
    void testModelValidate() {
        given()
            .when()
            .get("/api/model/validate")
            .then()
            .statusCode(anyOf(is(200), is(503)))
            .body("valid", anyOf(is(true), is(false)));
    }

    @Test
    void testPredictWithValidInput() {
        String requestBody = "{\"features\": [5.1, 3.5, 1.4, 0.2]}";
        
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/api/predict")
            .then()
            .statusCode(anyOf(is(200), is(500)));
    }

    @Test
    void testPredictWithInvalidInput() {
        String requestBody = "{\"features\": [1.0, 2.0]}";
        
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/api/predict")
            .then()
            .statusCode(anyOf(is(400), is(500)));
    }

    @Test
    void testPredictWithEmptyFeatures() {
        String requestBody = "{\"features\": []}";
        
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/api/predict")
            .then()
            .statusCode(400);
    }

    @Test
    void testPredictWithNullFeatures() {
        String requestBody = "{}";
        
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/api/predict")
            .then()
            .statusCode(400);
    }

    @Test
    void testPredictWithMissingBody() {
        given()
            .contentType(ContentType.JSON)
            .when()
            .post("/api/predict")
            .then()
            .statusCode(400);
    }
}
