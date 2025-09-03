import requests
import json
import os
from dotenv import load_dotenv

load_dotenv()
API_KEY = os.getenv("DEEPSEEK_API_KEY")
if not API_KEY:
    raise ValueError("❌ 请在.env文件中设置DEEPSEEK_API_KEY")

url = "https://api.deepseek.com/v1/chat/completions"
headers = {
    "Authorization": f"Bearer {API_KEY}",
    "Content-Type": "application/json"
}

data = {
    "model": "deepseek-reasoner",
    "messages": [{"role": "user", "content": "你好"}],
    "stream": True,
    "temperature": 0.7
}

try:
    response = requests.post(url, headers=headers, json=data, stream=True, timeout=10)
    response.raise_for_status()
    print("HTTP Status Code:", response.status_code, '\n')  # 打印状态码
    print("Raw Response:", response.text, '\n')  # 打印原始响应

    print("AI回复：", end="", flush=True)
    for line in response.iter_lines():
        if line:
            decoded = line.decode('utf-8')
            if decoded.startswith("data:"):
                try:
                    chunk = json.loads(decoded[5:])
                    # 关键修改：检查delta是否存在且包含content
                    if chunk.get("choices") and chunk["choices"][0].get("delta", {}).get("content"):
                        print(chunk["choices"][0]["delta"]["content"], end="", flush=True)
                except json.JSONDecodeError:
                    continue

except requests.exceptions.RequestException as e:
    print(f"请求失败: {e}")