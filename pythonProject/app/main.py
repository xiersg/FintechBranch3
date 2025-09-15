# main.py
from fastapi import FastAPI, Header, HTTPException, Depends, UploadFile, File, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import Literal, Optional, List, Dict, Any
import os, json, math, asyncio, tempfile
from pathlib import Path
import httpx


# =========================
# 配置（可用环境变量覆盖）
# =========================
AUTH_BASE_URL = os.getenv("AUTH_BASE_URL", "http://192.168.2.30:8080")
AUTH_VALIDATE_PATH = os.getenv("AUTH_VALIDATE_PATH", "/api/auth/validate")
AUTH_TIMEOUT_S = float(os.getenv("AUTH_TIMEOUT_S", "5.0"))

"""
AUTH_BASE_URL        验证服务基础URL，后端地址
AUTH_VALIDATE_PATH   验证Token的接口路径
AUTH_TIMEOUT_S       服务超时时间 
"""

def _auth_url() -> str:
    """
    返回路径
    """
    return f"{AUTH_BASE_URL.rstrip('/')}/{AUTH_VALIDATE_PATH.lstrip('/')}"


# =========================
# rag外部服务调用定义
# =========================
class RAGService(BaseModel):
    query:str
    model_choice: str = 'llama3'
    cache_model: str = 'best_roberta_rnn_model_ent_aug'

# =========================
# 鉴权工具
# =========================
async def jwt_val(token: str) -> Dict[str, Any]:
    """
    JWT鉴权
    :param token: 鉴权token
    :return:  data  # 可包含 username/roles/tenant 等
    """
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
    if os.getenv("ALLOW_WS_NO_TOKEN") == "1":
        return {"username": "dev"}  # 本地联调放行
    test_token = os.getenv("Token")
    # 测试token
    if token == test_token:
        return {"username":"xiersg"}
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
    return {"message": "服务正在运行喵"}

# =========================
# 实例化
# =========================
from schemas import *

@app.on_event("startup")
def _lazy_init_heavy_components():
    """
    启动后再加载重组件，避免 import 阶段阻塞 / 根路由。
    """
    from module.Alternatives_API.API import DeepseekStreamer
    from module.anti_spoof.inference import AASISTDetector
    global ds, detector
    ROOT = Path(__file__).resolve().parent

    # 先把服务跑起来，再加载 deepseek（如果它初始化会访问网络，就不会卡住启动）
    try:
        ds = DeepseekStreamer(model="deepseek-chat")
        print("[startup] DeepseekStreamer 准备好了")
    except Exception as e:
        print(f"[startup] Deepseek init failed: {e}")

    # 再加载 AASIST（权重大，可能要几秒）
    conf = ROOT / "config" / "AASIST.conf"
    ckpt = ROOT / "module" / "anti_spoof" / "models" / "weights" / "AASIST.pth"

    try:
        detector = AASISTDetector(
            conf_path=str(conf),
            weight_path=str(ckpt),
            use_cuda=False,
            min_duration_sec=2.0,
            vad=False
        )
        print("[startup] AASISTDetector 准备好了")
    except Exception as e:
        # 打印出来好排查（不会让进程静默卡着）
        print(f"[startup] AASIST init failed: {e}")
        # 也可以选择 raise，让启动直接失败

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
        {"action":"chat.create",
        "request_id":"req-1",
        "payload":{"messages":[{"role":"user","content":"你好"}]}}
      服务端 -> 客户端（流）：
        {"type":"ack","request_id":"req-1"}
        {"type":"delta","request_id":"req-1","data":{"index":0,"delta":"你"}}
        {"type":"delta","request_id":"req-1","data":{"index":0,"delta":"好"}}
        {"type":"result","request_id":"req-1","data":{"finish_reason":"stop","usage":{...}}}
    """
    token = websocket.query_params.get("token")

    # 验证token
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
            if raw is None:
                print("格式错误")
            else:
                print(raw)
            try:
                # 获取message
                msg = json.loads(raw)
            except json.JSONDecodeError:
                # 捕获json解析错误
                await websocket.send_text(json.dumps(
                    {"type": "error",
                     "error": {"code": "BAD_REQUEST",
                               "message": "invalid json"}
                     }))
                continue

            action = msg.get("action")          # 用于分支功能
            req_id = msg.get("request_id", "")  # request_id为对话id
            payload = msg.get("payload", {})    # 请求的"有效载荷"，真正装业务数据的部分

            if action == "chat.create":
                await websocket.send_text(json.dumps({"type": "ack", "request_id": req_id}))

                # 取首条 user 消息
                text = ""
                for m in payload.get("messages", None):
                    if m.get("role") == "user":
                        # 这里还没有实现记录历史，之后增加
                        text = m.get("content", "")
                        break

                # 检查是否是RAG查询请求
                is_rag_query = "rag" in text.lower() or "查询" in text.lower() or "诈骗" in text.lower()
                
                if is_rag_query and os.getenv("USE_EXTERNAL_RAG", "false").lower() == "true":
                    # 使用外部 RAG 服务
                    RAG_SERVICE_URL = os.getenv("RAG_SERVICE_URL", "http://192.168.1.254:11434/api/generate")
                    rag_payload = {
                        "model": "llama3",
                        "prompt": text,
                        "stream": True
                    }
                    
                    try:
                        async with httpx.AsyncClient(timeout=30.0) as client:
                            async with client.stream("POST", RAG_SERVICE_URL, json=rag_payload) as response:
                                if response.status_code == 200:
                                    text_total = ""
                                    async for line in response.aiter_lines():
                                        if line:
                                            try:
                                                data = json.loads(line)
                                                if "response" in data:
                                                    delta = data["response"]
                                                    text_total += delta
                                                    await websocket.send_text(json.dumps({
                                                        "type": "delta",
                                                        "request_id": req_id,
                                                        "data": {"index": 0, "delta": delta}}))
                                                    await asyncio.sleep(0)
                                            except json.JSONDecodeError:
                                                continue
                                else:
                                    # 如果外部服务出错，使用默认回复
                                    error_msg = "抱歉，暂时无法查询相关信息。"
                                    for delta in error_msg:
                                        await websocket.send_text(json.dumps({
                                            "type": "delta",
                                            "request_id": req_id,
                                            "data": {"index": 0, "delta": delta}}))
                                        await asyncio.sleep(0)
                    except Exception as e:
                        # 如果外部服务不可用，使用默认回复
                        error_msg = "抱歉，查询服务暂时不可用。"
                        for delta in error_msg:
                            await websocket.send_text(json.dumps({
                                "type": "delta",
                                "request_id": req_id,
                                "data": {"index": 0, "delta": delta}}))
                            await asyncio.sleep(0)
                elif os.getenv("Type")=="ollama":
                    text_total = ""
                    for delta in "ollama功能还未实装---------请期待":
                        text_total = text_total + delta
                        await websocket.send_text(json.dumps({
                            "type": "delta",
                            "request_id": req_id,
                            "data": {"index": 0, "delta": delta}}))

                # 发送结束字段
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
        print("\n=== 客户端断开 ===\n")
        pass

# =========================
# 语音克隆检查
# =========================

@app.post("/anti_spoof_score")
async def score(file: UploadFile = File(...)):
    """
    spoof_prob：伪造概率（越大越像伪造/合成音频）
    label：
        "genuine" = 真人/真实音频
        "spoof" = 伪造/深度合成音频
    valid：是否通过前置校验（最小时长、VAD）；False 时给出 reason（比如 too_short_or_no_speech）
    meta：一些有用的附加信息（时长、语音占比），便于后续监控与调参
    """
    if detector is None:
        raise HTTPException(status_code=503, detail="detector 未实例化")
    # 阈值
    THRESHOLD = 0.5

    # 将文件本地缓存
    raw = await file.read()
    with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as tmp:
        tmp.write(raw)
        tmp_path = tmp.name
    try:
        # 调用库处理文件
        prob, meta = detector.score_wav(tmp_path)  # 你的类里会统一到 16k/mono 并推理

        # 如果文件预处理不通过，就直接返回固定格式
        valid = not meta.get("reason")
        if not valid:
            return {
                "spoof_prob": -1.0,
                "label": "invalid",
                "valid": False,
                "reason": meta.get("reason", ""),
                "meta": {k: v for k, v in meta.items() if k in ("duration","speech_ratio")},
            }

        # 返回模型输出
        label = "spoof" if (prob is not None and float(prob) >= THRESHOLD) else "genuine"
        return {
            "spoof_prob": float(prob) if prob is not None else -1.0,
            "label": label,
            "valid": True,
            "reason": "",
            "meta": {k: v for k, v in meta.items() if k in ("duration","speech_ratio")},
        }
    # 删除缓存文件
    finally:
        try: os.remove(tmp_path)
        except: pass

# =========================
# RAG服务调用接口
# =========================
@app.post("/rag/external_query")
async def rag_external_query(request: RAGService, user_info: Dict[str, Any] = Depends(bearer_auth)):
    """
    调用外部 RAG 服务进行查询
    """
    RAG_SERVICE_URL = os.getenv("RAG_SERVICE_URL", "http://localhost:11434/api/generate")

    try:
        # 构建发送给外部 RAG 服务的请求
        rag_payload = {
            "model": request.model_choice,
            "prompt": request.query,
            "stream": False
        }

        # 调用外部 RAG 服务
        async with httpx.AsyncClient(timeout=30.0) as client:
            response = await client.post(RAG_SERVICE_URL, json=rag_payload)

        if response.status_code == 200:
            rag_result = response.json()
            return {
                "query": request.query,
                "answer": rag_result.get("response", ""),
                "meta": {"processed_by": f"external_rag:{user_info.get('username', 'unknown')}"}
            }
        else:
            raise HTTPException(status_code=502, detail=f"External RAG service error: {response.status_code}")

    except httpx.RequestError as e:
        raise HTTPException(status_code=503, detail=f"Cannot connect to external RAG service: {str(e)}")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"External RAG query failed: {str(e)}")


@app.post("/rag/external_query_stream")
async def rag_external_query_stream(request: RAGService, user_info: Dict[str, Any] = Depends(bearer_auth)):
    """
    调用外部 RAG 服务进行流式查询
    """
    RAG_SERVICE_URL = os.getenv("RAG_SERVICE_URL", "http://192.168.1.254:11434/api/generate")

    try:
        # 构建发送给外部 RAG 服务的请求
        rag_payload = {
            "model": request.model_choice,
            "prompt": request.query,
            "stream": True
        }

        async def generate():
            try:
                async with httpx.AsyncClient(timeout=30.0) as client:
                    async with client.stream("POST", RAG_SERVICE_URL, json=rag_payload) as response:
                        if response.status_code == 200:
                            async for line in response.aiter_lines():
                                if line:
                                    try:
                                        data = json.loads(line)
                                        if "response" in data:
                                            yield data["response"]
                                    except json.JSONDecodeError:
                                        continue
                        else:
                            yield json.dumps({"error": f"External RAG service error: {response.status_code}"})
            except httpx.RequestError as e:
                yield json.dumps({"error": f"Cannot connect to external RAG service: {str(e)}"})
            except Exception as e:
                yield json.dumps({"error": f"External RAG stream failed: {str(e)}"})

        return generate()

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"External RAG stream query failed: {str(e)}")


# =========================
# 本地直接运行
# =========================
if __name__ == "__main__":

    import uvicorn
    uvicorn.run("main:app", host="127.0.0.1", port=8000, reload=False)
