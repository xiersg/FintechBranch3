package com.financialfinishieldguard.data.sessionService.createUserSession;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CreateUserSessionDTO {

    private String sessionName;
}
