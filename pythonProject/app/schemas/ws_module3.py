from pydantic import BaseModel, Field
from typing import Literal, Optional, List, Dict, Any

# Chat (WS) payload 结构（非必须，但便于类型提示）
class ChatMessage(BaseModel):
    role: Literal["system", "user", "assistant"]
    content: str

class ChatCreatePayload(BaseModel):
    messages: List[ChatMessage]
    model: Optional[str] = "default"
    temperature: Optional[float] = 0.7
    max_tokens: Optional[int] = 512