package com.financialfinishieldguard.data.aiService.getCurrentUserDialogues;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class GetCurrentUserDialoguesVO {

    /**
     * 当前用户的对话列表
     */
    private List<UserDialogueInfo> userDialogues;
}
