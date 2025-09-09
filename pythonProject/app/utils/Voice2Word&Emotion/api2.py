
"""
语音识别API服务
功能说明：基于FastAPI和SenseVoiceSmall模型提供语音转文字(ASR)服务，支持多语言识别、情绪识别和背景音识别
音频要求：支持wav/mp3格式，建议16KHz采样率（服务会自动重采样至16KHz）
部署说明：
1. 依赖环境：Python、FastAPI、uvicorn、torchaudio、SenseVoiceSmall模型
2. 模型路径：默认使用"SenseVoiceSmall"
3. 设备配置：通过环境变量SENSEVOICE_DEVICE指定运行设备（默认cuda:0，CPU可设为cpu）
4. 启动服务：直接运行本文件，服务将在127.0.0.2:5000端口启动
接口文档：启动服务后访问 http://127.0.0.2:5000/docs 查看详细接口说明
返回结果包含：raw_text(原始识别文本)、clean_text(去除标记的文本)、text(后处理文本)、emotions(情绪列表)、background_sounds(背景音列表)
使用提示：
- 可同时上传多个音频文件，通过keys参数指定每个文件的名称（逗hao分隔）
- 建议明确指定语言参数(lang)以提高识别准确率，未知语言时使用auto
"""
import os, re
from fastapi import FastAPI, File, Form, UploadFile
from fastapi.responses import HTMLResponse
from typing_extensions import Annotated
from typing import List
from enum import Enum
import torchaudio
from model import SenseVoiceSmall
from funasr.utils.postprocess_utils import rich_transcription_postprocess
from io import BytesIO
TARGET_FS = 16000

# 情绪识别字典
EMOTION_DICT ={
	"<|HAPPY|>": "Happy",
	"<|SAD|>": "Sad",
	"<|ANGRY|>": "Angry",
	"<|NEUTRAL|>": "中性",
	"<|FEARFUL|>": "Fearful",
	"<|DISGUSTED|>": "Disgusted",
	"<|SURPRISED|>": "Surprised",
}

# 背景声识别字典
SOUND_DICT = {
	"<|BGM|>": "BGM",
	"<|Speech|>": "",
	"<|Applause|>": "Applause",
	"<|Laughter|>": "Laughter",
	"<|Cry|>": "Cry",
	"<|Sneeze|>": "Sneeze",
	"<|Breath|>": "Breath",
	"<|Cough|>": "Cough",
}


class Language(str, Enum):
    auto = "auto"
    zh = "zh"
    en = "en"
    # yue = "yue"
    # ja = "ja"
    # ko = "ko"
    nospeech = "nospeech"

model_dir = "SenseVoiceSmall"
m, kwargs = SenseVoiceSmall.from_pretrained(model=model_dir, device=os.getenv("SENSEVOICE_DEVICE", "cuda:0"))# 默认cuda:0，CPU可设为cpu
m.eval()
regex = r"<\|.*\|>"
app = FastAPI()
@app.get("/", response_class=HTMLResponse)
async def root():
    return """
    <!DOCTYPE html>
    <html>
        <head>
            <meta charset=utf-8>
            <title>Api information</title>
        </head>
        <body>
            <a href='./docs'>Documents of API</a>
        </body>
    </html>
    """

@app.post("/api/v1/asr")
async def turn_audio_to_text(
    files: Annotated[List[UploadFile], File(description="wav or mp3 audios in 16KHz")],
    keys: Annotated[str, Form(description="name of each audio joined with comma")] = None,
    lang: Annotated[Language, Form(description="language of audio content")] = "auto",
):
    audios = []
    for file in files:
        file_io = BytesIO(await file.read())
        data_or_path_or_list, audio_fs = torchaudio.load(file_io)
        if audio_fs != TARGET_FS:
            resampler = torchaudio.transforms.Resample(orig_freq=audio_fs, new_freq=TARGET_FS)
            data_or_path_or_list = resampler(data_or_path_or_list)

        data_or_path_or_list = data_or_path_or_list.mean(0)
        audios.append(data_or_path_or_list)

    if lang == "":
        lang = "auto"

    if not keys:
        key = [f.filename for f in files]
    else:
        key = keys.split(",")

    res = m.inference(
        data_in=audios,
        language=lang,  # "zh", "en", "nospeech"
        use_itn=False,
        ban_emo_unk=False,
        key=key,
        fs=TARGET_FS,
        **kwargs,
    )
    if len(res) == 0:
        return {"result": []}

    for it in res[0]:
        raw_text = it["text"]
        it["raw_text"] = raw_text
        it["clean_text"] = re.sub(regex, "", raw_text, 0, re.MULTILINE)
        it["text"] = rich_transcription_postprocess(raw_text)
        tags = re.findall(r"<\|.*?\|>", raw_text)
        emotions = []
        background_sounds = []
        for tag in tags:
            if tag in EMOTION_DICT:
                emotions.append(EMOTION_DICT[tag])
            if tag in SOUND_DICT:
                background_sounds.append(SOUND_DICT[tag])
        it["emotions"] = emotions
        it["background_sounds"] = background_sounds
    return {"result": res[0]}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="127.0.0.2", port=5000)