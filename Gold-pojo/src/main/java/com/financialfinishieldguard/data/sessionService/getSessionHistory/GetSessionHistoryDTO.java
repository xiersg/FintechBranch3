package com.financialfinishieldguard.data.sessionService.getSessionHistory;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GetSessionHistoryDTO {

    private Long sessionId;
}
