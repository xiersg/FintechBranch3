from pymilvus import connections, FieldSchema, CollectionSchema, DataType, Collection, utility
from sentence_transformers import SentenceTransformer
from data_model import MonitoringData  # 使用更新后的MonitoringData类
import os
from dotenv import load_dotenv
from typing import List, Dict, Tuple

os.environ["HF_ENDPOINT"] = "https://hf-mirror.com"


class MilvusProcessor:
    def __init__(self):
        self.collection_name = "monitoring_rag"
        self.milvus_host = "47.113.224.195"
        self.milvus_port = "34530"
        self.embedding_model = SentenceTransformer('all-MiniLM-L6-v2') 
        self._connect_milvus()  # 初始化时创建连接
        self._init_collection()  # 初始化集合

    def _connect_milvus(self) -> None:
        """显式创建并绑定连接别名"""
        connect_params = {
            "host": self.milvus_host,
            "port": self.milvus_port,
            "alias": "rag_connection"  # 固定连接别名
        }
        # 若连接已存在，复用；否则新建
        if connections.has_connection("rag_connection"):
            print("复用已存在的连接")
            return
        connections.connect(**connect_params)
        if not connections.has_connection("rag_connection"):
            raise ConnectionError("Milvus连接创建失败")

        connections.connect(**connect_params)
        try:
            collections = utility.list_collections(using="rag_connection")
            print(f"连接成功，当前集合列表：{collections}")
        except Exception as e:
            raise ConnectionError(f"连接无效：{str(e)}")

    def _init_collection(self) -> None:
        """修复索引不存在问题：强制检查并创建索引"""
        # 1. 检查集合是否存在，不存在则创建
        if utility.has_collection(self.collection_name, using="rag_connection"):
            self.collection = Collection(self.collection_name, using="rag_connection")
            print(f" 加载现有集合：{self.collection_name}")
        else:
            # 定义集合Schema
            fields = [
                FieldSchema(name="id", dtype=DataType.STRING, is_primary=True),
                FieldSchema(name="vector", dtype=DataType.FLOAT_VECTOR,
                            dim=self.embedding_model.get_sentence_embedding_dimension()),
                FieldSchema(name="timestamp", dtype=DataType.INT64),
                FieldSchema(name="project_id", dtype=DataType.STRING),
                FieldSchema(name="error_type", dtype=DataType.STRING),
                FieldSchema(name="event", dtype=DataType.INT64),
                FieldSchema(name="duration", dtype=DataType.INT64),
                FieldSchema(name="raw_text", dtype=DataType.STRING)
            ]
            schema = CollectionSchema(fields, "Monitoring data for RAG")
            self.collection = Collection(
                name=self.collection_name,
                schema=schema,
                using="rag_connection"
            )
            print(f" 创建新集合：{self.collection_name}")

        # 2. 强制删除旧索引
        try:
            if self.collection.has_index():
                self.collection.drop_index(using="rag_connection")
                print(" 已删除旧索引")
        except Exception as e:
            print(f" 无旧索引或删除失败（可忽略）：{e}")

        # 3. 重新创建索引（确保一定存在）
        index_params = {
            "index_type": "IVF_FLAT",
            "params": {"nlist": 128},
            "metric_type": "L2"
        }
        print(f" 为集合 {self.collection_name} 创建新索引...")
        self.collection.create_index(
            field_name="vector",
            index_params=index_params,
            using="rag_connection"
        )
        self.collection.flush(using="rag_connection")  # 立即生效
        print(" 索引创建成功")

        # 4. 加载集合（索引已存在）
        self.collection.load(using="rag_connection")
        print(" 集合已加载到内存")

    def insert_data(self, raw_data_list: list[dict]) -> None:
        """插入数据：显式绑定连接"""
        if not raw_data_list:
            print("无数据可插入")
            return
        # 转换为数据模型
        monitoring_data = [MonitoringData.from_raw(data) for data in raw_data_list]
        # 构造插入数据
        entities = [
            [str(data.id) for data in monitoring_data],  # id转为字符串（匹配VARCHAR）
            [self.embedding_model.encode(data.to_text()) for data in monitoring_data],
            [data.timestamp for data in monitoring_data],
            [data.projectId for data in monitoring_data],
            [data.errorType or "" for data in monitoring_data],
            [data.event for data in monitoring_data],
            [data.duration for data in monitoring_data],
            [data.to_text() for data in monitoring_data]
        ]
        # 显式指定连接插入数据
        insert_result = self.collection.insert(entities, using="rag_connection")
        self.collection.flush(using="rag_connection")  # 显式指定连接刷新
        print(f" 插入成功：{len(insert_result.primary_keys)}条数据")

    def search_similar(self, query: str, top_k: int = 5) -> list:
        """搜索：显式绑定连接"""
        query_vector = self.embedding_model.encode(query)
        # 显式指定连接执行搜索
        results = self.collection.search(
            data=[query_vector],
            anns_field="vector",
            param={"metric_type": "L2", "params": {"nprobe": 20}},
            limit=top_k,
            output_fields=["timestamp", "project_id", "error_type", "event", "duration", "raw_text"],
            using="rag_connection"  # 显式指定连接
        )
        # 格式化结果
        similar_docs = []
        for hit in results[0]:
            entity = hit.entity.to_dict()
            similar_docs.append({
                "distance": hit.distance,
                "id": hit.id,
                "timestamp": entity["timestamp"],
                "project_id": entity["project_id"],
                "error_type": entity["error_type"],
                "event": entity["event"],
                "duration": entity["duration"],
                "raw_text": entity["raw_text"]
            })
        return similar_docs