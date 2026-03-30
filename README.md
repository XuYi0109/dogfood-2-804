# Scikit-learn Model Service

在 CICD 平台中自动验证 scikit-learn 模型可用性的标准服务框架。

## 需求

- 在 CICD 平台中自动验证 scikit-learn 模型可用性
- 提供标准化模型服务接口，支持预测 / 校验 / 健康检查
- 可编译为原生镜像（GraalVM Native Image），超轻量、秒启动
- 用于 Jenkins/GitLab CI/GitHub Actions 自动化测试与部署门禁
- 纯可验证工程：一键构建、一键测试、一键运行

## 功能

- 加载并验证 scikit-learn 模型文件
- 提供 HTTP 接口完成模型预测
- CICD 自动化测试用例（可直接在流水线运行）
- 健康检查接口，用于部署校验
- 支持 JVM 模式 + GraalVM 原生镜像模式
- 可容器化部署（Dockerfile 自动生成）

## 技术栈

- 开发语言：Java 17+
- 微服务框架：Quarkus（云原生 / 快速构建）
- 原生编译：GraalVM
- 模型引擎：scikit-learn 模型（.joblib）
- 模型调用：Python 环境 / PMML / ONNX 兼容
- 构建工具：Maven / Gradle
- 测试：JUnit 5 + RestAssured
- 部署：Docker / CICD 流水线

## 强约束

- 可直接在 CICD 平台构建、测试、运行
- 测试用例必须可自动通过 / 失败，作为门禁标准
- 支持 Quarkus 开发模式、JVM 模式、GraalVM 原生镜像
- 不依赖 GPU，不依赖复杂环境
- 接口可自动验证，支持 curl / 自动化调用
- 提供完整可运行命令（dev/test/build/package/run）

## 快速开始

### 开发模式

```bash
./mvnw compile quarkus:dev
```

### 测试

```bash
./mvnw test
```

### 构建 JVM 模式

```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

### 构建原生镜像

```bash
./mvnw package -Dnative
./target/sklearn-model-service-1.0.0-SNAPSHOT-runner
```

### Docker 构建

```bash
docker build -f src/main/docker/Dockerfile.jvm -t sklearn-model-service .
docker run -p 8080:8080 sklearn-model-service
```

## API 接口

### 健康检查

```bash
curl http://localhost:8080/q/health
```

### 模型预测

```bash
curl -X POST http://localhost:8080/api/predict \
  -H "Content-Type: application/json" \
  -d '{"features": [1.0, 2.0, 3.0, 4.0]}'
```

### 模型验证

```bash
curl http://localhost:8080/api/model/validate
```

## CICD 配置

### GitHub Actions

```yaml
name: Build and Test
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Build and Test
        run: ./mvnw verify
```

### GitLab CI

```yaml
image: maven:3.9-eclipse-temurin-17
build:
  stage: build
  script:
    - mvn verify
  artifacts:
    paths:
      - target/quarkus-app/
```

### Jenkinsfile

```groovy
pipeline {
    agent any
    tools {
        maven 'Maven 3.9'
        jdk 'JDK 17'
    }
    stages {
        stage('Build') {
            steps {
                sh 'mvn verify'
            }
        }
    }
}
```

## 输出结果

- 完整可运行项目代码
- 自动生成 CICD 脚本示例
- 提供构建、测试、运行、原生镜像编译命令
- 可本地验证 + 可 CICD 验证
