package com.mlops.model.validator;

import com.mlops.model.validator.entity.PredictionRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CICDIntegrationTest {

    @Test
    @Order(1)
    @DisplayName("[CICD] 服务健康检查 - 验证服务已启动")
    public void testServiceHealth() {
        given()
            .when()
            .get("/health/ready")
            .then()
            .statusCode(200)
            .body("status", equalTo("UP"));
    }

    @Test
    @Order(2)
    @DisplayName("[CICD] 模型列表检查 - 验证至少有一个模型加载")
    public void testModelList() {
        given()
            .when()
            .get("/api/v1/models")
            .then()
            .statusCode(200)
            .body("count", greaterThanOrEqualTo(1))
            .body("models", notNullValue());
    }

    @Test
    @Order(3)
    @DisplayName("[CICD] 模型元数据检查 - 验证模型元数据完整性")
    public void testModelMetadata() {
        given()
            .when()
            .get("/api/v1/models/mock-model")
            .then()
            .statusCode(200)
            .body("model_name", notNullValue())
            .body("status", equalTo("LOADED"))
            .body("framework", equalTo("scikit-learn"))
            .body("input_features", notNullValue())
            .body("output_type", notNullValue());
    }

    @Test
    @Order(4)
    @DisplayName("[CICD] 模型预测功能 - 验证预测接口正常工作")
    public void testModelPrediction() {
        PredictionRequest request = new PredictionRequest();
        request.setData(List.of(
            Map.of("feature1", 1.0, "feature2", 2.0, "feature3", 3.0),
            Map.of("feature1", 0.1, "feature2", 0.2, "feature3", 0.3)
        ));

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/models/mock-model/predict")
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("predictions", notNullValue())
            .body("predictions.size()", equalTo(2))
            .body("inference_time_ms", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(5)
    @DisplayName("[CICD] 模型验证功能 - 验证模型验证接口")
    public void testModelValidation() {
        given()
            .when()
            .post("/api/v1/models/mock-model/validate")
            .then()
            .statusCode(200)
            .body("valid", equalTo(true))
            .body("checks.model_exists", equalTo(true))
            .body("checks.model_loaded", equalTo(true));
    }

    @Test
    @Order(6)
    @DisplayName("[CICD] 所有模型验证 - 验证所有模型都通过验证")
    public void testAllModelsValidation() {
        given()
            .when()
            .post("/api/v1/models/validate-all")
            .then()
            .statusCode(200)
            .body("valid", equalTo(true))
            .body("checks.all_valid", equalTo(true));
    }

    @Test
    @Order(7)
    @DisplayName("[CICD] 服务统计信息 - 验证统计接口")
    public void testServiceStats() {
        given()
            .when()
            .get("/api/v1/models/stats")
            .then()
            .statusCode(200)
            .body("engine_type", notNullValue())
            .body("loaded_models_count", greaterThanOrEqualTo(1))
            .body("prediction_count", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(8)
    @DisplayName("[CICD] 预测边界测试 - 验证边界值处理")
    public void testPredictionEdgeCases() {
        PredictionRequest request = new PredictionRequest();
        request.setData(List.of(
            Map.of("feature1", 0.0, "feature2", 0.0, "feature3", 0.0),
            Map.of("feature1", 100.0, "feature2", 100.0, "feature3", 100.0)
        ));

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/models/mock-model/predict")
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("predictions.size()", equalTo(2));
    }

    @Test
    @Order(9)
    @DisplayName("[CICD] 错误处理测试 - 验证无效请求处理")
    public void testErrorHandling() {
        PredictionRequest request = new PredictionRequest();
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
    @Order(10)
    @DisplayName("[CICD] 端到端流水线测试 - 完整验证流程")
    public void testEndToEndPipeline() {
        given()
            .when()
            .get("/health")
            .then()
            .statusCode(200)
            .body("status", equalTo("UP"));

        given()
            .when()
            .post("/api/v1/models/mock-model/validate")
            .then()
            .statusCode(200)
            .body("valid", equalTo(true));

        PredictionRequest request = new PredictionRequest();
        request.setData(List.of(Map.of("feature1", 1.5, "feature2", 2.5, "feature3", 3.5)));

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/models/mock-model/predict")
            .then()
            .statusCode(200)
            .body("success", equalTo(true));
    }
}
