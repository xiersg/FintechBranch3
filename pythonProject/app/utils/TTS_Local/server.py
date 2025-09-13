import torch
import os
import uuid
import uvicorn
import io 
import sys 
import soundfile as sf 
from fastapi import FastAPI, UploadFile, File, Form, HTTPException
from fastapi.responses import StreamingResponse 
from TTS.api import TTS
from TTS.tts.configs.xtts_config import XttsConfig
# python server.py local
"""
Coqui XTTS v2 语音合成 FastAPI 服务
功能:
1.API 服务器模式 (默认):
-启动一个 FastAPI Web 服务，监听在 http://127.0.0.1:8000。
-提供一个 /tts_stream/ API 端点，接收 POST 请求。
-请求参数包括:
-text: 要转换为语音的文本。
-speaker_wav: 用于声音克隆的参考 WAV 音频文件 (通过文件上传)。
-language: 目标语言代码 (例如 'zh-cn', 'en')。
-在内存中实时生成语音，并通过流式响应 (StreamingResponse) 直接返回 WAV 音频数据。
-这种方式效率高，不会在服务器上留下生成的音频文件，适合前端直接调用播放。
2.本地模式 (local):
-当通过命令行 `python api_server.py local` 运行时触发。
-不会启动网络服务器。
-执行一次性的语音合成任务，使用脚本内预设的文本和参考音频路径。
-将生成的语音文件直接保存到 `output_audio` 目录下。
-主要用于快速的功能验证和离线调试。
使用前置条件:
-必须先通过 Coqui TTS 下载 XTTS v2 模型文件到 `MD/` 目录下。
-transformers库版本建议锁定在4.36.2以避免兼容性问题。
"""
device = "cuda" if torch.cuda.is_available() else "cpu"
MODEL_DIR = "MD/"
OUTPUT_DIR = "output_audio/"
os.makedirs(OUTPUT_DIR, exist_ok=True)
config_path = os.path.join(MODEL_DIR, "config.json")
if not os.path.exists(config_path):
    print(f"错误：配置文件未在 {config_path} 找到。")
    exit()
torch.serialization.add_safe_globals([XttsConfig])
print("正在加载 XTTS v2 模型 (这可能需要一些时间)...")
tts = TTS(
    model_path=MODEL_DIR,
    config_path=config_path,
    gpu=(device == "cuda")
).to(device)
print("模型加载完成。")

app = FastAPI(
    title="Coqui TTS API",
    description="一个用于 XTTS v2 模型语音合成的 FastAPI 服务",
    version="1.0",
)
@app.post("/tts_stream/", tags=["Text-to-Speech"])
async def tts_stream_endpoint(
    text: str = Form(..., description="要转换为语音的文本。"),
    speaker_wav: UploadFile = File(..., description="用于克隆声音的参考 WAV 音频文件。"),
    language: str = Form("zh-cn", description="目标语言的语言代码 (例如: 'en', 'zh-cn')。")
):
    try:
        temp_speaker_path = os.path.join(OUTPUT_DIR, f"temp_{uuid.uuid4()}.wav")
        with open(temp_speaker_path, "wb") as f:
            content = await speaker_wav.read()
            f.write(content)
        print(f"正在合成文本: '{text}'")
        wav = tts.tts(
            text=text,
            speaker_wav=temp_speaker_path,
            language=language
        )
        os.remove(temp_speaker_path)
        buffer = io.BytesIO()
        sf.write(buffer, wav, tts.synthesizer.output_sample_rate, format='WAV')
        buffer.seek(0)
        print("合成完成，正在返回音频流。")

        #使用 StreamingResponse 直接返回内存中的音频数据流
        #前端可以直接播放这个响应
        return StreamingResponse(buffer, media_type="audio/wav")

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/", tags=["General"])
def read_root():
    return {"message": "欢迎使用 Coqui TTS FastAPI 服务。请访问 /docs 查看 API 文档。"}

def run_local_mode():
    print("--- 正在以本地模式运行 ---")
    test_text = "这是一个在本地直接生成的测试语音"
    reference_audio_path = "./reference_audio/sample.wav" 
    output_file_path = os.path.join(OUTPUT_DIR, "local_output.wav")
    if not os.path.exists(reference_audio_path):
        print(f"错误：未找到参考音频文件 '{reference_audio_path}'")
        print("请在 reference_audio 文件夹下放置一个 sample.wav 文件。")
        return
    print(f"使用参考音频: {reference_audio_path}")
    print(f"合成文本: {test_text}")
    tts.tts_to_file(
        text=test_text,
        speaker_wav=reference_audio_path,
        language="zh-cn",
        file_path=output_file_path,
    )
    
    print(f"--- 操作完成，文件已保存至: {output_file_path} ---")

if __name__ == "__main__":
    if "local" in sys.argv:
        run_local_mode()
    else:
        print("--- 正在启动 FastAPI 服务器 ---")
        uvicorn.run("server:app", host="127.0.0.1", port=8000, reload=True)