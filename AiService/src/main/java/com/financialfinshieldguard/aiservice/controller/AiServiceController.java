package com.financialfinshieldguard.aiservice.controller;


import com.financialfinishieldguard.data.aiService.analyseAudio.AnalyseAudioVO;
import com.financialfinishieldguard.data.aiService.getCurrentUserDialogues.GetCurrentUserDialoguesVO;
import com.financialfinishieldguard.data.common.Result;
import com.financialfinshieldguard.aiservice.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/ai")
public class AiServiceController {

    @Autowired
    private AiService aiService;

    /**
     * 获取用户当前对话信息
     * @return
     */
    @GetMapping("/dialogues")
    public Result<GetCurrentUserDialoguesVO> getCurrentUserDialogues() {

        GetCurrentUserDialoguesVO response = aiService.getCurrentUserDialogues();

        return Result.OK(response);
    }

    /**
     * 获取当前选择的对话的记录
     * @param sessionId
     * @return
     */
    @GetMapping("/dialogue")
    public Result<String> getDialogue(Long sessionId) {

        String resposne = aiService.getDialogue(sessionId);

        return Result.OK(resposne);
    }

    /**
     * 传音频文件，判断AI率
     * @param file
     * @return
     */
    @PostMapping("/audio")
    public Result<AnalyseAudioVO> analyseAudio(MultipartFile file) {

        AnalyseAudioVO response = aiService.analyseAudio(file);

        return Result.OK(response);
    }
}
