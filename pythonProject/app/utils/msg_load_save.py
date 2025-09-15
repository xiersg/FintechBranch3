# import json
# import os
# from typing import List, Dict, Any, Optional
# import sqlite3
# from datetime import datetime
# #
# # class DatabaseManager:
# #     def __init__(self, db_path: str = "data/chat.db"):
# #         self.db_path = db_path
# #         self.init_database()
# #         self.conn = sqlite3.connect(self.db_path)
# #
# #     def init_database(self):
# #         """
# #         初始化数据库，如果数据库不存在则创建。
# #         """
# #         with sqlite3.connect(self.db_path) as conn:
# #             cursor = conn.cursor()
# #
# #             cursor.execute("""
# #                 CREATE TABLE IF NOT EXISTS messages(
# #                     id INTEGER PRIMARY KEY AUTOINCREMENT,
# #                     user_name TEXT NOT NULL,
# #                     message TEXT NOT NULL,
# #                 timestamp TEXT NOT NULL
# #                 """)
# #
# #             cursor.execute("""
# #                 CREATE TABLE IF NOT EXISTS users(
# #                     id INTEGER PRIMARY KEY AUTOINCREMENT,
# #                     user_name TEXT NOT NULL UNIQUE
# #
# #             """)
# #
# #     def save_messages(self, user_name: str, messages: List[Dict[str, Any]]) -> None:
# #         """
# #         保存用户的对话记录到数据库。
# #         """
# #         os.makedirs(os.path.dirname(self.db_path), exist_ok=True)
# #         with sqlite3.connect(self.db_path) as conn:
# #             pass
#
#
#
#
#
#
# def save_messages(logfile: str, msgs: List[Dict[str, Any]]) -> None:
#     """把整个对话列表保存为 JSON 文件。"""
#     os.makedirs(os.path.dirname(os.path.abspath(logfile)), exist_ok=True)
#     with open(logfile, "w", encoding="utf-8") as f:
#         json.dump(msgs, f, ensure_ascii=False, indent=2)  # 格式化写入，更好读
#
# def load_messages(logfile: str) -> List[Dict[str, Any]]:
#     """从 JSON 文件读取对话列表，如果文件不存在则返回空列表。"""
#     if not os.path.exists(logfile):
#         print("历史会话不存在，新建会话")
#         return []
#     with open(logfile, "r", encoding="utf-8") as f:
#         return json.load(f)
#
# # ========== 示例 ==========
# if __name__ == "__main__":
#
#     User_name = "xiersg"
#     File_name = f"{User_name}_chat.json"
#     logfile = f"data/{File_name}"
#
#     # 你传入的格式
#     msgs = [
#         {"role": "system", "content": "你是一个简洁的助理，只用中文回答。"},
#         {"role": "user", "content": "你好呀！"},
#     ]
#
#     # 保存
#     save_messages(logfile, msgs)
#
#     # 读取
#     loaded = load_messages(logfile)
#     print("读取到的消息：", loaded)
#     print("第一条消息内容：", loaded[0]["content"])

import sqlite3
from typing import Dict, List, Optional
import json
from datetime import datetime
import os


class DatabaseManager:
    def __init__(self, db_path: str = "chat_data.db"):
        self.db_path = db_path
        self.init_database()

    def init_database(self):
        """初始化数据库表"""
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()

            # 创建对话会话表
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS chat_sessions (
                    session_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    character_type TEXT NOT NULL,
                    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    last_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    context_data TEXT,
                    name TEXT,
                    description TEXT
                )
            """)

            # 创建对话历史表
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS chat_history (
                    message_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    session_id INTEGER,
                    role TEXT NOT NULL,
                    content TEXT NOT NULL,
                    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (session_id) REFERENCES chat_sessions(session_id)
                )
            """)

            conn.commit()

    def create_session(self, character_type: str, name: str, description: str) -> int:
        """创建新的对话会话"""
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute("""
                INSERT INTO chat_sessions (character_type, name, description)
                VALUES (?, ?, ?)
            """, (character_type, name, description))
            conn.commit()
            return cursor.lastrowid

    def save_context(self, session_id: int, context_data: Dict):
        """保存上下文数据"""
        context_json = json.dumps(context_data, default=str)
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute("""
                UPDATE chat_sessions 
                SET context_data = ?, last_time = CURRENT_TIMESTAMP
                WHERE session_id = ?
            """, (context_json, session_id))
            conn.commit()

    def load_context(self, session_id: int) -> Optional[Dict]:
        """加载上下文数据"""
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute("""
                SELECT context_data FROM chat_sessions
                WHERE session_id = ?
            """, (session_id,))
            result = cursor.fetchone()
            if result and result[0]:
                return json.loads(result[0])
        return None

    def add_message(self, session_id: int, role: str, content: str):
        """添加对话消息"""
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute("""
                INSERT INTO chat_history (session_id, role, content)
                VALUES (?, ?, ?)
            """, (session_id, role, content))
            conn.commit()

    def get_session_history(self, session_id: int, limit: int = None) -> List[Dict]:
        """获取会话历史记录"""
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            query = """
                SELECT role, content, timestamp 
                FROM chat_history
                WHERE session_id = ?
                ORDER BY timestamp ASC
            """
            if limit:
                query += f" LIMIT {limit}"

            cursor.execute(query, (session_id,))
            return [
                {
                    "role": role,
                    "content": content,
                    "timestamp": timestamp
                }
                for role, content, timestamp in cursor.fetchall()
            ]

    def get_available_sessions(self) -> List[Dict]:
        """获取所有可用的会话"""
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute("""
                SELECT 
                    session_id, character_type, start_time, last_time,
                    name, description
                FROM chat_sessions
                ORDER BY last_time DESC
            """)
            return [
                {
                    "session_id": sid,
                    "character_type": char_type,
                    "start_time": start_time,
                    "last_time": last_time,
                    "name": name,
                    "description": desc
                }
                for sid, char_type, start_time, last_time, name, desc
                in cursor.fetchall()
            ]


if __name__ == '__main__':
    db = DatabaseManager()
    db.init_database()
    session_id = db.create_session("AI_financial_assistant", "Character 2", "用于协助金融相关服务的助手")
    print("对话创建成功！")
    db.add_message(session_id, "user", "Hello")
    db.add_message(session_id, "assistant", "Hi there!")
    print("添加记录成功！")
    history = db.get_session_history(session_id)
    print(history)
    available_sessions = db.get_available_sessions()
    print(available_sessions)