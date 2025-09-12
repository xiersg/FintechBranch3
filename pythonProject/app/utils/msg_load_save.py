import json
import os
from typing import List, Dict, Any

def save_messages(logfile: str, msgs: List[Dict[str, Any]]) -> None:
    """把整个对话列表保存为 JSON 文件。"""
    os.makedirs(os.path.dirname(os.path.abspath(logfile)), exist_ok=True)
    with open(logfile, "w", encoding="utf-8") as f:
        json.dump(msgs, f, ensure_ascii=False, indent=2)  # 格式化写入，更好读

def load_messages(logfile: str) -> List[Dict[str, Any]]:
    """从 JSON 文件读取对话列表，如果文件不存在则返回空列表。"""
    if not os.path.exists(logfile):
        print("历史会话不存在，新建会话")
        return []
    with open(logfile, "r", encoding="utf-8") as f:
        return json.load(f)

# ========== 示例 ==========
if __name__ == "__main__":
    User_name = "xiersg"
    File_name = f"{User_name}_chat.json"
    logfile = f"data/{File_name}"

    # 你传入的格式
    msgs = [
        {"role": "system", "content": "你是一个简洁的助理，只用中文回答。"},
        {"role": "user", "content": "你好呀！"},
    ]

    # 保存
    save_messages(logfile, msgs)

    # 读取
    loaded = load_messages(logfile)
    print("读取到的消息：", loaded)
    print("第一条消息内容：", loaded[0]["content"])
