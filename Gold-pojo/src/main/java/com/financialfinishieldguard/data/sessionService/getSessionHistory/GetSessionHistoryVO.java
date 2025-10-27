package com.financialfinishieldguard.data.sessionService.getSessionHistory;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class GetSessionHistoryVO {

    private List<SessionHistory> list;
}
