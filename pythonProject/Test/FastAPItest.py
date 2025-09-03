from fastapi import FastAPI, HTTPException, Depends, Header
from pydantic import BaseModel
import requests
from typing import Optional

app = FastAPI(title="AI Service API")


# 请求模型
class PredictionRequest(BaseModel):
    text: str
    model: Optional[str] = "default"


# 响应模型
class PredictionResponse(BaseModel):
    result: str
    confidence: float
    processed_by: str


# 依赖项：验证JWT
async def verify_token(authorization: str = Header(...)):
    if not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Invalid authorization header")

    token = authorization.replace("Bearer ", "")

    # 调用Spring Boot验证token
    validation_url = "http://192.168.2.30:8080/api/auth/validate"
    try:
        response = requests.post(validation_url, json={"token": token}, timeout=5)
        if response.status_code == 200 and response.json().get("valid"):
            return response.json()
        else:
            raise HTTPException(status_code=401, detail="Invalid token")
    except requests.exceptions.RequestException:
        raise HTTPException(status_code=503, detail="Authentication service unavailable")


# 健康检查端点
@app.get("/")
async def root():
    return {"message": "AI Service is running"}


# 预测端点
@app.post("/predict", response_model=PredictionResponse)
async def predict(request: PredictionRequest, user_info: dict = Depends(verify_token)):
    # 这里是模拟的AI处理逻辑
    username = user_info.get("username", "unknown")

    # 简单的文本处理示例
    processed_text = request.text.upper()

    return PredictionResponse(
        result=f"Processed: {processed_text}",
        confidence=0.95,
        processed_by=f"AI Service for user: {username}"
    )


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)