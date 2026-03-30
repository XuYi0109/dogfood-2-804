package com.mlops.model.validator;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@QuarkusTest
public class HealthResourceTest {

    @Test
    public void testLivenessCheck() {
        given()
            .when()
            .get("/health/live")
            .then()
            .statusCode(200)
            .body("service", notNullValue())
            .body("status", equalTo("UP"));
    }

    @Test
    public void testReadinessCheck() {
        given()
            .when()
            .get("/health/ready")
            .then()
            .statusCode(200)
            .body("service", notNullValue())
            .body("status", equalTo("UP"))
            .body("models_loaded", greaterThanOrEqualTo(0))
            .body("models", notNullValue());
    }

    @Test
    public void testHealthCheck() {
        given()
            .when()
            .get("/health")
            .then()
            .statusCode(200)
            .body("status", equalTo("UP"))
            .body("checks", notNullValue())
            .body("checks.models", notNullValue())
            .body("checks.service", notNullValue());
    }

    @Test
    public void testDetailedStatus() {
        given()
            .when()
            .get("/health/status")
            .then()
            .statusCode(200)
            .body("status", equalTo("UP"))
            .body("service", notNullValue())
            .body("version", notNullValue())
            .body("uptime_seconds", greaterThanOrEqualTo(0))
            .body("checks", notNullValue())
            .body("checks.model_service", notNullValue())
            .body("checks.jvm", notNullValue());
    }
}
