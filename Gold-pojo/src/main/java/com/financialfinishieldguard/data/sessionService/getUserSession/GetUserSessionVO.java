package com.financialfinishieldguard.data.sessionService.getUserSession;

import com.financialfinishieldguard.entity.UserSessions;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class GetUserSessionVO {

    private List<UserSessions> userSessions;
}
