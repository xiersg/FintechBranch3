import torch
import pickle
import random
from py2neo import Graph
from transformers import AutoTokenizer, AutoModel, BertTokenizer
import ollama
import ner_model as zwk  # 导入ner_model中的工具类

"""加载模型和相关工具"""


def load_model(cache_model):
    """加载模型和相关工具"""
    device = torch.device('cuda') if torch.cuda.is_available() else torch.device('cpu')

    # 加载标签映射
    with open('tmp_data/tag2idx.npy', 'rb') as f:
        tag2idx = pickle.load(f)
    idx2tag = list(tag2idx)

    # 加载规则匹配和实体对齐工具
    rule = zwk.rule_find()  # 实例化规则匹配工具
    tfidf_r = zwk.tfidf_alignment()  # 实例化TF-IDF实体对齐工具

    # 加载BERT实体识别模型
    model_name = 'hfl/chinese-roberta-wwm-ext'

    bert_tokenizer = BertTokenizer.from_pretrained(model_name)
    bert_model = zwk.Bert_Model(model_name, hidden_size=128, tag_num=len(tag2idx), bi=True)
    bert_model.load_state_dict(torch.load(f'model/{cache_model}.pt'))
    bert_model = bert_model.to(device)
    bert_model.eval()

    return bert_tokenizer, bert_model, idx2tag, rule, tfidf_r, device


"""识别用户查询意图"""


def intent_recognition(query, model_choice):
    """识别用户查询意图"""
    prompt = f"""
阅读下列提示，识别用户问题中的查询意图：

**查询类别**
- "查询诈骗类型定义"
- "查询诈骗高发原因"
- "查询诈骗预防措施"
- "查询诈骗真实案例"
- "查询被骗应对方法"
- "查询诈骗处理机构"
- "查询诈骗核实方式"
- "查询诈骗关联类型区别"
- "查询诈骗作案工具"

处理步骤：
1. 仔细阅读用户问题
2. 依次判断每个类别是否相关
3. 将相关类别加入输出列表（不超过5个）
4. 用#添加简短解释

现在，你已经知道如何解决问题了，请你解决下面这个问题并将结果输出！
问题输入："{query}"
"""
    return ollama.generate(model=model_choice, prompt=prompt)['response']


"""生成实体属性查询提示"""


def add_property_prompt(entity, property_name, graph_client):
    """生成实体属性查询提示"""
    try:
        query = f"match (a:诈骗类型{{名称:'{entity}'}}) return a.{property_name}"
        result = graph_client.run(query).data()
        if result:
            prompt = f"<提示>用户查询{entity}的{property_name}："
            prompt += "".join(list(result[0].values()))
            return prompt + "</提示>"
        return ""
    except Exception as e:
        print(f"属性查询错误: {e}")
        return ""


"""生成实体关系查询提示"""


def add_relation_prompt(entity, relation, target_type, graph_client):
    """生成实体关系查询提示"""
    try:
        query = f"match (a:诈骗类型{{名称:'{entity}'}})-[r:{relation}]->(b:{target_type}) return b.名称"
        result = [list(item.values())[0] for item in graph_client.run(query).data()]
        if result:
            prompt = f"<提示>用户查询{entity}的{relation}："
            prompt += "、".join(result)
            return prompt + "</提示>"
        return ""
    except Exception as e:
        print(f"关系查询错误: {e}")
        return ""


"""生成最终回答"""


def generate_answer(query, model_choice="llama3", cache_model="ner_model"):
    """生成最终回答"""
    bert_tokenizer, bert_model, idx2tag, rule, tfidf_r, device = load_model(cache_model)
    graph_client = Graph('bolt://localhost:7687', user='neo4j', password='lyy20060525', name='neo4j')  # 实例化Neo4j客户端

    entities = zwk.get_ner_result(bert_model, bert_tokenizer, query, rule, tfidf_r, device, idx2tag)
    print(f"识别到的实体: {entities}")

    intent_result = intent_recognition(query, model_choice)
    print(f"意图识别结果: {intent_result}")

    base_prompt = """<指令>你是反欺诈问答机器人，仅基于提供的提示回答，不可自由发挥。
非反欺诈问题回答"我只能回答反欺诈相关的问题。"，无信息时回答"根据已知信息无法回答该问题"。</指令>"""

    # 意图与知识图谱映射关系
    intent_mappings = {
        "查询诈骗类型定义": ("desc", add_property_prompt),
        "查询诈骗高发原因": ("cause", add_property_prompt),
        "查询诈骗预防措施": ("prevent", add_property_prompt),
        "查询诈骗真实案例": ("case", add_property_prompt),
        "查询被骗应对方法": ("measure", add_property_prompt),
        "查询诈骗处理机构": ("handling_agency", "机构", add_relation_prompt),
        "查询诈骗核实方式": ("verify_method", add_property_prompt),
        "查询诈骗关联类型区别": ("related_type", "关联类型", add_relation_prompt),
        "查询诈骗作案工具": ("common_tool", "诈骗工具", add_relation_prompt)
    }

    # 根据识别到的实体和意图添加知识
    if '诈骗类型' in entities:
        fraud_type = entities['诈骗类型']
        for intent_key, handler in intent_mappings.items():
            if intent_key in intent_result:
                if len(handler) == 2:  # 属性查询
                    prop_name, func = handler
                    base_prompt += func(fraud_type, prop_name, graph_client)
                else:  # 关系查询
                    rel_name, target, func = handler
                    base_prompt += func(fraud_type, rel_name, target, graph_client)

    final_prompt = f"{base_prompt}<用户问题>{query}</用户问题>"
    return ollama.generate(model=model_choice, prompt=final_prompt)['response']


if __name__ == "__main__":
    # 示例查询
    user_query = "中奖诈骗的原因是什么？"


    answer = generate_answer(
        query=user_query,
        model_choice="llama3",
        cache_model="best_roberta_rnn_model_ent_aug"
    )

    print("\n回答：")
    print(answer)