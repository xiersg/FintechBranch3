import os
import re
import json
import py2neo
from tqdm import tqdm
import argparse


#导入普通实体
def import_entity(client,type,entity):
    def create_node(client,type,name):
        safe_name = name.replace(' ','\\')
        order = f'create (n:{type}{{名称:"{safe_name}"}})'
        client.run(order)

    print(f'正在导入{type}类数据')
    for en in tqdm(entity):
        create_node(client,type,en)
#导入疾病类实体
def import_fraud_data(client,type,entity):
    print(f'正在导入{type}类数据')
    for fraud in tqdm(entity):
        node = py2neo.Node(type,
                           名称=fraud.get("名称", "").replace(' ','\\'),  # 名称为必须要有的属性
                           诈骗简介=fraud.get("desc", "").replace(' ','\\'),  # 处理desc可能缺失的情况
                           高发原因=fraud.get("cause", "").replace(' ','\\'),  # 处理cause可能缺失的情况
                           预防措施=fraud.get("prevent", "").replace(' ','\\'),  # 处理prevent可能缺失的情况
                           真实案例=fraud.get("case", "").replace(' ','\\') # 处理case可能缺失的情况
                           )
        client.create(node)

def create_all_relationship(client,all_relationship):
    def create_relationship(client,type1, name1,relation, type2,name2):
        safe_name1 = name1.replace(' ','\\')
        safe_name2 = name2.replace(' ','\\')
        order = f"""
            MATCH (a:{type1}{{名称:"{safe_name1}"}}), 
                  (b:{type2}{{名称:"{safe_name2}"}}) 
            CREATE (a)-[r:{relation}]->(b)
            """
        client.run(order)
    print("正在导入关系.....")
    for type1, name1,relation, type2,name2  in tqdm(all_relationship):
        create_relationship(client,type1, name1,relation, type2,name2)

if __name__ == "__main__":
    #连接数据库的一些参数
    parser = argparse.ArgumentParser(description="通过anti_fraud.json文件,创建一个知识图谱")
    parser.add_argument('--website', type=str, default='bolt://localhost:7687', help='neo4j的连接网站')
    parser.add_argument('--user', type=str, default='neo4j', help='neo4j的用户名')
    parser.add_argument('--password', type=str, default='lyy20060525', help='neo4j的密码')
    parser.add_argument('--dbname', type=str, default='neo4j', help='数据库名称')
    args = parser.parse_args()

    #连接...
    client = py2neo.Graph(args.website, user=args.user, password=args.password, name=args.dbname)

    #将数据库中的内容删光
    is_delete = input('注意:是否删除neo4j上的所有实体 (y/n):')
    if is_delete=='y':
        client.run("match (n) detach delete (n)")

    with open('./data/anti_fraud_new.json','r',encoding='utf-8') as f:
        all_data = json.load(f)
        # all_data = f.read().split('\n')
    
    #所有实体
    all_entity = {
        "诈骗类型": [],
        "诈骗特征": [],
        "诈骗工具": [],
        "反诈措施": [],
        "处理机构": [],
        "验证方法": [],
        "关联类型": []
    }
    
    # 实体间的关系
    relationship = []
    for data in all_data:
        if not data.get("name"):
            print("跳过缺少名称的记录")
            continue

        fraud_name = data.get("name","")
        all_entity["诈骗类型"].append({
            "名称": fraud_name,
            "诈骗简介": data.get("desc", ""),
            "高发原因": data.get("cause", ""),
            "预防措施": data.get("prevent", ""),
            "真实案例": data.get("case", "")
        })
        # 诈骗工具关系（诈骗类型-使用工具-诈骗工具）
        tools = data.get("common_tool", [])
        all_entity["诈骗特征"].extend(tools)  # 添加实体
        if tools:
            relationship.extend([("诈骗类型", fraud_name, "使用工具", "诈骗工具",tool)for tool in tools])

        # 反诈措施关系（诈骗类型-对应措施-反诈措施）
        measures = data.get("measure", [])
        all_entity["反诈措施"].extend(measures)
        if measures:
            relationship.extend([
                ("诈骗类型", fraud_name, "对应措施", "反诈措施", mea)
                for mea in measures if mea
            ])

        # 处理机构关系（诈骗类型-处理机构-处理机构）
        agencies = data.get("handling_agency", [])
        all_entity["处理机构"].extend(agencies)
        if agencies:
            relationship.extend([
                ("诈骗类型", fraud_name, "处理机构", "处理机构", agency)
                for agency in agencies if agency
            ])

        # 验证方法关系（诈骗类型-验证方法-验证方法）
        verifies = data.get("verify_method", [])
        all_entity["验证方法"].extend(verifies)
        if verifies:
            relationship.extend([
                ("诈骗类型", fraud_name, "验证方法", "验证方法", verify)
                for verify in verifies if verify
            ])

        # 关联诈骗类型关系（诈骗类型-关联类型-诈骗类型）
        related_types = data.get("related_type", [])
        all_entity["关联类型"].extend(related_types)
        if related_types:
             relationship.extend([
                ("诈骗类型", fraud_name, "关联类型", "诈骗类型", related)
                 for related in related_types if related
            ])

    # 数据清洗：去重关系和实体
    relationship = list(set(relationship))
    all_entity = {
        k: (list(set(v)) if k != "诈骗类型" else v)
        for k, v in all_entity.items()
    }

    # 保存关系到文件（用于后续NER和模型训练）
    with open("./data/rel_aug.txt", 'w', encoding='utf-8') as f:
        for rel in relationship:
            f.write(" ".join(rel) + '\n')

    # 保存实体到文件
    if not os.path.exists('data/ent_aug'):
        os.mkdir('data/ent_aug')
    for k, v in all_entity.items():
        with open(f'data/ent_aug/{k}.txt', 'w', encoding='utf8') as f:
            if k != '诈骗类型':
                # 非核心实体直接写入名称
                for i, ent in enumerate(v):
                    f.write(ent + ('\n' if i != len(v) - 1 else ''))
            else:
                # 核心实体提取名称字段
                for i, ent in enumerate(v):
                    f.write(ent['名称'] + ('\n' if i != len(v) - 1 else ''))

    # 导入实体到Neo4j（仅诈骗类型有属性，其他为普通实体）
    for k in all_entity:
        if k != "诈骗类型":
            import_entity(client, k, all_entity[k])
        else:
            import_fraud_data(client, k, all_entity[k])

    # 导入关系到Neo4j
    create_all_relationship(client, relationship)
    print("金融反诈知识图谱构建完成")