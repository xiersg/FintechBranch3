# anti_spoof/inference.py
import torch, torchaudio, yaml, numpy as np
from pathlib import Path
from typing import Optional, Tuple
from .models.AASIST import Model
import soundfile as sf

class AASISTDetector:
    def __init__(self,
                 conf_path: str = "config/AASIST.conf",
                 weight_path: Optional[str] = None,
                 use_cuda: bool = True,
                 min_duration_sec: float = 2.0,
                 vad: bool = True):
        self.device = "cuda" if (use_cuda and torch.cuda.is_available()) else "cpu"
        with open(conf_path, "r", encoding="utf-8") as f:
            cfg = yaml.safe_load(f)  # AASIST.conf 是 JSON 也可用 safe_load 解析
        self.cfg = cfg
        self.model = Model(cfg["model_config"]).to(self.device).eval()
        ckpt = weight_path or cfg["inference"]["pretrained_ckpt"]
        sd = torch.load(ckpt, map_location=self.device)
        sd = sd["state_dict"] if isinstance(sd, dict) and "state_dict" in sd else sd
        self.model.load_state_dict(sd, strict=False)

        self.min_dur = min_duration_sec
        self.enable_vad = vad
        try:
            import webrtcvad  # 可选
            self._vad = webrtcvad.Vad(2)
        except Exception:
            self._vad = None
            self.enable_vad = False

    @staticmethod
    def _to_mono16k(wav: torch.Tensor, sr: int) -> torch.Tensor:
        if wav.dim() == 2 and wav.size(0) > 1:
            wav = wav[:1, :]
        if sr != 16000:
            wav = torchaudio.functional.resample(wav, sr, 16000)
        return wav.squeeze(0)  # [T]

    def _speech_ratio(self, x_16k: np.ndarray) -> float:
        if not self.enable_vad or self._vad is None:
            return 1.0
        import struct
        sr = 16000
        frame = int(0.02 * sr)  # 20ms
        pcm = (np.clip(x_16k, -1, 1) * 32768).astype(np.int16).tobytes()
        n = len(pcm) // (2 * frame)
        speech = 0
        for i in range(n):
            chunk = pcm[i*2*frame:(i+1)*2*frame]
            if self._vad.is_speech(chunk, sr): speech += 1
        return speech / max(n, 1)

    @torch.no_grad()
    def score_wav(self, path: str) -> Tuple[float, dict]:

        # 读取音频
        data, sr = sf.read(path, dtype="float32", always_2d=False)  # sr为采样率
        if data.ndim == 2:
            data = data.mean(axis=1)
        wav = torch.from_numpy(np.asarray(data, dtype=np.float32))[None, :]  # [1, T]

        # 重采样 + 预处理
        x = self._to_mono16k(wav, sr)       # [T]  转化为16k单声道
        dur = x.numel() / 16000.0           # 计算时长 = 样本点数/采样率

        # 计算语音占比
        speech_ratio = self._speech_ratio(x.cpu().numpy()) if self.enable_vad else 1.0
        meta = {"duration": dur, "speech_ratio": speech_ratio}

        # 过滤掉无效样本  时间过短或者人声占比太小都会直接过滤
        if dur < self.min_dur or speech_ratio < 0.3:
            return float("nan"), {**meta, "reason": "时间过短或者人声占比太小"}

        # 符合条件，开始调用模型处理
        # AASIST 期待 [B, T]
        X = x.unsqueeze(0).to(self.device)
        _, logits = self.model(X)
        prob_spoof = torch.softmax(logits, dim=1)[0, 1].item()
        return prob_spoof, meta

    @torch.no_grad()
    def score_tensor(self, x: torch.Tensor, sr: int) -> float:
        x = self._to_mono16k(x, sr)           # [T]
        X = x.unsqueeze(0).to(self.device)
        _, logits = self.model(X)
        return torch.softmax(logits, dim=1)[0, 1].item()
