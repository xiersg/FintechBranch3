package com.financialfinshieldguard.aiservice.service;

import com.financialfinishieldguard.data.aiService.analyseAudio.AnalyseAudioVO;
import com.financialfinishieldguard.data.aiService.analyseImage.AnalyseImageVO;
import com.financialfinishieldguard.data.aiService.getCurrentUserDialogues.GetCurrentUserDialoguesVO;
import org.springframework.web.multipart.MultipartFile;

public interface AiService {

    /**
     * 获取用户当前对话信息
     * @return
     */
    GetCurrentUserDialoguesVO getCurrentUserDialogues();

    /**
     * 获取当前选择的对话的记录
     * @param sessionId
     * @return
     */
    String getDialogue(Long sessionId);

    /**
     * 传音频文件，判断AI率
     * @param file
     * @return
     */
    AnalyseAudioVO analyseAudio(MultipartFile file);

    /**
     * 传图片文件，分析诈骗情况
     * @param image
     * @return
     */
    AnalyseImageVO analyseImage(MultipartFile image);
}
