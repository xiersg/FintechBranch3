package com.financialfinshieldguard.gold.data.user.updateUser;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
public class UpdateUserResponse {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 是否更新成功
     */
    private boolean isUpdate;


}
