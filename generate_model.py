"""
示例脚本：生成简单的 scikit-learn 模型
使用前请安装: pip install scikit-learn joblib numpy
"""

import numpy as np
from sklearn.linear_model import LinearRegression
import joblib
import os

def generate_sample_model():
    """生成简单的线性回归模型"""
    
    X = np.array([[1, 1], [1, 2], [2, 2], [2, 3]])
    y = np.dot(X, np.array([1, 2])) + 3
    
    model = LinearRegression()
    model.fit(X, y)
    
    model_dir = "src/main/resources/model"
    os.makedirs(model_dir, exist_ok=True)
    
    model_path = os.path.join(model_dir, "demo-model.joblib")
    joblib.dump(model, model_path)
    
    print(f"示例模型已生成: {model_path}")
    
    sample_prediction = model.predict([[3, 5]])
    print(f"测试预测结果: {sample_prediction}")

if __name__ == "__main__":
    generate_sample_model()
