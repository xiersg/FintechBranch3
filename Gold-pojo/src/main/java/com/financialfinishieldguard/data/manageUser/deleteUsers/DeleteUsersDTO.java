package com.financialfinishieldguard.data.manageUser.deleteUsers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Accessors(chain = true)
public class DeleteUsersDTO {

    /**
     * 要删除的用户ID
     */
    @NotEmpty(message = "id数组不能为空")
    List<Long> ids;
}
