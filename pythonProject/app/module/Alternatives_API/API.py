# deepseek_stream.py
import os
from typing import Iterable, List, Dict, Any, Generator
from openai import OpenAI, OpenAIError

class DeepseekStreamer:
    """
    最小封装：
    - 通过 OpenAI SDK 指向 DeepSeek 兼容端点
    - chat.completions 流式增量输出（yield 文本增量）
    """
    def __init__(
        self,
        api_key: str | None = None,
        base_url: str | None = None,
        model: str = "deepseek-chat",    # 也可用 "deepseek-reasoner" 等
        timeout: float | None = 60.0,
    ):
        self.api_key = api_key if api_key is not None else os.getenv("DeepSeekAPI")
        self.base_url = "https://api.deepseek.com"
        self.model = model
        # OpenAI 兼容：指定 api_key + base_url 即可
        self.client = OpenAI(api_key=self.api_key, base_url=f"{self.base_url}/v1", timeout=timeout)

    def stream_chat(
        self,
        messages: List[Dict[str, Any]],
        **kwargs,
    ) -> Generator[str, None, dict]:
        """
        流式对话：
        - 入参 messages: [{"role":"user","content":"你好"}]
        - yield: 纯文本增量（delta）
        - return: 最终的完整 finish 对象（包含 finish_reason/usage 等）
        """
        try:
            stream = self.client.chat.completions.create(
                model=self.model,
                messages=messages,
                stream=True,          # 开启流式
                **kwargs,             # 可传 temperature, max_tokens 等
            )
            final_obj = {"finish_reason": None, "usage": None, "id": None}

            for chunk in stream:
                # 兼容 OpenAI 的流：choices[0].delta.content 里是增量字符串
                if not chunk.choices:
                    continue
                choice = chunk.choices[0]

                # 增量内容
                delta = getattr(choice.delta, "content", None)
                if delta:
                    yield delta

                # 结束信息（最后一个 chunk）
                if choice.finish_reason is not None:
                    final_obj["finish_reason"] = choice.finish_reason
                    # 有些实现会把 usage 放在最后一帧或整体响应里，这里尽量取
                    final_obj["id"] = getattr(chunk, "id", None)
                    final_obj["usage"] = getattr(chunk, "usage", None)
            return final_obj

        except OpenAIError as e:
            # SDK 层错误（鉴权、配额、网络等）
            raise RuntimeError(f"DeepSeek API 调用失败: {e}") from e
        except Exception as e:
            raise

# ------------------ 最小示例 ------------------
if __name__ == "__main__":
    ds = DeepseekStreamer(model="deepseek-chat")

    msgs = [
        {"role": "system", "content": "你是一个贴心的智能金融助手，只用中文回答。"},
        {"role": "user", "content": "用一句话解释什么是流式输出。"},
    ]

    print(">>> 开始流式：")
    full_text = []
    result = None
    for delta in ds.stream_chat(msgs, temperature=0.3):
        print(delta, end="", flush=True)
        full_text.append(delta)
    print("\n>>> 完整答案：", "".join(full_text))
