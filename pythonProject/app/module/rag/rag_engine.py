from milvus_processor import MilvusProcessor
import openai
from typing import Optional, List, Dict
import os
from dotenv import load_dotenv


class RAGEngine:
    """RAG"""
    PROMPT_TEMPLATE = """
    任务：你是一个金融助手，需基于提供的金融交易数据，生成精准结论和可视化建议。请严格遵循以下规则。

    一、数据处理规则
    1.数据过滤：仅使用符合时间范围和项目范围的金融交易数据。
    2.可视化建议：
    可视化均用markdown形式展示。

    二、输出格式要求    
    1. 关键结论：分点列出核心发现，每条结论需关联具体数据。
    2. 回答不需要说多余的部分，只需要回答出要点就可以
    3. 若数据不足，明确说明“未找到足够信息”
    4. 所有回答均需根据数据库里面的数据来进行回答
    5. 要把搜索数据库，以及数据库搜索出来的数据也展示出来。
    6. 把当前的时间也展示出来。
    
    用户问题：{query}
    请根据以上规则，结合金融交易数据，生成符合要求的分析结果。
    """

    def __init__(self, milvus_processor: MilvusProcessor, ollama_model: str = "deepseek-r1:8b"):
        load_dotenv()
        self.milvus_processor = milvus_processor

        # 配置Ollama（本地模型）
        self.ollama_model = ollama_model
        openai.api_base = "http://localhost:11434/v1"  # Ollama API地址
        openai.api_key = "dummy_key"  # 任意值，Ollama不验证

    def query(self, query: str, top_k: int = 5) -> Dict:
        """处理用户查询，返回RAG结果"""
        # 1. 检索相似数据
        similar_docs = self.milvus_processor.search_similar(query, top_k)

        # 2. 生成回答
        if not similar_docs:
            answer = "未找到相关数据"
        else:
            # 构建上下文
            context = "\n".join([
                f"[{doc['timestamp']}] 项目{doc['project_id']} 错误类型：{doc['error_type']}：{doc['raw_text']}"
                for doc in similar_docs
            ])

            # 调用Ollama生成回答
            try:
                response = openai.ChatCompletion.create(
                    model=self.ollama_model,
                    messages=[
                        {"role": "system", "content": "你是专业的金融助手，严格基于提供的数据回答。"},
                        {"role": "user", "content": self.PROMPT_TEMPLATE.format(context=context, query=query)}
                    ],
                    temperature=0.2,
                    max_tokens=100000
                )
                answer = response.choices[0].message["content"]
            except Exception as e:
                answer = f"生成回答失败：{str(e)}"

        # 3. 返回结果
        return {
            "query": query,
            "answer": answer,
            "similar_docs": similar_docs,
            "doc_count": len(similar_docs)
        }