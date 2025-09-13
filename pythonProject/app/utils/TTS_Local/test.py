import torch
import os
from TTS.api import TTS
# 获取设备
device = "cuda" if torch.cuda.is_available() else "cpu"
from TTS.tts.configs.xtts_config import XttsConfig
# 将 XttsConfig 类添加到安全全局变量列表中
torch.serialization.add_safe_globals([XttsConfig])
model_path = "MD" 
config_path = model_path + "/config.json"
# 初始化TTS，模型会下载到上面指定的目录
tts = TTS(
    model_name="tts_models/multilingual/multi-dataset/xtts_v2",
    config_path=config_path,
    model_path=model_path,
    gpu=(device == "cuda")
    ).to(device)
print(tts.speakers)
# 文本转语音并保存到D盘指定路径
tts.tts_to_file(
    text="Hello world! 这是一个测试语音。",
    # speaker_wav="./",  # 替换为你的参考音频路径
    language="zh-cn",
    file_path="./output.wav"  # 直接指定D盘输出路径
)
