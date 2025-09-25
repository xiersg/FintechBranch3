package com.financialfinshieldguard.adminservice.controller;


import com.financialfinishieldguard.data.common.Result;
import com.financialfinishieldguard.data.manageUser.addUser.AddUserDTO;
import com.financialfinishieldguard.data.manageUser.addUser.AddUserVO;
import com.financialfinishieldguard.data.manageUser.deleteUsers.DeleteUsersDTO;
import com.financialfinishieldguard.data.manageUser.deleteUsers.DeleteUsersVO;
import com.financialfinishieldguard.data.manageUser.getUsersInfo.GetUsersInfoVO;
import com.financialfinishieldguard.data.manageUser.updateUser.UpdateUserDTO;
import com.financialfinishieldguard.data.manageUser.updateUser.UpdateUserVO;
import com.financialfinshieldguard.adminservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin")
public class ManageUserController {

    @Autowired
    private UserService userService;

    /**
     * 添加普通用户
     * @param request
     * @return
     */
    @PostMapping("/addUser")
    public Result<AddUserVO> addUser(@Valid @RequestBody AddUserDTO request) {
        AddUserVO response = userService.addUser(request);
        return Result.OK(response);
    }


    @GetMapping("/getUsersInfo")
    public Result<GetUsersInfoVO> getUserInfo() {
        GetUsersInfoVO response = userService.getUsersInfo();
        return Result.OK(response);
    }

    @PostMapping("/update")
    public Result<UpdateUserVO> updateUser(@Valid @RequestBody UpdateUserDTO request) {
        UpdateUserVO response = userService.updateUser(request);

        return Result.OK(response);
    }

    @DeleteMapping("/deleteUsers")
    public Result<DeleteUsersVO> deleteUsers(@Valid DeleteUsersDTO request) {
        DeleteUsersVO response = userService.deleteUsers(request);

        return Result.OK(response);
    }

}
