#处理监控数据，统一监控数据的格式
from dataclasses import dataclass, fields
from typing import Optional
from pydantic import BaseModel, Field

@dataclass # 快速定义存储数据类
class MonitoringData:
    """监控数据模型，统一原始数据格式"""
    id : Optional[int]
    stack: Optional[str]
    timestamp: int
    projectId: str
    errorType: Optional[str]
    event: int
    duration: int
    memoryUsage: Optional[str]
    apiTime: Optional[int]
    apiName: Optional[str]
    className: Optional[str]
    metric: Optional[str]
    message: Optional[str]
    breadcrumbs: Optional[str]
    api: Optional[str]
    userAgent: Optional[str]

    @classmethod
    def from_raw(cls, raw_data: dict) -> "MonitoringData":
        """从原始字典转换为模型"""
        class_fields = {field.name for field in fields(cls)}
        # 过滤原始数据，只保留类中定义的字段
        filtered_data = {k: v for k, v in raw_data.items() if k in class_fields}

        return cls(
            id=filtered_data.get("id"),
            stack=filtered_data.get("stack"),
            timestamp=int(filtered_data.get("timestamp", "0")),
            projectId=filtered_data.get("projectId", ""),
            errorType=filtered_data.get("errorType"),
            event=filtered_data.get("event", 0),
            duration=filtered_data.get("duration", 0),
            memoryUsage=filtered_data.get("memoryUsage"),
            apiTime=filtered_data.get("apiTime"),
            apiName=filtered_data.get("apiName"),
            className=filtered_data.get("className"),
            metric=filtered_data.get("metric"),
            message=filtered_data.get("message"),
            breadcrumbs=filtered_data.get("breadcrumbs"),
            api=filtered_data.get("api"),
            userAgent=filtered_data.get("userAgent")
        )
    
    def to_text(self) -> str:
        """转换为生成向量的文本，方便分析检索"""
        # 基础信息
        base_info = f"项目{self.projectId}在{self.timestamp}记录到事件{self.event}，持续{self.duration}毫秒"

        # 错误相关信息
        error_info = []
        if self.errorType:
            error_info.append(f"错误类型：{self.errorType}")
        if self.message:
            error_info.append(f"错误消息：{self.message}")
        if self.stack:
            error_info.append(f"堆栈信息：{self.stack}")

        # 接口相关信息
        api_info = []
        if self.apiName:
            api_info.append(f"接口名称：{self.apiName}")
        if self.api:
            api_info.append(f"接口地址：{self.api}")
        if self.apiTime is not None:
            api_info.append(f"接口耗时：{self.apiTime}毫秒")

        # 其他信息
        other_info = []
        if self.className:
            other_info.append(f"类名：{self.className}")
        if self.memoryUsage:
            other_info.append(f"内存使用：{self.memoryUsage}")
        if self.metric:
            other_info.append(f"指标：{self.metric}")
        if self.userAgent:
            other_info.append(f"用户代理：{self.userAgent}")  # 简化userAgent

        # 组合所有信息
        parts = [base_info]
        if error_info:
            parts.append("；".join(error_info))
        if api_info:
            parts.append("；".join(api_info))
        if other_info:
            parts.append("；".join(other_info))

        return "。".join(parts) + "。"