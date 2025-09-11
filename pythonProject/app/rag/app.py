import os
from dotenv import load_dotenv
from milvus_processor import MilvusProcessor
from data_model import MonitoringData
from rag_engine import RAGEngine
# 接口对接
from fastapi import FastAPI, HTTPException, Header, Request
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional
import uvicorn
from fastapi.responses import JSONResponse


# 定义请求体模型
class ChatRequest(BaseModel):
    message: str
    projectId: str


# 定义响应体模型
class ChatResponseData(BaseModel):
    reply: str


class ChatResponse(BaseModel):
    data: ChatResponseData
    code: int
    message: str


# 全局变量用于存储 MilvusProcessor 和 RAGEngine 实例
milvus_processor = None
rag_engine = None

app = FastAPI()

# 添加CORS中间件，解决跨域问题
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 允许所有来源，生产环境中应该指定具体的域名
    allow_credentials=True,
    allow_methods=["*"],  # 允许所有方法
    allow_headers=["*"],  # 允许所有请求头
    expose_headers=["*"]  # 允许浏览器访问所有响应头
)


@app.exception_handler(422)
async def validation_exception_handler(request: Request, exc: Exception):
    """处理422错误，提供更详细的错误信息"""
    return JSONResponse(
        status_code=422,
        content={
            "code": 422,
            "message": "请求参数验证失败",
            "detail": str(exc),
            "data": {}
        }
    )


@app.on_event("startup")
async def startup_event():
    global milvus_processor, rag_engine
    load_dotenv()
    # 初始化Milvus连接
    try:
        milvus_processor = MilvusProcessor()
        print("Milvus连接成功")

        # 初始化RAG引擎
        rag_engine = RAGEngine(milvus_processor)
        print("RAG引擎初始化成功")
    except Exception as e:
        print(f"初始化失败：{str(e)}")
        # 不要抛出异常，避免服务启动失败
        # 可以记录日志或采取其他措施

@app.options("/messages/chat")
async def chat_options():
    """
    处理OPTIONS预检请求
    """
    return JSONResponse(
        content={"code": 200, "message": "success"},
        status_code=200
    )
@app.post("/messages/chat", response_model=ChatResponse)
async def chat_endpoint(
        request: ChatRequest,
        authorization: str = Header(None),
        rsakey: str = Header(None)
):
    """
    处理聊天请求的接口
    - request: 包含用户消息和项目ID的请求体
    - authorization: Bearer Token
    - rsakey: RSA密钥
    """
    global rag_engine

    # 检查RAG引擎是否已初始化
    if rag_engine is None:
        raise HTTPException(status_code=503, detail="服务未完全初始化，请稍后重试")

    try:
        # 构造查询语句，结合用户消息和项目ID
        user_query = f"{request.message} 项目ID: {request.projectId}"

        # 调用RAG引擎获取回答
        answer_result = rag_engine.query(user_query)

        # 构造响应体
        response = ChatResponse(
            data=ChatResponseData(reply=answer_result['answer']),
            code=200,
            message="success"
        )

        return response
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"处理请求时发生错误: {str(e)}")


def main():
    load_dotenv()
    # 初始化Milvus连接
    try:
        milvus_processor = MilvusProcessor()
        print("Milvus连接成功")
    except Exception as e:
        print(f"Milvus连接失败：{str(e)}")
        return

    # 测试数据：严格匹配MonitoringData模型
    test_data = [
        {
            "id": "1",
            "api": "getUser",
            "duration": 150,
            "timestamp": 1754537990821,
            "projectId": "project123",
            "event": 1,
            "errorType": "timeout"
        }
    ]

    # 插入测试数据
    try:
        milvus_processor.insert_data(test_data)
        print("测试数据插入成功")
    except Exception as e:
        print(f"插入数据失败：{str(e)}")
        return

    # 测试搜索
    try:
        query = "getUser 接口超时"
        results = milvus_processor.search_similar(query)
        print(f"搜索结果：{results}")
    except Exception as e:
        print(f"搜索失败：{str(e)}")
        return

    # 新增：允许用户自定义输入查询内容
    try:
        # 初始化RAG引擎
        rag_engine = RAGEngine(milvus_processor)
        # 获取用户输入
        user_query = input("请输入你的查询：")
        # 调用问答接口
        answer_result = rag_engine.query(user_query)
        print("\n===== 问答结果 =====")
        print(f"用户问题：{answer_result['query']}")
        print(f"回答：\n{answer_result['answer']}")
    except Exception as e:
        print(f"问答失败：{str(e)}")
        return

    print("===== 操作完成 =====")


if __name__ == "__main__":
    # 如果需要运行测试功能，取消下面的注释
    # main()

    # 启动FastAPI服务
    uvicorn.run("app:app", host="0.0.0.0", port=8000, reload=True)
