# main.py
from fastapi import FastAPI, Header, HTTPException, Depends, UploadFile, File, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import Literal, Optional, List, Dict, Any
import os, json, math, asyncio
import httpx

from schemas import *

# =========================
# 配置（可用环境变量覆盖）
# =========================
AUTH_BASE_URL = os.getenv("AUTH_BASE_URL", "http://192.168.2.30:8080")
AUTH_VALIDATE_PATH = os.getenv("AUTH_VALIDATE_PATH", "/api/auth/validate")
AUTH_TIMEOUT_S = float(os.getenv("AUTH_TIMEOUT_S", "3.0"))

"""
AUTH_BASE_URL        验证服务基础URL，后端地址
AUTH_VALIDATE_PATH   验证Token的接口路径
AUTH_TIMEOUT_S       服务超时时间 
"""

def _auth_url() -> str:
    return f"{AUTH_BASE_URL.rstrip('/')}/{AUTH_VALIDATE_PATH.lstrip('/')}"

# =========================
# 鉴权工具
# =========================
async def jwt_val(token: str) -> Dict[str, Any]:
    url = _auth_url()
    async with httpx.AsyncClient(timeout=AUTH_TIMEOUT_S) as client:
        try:
            resp = await client.post(url, json={"token": token})  # 调用后端接口验证JWT
        except httpx.RequestError as e:
            raise HTTPException(status_code=503, detail=f"Authentication service unavailable: {e}") # 抛出503错误，提示连接错误，和tokeb没有关系
    if resp.status_code == 200:
        data = resp.json() # 获取返回JWT验证结果
        if data.get("valid"):
            return data  # 可包含 username/roles/tenant 等
        raise HTTPException(status_code=401, detail="Invalid token") # 如果valid字段为False抛出错误验证失败
    elif 400 <= resp.status_code < 500:
        raise HTTPException(status_code=401, detail="Invalid token") # 抛出错误
    else:
        raise HTTPException(status_code=503, detail="Authentication service unavailable")  # 其余错误抛出503服务器出现错误

# 获取请求之后，将请求中的token抽离，传入 jwt_val 挂起等待验证
async def bearer_auth(authorization: str = Header(...)) -> Dict[str, Any]:
    if not authorization.lower().startswith("bearer "):
        raise HTTPException(status_code=401, detail="Invalid authorization header")  # 请求头不符合规则，抛出401
    token = authorization.split(" ", 1)[1].strip()
    return await jwt_val(token)

# 客户端在连接时必须带上 ?token=xxx 这样的 URL 参数，否则直接拒绝。
async def validate_ws_token(token: Optional[str]) -> Dict[str, Any]:
    if not token:
        raise HTTPException(status_code=401, detail="Missing token")
    return await jwt_val(token)

# =========================
# FastAPI 应用 & CORS
# =========================
app = FastAPI(title="AI Service (WS + POST)", version="0.3")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 如需限制域名请修改
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# =========================
# 健康检查
# =========================
@app.get("/")
async def root():
    return {"message": "AI Service is running"}

# =========================
# Fraud 路由（POST）
# =========================
@app.post("/fraud/detect", response_model=FraudDetectResponse)
async def fraud_detect(req: FraudDetectRequest, user=Depends(bearer_auth)):
    """
    单笔交易欺诈检测（占位逻辑）：
    - 使用源/目的余额变化与交易金额的简单差值构造一个分数
    - 请在此处替换为真实模型推理
    """
    delta_src = req.oldbalanceOrg - req.newbalanceOrig
    delta_dst = req.newbalanceDest - req.oldbalanceDest
    raw = abs(delta_src - req.amount) + abs(delta_dst - req.amount)
    score = 1.0 - math.exp(-raw / max(req.amount, 1.0))
    score = max(0.0, min(1.0, score))
    return FraudDetectResponse(is_fraud=score > 0.65, fraud_score=round(score, 4))

@app.post("/fraud/train", response_model=FraudTrainResponse)
async def fraud_train(req: FraudTrainRequest, user=Depends(bearer_auth)):
    """
    触发训练（占位）：
    - incremental=True 表示增量训练
    - 生产环境建议改为异步任务 + 进度查询
    """
    return FraudTrainResponse()

@app.get("/fraud/status", response_model=FraudStatusResponse)
async def fraud_status(user=Depends(bearer_auth)):
    """
    模型状态查询（占位）：
    - 返回是否加载、阈值、特征数等
    """
    return FraudStatusResponse()

# =========================
# Content 路由（POST）
# =========================

def _simple_text_score(text: str) -> float:
    # 例：按长度给一个风险分（占位），请替换为真实风控模型
    return min(1.0, len(text) / 200.0)

@app.post("/content/detect", response_model=ContentDetectResponse)
async def content_detect(req: ContentDetectRequest, user=Depends(bearer_auth)):
    """
    内容风险检测（文本/图片URL/HTML）
    - text: 直接对文本打分
    - image + is_url=True: 使用 URL 占位打分（真实实现应下载并检测）
    - html: 对 HTML 文本做简易打分
    """
    if req.content_type == "text":
        score = _simple_text_score(req.content)
        return ContentDetectResponse(
            is_fraudulent=score > 0.7,
            risk_score=round(score, 4),
            content_type="text",
            content_preview=req.content[:50]
        )
    elif req.content_type == "image":
        if not req.is_url:
            raise HTTPException(status_code=400, detail="For local image use /content/image (multipart/form-data)")


        # 接入评分逻辑
        score = 0.12  # 占位：可按域名/尺寸/EXIF 等要素实现真实评分


        return ContentDetectResponse(
            is_fraudulent=score > 0.7,
            risk_score=score,
            content_type="image",
            content_preview=req.content[:100]
        )
    elif req.content_type == "html":
        score = _simple_text_score(req.content)
        return ContentDetectResponse(
            is_fraudulent=score > 0.7,
            risk_score=round(score, 4),
            content_type="html",
            content_preview=req.content[:50].replace("\n", " ")
        )
    else:
        raise HTTPException(status_code=400, detail="Unsupported content_type")

@app.post("/content/image", response_model=ContentDetectResponse)
async def content_image(image: UploadFile = File(...), user=Depends(bearer_auth)):
    """
    表单直传图片（multipart/form-data）
    - 字段名：image
    """
    content = await image.read()
    if not content:
        raise HTTPException(status_code=400, detail="Empty file")
    kb = max(1, len(content) // 1024)
    score = min(1.0, kb / 1024.0)  # 占位：用文件大小粗略估计
    return ContentDetectResponse(
        is_fraudulent=score > 0.7,
        risk_score=round(score, 4),
        content_type="image",
        content_preview=f"uploaded:{image.filename} ({kb}KB)"
    )

# =========================
# WebSocket（对话流式）
# =========================
@app.websocket("/ws")
async def chat_ws(websocket: WebSocket):
    """
    LLM 对话流式（占位）：
    - 握手用 ?token=<JWT> 鉴权
    - 消息格式：
      客户端 -> 服务端：
        {"action":"chat.create","request_id":"req-1","payload":{"messages":[{"role":"user","content":"你好"}]}}
      服务端 -> 客户端（流）：
        {"type":"ack","request_id":"req-1"}
        {"type":"delta","request_id":"req-1","data":{"index":0,"delta":"你"}}
        {"type":"delta","request_id":"req-1","data":{"index":0,"delta":"好"}}
        {"type":"result","request_id":"req-1","data":{"finish_reason":"stop","usage":{...}}}
    """
    token = websocket.query_params.get("token")
    try:
        user_info = await validate_ws_token(token)
    except HTTPException as e:
        code = 1008 if e.status_code == 401 else 1011
        await websocket.close(code=code)
        return

    await websocket.accept()
    username = user_info.get("username", "unknown")

    try:
        while True:
            raw = await websocket.receive_text()
            try:
                msg = json.loads(raw)
            except json.JSONDecodeError:
                await websocket.send_text(json.dumps({"type": "error", "error": {"code": "BAD_REQUEST", "message": "invalid json"}}))
                continue

            action = msg.get("action")
            req_id = msg.get("request_id", "")
            payload = msg.get("payload", {})

            if action == "chat.create":
                await websocket.send_text(json.dumps({"type": "ack", "request_id": req_id}))

                # 取首条 user 消息
                text = ""
                for m in payload.get("messages", []):
                    if m.get("role") == "user":
                        text = m.get("content", "")
                        break

                # 占位：每 2 字符一个增量
                for i in range(0, len(text), 2):
                    delta = text[i:i+2]
                    await asyncio.sleep(0.1)
                    await websocket.send_text(json.dumps({
                        "type": "delta",
                        "request_id": req_id,
                        "data": {"index": 0, "delta": delta}
                    }))

                await websocket.send_text(json.dumps({
                    "type": "result",
                    "request_id": req_id,
                    "data": {
                        "finish_reason": "stop",
                        "usage": {
                            "prompt_tokens": 0,
                            "completion_tokens": len(text),
                            "total_tokens": len(text)
                        }
                    },
                    "meta": {"processed_by": f"ws:{username}"}
                }))
            else:
                await websocket.send_text(json.dumps({
                    "type": "error",
                    "request_id": req_id,
                    "error": {"code": "BAD_REQUEST", "message": "unknown action"}
                }))
    except WebSocketDisconnect:
        # 客户端断开
        pass

# =========================
# 本地直接运行
# =========================
if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
