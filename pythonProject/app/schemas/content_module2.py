from pydantic import BaseModel, Field
from typing import Literal, Optional, List, Dict, Any

# Content  内容风险检测
# 检测请求体
class ContentDetectRequest(BaseModel):
    content: str
    content_type: Literal["text", "image", "html"]
    is_url: Optional[bool] = None  # 仅对 image 有意义：True=URL；False=请用 /content/image 上传
    print_yn: Optional[bool] = False

# 检测响应体
class ContentDetectResponse(BaseModel):
    success: bool = True
    is_fraudulent: bool
    risk_score: float
    content_type: str
    content_preview: str

# Chat (WS) payload 结构（非必须，但便于类型提示）
class ChatMessage(BaseModel):
    role: Literal["system", "user", "assistant"]
    content: str

class ChatCreatePayload(BaseModel):
    messages: List[ChatMessage]
    model: Optional[str] = "default"
    temperature: Optional[float] = 0.7
    max_tokens: Optional[int] = 512


