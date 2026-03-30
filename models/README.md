# 模型目录

此目录用于存放 scikit-learn 模型文件。

## 支持的模型格式

- `.joblib` - scikit-learn joblib 格式（推荐）
- `.pkl` / `.pickle` - Python pickle 格式
- `.pmml` - PMML 格式（预测模型标记语言）

## 模型命名规范

建议按照以下格式命名模型文件：

```
{model_name}_{version}.{format}
```

例如：
- `iris_classifier_v1.0.0.joblib`
- `fraud_detection_20240301.pkl`

## 模型元数据

模型加载时会自动提取以下元数据：

- 模型名称
- 模型版本
- 模型类型（分类/回归/聚类）
- 输入特征列表
- 输出类型
- 框架版本

## 示例模型

项目启动时会自动创建一个 mock 模型用于测试。在生产环境中，请将实际模型文件放入此目录。

## 模型加载配置

在 `application.properties` 中配置模型路径：

```properties
model.path=models
model.default=model.joblib
```

或通过环境变量：

```bash
export MODEL_PATH=/path/to/models
export MODEL_DEFAULT=my_model.joblib
```
