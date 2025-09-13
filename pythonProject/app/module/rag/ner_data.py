import os
import random
import ahocorasick
import re
import json
from tqdm import tqdm

class Build_Ner_data():
    """
        这是一个金融反诈领域的ner数据生成类。
        这里有七个金融反诈类相关标签
        """
    def __init__(self):
        self.idx2type = ["关联类型", "反诈措施", "处理机构", "诈骗工具", "诈骗特征", "诈骗类型", "验证方法"]
        self.type2idx = {t: i for i, t in enumerate(self.idx2type)}
        self.max_len = 100
        self.p = ['，', '。' , '！' , '；' , '：' , ',' ,'.','?','!',';']
        self.ahos = [ahocorasick.Automaton() for _ in range(len(self.idx2type))]

        for type in self.idx2type:
            idx = self.type2idx[type]
            file_path = os.path.join('data', 'ent_aug', f'{type}.txt')

            if not os.path.exists(file_path):
                print(f"警告：{file_path} 不存在，为该类型自动机添加占位词")
                self.ahos[idx].add_word('__EMPTY__', '__EMPTY__')
                self.ahos[idx].make_automaton()
                continue

            with open(file_path, encoding='utf-8') as f:
                all_en = f.read().split('\n')

            valid_entities = [en.strip() for en in all_en if en.strip() and len(en.strip()) >= 2]

            if not valid_entities:
                print(f"警告：{file_path} 中没有有效实体（长度≥2），添加占位词")
                self.ahos[idx].add_word('__EMPTY__', '__EMPTY__')
            else:
                for en in valid_entities:
                    self.ahos[idx].add_word(en, en)

            # 构建自动机（无论是否有有效实体，都必须调用）
            self.ahos[idx].make_automaton()
    def split_text(self,text):
        """
        将长文本随机分割为短文本

        :param arg1: 长文本
        :return: 返回一个list,代表分割后的短文本
        :rtype: list
        """
        text = text.replace('\n',',')
        pattern = r'([，。！；：,.?!;])(?=.)|[？,]'

        sentences = []

        for s in re.split(pattern, text):
            if s and len(s)>0:
                sentences.append(s)

        sentences_text = [x for x in sentences if x not in self.p]
        sentences_Punctuation = [x for x in sentences[1::2] if x in self.p]
        split_text = []
        now_text = ''

        #随机长度,有15%的概率生成短文本 10%的概率生成长文本
        for i in range(len(sentences_text)):
            if (len(now_text)> self.max_len and random.random()<0.9 or random.random()<0.15) and len(now_text)>0:
                split_text.append(now_text)
                now_text = sentences_text[i]
                if i < len(sentences_Punctuation):
                    now_text += sentences_Punctuation[i]
            else:
                now_text += sentences_text[i]
                if i < len(sentences_Punctuation):
                    now_text+=sentences_Punctuation[i]
        if len(now_text)>0:
            split_text.append(now_text)

        #随机选取30%的数据,把末尾标点改为。
        for i in range(len(split_text)):
            if random.random()<0.3:
                if(split_text[i][-1] in self.p):
                    split_text[i] = split_text[i][:-1]+'。'
                else:
                    split_text[i] = split_text[i]+'。'
        return split_text
    def make_text_label(self,text):
        """
        通过ahocorasick类对文本进行识别，创造出文本的ner标签
        :param arg1: 文本
        :return: 返回一个list,代表标签
        :rtype: list
        """
        label = ['O']*len(text)
        flag = 0
        mp = {}
        for type in self.idx2type:
            li = list(self.ahos[self.type2idx[type]].iter(text))
            if len(li)==0:
                continue
            li = sorted(li,key=lambda x:len(x[1]),reverse=True)
            for en in li:
                ed,name = en
                st = ed-len(name)+1
                if st in mp or ed in mp:
                    continue
                label[st:ed+1] = ['B-'+type] + ['I-'+type]*(ed-st)
                flag = flag+1
                for i in range(st,ed+1):
                    mp[i] = 1
        return label,flag

#将文本和对应的标签写入ner_data2.txt
def build_file(all_text,all_label):
    with open(os.path.join('data','ner_data_aug.txt'),"w",encoding="utf-8") as f:
        for text, label in zip(all_text, all_label):
            for t, l in zip(text, label):
                f.write(f'{t} {l}\n')
            f.write('\n')
if __name__ == "__main__":
    build_ner_data = Build_Ner_data()
    all_text, all_label = [], []
    with open(os.path.join('data','anti_fraud.json'),'r',encoding='utf-8') as f:
        all_data = json.load(f)


    for data in tqdm(all_data):
        data_text = [data.get("desc",""),data.get("prevent", ""),data.get("cause", "")]

        data_text_split = []
        for text in data_text:
            if len(text)==0:
                continue
            text_split = build_ner_data.split_text(text)
            for tmp in text_split:
                if len(tmp)>0:
                    data_text_split.append(tmp)
        for text in data_text_split:
            if len(text)==0:
                continue
            label,flag = build_ner_data.make_text_label(text)
            if flag>=1:
                assert (len(text) == len(label))
                all_text.append(text)
                all_label.append(label)

    build_file(all_text,all_label)

