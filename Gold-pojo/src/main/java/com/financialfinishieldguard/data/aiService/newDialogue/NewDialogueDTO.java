package com.financialfinishieldguard.data.aiService.newDialogue;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class NewDialogueDTO {
    private Long userId;
    private String characterType;
    private String name;
    private String description;

}
