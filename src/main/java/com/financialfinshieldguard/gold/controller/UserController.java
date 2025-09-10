package com.financialfinshieldguard.gold.controller;

import com.financialfinshieldguard.gold.data.common.Result;
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
import com.financialfinshieldguard.gold.service.UserService;
import com.financialfinshieldguard.gold.utils.JwtUtil;
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
    public Result<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {

        RegisterResponse response = userService.register(request);

        return Result.OK(response);
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        LoginResponse response = userService.login(request);

        return Result.OK(response);
    }

    @PostMapping("/loginCode")
    public Result<LoginCodeResponse> loginCode(@Valid @RequestBody LoginCodeRequest request) {

        LoginCodeResponse response = userService.loginCode(request);

        return Result.OK(response);
    }

    @PostMapping("/changePassword")
    public Result<ChangePasswordResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        ChangePasswordResponse response = userService.changePassword(request);

        return Result.OK(response);
    }


    @GetMapping("getUserInfo")
    public Result<GetUserInfoResponse> getUserInfo() {
        GetUserInfoResponse response = userService.getUserInfo();

        return Result.OK(response);
    }

    @PostMapping("/update")
    public Result<UpdateUserResponse> updateUser(@Valid @RequestBody UpdateUserRequest request) {
        UpdateUserResponse response = userService.updateUser(request);

        return Result.OK(response);
    }


    @PatchMapping("/updateAvatar")

    public Result<UpdateAvatarResponse> updateAvatar(@Valid @RequestBody UpdateAvatarRequest request,
                                                     @RequestHeader String token) {
        String id = JwtUtil.parse(token).getSubject();
        UpdateAvatarResponse response = userService.updateAvatar(id, request);

        return Result.OK(response);
    }
}
