package com.financialfinshieldguard.authenticationservice.controller;

import com.financialfinishieldguard.data.common.Result;
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
import com.financialfinshieldguard.authenticationservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<RegisterVO> register(@Valid @RequestBody RegisterDTO request) {

        RegisterVO response = userService.register(request);

        return Result.OK(response);
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO request) {

        LoginVO response = userService.login(request);

        return Result.OK(response);
    }

    @PostMapping("/loginCode")
    public Result<LoginCodeVO> loginCode(@Valid @RequestBody LoginCodeDTO request) {

        LoginCodeVO response = userService.loginCode(request);

        return Result.OK(response);
    }

    @PostMapping("/changePassword")
    public Result<ChangePasswordVO> changePassword(@Valid @RequestBody ChangePasswordDTO request) {
        ChangePasswordVO response = userService.changePassword(request);

        return Result.OK(response);
    }


    @GetMapping("getUserInfo")
    public Result<GetUserInfoVO> getUserInfo() {
        GetUserInfoVO response = userService.getUserInfo();

        return Result.OK(response);
    }

    @PostMapping("/update")
    public Result<UpdateUserVO> updateUser(@Valid @RequestBody UpdateUserDTO request) {
        UpdateUserVO response = userService.updateUser(request);

        return Result.OK(response);
    }


    @PatchMapping("/updateAvatar")
    public Result<UpdateAvatarVO> updateAvatar(@Valid @RequestBody UpdateAvatarDTO request) {
        UpdateAvatarVO response = userService.updateAvatar(request);

        return Result.OK(response);
    }
}
