# 测试连接代码（是否连接上neoj）
from py2neo import Graph

graph = Graph(
    "bolt://localhost:7687",
    user="neo4j",
    password="lyy20060525",
    name="neo4j"
)

# 测试连接是否成功
try:
    result = graph.run("MATCH (n) RETURN count(n) AS node_count").data()
    print("连接成功，节点数量：", result[0]["node_count"])
except Exception as e:
    print("连接失败：", e)

