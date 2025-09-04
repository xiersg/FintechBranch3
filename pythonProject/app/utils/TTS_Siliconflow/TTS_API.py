import requests
import os
import dotenv
dotenv.load_dotenv()
token=os.getenv("SILICONFLOW_API_KEY")
"""
SiliconFlow TTS API 调用工具模块(🐱)
功能说明：
- 封装SiliconFlow平台的文本转语音(TTS)API调用逻辑，实现文本到MP3音频的转换
- 核心函数TTS_main接收API密钥和待转换文本，返回包含音频二进制数据的响应对象
- 支持通过环境变量SILICONFLOW_API_KEY配置默认密钥，也可在调用时动态传入密钥
- 默认使用模型"FunAudioLLM/CosyVoice2-0.5B"及"david"语音，输出44100Hz采样率的MP3格式音频，语速1.2倍，可以自己调一下
- 采用requests库发送POST请求，Content-Type为application/json
- 主程序入口提供简单测试功能，可直接输入文本生成并保存音频文件到output_audio.mp3
- 使用前需确保已安装相关依赖(pip install requests python-dotenv)
"""
def TTS_main(inner_token,content):
        url = "https://api.siliconflow.cn/v1/audio/speech"
        payload = {
            "model": "FunAudioLLM/CosyVoice2-0.5B",
            "voice":"FunAudioLLM/CosyVoice2-0.5B:david",
            "response_format": "mp3",
            "sample_rate": 44100,
            "speed": 1.2,
            "input": f"{content}"
        }
        headers = { 
            "Authorization": f"Bearer {inner_token}",
            "Content-Type": "application/json"
        }
        return requests.post(url, json=payload, headers=headers)

if __name__ == "__main__":
    user_input = input("输入测试音频文本,不输入直接按回车:").strip()
    if not user_input:
        content = "八百标兵奔北波，砰砰砰砰砰砰砰"
    else:
        content = user_input

    response = TTS_main(token, content)  # 主调用，先输入ApiKEY后输入调用文本,这个responce是一个二进制流的MP3文件

    if response.status_code == 200:
        print("Audio generated successfully")
        try:  
            save_path = "output_audio.mp3"
            with open(save_path, "wb") as f:
                f.write(response.content) #保存音频示例
            print("Audio saved to " + save_path)
        except Exception as e:
            print("Error saving audio:", e)
            

