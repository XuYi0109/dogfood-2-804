# Scikit-learn Model Service

在 CICD 平台中自动验证 scikit-learn 模型可用性的标准服务框架。

## 需求

- 在 CICD 平台中自动验证 scikit-learn 模型可用性
- 提供标准化模型服务接口，支持预测 / 校验 / 健康检查
- 可编译为原生镜像（GraalVM Native Image），超轻量、秒启动
- 用于 Jenkins/GitLab CI/GitHub Actions 自动化测试与部署门禁
- 纯可验证工程：一键构建、一键测试、一键运行

## 功能

- 加载并验证 scikit-learn 模型文件（ONNX 格式）
- 提供 HTTP 接口完成模型预测
- CICD 自动化测试用例（可直接在流水线运行）
- 健康检查接口，用于部署校验
- 支持 JVM 模式 + GraalVM 原生镜像模式
- 可容器化部署（Dockerfile 自动生成）

## 技术栈

- 开发语言：Java 17+
- 微服务框架：Quarkus（云原生 / 快速构建）
- 原生编译：GraalVM
- 模型引擎：scikit-learn 模型 → ONNX 格式
- 模型调用：ONNX Runtime（无需 Python 依赖）
- 构建工具：Maven
- 测试：JUnit 5 + RestAssured
- 部署：Docker / CICD 流水线

## 项目结构

```
sklearn-model-service/
├── pom.xml                          # Maven 配置
├── README.md                        # 项目说明
├── Jenkinsfile                      # Jenkins 流水线
├── .gitlab-ci.yml                   # GitLab CI 配置
├── .github/workflows/build.yml      # GitHub Actions 配置
├── scripts/
│   ├── train_model.py               # 训练并导出 ONNX 模型
│   ├── requirements.txt             # Python 依赖
│   ├── test-api.sh                  # Linux/Mac 测试脚本
│   └── test-api.bat                 # Windows 测试脚本
├── src/
│   ├── main/
│   │   ├── java/com/sklearn/
│   │   │   ├── SklearnModelServiceApplication.java
│   │   │   ├── dto/
│   │   │   │   ├── PredictRequest.java
│   │   │   │   ├── PredictResponse.java
│   │   │   │   └── ModelValidationResponse.java
│   │   │   ├── resource/
│   │   │   │   └── ModelResource.java
│   │   │   └── service/
│   │   │       └── ModelService.java
│   │   ├── resources/
│   │   │   ├── application.properties
│   │   │   └── META-INF/native-image/  # GraalVM 配置
│   │   └── docker/
│   │       ├── Dockerfile.jvm
│   │       ├── Dockerfile.native
│   │       └── Dockerfile.jvm.build
│   └── test/java/com/sklearn/
│       ├── ModelResourceTest.java
│       ├── NativeModelResourceTest.java
│       └── service/ModelServiceTest.java
└── models/                          # 模型文件目录
    └── iris_classifier.onnx         # 示例模型（需生成）
```

## 快速开始

### 1. 环境准备

```bash
# 安装 Python 依赖并生成模型
cd scripts
pip install -r requirements.txt
python train_model.py
cd ..
```

### 2. 开发模式

```bash
# Windows
mvnw.cmd compile quarkus:dev

# Linux/Mac
./mvnw compile quarkus:dev
```

### 3. 测试

```bash
# 运行单元测试
mvnw test

# 运行所有测试（含集成测试）
mvnw verify
```

### 4. 构建 JVM 模式

```bash
# 打包
mvnw package

# 运行
java -jar target/quarkus-app/quarkus-run.jar
```

### 5. 构建原生镜像（需要 GraalVM）

```bash
# 安装 GraalVM (推荐使用 SDKMAN)
sdk install java 17.0.9-graalce

# 构建
mvnw package -Dnative

# 运行
target/sklearn-model-service-1.0.0-SNAPSHOT-runner
```

### 6. Docker 构建

```bash
# JVM 模式（需要先生成模型和打包）
mvnw package -DskipTests
docker build -f src/main/docker/Dockerfile.jvm -t sklearn-model-service:jvm .

# 一键构建（包含模型训练）
docker build -f src/main/docker/Dockerfile.jvm.build -t sklearn-model-service:latest .

# Native 模式
docker build -f src/main/docker/Dockerfile.native -t sklearn-model-service:native .

# 运行容器
docker run -p 8080:8080 sklearn-model-service:jvm
```

## API 接口

### 健康检查

```bash
# 基础健康检查
curl http://localhost:8080/q/health

# 就绪检查
curl http://localhost:8080/q/health/ready

# 存活检查
curl http://localhost:8080/q/health/live
```

### 模型预测

```bash
# Iris 预测（4个特征：sepal_length, sepal_width, petal_length, petal_width）
curl -X POST http://localhost:8080/api/predict \
  -H "Content-Type: application/json" \
  -d '{"features": [5.1, 3.5, 1.4, 0.2]}'

# 响应示例
{
  "predictedClass": 0,
  "predictedLabel": "setosa",
  "probabilities": [0.9, 0.05, 0.05],
  "success": true
}
```

### 模型验证

```bash
curl http://localhost:8080/api/model/validate

# 响应示例
{
  "valid": true,
  "modelName": "iris_classifier",
  "modelType": "RandomForestClassifier",
  "inputFeatures": 4,
  "outputClasses": 3,
  "message": "Model is valid and ready for predictions"
}
```

### 模型状态

```bash
curl http://localhost:8080/api/model/status

# 响应示例
{
  "modelLoaded": true
}
```

### 模型信息

```bash
curl http://localhost:8080/api/model/info

# 响应示例
{
  "name": "iris_classifier",
  "type": "RandomForestClassifier",
  "inputFeatures": 4,
  "outputClasses": 3,
  "description": "Iris flower classification model"
}
```

## 自动化测试

### 使用测试脚本

```bash
# Linux/Mac
./scripts/test-api.sh http://localhost:8080

# Windows
scripts\test-api.bat http://localhost:8080
```

### 测试用例说明

| 测试项 | 说明 | 预期结果 |
|--------|------|----------|
| Health Check | 健康检查 | HTTP 200 |
| Model Status | 模型加载状态 | HTTP 200, modelLoaded: true |
| Model Validate | 模型验证 | HTTP 200, valid: true |
| Predict | 模型预测 | HTTP 200, success: true |
| Model Info | 模型信息 | HTTP 200 |

## CICD 配置

### GitHub Actions

项目已配置完整的 GitHub Actions 工作流：

- **build-and-test**: 构建和测试
- **build-native**: 构建原生镜像
- **docker-build**: 构建 Docker 镜像

查看 [.github/workflows/build.yml](.github/workflows/build.yml)

### GitLab CI

项目已配置 GitLab CI 流水线：

- **build**: 编译项目
- **test**: 运行测试
- **package-jvm**: JVM 打包
- **package-native**: 原生镜像打包
- **docker-build**: Docker 构建

查看 [.gitlab-ci.yml](.gitlab-ci.yml)

### Jenkins

项目已配置 Jenkinsfile：

- **Checkout**: 代码检出
- **Setup Python**: 安装 Python 依赖
- **Build**: 构建
- **Test**: 测试
- **Package JVM/Native**: 打包
- **Docker Build**: Docker 构建
- **Integration Test**: 集成测试

查看 [Jenkinsfile](Jenkinsfile)

## 命令速查

| 命令 | 说明 |
|------|------|
| `mvnw compile quarkus:dev` | 开发模式（热重载） |
| `mvnw test` | 运行单元测试 |
| `mvnw verify` | 运行所有测试 |
| `mvnw package` | JVM 打包 |
| `mvnw package -Dnative` | 原生镜像打包 |
| `mvnw clean verify` | 清理并完整测试 |

## 部署验证清单

- [ ] 模型文件已生成（models/iris_classifier.onnx）
- [ ] 单元测试通过（mvnw test）
- [ ] 集成测试通过（mvnw verify）
- [ ] 健康检查正常（/q/health）
- [ ] 模型验证通过（/api/model/validate）
- [ ] 预测接口正常（/api/predict）
- [ ] Docker 镜像构建成功
- [ ] 容器运行正常

## 注意事项

1. **模型生成**: 首次运行前需要执行 `scripts/train_model.py` 生成 ONNX 模型
2. **GraalVM**: 构建原生镜像需要 GraalVM 环境
3. **内存**: JVM 模式建议至少 512MB 内存，Native 模式可低至 64MB
4. **端口**: 默认使用 8080 端口，可通过环境变量 `QUARKUS_HTTP_PORT` 修改

## 输出结果

- ✅ 完整可运行项目代码
- ✅ 自动生成 CICD 脚本示例
- ✅ 提供构建、测试、运行、原生镜像编译命令
- ✅ 可本地验证 + 可 CICD 验证
- ✅ 支持 JVM 模式和 GraalVM Native Image
- ✅ 完整的测试用例和自动化测试脚本
