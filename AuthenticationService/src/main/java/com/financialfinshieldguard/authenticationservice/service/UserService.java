package com.financialfinshieldguard.authenticationservice.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.financialfinishieldguard.data.user.changePassword.ChangePasswordDTO;
import com.financialfinishieldguard.data.user.changePassword.ChangePasswordVO;
import com.financialfinishieldguard.data.user.getUserInfo.GetUserInfoVO;
import com.financialfinishieldguard.data.user.login.LoginDTO;
import com.financialfinishieldguard.data.user.login.LoginVO;
import com.financialfinishieldguard.data.user.loginCode.LoginCodeDTO;
import com.financialfinishieldguard.data.user.loginCode.LoginCodeVO;
import com.financialfinishieldguard.data.user.register.RegisterDTO;
import com.financialfinishieldguard.data.user.register.RegisterVO;
import com.financialfinishieldguard.data.user.updateAvatar.UpdateAvatarDTO;
import com.financialfinishieldguard.data.user.updateAvatar.UpdateAvatarVO;
import com.financialfinishieldguard.data.user.updateUser.UpdateUserDTO;
import com.financialfinishieldguard.data.user.updateUser.UpdateUserVO;
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

    RegisterVO register(RegisterDTO request);

    LoginVO login(LoginDTO request);

    LoginCodeVO loginCode(LoginCodeDTO request);

    ChangePasswordVO changePassword(ChangePasswordDTO request);

    GetUserInfoVO getUserInfo();

    UpdateUserVO updateUser(UpdateUserDTO request);

    UpdateAvatarVO updateAvatar(UpdateAvatarDTO request);


}
