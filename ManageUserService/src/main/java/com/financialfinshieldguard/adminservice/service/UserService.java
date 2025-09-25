package com.financialfinshieldguard.adminservice.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.financialfinishieldguard.data.manageUser.addUser.AddUserDTO;
import com.financialfinishieldguard.data.manageUser.addUser.AddUserVO;
import com.financialfinishieldguard.data.manageUser.deleteUsers.DeleteUsersDTO;
import com.financialfinishieldguard.data.manageUser.deleteUsers.DeleteUsersVO;
import com.financialfinishieldguard.data.manageUser.getUsersInfo.GetUsersInfoVO;
import com.financialfinishieldguard.data.manageUser.updateUser.UpdateUserDTO;
import com.financialfinishieldguard.data.manageUser.updateUser.UpdateUserVO;
import com.financialfinishieldguard.data.user.getUserInfo.GetUserInfoVO;
import com.financialfinishieldguard.data.user.updateAvatar.UpdateAvatarDTO;
import com.financialfinishieldguard.data.user.updateAvatar.UpdateAvatarVO;
import com.financialfinishieldguard.entity.User;
import com.financialfinishieldguard.gateutils.constants.user.UserDataBaseConstant;


/**
* @author 20316
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2025-07-15 09:00:27
*/
public interface UserService extends IService<User> {

    default User getOnly(QueryWrapper<User> wrapper, boolean throwEx) {
        //在sql中加limit 1，以防getOne底层在数据库获取多条数据，降低性能
        wrapper.last(UserDataBaseConstant.LIMIT_1);

        return this.getOne(wrapper,throwEx);
    }

    AddUserVO addUser(AddUserDTO request);

    GetUsersInfoVO getUsersInfo();

    UpdateUserVO updateUser(UpdateUserDTO request);

    DeleteUsersVO deleteUsers(DeleteUsersDTO request);



}
