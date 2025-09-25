package com.financialfinshieldguard.adminservice.service.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.financialfinishieldguard.data.manageUser.addUser.AddUserDTO;
import com.financialfinishieldguard.data.manageUser.addUser.AddUserVO;
import com.financialfinishieldguard.data.manageUser.deleteUsers.DeleteUsersDTO;
import com.financialfinishieldguard.data.manageUser.deleteUsers.DeleteUsersVO;
import com.financialfinishieldguard.data.manageUser.getUsersInfo.GetUsersInfoVO;
import com.financialfinishieldguard.data.manageUser.getUsersInfo.UserInfo;
import com.financialfinishieldguard.data.manageUser.updateUser.UpdateUserDTO;
import com.financialfinishieldguard.data.manageUser.updateUser.UpdateUserVO;
import com.financialfinishieldguard.entity.User;
import com.financialfinishieldguard.gateutils.constants.ExceptionConstant;
import com.financialfinishieldguard.gateutils.constants.UserContext;
import com.financialfinishieldguard.gateutils.constants.enumm.RoleEnum;
import com.financialfinishieldguard.gateutils.constants.user.ErrorEnum;
import com.financialfinishieldguard.gateutils.constants.user.UserDataBaseConstant;
import com.financialfinishieldguard.gateutils.exception.DatabaseException;
import com.financialfinishieldguard.gateutils.exception.UserException;
import com.financialfinshieldguard.adminservice.mapper.UserMapper;
import com.financialfinshieldguard.adminservice.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 20316
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2025-07-15 09:00:27
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 添加用户
     * @param request
     * @return
     */
    @Override
    public AddUserVO addUser(AddUserDTO request) {
        String email = request.getEmail();
        String password = request.getPassword();
        String username = request.getUsername();

        if (isRegister(email)) {
            throw new UserException(ErrorEnum.REGISTER_ERROR);
        }

        //雪花算法（Snowflake）
        Snowflake snowflake = IdUtil.getSnowflake(1, 1);
        //密文存储用户密码，md5(password)
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());

        User user = new User()
                .setUserId(snowflake.nextId())
                .setEmail(email)
                .setUserName(username)
                .setPassword(encryptedPassword)
                .setRole(RoleEnum.USER.getValue());

        boolean isUserSave = this.save(user);
        if (!isUserSave) {
            throw new DatabaseException(ExceptionConstant.DATABASE_ERROR);
        }

        return new AddUserVO().setEmail(email);
    }


    @Override
    public GetUsersInfoVO getUsersInfo() {

        List<User> userList = this.list();
        List<UserInfo> userInfoList = userList.stream()
                .map(user -> {
                    UserInfo userInfo = new UserInfo();
                    BeanUtils.copyProperties(user, userInfo);
                    return userInfo;
                })
                .collect(Collectors.toList());

        return new GetUsersInfoVO().setUsersInfoList(userInfoList);
    }



    @Override
    public UpdateUserVO updateUser(UpdateUserDTO request) {
        Long userId = request.getUserId();
//        //通过token找到对应user
//        //queryWrapper.eq("user_id", ) 中传入的 userId 的数据类型应该与数据库中 user_id 字段的类型一致。
//        //MyBatis-Plus 在构建 SQL 查询时，会根据传入的参数类型生成相应的 SQL 语句。
//        //在我的数据库表中，user_id 字段的类型是 bigint，在我的 Java 实体类中，user_id 字段应该对应为 Long 类型，以匹配数据库中的 bigint 类型。
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(UserDataBaseConstant.USER_ID, userId);

        //注意：
        //updateById 方法会根据实体对象的主键字段更新数据库中的记录。它会将实体对象中的所有字段（包括 null 值）写入数据库，覆盖原有的值。因此，如果实体对象中的某些字段为 null，这些 null 值会被写入数据库，覆盖原有的值。
        //update 方法需要一个实体对象和一个条件（如 QueryWrapper 或 LambdaUpdateWrapper）。
        // 它只会更新实体对象中非 null 的字段。
        // 如果实体对象中的某些字段为 null，这些字段不会被更新，原有的值会保持不变。
        User user = new User();

        if (request.getUserName() != null && !request.getUserName().isEmpty()) {
            user.setUserName(request.getUserName());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            //密文存储用户密码，md5(password)
            String encryptedPassword = DigestUtils.md5DigestAsHex(request.getPassword().getBytes());
            user.setPassword(encryptedPassword);
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            user.setPhone(request.getPhone());
        }
        if (request.getSignature() != null && !request.getSignature().isEmpty()) {
            user.setSignature(request.getSignature());
        }
        if (request.getGender() != -1) {
            user.setGender(request.getGender());
        }
        if (request.getStatus() != -1) {
            user.setStatus(request.getStatus());
        }
        if (request.getHabit() != null && !request.getHabit().isEmpty()) {
            user.setHabit(request.getHabit());
        }
        if (request.getInvestmentPreference() != null && !request.getInvestmentPreference().isEmpty()) {
            user.setInvestmentPreference(request.getInvestmentPreference());
        }
        if (request.getEducationalBackground() != null && !request.getEducationalBackground().isEmpty()) {
            user.setEducationalBackground(request.getEducationalBackground());
        }
        if (request.getInvestmentBudget() != null && !request.getInvestmentBudget().isEmpty()) {
            user.setInvestmentBudget(request.getInvestmentBudget());
        }
        if (request.getCareer() != null && !request.getCareer().isEmpty()) {
            user.setCareer(request.getCareer());
        }

        //在实体类中，需要在主键属性上加上 @TableId 注解。
        // 使用 updateById 方法时，只需要传入一个实体对象，MyBatis-Plus 会自动更新这个实体中非空字段对应的数据库记录
        boolean isUpdate = this.update(user, queryWrapper);

        return new UpdateUserVO().setUserId(userId).setUpdate(isUpdate);
    }

    /**
     * 删除用户
     * @param request
     * @return
     */
    @Override
    @Transactional
    public DeleteUsersVO deleteUsers(DeleteUsersDTO request) {
        List<Long> ids = request.getIds();
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>().in(UserDataBaseConstant.USER_ID, ids);
        boolean removed = this.remove(queryWrapper);
        if (!removed) {
            throw new DatabaseException(ExceptionConstant.DATABASE_ERROR);
        }

        DeleteUsersVO response = new DeleteUsersVO();
        response.setUserIds(ids);
        response.setDelete(removed);
        return response;
    }


    /**
     * 查看当前邮箱是否注册
     *
     * @param email
     * @return
     */
    private boolean isRegister(String email) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(UserDataBaseConstant.EMAIL, email);

        long count = this.count(queryWrapper);

        return count > 0;
    }


}




