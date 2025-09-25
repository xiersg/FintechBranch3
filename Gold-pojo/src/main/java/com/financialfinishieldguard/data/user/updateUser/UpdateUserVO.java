package com.financialfinishieldguard.data.user.updateUser;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UpdateUserVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 是否更新成功
     */
    private boolean isUpdate;


}
