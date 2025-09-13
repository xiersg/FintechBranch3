import random
import torch
from torch import nn
import os
import pickle

from sklearn.model_selection import train_test_split
from torch.utils.data import Dataset,DataLoader
from transformers import BertModel,BertTokenizer
from tqdm import tqdm
from seqeval.metrics import f1_score
import ahocorasick
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

cache_model = 'best_roberta_rnn_model_ent_aug'

def get_data(path,max_len=None):
    all_text,all_tag = [],[]
    with open(path,'r',encoding='utf8') as f:
        all_data = f.read().split('\n')

    sen,tag = [],[]
    for data in all_data:
        data = data.split(' ')
        if(len(data)!=2):
            if len(sen)>2:
                all_text.append(sen)
                all_tag.append(tag)
            sen, tag = [], []
            continue
        te,ta = data
        sen.append(te)
        tag.append(ta)
    if max_len is not None:
        return all_text[:max_len], all_tag[:max_len]
    return all_text,all_tag

class rule_find:
    def __init__(self):
        self.idx2type = ["关联类型", "反诈措施", "处理机构", "诈骗工具", "诈骗特征", "诈骗类型", "验证方法"]
        self.type2idx = {"关联类型": 0, "反诈措施": 1, "处理机构": 2, "诈骗工具": 3, "诈骗特征": 4, "诈骗类型": 5,
                         "验证方法": 6}
        self.ahos = [ahocorasick.Automaton() for _ in range(len(self.type2idx))]
        self.has_words = [False] * len(self.type2idx)  # 记录每个自动机是否添加了词汇

        for type in self.idx2type:
            idx = self.type2idx[type]
            try:
                with open(os.path.join('data', 'ent_aug', f'{type}.txt'), encoding='utf-8') as f:
                    all_en = f.read().split('\n')
                for en in all_en:
                    en = en.split(' ')[0].strip()  # 增加strip()处理空白
                    if len(en) >= 2:  # 只添加长度>=2的实体
                        self.ahos[idx].add_word(en, en)
                        self.has_words[idx] = True  # 标记已添加词汇
                # 只有添加了词汇的自动机才需要构建
                if self.has_words[idx]:
                    self.ahos[idx].make_automaton()
            except FileNotFoundError:
                print(f"警告：未找到{type}的实体文件，跳过该类型")
            except Exception as e:
                print(f"处理{type}时出错：{e}")

    def find(self, sen):
        rule_result = []
        mp = {}
        all_res = []
        all_ty = []
        for i in range(len(self.ahos)):
            # 只处理已成功构建的自动机
            if not self.has_words[i]:
                continue
            try:
                now = list(self.ahos[i].iter(sen))
                all_res.extend(now)
                for _ in range(len(now)):
                    all_ty.append(self.idx2type[i])
            except Exception as e:
                print(f"查询自动机{i}时出错：{e}")

        if len(all_res) != 0:
            all_res = sorted(all_res, key=lambda x: len(x[1]), reverse=True)
            for i, res in enumerate(all_res):
                be = res[0] - len(res[1]) + 1
                ed = res[0]
                if be in mp or ed in mp:
                    continue
                rule_result.append((be, ed, all_ty[i], res[1]))
                for t in range(be, ed + 1):
                    mp[t] = 1
        return rule_result


#找出tag(label)中的所有实体及其下表，为实体动态替换/随机掩码策略/实体动态拼接做准备
def find_entities(tag):
    result = []
    label_len = len(tag)
    i = 0
    while(i<label_len):
        if(tag[i][0]=='B'):
            type = tag[i].strip('B-')
            j=i+1
            while(j<label_len and tag[j][0]=='I'):
                j += 1
            result.append((i,j-1,type))
            i=j
        else:
            i = i + 1
    return result


class tfidf_alignment():
    def __init__(self):
        eneities_path = os.path.join('data', 'ent_aug')
        # 确保文件夹存在
        if not os.path.exists(eneities_path):
            raise FileNotFoundError(f"实体文件夹不存在: {eneities_path}")

        files = os.listdir(eneities_path)
        # 只保留txt文件，排除其他类型文件
        files = [docu for docu in files if docu.endswith('.txt') and '.py' not in docu]

        self.tag_2_embs = {}
        self.tag_2_tfidf_model = {}
        self.tag_2_entity = {}

        for ty in files:
            ty_path = os.path.join(eneities_path, ty)
            # 读取实体文件
            try:
                with open(ty_path, 'r', encoding='utf-8') as f:
                    entities = f.read().split('\n')
            except Exception as e:
                print(f"读取文件 {ty_path} 失败: {e}")
                continue

            valid_entities = []
            for ent in entities:
                ent = ent.strip()
                if not ent:
                    continue
                ent_name = ent.split(' ')[0]
                # 放宽长度限制（允许长度1-15的实体）
                if 1 <= len(ent_name) <= 15:
                    valid_entities.append(ent_name)

            if not valid_entities:
                print(f"警告：{ty_path} 中没有有效实体，已跳过该文件")
                continue

            # 构建TF-IDF模型
            ty_name = ty.strip('.txt')
            self.tag_2_entity[ty_name] = valid_entities
            tfidf_model = TfidfVectorizer(analyzer="char")
            try:
                embs = tfidf_model.fit_transform(valid_entities).toarray()
                self.tag_2_embs[ty_name] = embs
                self.tag_2_tfidf_model[ty_name] = tfidf_model
            except Exception as e:
                print(f"为 {ty_name} 构建TF-IDF模型失败: {e}")


    def align(self,ent_list):
        new_result = {}
        for s,e,cls,ent in ent_list:
            ent_emb = self.tag_2_tfidf_model[cls].transform([ent])
            sim_score = cosine_similarity(ent_emb, self.tag_2_embs[cls])
            max_idx = sim_score[0].argmax()
            max_score = sim_score[0][max_idx]

            if max_score >= 0.5:
                new_result[cls]= self.tag_2_entity[cls][max_idx]
        return new_result


class Entity_Extend:
    def __init__(self):
        eneities_path = os.path.join('data','ent_aug')
        files = os.listdir(eneities_path)
        files = [docu for docu in files if '.py' not in docu]

        self.type2entity = {}
        self.type2weight = {}
        for type in files:
            with open(os.path.join(eneities_path,type),'r',encoding='utf-8') as f:
                entities = f.read().split('\n')
                en_name = [ent for ent in entities if len(ent.split(' ')[0])<=15 and len(ent.split(' ')[0])>=1]
                en_weight = [1]*len(en_name)
                type = type.strip('.txt')
                self.type2entity[type] = en_name
                self.type2weight[type] = en_weight
    def no_work(self,te,tag,type):
        return te,tag

    # 1. 实体替换
    def entity_replace(self, te, ta, type):
        choice_ent = random.choices(self.type2entity[type], weights=self.type2weight[type], k=1)[0]
        ta = ["B-" + type] + ["I-" + type] * (len(choice_ent) - 1)  # 此处逻辑正确，若有修改需还原
        return list(choice_ent), ta

    # 2. 实体掩盖
    def entity_mask(self,te,ta,type):
        if(len(te)<=3):
            return te,ta
        elif(len(te)<=5):
            te.pop(random.randint(0,len(te)-1))
        else:
            te.pop(random.randint(0, len(te) - 1))
            te.pop(random.randint(0, len(te) - 1))
        ta = ["B-" + type] + ["I-" + type] * (len(te) - 1)
        return te,ta

    # 3. 实体拼接
    def entity_union(self,te,ta,type):
        words = ['和','与','以及']
        wor = random.choice(words)
        choice_ent = random.choices(self.type2entity[type],weights=self.type2weight[type],k=1)[0]
        te = te+list(wor)+list(choice_ent)
        ta = ta+['O']*len(wor)+["B-"+type] + ["I-"+type]*(len(choice_ent)-1)
        return te,ta
    def entities_extend(self,text,tag,ents):
        cho = [self.no_work,self.entity_union,self.entity_mask,self.entity_replace,self.no_work]
        new_text = text.copy()
        new_tag = tag.copy()
        sign = 0
        for ent in ents:
            p = random.choice(cho)
            te,ta = p(text[ent[0]:ent[1]+1],tag[ent[0]:ent[1]+1],ent[2])
            new_text[ent[0] + sign:ent[1] + 1 + sign], new_tag[ent[0] + sign:ent[1] + 1 + sign] = te,ta
            sign += len(te)-(ent[1]-ent[0]+1)

        return new_text, new_tag




class Nerdataset(Dataset):
    def __init__(self,all_text,all_label,tokenizer,max_len,tag2idx,is_dev=False,enhance_data=False):
        self.all_text = all_text
        self.all_label = all_label
        self.tokenizer = tokenizer
        self.max_len= max_len
        self.tag2idx = tag2idx
        self.is_dev = is_dev
        self.entity_extend = Entity_Extend()
        self.enhance_data = enhance_data
    def __getitem__(self, x):
        text, label = self.all_text[x], self.all_label[x]
        if self.is_dev:
            max_len = min(len(self.all_text[x])+2,500)
        else:
            # 几种策略
            if self.enhance_data and e>=7 and e%2==1:
                ents = find_entities(label)
                text,label = self.entity_extend.entities_extend(text,label,ents)
            max_len = self.max_len
        text, label =text[:max_len - 2], label[:max_len - 2]

        x_len = len(text)
        assert len(text)==len(label)
        text_idx = self.tokenizer.encode(text,add_special_token=True)
        label_idx = [self.tag2idx['<PAD>']] + [self.tag2idx[i] for i in label] + [self.tag2idx['<PAD>']]

        text_idx +=[0]*(max_len-len(text_idx))
        label_idx +=[self.tag2idx['<PAD>']]*(max_len-len(label_idx))
        return torch.tensor(text_idx),torch.tensor(label_idx),x_len
    def __len__(self):
        return len(self.all_text)




def build_tag2idx(all_tag):
    tag2idx = {'<PAD>': 0}  # 初始包含填充标签
    for sen in all_tag:
        for tag in sen:
            if tag not in tag2idx:  # 确保所有标签都被添加
                tag2idx[tag] = len(tag2idx)

    return tag2idx




class Bert_Model(nn.Module):
    def __init__(self,model_name,hidden_size,tag_num,bi):
        super().__init__()
        self.bert = BertModel.from_pretrained(model_name)
        self.gru = nn.RNN(input_size=768,hidden_size=hidden_size,num_layers=2,batch_first=True,bidirectional=bi)
        if bi:
            self.classifier = nn.Linear(hidden_size*2,tag_num)
        else:
            self.classifier = nn.Linear(hidden_size, tag_num)
        self.loss_fn = nn.CrossEntropyLoss(ignore_index=0)
    def forward(self,x,label=None):
        bert_0,_ = self.bert(x,attention_mask=(x>0),return_dict=False)
        gru_0,_ = self.gru(bert_0)
        pre = self.classifier(gru_0)
        if label is not None:
            loss = self.loss_fn(pre.reshape(-1,pre.shape[-1]),label.reshape(-1))
            return loss
        else:
            return torch.argmax(pre,dim=-1).squeeze(0)

def merge(model_result_word,rule_result):
    result = model_result_word+rule_result
    result = sorted(result,key=lambda x:len(x[-1]),reverse=True)
    check_result = []
    mp = {}
    for res in result:
        if res[0] in mp or res[1] in mp:
            continue
        check_result.append(res)
        for i in range(res[0],res[1]+1):
            mp[i] = 1
    return check_result

def get_ner_result(model,tokenizer,sen,rule,tfidf_r,device,idx2tag):
    sen_to = tokenizer.encode(sen, add_special_tokens=True, return_tensors='pt').to(device)

    pre = model(sen_to).tolist()

    pre_tag = [idx2tag[i] for i in pre[1:-1]]
    model_result = find_entities(pre_tag)
    model_result_word = []
    for res in model_result:
        word = sen[res[0]:res[1] + 1]
        model_result_word.append((res[0], res[1], res[2], word))
    rule_result = rule.find(sen)

    merge_result = merge(model_result_word, rule_result)
    tfidf_result = tfidf_r.align(merge_result)
    return tfidf_result

if __name__ == "__main__":
    all_text,all_label = get_data(os.path.join('data','ner_data_aug.txt'))
    train_text, dev_text, train_label, dev_label = train_test_split(all_text, all_label, test_size = 0.02, random_state = 42)

    if os.path.exists('tmp_data/tag2idx.npy'):
        with open('tmp_data/tag2idx.npy','rb') as f:
            tag2idx = pickle.load(f)
    else:
        tag2idx = build_tag2idx(all_label)
        with open('tmp_data/tag2idx.npy','wb') as f:
            pickle.dump(tag2idx,f)


    idx2tag = list(tag2idx)

    max_len = 50
    epoch = 30
    batch_size = 60
    hidden_size = 128
    bi = True
    model_name="hfl/chinese-roberta-wwm-ext"#bert_base_chinese
    tokenizer = BertTokenizer.from_pretrained(model_name)
    lr =1e-5
    is_train=True

    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')

    train_dataset = Nerdataset(train_text,train_label,tokenizer,max_len,tag2idx,enhance_data=True)
    train_dataloader = DataLoader(train_dataset,batch_size=batch_size,shuffle=True)

    dev_dataset = Nerdataset(dev_text, dev_label, tokenizer, max_len, tag2idx,is_dev=True)
    dev_dataloader = DataLoader(dev_dataset, batch_size=1, shuffle=False)

    model = Bert_Model(model_name,hidden_size,len(tag2idx),bi)
    # if os.path.exists(f'model/best_roberta_gru_model_ent_aug.pt'):
    #     model.load_state_dict(torch.load('model/best_roberta_gru_model_ent_aug.pt'))
    model = model.to(device)
    opt = torch.optim.Adam(model.parameters(),lr = lr)
    bestf1 = -1
    if is_train:
        for e in range(epoch):
            loss_sum = 0
            ba = 0
            for x,y,batch_len in tqdm(train_dataloader):
                x = x.to(device)
                y = y.to(device)
                opt.zero_grad()
                loss = model(x,y)
                loss.backward()

                opt.step()
                loss_sum+=loss
                ba += 1
            all_pre = []
            all_label = []
            for x,y,batch_len in tqdm(dev_dataloader):
                assert len(x)==len(y)
                x = x.to(device)
                pre = model(x)
                pre = [idx2tag[i] for i in pre[1:batch_len+1]]
                all_pre.append(pre)

                label = [idx2tag[i] for i in y[0][1:batch_len+1]]
                all_label.append(label)
            f1 = f1_score(all_pre, all_label)
            if f1>bestf1:
                bestf1 = f1
                print(f'e={e},loss={loss_sum / ba:.5f} f1={f1:.5f} ---------------------->best')
                torch.save(model.state_dict(),f'model/{cache_model}.pt')
            else:print(f'e={e},loss={loss_sum/ba:.5f} f1={f1:.5f}')

    rule = rule_find()
    tfidf_r = tfidf_alignment()

    while(True):
        sen = input('请输入:')
        print(get_ner_result(model, tokenizer, sen, rule, tfidf_r,device,idx2tag))
