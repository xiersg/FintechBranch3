package com.financialfinshieldguard.aiservice.service;

import com.financialfinishieldguard.data.aiService.analyseAudio.AnalyseAudioVO;
import com.financialfinishieldguard.data.aiService.analyseImage.AnalyseImageVO;
import com.financialfinishieldguard.data.aiService.analyseImageText.AnalyseImageTextVO;
import com.financialfinishieldguard.data.aiService.getCurrentUserDialogues.GetCurrentUserDialoguesVO;
import com.financialfinishieldguard.data.aiService.module1Detect.Module1DetectDTO;
import com.financialfinishieldguard.data.aiService.module1Detect.Module1DetectVO;
import com.financialfinishieldguard.data.aiService.module2Detect.Module2DetectDTO;
import com.financialfinishieldguard.data.aiService.module2Detect.Module2DetectVO;
import com.financialfinishieldguard.data.aiService.newDialogue.NewDialogueDTO;
import com.financialfinishieldguard.data.sessionService.HumanCustomerInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
     * 传图片文件，分析诈骗情况
     * @param image
     * @return
     */
    AnalyseImageVO analyseImage(MultipartFile image);

    /**
     * 传图片文件，识别图片中的文字
     * @param image
     * @return
     */
    AnalyseImageTextVO analyseImageText(MultipartFile image);

    /**
     * 生成一个新对话
     * @param dialogueDTO
     */
    String newDialogue(NewDialogueDTO dialogueDTO);

    /**
     * 交易数据欺诈系数判定
     * @param request
     * @return
     */
    Module1DetectVO detect1(Module1DetectDTO request);

    /**
     * 对文本或图片url进行风险判定
     * @param request
     * @return
     */
    Module2DetectVO detect2(Module2DetectDTO request);

    /**
     * 获取客服的userIds
     * @return
     */
    List<HumanCustomerInfo> getHumanCustomerUserIds();

    AnalyseAudioVO analyseAudio(MultipartFile file);


}
