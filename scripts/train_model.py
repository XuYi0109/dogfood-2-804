"""
训练示例 scikit-learn 模型并转换为 ONNX 格式
该脚本创建一个简单的 Iris 分类模型
"""
import numpy as np
from sklearn.datasets import load_iris
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score
try:
    from skl2onnx import convert_sklearn
    from skl2onnx.common.data_types import FloatTensorType
    ONNX_AVAILABLE = True
except ImportError:
    ONNX_AVAILABLE = False
    print("Warning: skl2onnx not available, will save as joblib only")

import joblib
import os

def train_and_export_model():
    print("Loading Iris dataset...")
    iris = load_iris()
    X, y = iris.data, iris.target
    
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42
    )
    
    print("Training RandomForest classifier...")
    model = RandomForestClassifier(
        n_estimators=10,
        max_depth=3,
        random_state=42
    )
    model.fit(X_train, y_train)
    
    y_pred = model.predict(X_test)
    accuracy = accuracy_score(y_test, y_pred)
    print(f"Model accuracy: {accuracy:.4f}")
    
    os.makedirs("models", exist_ok=True)
    
    joblib_path = "models/iris_classifier.joblib"
    joblib.dump(model, joblib_path)
    print(f"Model saved to {joblib_path}")
    
    if ONNX_AVAILABLE:
        print("Converting to ONNX format...")
        initial_type = [('float_input', FloatTensorType([None, 4]))]
        onnx_model = convert_sklearn(
            model, 
            initial_types=initial_type,
            target_opset=12
        )
        
        onnx_path = "models/iris_classifier.onnx"
        with open(onnx_path, "wb") as f:
            f.write(onnx_model.SerializeToString())
        print(f"ONNX model saved to {onnx_path}")
    else:
        print("Skipping ONNX conversion - skl2onnx not installed")
        print("To install: pip install skl2onnx onnxmltools")
    
    print("\nModel info:")
    print(f"  - Input features: 4 (sepal length, sepal width, petal length, petal width)")
    print(f"  - Output classes: 3 (setosa, versicolor, virginica)")
    print(f"  - Sample input: [5.1, 3.5, 1.4, 0.2]")
    
    return model, accuracy

if __name__ == "__main__":
    train_and_export_model()
