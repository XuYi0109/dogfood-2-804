package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class ModelResourceTest {

    @Test
    public void testHealthCheckEndpoint() {
        given()
                .when().get("/q/health")
                .then()
                .statusCode(200)
                .body("checks", not(empty()));
    }

    @Test
    public void testModelValidationEndpoint() {
        given()
                .when().get("/api/model/validate")
                .then()
                .statusCode(200)
                .body("valid", is(true))
                .body("modelType", notNullValue())
                .body("version", notNullValue());
    }

    @Test
    public void testModelInfoEndpoint() {
        given()
                .when().get("/api/model/info")
                .then()
                .statusCode(200)
                .body("valid", is(true));
    }

    @Test
    public void testPredictionEndpoint() {
        List<Double> features = Arrays.asList(1.0, 2.0, 3.0, 4.0);
        PredictionRequest request = new PredictionRequest(features);

        given()
                .contentType("application/json")
                .body(request)
                .when().post("/api/predict")
                .then()
                .statusCode(200)
                .body("status", is("success"))
                .body("predictions", hasSize(4))
                .body("processingTimeMs", greaterThanOrEqualTo(0));
    }

    @Test
    public void testPredictionWithEmptyFeatures() {
        List<Double> features = Arrays.asList();
        PredictionRequest request = new PredictionRequest(features);

        given()
                .contentType("application/json")
                .body(request)
                .when().post("/api/predict")
                .then()
                .statusCode(200)
                .body("status", is("success"))
                .body("predictions", hasSize(1));
    }

    @Test
    public void testLivenessProbe() {
        given()
                .when().get("/q/health/live")
                .then()
                .statusCode(200);
    }

    @Test
    public void testReadinessProbe() {
        given()
                .when().get("/q/health/ready")
                .then()
                .statusCode(200);
    }
}
