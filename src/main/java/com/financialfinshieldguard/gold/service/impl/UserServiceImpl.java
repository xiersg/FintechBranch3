package com.financialfinshieldguard.gold.service.impl;


import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.financialfinshieldguard.gold.constants.user.ErrorEnum;
import com.financialfinshieldguard.gold.constants.user.redisConstant;
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
import com.financialfinshieldguard.gold.exception.CodeException;
import com.financialfinshieldguard.gold.exception.DatabaseException;
import com.financialfinshieldguard.gold.exception.UserException;
import com.financialfinshieldguard.gold.mapper.UserMapper;
import com.financialfinshieldguard.gold.model.User;
import com.financialfinshieldguard.gold.service.UserService;
import com.financialfinshieldguard.gold.utils.HweiOBSUtil;
import com.financialfinshieldguard.gold.utils.JwtUtil;
import com.financialfinshieldguard.gold.utils.NickNameGeneratorUtil;
import com.financialfinshieldguard.gold.utils.URLParseUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;


/**
 * @author 20316
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2025-07-15 09:00:27
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private HweiOBSUtil hweiOBSUtil;

    @Override
    public RegisterResponse register(RegisterRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();

        if (isRegister(email)) {
            throw new UserException(ErrorEnum.REGISTER_ERROR);
        }

        // 去查redis code == redisCode
        String key = redisConstant.REGISTER_CODE + email;
        //ops 是 operations 的缩写，意思是“操作”。
        // 在 Spring Data Redis 的上下文中，opsForValue() 方法返回的 ValueOperations 对象提供了一组对 Redis 中键值对的操作方法。
        String redisCode = redisTemplate.opsForValue().get(key);
        if (redisCode == null || !redisCode.equals(request.getCode())) {
            //不相等 -> 报错
            throw new CodeException(ErrorEnum.CODE_ERROR);
        }
        //相等 -> 存数据库
        //雪花算法（Snowflake）
        Snowflake snowflake = IdUtil.getSnowflake(1, 1);
        //明文存储用户密码
        //密文存储用户密码，md5(password)
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());

        User user = new User()
                .setUserId(snowflake.nextId())
                .setPassword(encryptedPassword)
                .setEmail(email)
                .setUserName(NickNameGeneratorUtil.generateNickName());

        boolean isUserSave = this.save(user);
        if (!isUserSave) {
            throw new DatabaseException("数据库异常，保存用户信息失败");
        }


        return new RegisterResponse().setEmail(email);

    }


    /**
     * 查看当前邮箱是否注册
     *
     * @param email
     * @return
     */
    private boolean isRegister(String email) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);

        long count = this.count(queryWrapper);

        return count > 0;
    }


    @Override
    public LoginResponse login(LoginRequest request) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", request.getEmail());

        User user = this.getOnly(queryWrapper, true);
        //md5加密
        String password = DigestUtils.md5DigestAsHex(request.getPassword().getBytes());
        if (user == null || !password.equals(user.getPassword())) {
            throw new UserException(ErrorEnum.LOGIN_ERROR);
        }

        LoginResponse response = new LoginResponse();


        //在使用 BeanUtils.copyProperties 方法时，字段名和数据类型都需要匹配才能正确地进行属性复制。具体来说：
        //1. 字段名必须一致
        //BeanUtils.copyProperties 方法会通过反射机制查找源对象（source）和目标对象（target）中的字段名称。只有字段名称完全一致的属性才会被复制。
        //2. 数据类型必须兼容
        //除了字段名称一致外，字段的数据类型也必须兼容。具体来说：
        //相同类型：如果两个字段的数据类型完全相同，可以直接复制。
        //兼容类型：如果目标字段的类型是源字段类型的子类型（或可以自动转换的类型），也可以复制。
        //不兼容类型：如果字段类型不兼容，BeanUtils.copyProperties 会跳过该字段，不会抛出异常

        //如果源对象的字段是 Long 类型，而目标对象的字段是 String 类型，BeanUtils.copyProperties 无法自动进行类型转换，因此这个字段不会被复制。
        BeanUtils.copyProperties(user, response);
        // token, session, jwt
        // jwt : json web token
        //header.payload.signature
        String token = JwtUtil.generate(String.valueOf(user.getUserId()));
        response.setToken(token);

        return response;
    }


    @Override
    public LoginCodeResponse loginCode(LoginCodeRequest request) {
        // 去查redis code == redisCode
        // 这里肯定是有个bug，和注册是同一个前缀！不过我看之后是会改动，这里明白就好！
        String key = redisConstant.LOGIN_CODE + request.getEmail();
        String redisCode = redisTemplate.opsForValue().get(key);
        if (redisCode == null || !redisCode.equals(request.getCode())) {
            //不相等 -> 报错
            throw new CodeException(ErrorEnum.CODE_ERROR);
        }

        //相等
        //查看该邮箱是否有账号
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", request.getEmail());
        User user = this.getOnly(queryWrapper, true);
        if (user == null) {
            throw new UserException(ErrorEnum.LOGIN_ERROR);
        }

        //登录成功
        LoginCodeResponse response = new LoginCodeResponse();
        BeanUtils.copyProperties(user, response);

        String token = JwtUtil.generate(response.getUserId());
        response.setToken(token);

        return response;
    }




    @Override
    public ChangePasswordResponse changePassword(ChangePasswordRequest request) {
        //校验验证码
        String key = redisConstant.CHANGEPASSWORD_CODE + request.getEmail();
        String redisCode = redisTemplate.opsForValue().get(key);
        if (redisCode == null || !redisCode.equals(request.getCode())) {
            throw new CodeException(ErrorEnum.CODE_ERROR);
        }


        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", request.getEmail());
        User user = this.getOnly(queryWrapper, true);

        if (user == null) {
            throw new UserException(ErrorEnum.LOGIN_ERROR);
        }


//        设置新密码
        //密文存储用户密码，md5(password)
        String encryptedPassword = DigestUtils.md5DigestAsHex(request.getNewPassword().getBytes());
        user.setPassword(encryptedPassword);
        this.update(user, queryWrapper);

        return new ChangePasswordResponse().setEmail(request.getEmail());
    }





    @Override
    public GetUserInfoResponse getUserInfo() {
        Long userId = getUserIdFromToken();

        User user = this.getById(userId);
        GetUserInfoResponse response = new GetUserInfoResponse();
        BeanUtils.copyProperties(user, response);

        return response;
    }

    @Override
    public UpdateUserResponse updateUser(UpdateUserRequest request) {
        Long userId = getUserIdFromToken();
//        //通过token找到对应user
//        //queryWrapper.eq("user_id", ) 中传入的 userId 的数据类型应该与数据库中 user_id 字段的类型一致。
//        //MyBatis-Plus 在构建 SQL 查询时，会根据传入的参数类型生成相应的 SQL 语句。
//        //在我的数据库表中，user_id 字段的类型是 bigint，在我的 Java 实体类中，user_id 字段应该对应为 Long 类型，以匹配数据库中的 bigint 类型。
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);

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

        //在实体类中，需要在主键属性上加上 @TableId 注解。
        // 使用 updateById 方法时，只需要传入一个实体对象，MyBatis-Plus 会自动更新这个实体中非空字段对应的数据库记录
        boolean isUpdate = this.update(user, queryWrapper);

        return new UpdateUserResponse().setUserId(userId).setUpdate(isUpdate);
    }

    private Long getUserIdFromToken() {
        String jwt = httpServletRequest.getHeader("token");
        Claims claims = JwtUtil.parse(jwt);
        String userIdStr = claims.getSubject();
        Long userId = Long.parseLong(userIdStr);
        return userId;
    }


    /**
     * 更新用户头像
     * @param id
     * @param request
     * @return
     */
    @Override
    public UpdateAvatarResponse updateAvatar(String id, UpdateAvatarRequest request) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //数据库里面的user_id是long类型，所以这里要进行一个转换！
        queryWrapper.eq("user_id", Long.valueOf(id));
        User user = this.getOnly(queryWrapper, true);

        if (user == null) {
            throw new UserException(ErrorEnum.NO_USER_ERROR);
        }

        boolean deleteOld = false;

        String oldAvatar = user.getAvatar();
        if (oldAvatar != null || !oldAvatar.isEmpty()) {
            //原来有头像，则删除原来的头像
            deleteOld = true;
        }

        user.setAvatar(request.avatarUrl);
        boolean isUpdate = updateById(user);

        if (!isUpdate) {
            throw new DatabaseException(ErrorEnum.UPDATE_AVATAR_ERROR);
        }

        if (deleteOld) {
            String oldFileName = URLParseUtil.extractObjectName(oldAvatar);
            hweiOBSUtil.deleteFile(oldFileName);
        }

        UpdateAvatarResponse response = new UpdateAvatarResponse();
        BeanUtils.copyProperties(user, response);

        return response;
    }

}




