package com.financialfinishieldguard.data.manageUser.deleteUsers;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors
public class DeleteUsersVO {
    /**
     * 用户ID
     */
    private List<Long> userIds;

    /**
     * 是否更新成功
     */
    private boolean isDelete;
}
