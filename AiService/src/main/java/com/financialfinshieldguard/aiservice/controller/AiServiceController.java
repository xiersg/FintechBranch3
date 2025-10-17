package com.financialfinshieldguard.aiservice.controller;

import com.financialfinishieldguard.data.aiService.analyseAudio.AnalyseAudioVO;
import com.financialfinishieldguard.data.aiService.analyseImage.AnalyseImageVO;
import com.financialfinishieldguard.data.aiService.getCurrentUserDialogues.GetCurrentUserDialoguesVO;
import com.financialfinishieldguard.data.aiService.module1Detect.Module1DetectDTO;
import com.financialfinishieldguard.data.aiService.module1Detect.Module1DetectVO;
import com.financialfinishieldguard.data.aiService.module2Detect.Module2DetectDTO;
import com.financialfinishieldguard.data.aiService.module2Detect.Module2DetectVO;
import com.financialfinishieldguard.data.aiService.newDialogue.NewDialogueDTO;
import com.financialfinishieldguard.data.common.Result;
import com.financialfinshieldguard.aiservice.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/ai")
public class AiServiceController {

    @Autowired
    private AiService aiService;

    /**
     * 获取用户当前对话信息
     *
     * @return
     */
    @GetMapping("/dialogues")
    public Result<GetCurrentUserDialoguesVO> getCurrentUserDialogues() {

        GetCurrentUserDialoguesVO response = aiService.getCurrentUserDialogues();

        return Result.OK(response);
    }

    /**
     * 获取当前选择的对话的记录
     *
     * @param sessionId
     * @return
     */
    @GetMapping("/dialogue")
    public Result<String> getDialogue(Long sessionId) {

        String response = aiService.getDialogue(sessionId);

        return Result.OK(response);
    }

//    /**
//     * 传音频文件，判断AI率(AI率结果由websocket响应)  前端是websocket传的，这个接口不用了
//     *
//     * @param file
//     * @return
//     */
//    @PostMapping("/audio")
//    public Result<AnalyseAudioVO> analyseAudio(MultipartFile file) {
//
//        aiService.analyseAudio(file);
//
//        return Result.OK();
//    }

    /**
     * 传图片文件，分析诈骗情况(http响应)
     *
     * @param image
     * @return
     */
    @PostMapping("/image")
    public Result<AnalyseImageVO> analyseImage(MultipartFile image) {

        AnalyseImageVO response = aiService.analyseImage(image);

        return Result.OK(response);
    }

    /**
     * 生成一个新对话
     *
     * @return
     */
    @PostMapping("/newDialogue")
    public Result<String> newDialogue(NewDialogueDTO dialogueDTO) {

        String response = aiService.newDialogue(dialogueDTO);

        return Result.OK(response);
    }

    /**
     * 交易数据欺诈系数判定
     * @param request
     * @return
     */
    @PostMapping("/detect1")
    public Result<Module1DetectVO> detect1(@RequestBody Module1DetectDTO request) {
        Module1DetectVO response = aiService.detect1(request);

        return Result.OK(response);
    }


    /**
     * 交易数据欺诈系数判定
     * @param request
     * @return
     */
    @PostMapping("/detect2")
    public Result<Module2DetectVO> detect2(@RequestBody Module2DetectDTO request) {
        Module2DetectVO response = aiService.detect2(request);

        return Result.OK(response);
    }

}
