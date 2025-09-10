package com.financialfinshieldguard.gold.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.financialfinshieldguard.gold.data.user.changePassword.ChangePasswordRequest;
import com.financialfinshieldguard.gold.data.user.changePassword.ChangePasswordResponse;
import com.financialfinshieldguard.gold.data.user.getUserInfo.GetUserInfoResponse;
import com.financialfinshieldguard.gold.data.user.login.LoginRequest;
import com.financialfinshieldguard.gold.data.user.login.LoginResponse;
import com.financialfinshieldguard.gold.data.user.loginCode.LoginCodeRequest;
import com.financialfinshieldguard.gold.data.user.loginCode.LoginCodeResponse;
import com.financialfinshieldguard.gold.data.user.register.RegisterRequest;
import com.financialfinshieldguard.gold.data.user.register.RegisterResponse;
import com.financialfinshieldguard.gold.data.user.updateAvatar.UpdateAvatarRequest;
import com.financialfinshieldguard.gold.data.user.updateAvatar.UpdateAvatarResponse;
import com.financialfinshieldguard.gold.data.user.updateUser.UpdateUserRequest;
import com.financialfinshieldguard.gold.data.user.updateUser.UpdateUserResponse;
import com.financialfinshieldguard.gold.model.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 20316
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2025-07-15 09:00:27
*/
public interface UserService extends IService<User> {

    default User getOnly(QueryWrapper<User> wrapper, boolean throwEx) {
       //在sql中加limit 1，以防getOne底层在数据库获取多条数据，降低性能
        wrapper.last("limit 1");

        return this.getOne(wrapper,throwEx);
    }

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    LoginCodeResponse loginCode(LoginCodeRequest request);

    ChangePasswordResponse changePassword(ChangePasswordRequest request);

    GetUserInfoResponse getUserInfo();

    UpdateUserResponse updateUser(UpdateUserRequest request);

    UpdateAvatarResponse updateAvatar(String id, UpdateAvatarRequest request);


}
