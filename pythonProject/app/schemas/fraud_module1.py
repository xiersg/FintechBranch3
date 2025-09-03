from pydantic import BaseModel, Field
from typing import Literal, Optional, List, Dict, Any

# Fraud  交易数据检测
# 单笔交易检测数据请求
class FraudDetectRequest(BaseModel):
    step: int = Field(..., ge=0)
    type: Literal["CASH_OUT", "TRANSFER"]
    amount: float
    nameOrig: str
    oldbalanceOrg: float
    newbalanceOrig: float
    nameDest: str
    oldbalanceDest: float
    newbalanceDest: float

# 单笔交易数据响应
class FraudDetectResponse(BaseModel):
    status: str = "success"
    is_fraud: bool
    fraud_score: float
    message: str = "欺诈检测完成"

# 触发增量训练的请求
class FraudTrainRequest(BaseModel):
    incremental: bool = False

# 触发增量训练的响应
class FraudTrainResponse(BaseModel):
    status: str = "success"
    message: str = "模型训练完成"

# 模型查询状态的响应体
class FraudStatusResponse(BaseModel):
    status: str = "success"
    model_loaded: bool = True
    threshold: float = 0.65
    feature_count: int = 13