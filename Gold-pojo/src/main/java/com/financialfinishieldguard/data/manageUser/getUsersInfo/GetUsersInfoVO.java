package com.financialfinishieldguard.data.manageUser.getUsersInfo;


import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class GetUsersInfoVO {

    private List<UserInfo> usersInfoList;
}
