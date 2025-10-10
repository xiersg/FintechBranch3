package com.financialfinishieldguard.data.aiService.getCurrentUserDialogues;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserDialogueInfo {

    /**
     * 对话名称
     */
    private String dialogueName;

    /**
     * 对话Id
     */
    private Long dialogueId;
}
