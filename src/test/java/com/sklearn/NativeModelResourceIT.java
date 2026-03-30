package com.sklearn;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusIntegrationTest
class NativeModelResourceIT {

    @Test
    void testHealthEndpointNative() {
        given()
            .when()
            .get("/q/health")
            .then()
            .statusCode(200);
    }

    @Test
    void testModelStatusNative() {
        given()
            .when()
            .get("/api/model/status")
            .then()
            .statusCode(200)
            .body("modelLoaded", is(true));
    }

    @Test
    void testPredictNative() {
        String requestBody = "{\"features\": [5.1, 3.5, 1.4, 0.2]}";
        
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/api/predict")
            .then()
            .statusCode(200)
            .body("success", is(true))
            .body("predictedClass", greaterThanOrEqualTo(0))
            .body("predictedLabel", notNullValue())
            .body("probabilities", hasSize(3));
    }
}
