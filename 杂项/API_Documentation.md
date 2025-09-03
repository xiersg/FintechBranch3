# AI Service API 接口文档（WS + POST）
> 版本 v0.3 | 本文面向：AI 组、前端、后端

本项目约定：**LLM 对话使用 WebSocket（流式）**；**其它能力走 HTTP POST**。JWT 由网关调用 Spring Boot 接口校验。

---

## 一、鉴权与网关流程
- **POST 接口**：必须在 Header 中携带 `Authorization: Bearer <JWT>`；网关会转发至 `http://192.168.2.30:8080/api/auth/validate` 验证（返回 200 且 `valid=true` 才放行）。
- **WebSocket 对话**：握手 URL `ws://<host>:8000/ws?token=<JWT>`。服务端在握手阶段调用同一鉴权接口校验，失败直接关闭（1008），鉴权服务异常（1011）。

> 建议：后端记录 `request_id`、用户名（从鉴权返回的 `username`）、响应时间与错误码，便于审计和告警。

---

## 二、对话（WebSocket 流式）
**连接**：`ws://<host>:8000/ws?token=<JWT>`

**消息格式（客户端 → 服务端）**
```json
{
  "action": "chat.create",
  "request_id": "uuid-optional",
  "payload": {
    "messages": [
      {"role": "system", "content": "你是客服助手"},
      {"role": "user", "content": "你好"}
    ],
    "model": "default",
    "temperature": 0.7,
    "max_tokens": 512
  }
}
```

**服务端推送（服务端 → 客户端）**
- `ack`：受理确认  
- `delta`：增量片段（逐 token/句）  
- `result`：完成（可包含 `finish_reason`、`usage`）  
- `error`：错误（与 `request_id` 对应）

**示例**
```json
{"type":"ack","request_id":"req-1"}
{"type":"delta","request_id":"req-1","data":{"index":0,"delta":"您"}}
{"type":"delta","request_id":"req-1","data":{"index":0,"delta":"好！"}}
{"type":"result","request_id":"req-1","data":{"finish_reason":"stop","usage":{"prompt_tokens":15,"completion_tokens":8,"total_tokens":23}}}
```

**前端最小示例（浏览器）**
```html
<script>
  const ws = new WebSocket("ws://localhost:8000/ws?token=<JWT>");
  ws.onopen = () => {
    ws.send(JSON.stringify({
      action:"chat.create",
      request_id:"req-1",
      payload:{messages:[{role:"user",content:"你好"}]}
    }));
  };
  ws.onmessage = (e) => console.log("server:", e.data);
</script>
```

---

## 三、结构化交易反诈 SFD（POST）

### 1) `POST /fraud/detect` — 单笔交易欺诈检测
**用途**：对单笔交易进行特征抽取与打分，判断是否可疑。  
**请求体（必填）**
```json
{
  "step": 1,
  "type": "CASH_OUT",
  "amount": 1000.0,
  "nameOrig": "C12345",
  "oldbalanceOrg": 5000.0,
  "newbalanceOrig": 4000.0,
  "nameDest": "M98765",
  "oldbalanceDest": 10000.0,
  "newbalanceDest": 11000.0
}
```
- `type` 仅支持 `CASH_OUT` / `TRANSFER`。  
- `old/new balance` 字段用于特征工程（余额变化、收付款画像等）。

**响应 200**
```json
{
  "status": "success",
  "is_fraud": false,
  "fraud_score": 0.37,
  "message": "欺诈检测完成"
}
```

**错误码**：`400`（缺字段）、`401`（JWT 失败）、`503`（鉴权服务不可用）、`500`（内部错误）

---

### 2) `POST /fraud/train` — 模型训练/增量训练
**用途**：触发训练任务（离线/在线皆可），完成后替换当前模型。  
**请求体**
```json
{ "incremental": false }
```
- `incremental=true` 表示增量训练（基于已有模型追加数据）。

**响应 200**
```json
{ "status": "success", "message": "模型训练完成" }
```

**错误码**：`401`、`503`、`500`

---

### 3) `GET /fraud/status` — 模型状态查询
**用途**：查询当前线上模型是否加载、阈值和特征数量等。  
**响应 200**
```json
{
  "status": "success",
  "model_loaded": true,
  "threshold": 0.65,
  "feature_count": 13
}
```

**错误码**：`401`、`503`、`500`

---

## 四、内容风险识别 IPW（POST）

### 1) `POST /content/detect` — 文本/图片URL/网页HTML 检测
**用途**：对文本、图片 URL 或 HTML 内容进行风险识别与打分。  
**请求体**
```json
{
  "content": "文本 或 图片URL 或 <html>...</html>",
  "content_type": "text",   // text | image | html
  "is_url": true,           // 当 content_type=image 时：true=URL；false=走 /content/image
  "print_yn": false
}
```
**响应 200**
```json
{
  "success": true,
  "is_fraudulent": false,
  "risk_score": 0.21,
  "content_type": "text",
  "content_preview": "一段文本"
}
```

**错误码**：`400`、`401`、`503`、`500`

---

### 2) `POST /content/image` — 表单直传图片
**用途**：当 `content_type=image` 且需要上传本地图片文件时使用。  
**请求头**：`Content-Type: multipart/form-data`  
**表单字段**：`image`（文件）  
**响应 200**
```json
{
  "success": true,
  "is_fraudulent": false,
  "risk_score": 0.12,
  "content_type": "image"
}
```

**错误码**：`400`、`401`、`503`、`500`

---

## 五、统一错误结构（建议）
```json
{
  "code": "AUTH_INVALID | BAD_REQUEST | INTERNAL_ERROR | UPSTREAM_UNAVAILABLE",
  "message": "错误描述",
  "request_id": "可选"
}
```

---

## 六、测试样例

**对话（WS）**
```js
const ws = new WebSocket(`ws://localhost:8000/ws?token=${encodeURIComponent('<JWT>')}`);
ws.onopen = () => ws.send(JSON.stringify({
  action:"chat.create",
  request_id:"req-1",
  payload:{messages:[{role:"user",content:"你好"}]}
}));
ws.onmessage = e => console.log(JSON.parse(e.data));
```

**交易检测（POST）**
```bash
curl -X POST http://localhost:8000/fraud/detect  -H "Authorization: Bearer <JWT>" -H "Content-Type: application/json"  -d '{"step":1,"type":"CASH_OUT","amount":1000,"nameOrig":"C1","oldbalanceOrg":5000,"newbalanceOrig":4000,"nameDest":"M1","oldbalanceDest":10000,"newbalanceDest":11000}'
```

**文本检测（POST）**
```bash
curl -X POST http://localhost:8000/content/detect  -H "Authorization: Bearer <JWT>" -H "Content-Type: application/json"  -d '{"content":"这是测试文本","content_type":"text"}'
```

**图片直传（POST）**
```bash
curl -X POST http://localhost:8000/content/image  -H "Authorization: Bearer <JWT>"  -F "image=@/path/to/local.jpg"
```

---

## 七、OpenAPI 文件
已在同目录提供 `openapi.yaml`，支持 Swagger UI / Postman / Hoppscotch 导入；前端/测试可据此生成 SDK、Mock、校验契约。

## 八、后续建议
- WebSocket 的消息规范可进一步用 AsyncAPI 描述；
- 训练任务建议异步化并增加进度查询；
- 对异常增加 trace-id；
- 敏感日志脱敏；
- 加上限流与用量审计。
