# sklearn-model-validator

CICD 平台中的 scikit-learn 模型自动验证服务

## 项目概述

本项目是一个基于 Quarkus 的微服务，用于在 CICD 流水线中自动验证 scikit-learn 模型的可用性。支持 JVM 模式和 GraalVM 原生镜像模式，具有超轻量、秒级启动的特点。

### 核心特性

- **模型验证**: 自动加载并验证 scikit-learn 模型文件
- **HTTP API**: 提供标准化的预测、校验、健康检查接口
- **原生编译**: 支持 GraalVM Native Image，超轻量、秒启动
- **CICD 集成**: 可直接在 Jenkins/GitLab CI/GitHub Actions 中运行
- **容器化**: 支持 Docker 容器化部署
- **多模式运行**: 支持开发模式、JVM 模式、原生镜像模式

## 技术栈

- **开发语言**: Java 17+
- **微服务框架**: Quarkus 3.6+
- **原生编译**: GraalVM / Mandrel
- **模型引擎**: scikit-learn 模型（.joblib）
- **构建工具**: Maven
- **测试框架**: JUnit 5 + RestAssured

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.9+
- (可选) GraalVM 22+ 用于原生镜像编译
- (可选) Docker 用于容器化部署

### 一键命令

```bash
# 开发模式（热加载）
./mvnw quarkus:dev

# 编译测试
./mvnw clean test

# 打包（JVM 模式）
./mvnw clean package

# 运行 JVM 版本
java -jar target/quarkus-app/quarkus-run.jar

# 编译原生镜像
./mvnw package -Dnative

# 运行原生镜像
./target/sklearn-model-validator-1.0.0-SNAPSHOT-runner

# Docker 构建（JVM）
docker build --target jvm -t sklearn-model-validator:jvm .

# Docker 构建（Native）
docker build -f Dockerfile.native -t sklearn-model-validator:native .
```

## API 接口

### 健康检查

```bash
# 存活检查
curl http://localhost:8080/health/live

# 就绪检查
curl http://localhost:8080/health/ready

# 完整健康检查
curl http://localhost:8080/health

# 详细状态
curl http://localhost:8080/health/status
```

### 模型管理

```bash
# 列出所有模型
curl http://localhost:8080/api/v1/models

# 获取模型元数据
curl http://localhost:8080/api/v1/models/{modelName}

# 加载模型
curl -X POST -F "file=@model.joblib" http://localhost:8080/api/v1/models/{modelName}/load

# 卸载模型
curl -X POST http://localhost:8080/api/v1/models/{modelName}/unload
```

### 预测与验证

```bash
# 模型预测
curl -X POST http://localhost:8080/api/v1/models/{modelName}/predict \
  -H "Content-Type: application/json" \
  -d '{
    "data": [
      {"feature1": 1.0, "feature2": 2.0, "feature3": 3.0},
      {"feature1": 0.5, "feature2": 1.5, "feature3": 2.5}
    ]
  }'

# 验证模型
curl -X POST http://localhost:8080/api/v1/models/{modelName}/validate

# 验证所有模型
curl -X POST http://localhost:8080/api/v1/models/validate-all

# 获取服务统计
curl http://localhost:8080/api/v1/models/stats
```

## CICD 集成

### GitHub Actions

项目已配置 `.github/workflows/ci-cd.yml`，包含：
- 自动构建与测试
- JVM 和 Native 镜像打包
- Docker 镜像构建
- 模型验证测试

### GitLab CI

项目已配置 `.gitlab-ci.yml`，包含：
- 多阶段流水线（build → test → package → validate → deploy）
- 自动模型验证
- Docker 镜像构建与推送

### Jenkins

项目已配置 `Jenkinsfile`，包含：
- 完整的 CI/CD 流水线
- 单元测试与集成测试
- 模型验证门禁
- Docker 镜像构建

### CICD 测试脚本

```bash
# Linux/Mac
./scripts/cicd-test.sh

# Windows
./scripts/cicd-test.ps1
```

## 项目结构

```
.
├── src/
│   ├── main/
│   │   ├── java/com/mlops/model/validator/
│   │   │   ├── entity/          # 实体类
│   │   │   ├── service/         # 业务逻辑
│   │   │   └── resource/        # REST API
│   │   └── resources/
│   │       ├── application.properties
│   │       └── META-INF/native-image/  # GraalVM 配置
│   └── test/
│       └── java/com/mlops/model/validator/
│           ├── ModelResourceTest.java
│           ├── HealthResourceTest.java
│           └── CICDIntegrationTest.java
├── .github/workflows/            # GitHub Actions
├── scripts/                      # CICD 测试脚本
├── Dockerfile                    # Docker 构建文件
├── Dockerfile.native             # Native 镜像构建
├── pom.xml                       # Maven 配置
├── Jenkinsfile                  # Jenkins 流水线
└── .gitlab-ci.yml               # GitLab CI 配置
```

## 配置说明

### 应用配置 (application.properties)

```properties
# 服务端口
quarkus.http.port=8080

# 模型配置
model.path=models
model.default=model.joblib
model.type=joblib

# 日志级别
quarkus.log.level=INFO
quarkus.log.category."com.mlops".level=DEBUG
```

### 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `MODEL_PATH` | 模型文件路径 | models |
| `MODEL_DEFAULT` | 默认模型名称 | model.joblib |
| `LOG_LEVEL` | 日志级别 | INFO |
| `QUARKUS_HTTP_PORT` | HTTP 端口 | 8080 |

## 测试

### 运行测试

```bash
# 运行所有测试
./mvnw test

# 运行特定测试类
./mvnw test -Dtest=ModelResourceTest

# 运行 CICD 集成测试
./mvnw test -Dtest=CICDIntegrationTest

# 运行集成测试
./mvnw verify
```

### 测试覆盖

- **ModelResourceTest**: API 接口测试
- **HealthResourceTest**: 健康检查测试
- **CICDIntegrationTest**: CICD 集成测试

## 部署

### Docker 部署

```bash
# 构建并运行 JVM 版本
docker build --target jvm -t sklearn-model-validator:jvm .
docker run -p 8080:8080 sklearn-model-validator:jvm

# 构建并运行 Native 版本
docker build -f Dockerfile.native -t sklearn-model-validator:native .
docker run -p 8080:8080 sklearn-model-validator:native
```

### Kubernetes 部署

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: sklearn-model-validator
spec:
  replicas: 1
  selector:
    matchLabels:
      app: sklearn-model-validator
  template:
    metadata:
      labels:
        app: sklearn-model-validator
    spec:
      containers:
      - name: validator
        image: sklearn-model-validator:native
        ports:
        - containerPort: 8080
        livenessProbe:
          httpGet:
            path: /health/live
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /health/ready
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
```

## 性能指标

| 指标 | JVM 模式 | Native 模式 |
|------|----------|-------------|
| 启动时间 | ~2s | ~0.05s |
| 内存占用 | ~150MB | ~50MB |
| 镜像大小 | ~200MB | ~80MB |

## 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

MIT License

## 联系方式

如有问题或建议，请提交 Issue 或 Pull Request。
