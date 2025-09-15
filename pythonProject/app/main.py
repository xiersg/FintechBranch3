# main.py
from fastapi import FastAPI, Header, HTTPException, Depends, UploadFile, File, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse, HTMLResponse
from pydantic import BaseModel, Field
from typing import Literal, Optional, List, Dict, Any
import os, json, math, asyncio, tempfile
from pathlib import Path
import httpx
from utils.msg_load_save import DatabaseManager
from fastapi.staticfiles import StaticFiles


# =========================
# 配置（可用环境变量覆盖）
# =========================
AUTH_BASE_URL = os.getenv("AUTH_BASE_URL", "http://192.168.2.30:8080")
AUTH_VALIDATE_PATH = os.getenv("AUTH_VALIDATE_PATH", "/api/auth/validate")
AUTH_TIMEOUT_S = float(os.getenv("AUTH_TIMEOUT_S", "5.0"))
RAG_MODEL = os.getenv("RAG_MODEL", "llama3")
RAG_CACHE_MODEL = os.getenv("RAG_CACHE_MODEL", "best_roberta_rnn_model_ent_aug")


"""
AUTH_BASE_URL        验证服务基础URL，后端地址
AUTH_VALIDATE_PATH   验证Token的接口路径
AUTH_TIMEOUT_S       服务超时时间 
RAG_MODEL            RAG模型名称
RAG_CACHE_MODEL      RAG缓存模型名称
"""

def _auth_url() -> str:
    """
    返回路径
    """
    return f"{AUTH_BASE_URL.rstrip('/')}/{AUTH_VALIDATE_PATH.lstrip('/')}"

# =========================
# 请求模型
# =========================
class RAGRequest(BaseModel):
    query: str = Field(..., description="用户查询内容")
    model_choice: Optional[str] = Field(RAG_MODEL, description="使用的模型")
    cache_model: Optional[str] = Field(RAG_CACHE_MODEL, description="缓存模型名称")

class RAGResponse(BaseModel):
    answer: str
    query: str
    model_used: str
    timestamp: float


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
    from utils.msg_load_save import DatabaseManager
    global ds, detector,db
    ROOT = Path(__file__).resolve().parent

    # 先把服务跑起来，再加载 deepseek（如果它初始化会访问网络，就不会卡住启动）
    try:
        ds = DeepseekStreamer(model="deepseek-chat")
        print("[startup] DeepseekStreamer 准备好了")
    except Exception as e:
        print(f"[startup] DeepseekAPI 启动失败: {e}")

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
        print(f"[startup] AASIST 启动失败: {e}")
        # 也可以选择 raise，让启动直接失败

    try:
        db = DatabaseManager(db_path=ROOT / "data" / "chat_data.db")
        db.init_database()
        print("[startup] DatabaseManager 准备好了")
    except Exception as e:
        print(f"[startup] DatabaseManager init failed: {e}")

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
    
    # 初始化数据库管理器和会话
    db_manager = DatabaseManager()
    session_id = db_manager.create_session("AI_financial_assistant", username, "金融助手对话")

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
            payload = msg.get("payload", {})    # 请求的“有效载荷”，真正装业务数据的部分
            chat_id = msg.get("chat_id", 1)

            if action == "chat.create":
                await websocket.send_text(json.dumps({"type": "ack", "request_id": req_id}))

                # 取首条 user 消息
                text = ""
                for m in payload.get("messages", None):
                    if m.get("role") == "user":
                        # 这里还没有实现记录历史，之后增加
                        text = m.get("content", "")
                        # 保存用户消息到数据库
                        db_manager.add_message(session_id, "user", text)
                        break

                # 获取历史记录
                prompt = {"role":"system","content":str("""
                    你是一个金融助手，需要全力劝阻用户进行高风险金融活动，并且需要耐心，客观的分析用户行为的利害。
                """)}
                history = db.get_session_history(chat_id,limit=20)
                msgs = {"role": "user", "content": f"{text}"}
                msgs = [prompt] + [{"role": h["role"], "content": h["content"]} for h in (history or []) if h.get("role") in {"user", "assistant"}] + [msgs]


                if os.getenv("Type")=="ollama":
                    text_total = ""
                    for delta in "ollama功能还未实装---------请期待":
                        text_total = text_total + delta
                        await websocket.send_text(json.dumps({
                            "type": "delta",
                            "request_id": req_id,
                            "data": {"index": 0, "delta": delta}}))
                else:
                    text_total = ""
                    for delta in ds.stream_chat(msgs,temperature = 0.3):
                        text_total = text_total + delta
                        print(delta, end="", flush=True)
                        await websocket.send_text(json.dumps({
                            "type": "delta",
                            "request_id": req_id,
                            "data": {"index": 0, "delta": delta}}))
                        await asyncio.sleep(0)

                # 保存聊天记录
                db.add_message(chat_id, "user", text)
                db.add_message(chat_id, "assistant", text_total)

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
# 获取所有对话名和ID
# =========================
@app.get("/get_chathistory_name")
async def get_chathistory_name() -> Dict[str, Any]:

    # 因为暂时没有做用户登录，所有这里省去了区分用户
    available_sessions = db.get_available_sessions()

    # 获取历史对话列表
    history_name = {}
    for dt in available_sessions:
        history_name[dt["name"]] = dt.get("session_id")
    return history_name

# =========================
# 获取chat_id下所有聊天
# =========================
@app.get("/get_chathistory")
async def get_chathistory(session_id: int, response_model=list[dict[str, Any]]):
    history = db.get_session_history(session_id)
    msgs = [{"role": h["role"], "content": h["content"]} for h in (history or []) if h.get("role") in {"user", "assistant"}]
    return msgs
  
  
# =========================
# lyy ----- 你待会自己写吧Ψ(￣∀￣)Ψ
# =========================
@app.post("/rag/query", response_model=RAGResponse)
async def rag_query(
        request: RAGRequest,
        user_info: dict = Depends(bearer_auth)
):
    """RAG知识问答接口"""
    if not rag_available:
        raise HTTPException(status_code=503, detail="RAG component not available")

    try:
        import module.rag.QA as rag_qa
        answer = rag_qa.generate_answer(
            query=request.query,
            model_choice=request.model_choice,
            cache_model=request.cache_model
        )
        return RAGResponse(
            answer=answer,
            query=request.query,
            model_used=request.model_choice,
            timestamp=math.floor(asyncio.get_event_loop().time())
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"RAG query failed: {str(e)}")

# =========================
# 挂载前端    路径目前为占位符 重构项目结构之后更改
# =========================
DIST_DIR = os.path.join("../Test/Frontend/dist(1)", "dist")
app.mount("/", StaticFiles(directory=DIST_DIR, html=True), name="spa")
"""
@app.get("/{full_path:path}", response_class=HTMLResponse, include_in_schema=False)
def spa_fallback(full_path: str):
    file_path = Path("../Test/Frontend") / full_path
    if file_path.is_file():
        return FileResponse(str(file_path))
    return FileResponse("../Test/Frontend/2025-9-15-chat_new_vhistory.html")
"""

# =========================
# 本地直接运行
# =========================
if __name__ == "__main__":

    import uvicorn
    uvicorn.run("main:app", host="127.0.0.1", port=8000, reload=False)
