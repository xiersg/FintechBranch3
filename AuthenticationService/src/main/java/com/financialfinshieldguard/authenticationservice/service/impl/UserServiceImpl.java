package com.financialfinshieldguard.authenticationservice.service.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import com.financialfinishieldguard.gateutils.constants.ExceptionConstant;
import com.financialfinishieldguard.gateutils.constants.UserContext;
import com.financialfinishieldguard.gateutils.constants.user.ErrorEnum;
import com.financialfinishieldguard.gateutils.constants.user.UserDataBaseConstant;
import com.financialfinishieldguard.gateutils.constants.user.redisConstant;
import com.financialfinishieldguard.gateutils.utils.JwtUtil;
import com.financialfinishieldguard.gateutils.utils.NickNameGeneratorUtil;
import com.financialfinishieldguard.gateutils.utils.URLParseUtil;
import com.financialfinshieldguard.authenticationservice.HweiOBSUtil;
import com.financialfinishieldguard.gateutils.exception.CodeException;
import com.financialfinishieldguard.gateutils.exception.DatabaseException;
import com.financialfinishieldguard.gateutils.exception.UserException;
import com.financialfinshieldguard.authenticationservice.mapper.UserMapper;
import com.financialfinshieldguard.authenticationservice.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

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
    private HweiOBSUtil hweiOBSUtil;

    @Override
    public RegisterVO register(RegisterDTO request) {
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
            throw new DatabaseException(ExceptionConstant.DATABASE_ERROR);
        }


        return new RegisterVO().setEmail(email);

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


    @Override
    public LoginVO login(LoginDTO request) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(UserDataBaseConstant.EMAIL, request.getEmail());

        User user = this.getOnly(queryWrapper, true);

        //md5加密
        String password = DigestUtils.md5DigestAsHex(request.getPassword().getBytes());
        if (user == null || !password.equals(user.getPassword())) {
            throw new UserException(ErrorEnum.LOGIN_ERROR);
        }

        LoginVO response = new LoginVO();


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

        String token = JwtUtil.generate(String.valueOf(response.getUserId()),response.getRole().toString());
        response.setToken(token);

        return response;
    }


    @Override
    public LoginCodeVO loginCode(LoginCodeDTO request) {
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
        queryWrapper.eq(UserDataBaseConstant.EMAIL, request.getEmail());
        User user = this.getOnly(queryWrapper, true);
        if (user == null) {
            throw new UserException(ErrorEnum.LOGIN_ERROR);
        }

        //登录成功
        LoginCodeVO response = new LoginCodeVO();
        BeanUtils.copyProperties(user, response);


        String token = JwtUtil.generate(response.getUserId(), response.getRole().toString());
        response.setToken(token);

        return response;
    }




    @Override
    public ChangePasswordVO changePassword(ChangePasswordDTO request) {
        //校验验证码
        String key = redisConstant.CHANGEPASSWORD_CODE + request.getEmail();
        String redisCode = redisTemplate.opsForValue().get(key);
        if (redisCode == null || !redisCode.equals(request.getCode())) {
            throw new CodeException(ErrorEnum.CODE_ERROR);
        }


        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(UserDataBaseConstant.EMAIL, request.getEmail());
        User user = this.getOnly(queryWrapper, true);

        if (user == null) {
            throw new UserException(ErrorEnum.LOGIN_ERROR);
        }


//        设置新密码
        //密文存储用户密码，md5(password)
        String encryptedPassword = DigestUtils.md5DigestAsHex(request.getNewPassword().getBytes());
        user.setPassword(encryptedPassword);
        this.update(user, queryWrapper);

        return new ChangePasswordVO().setEmail(request.getEmail());
    }





    @Override
    public GetUserInfoVO getUserInfo() {
        Long userId = UserContext.getCurrentId();

        User user = this.getById(userId);
        GetUserInfoVO response = new GetUserInfoVO();
        BeanUtils.copyProperties(user, response);

        return response;
    }

    @Override
    public UpdateUserVO updateUser(UpdateUserDTO request) {
        Long userId = UserContext.getCurrentId();

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
     * 更新用户头像
     * @param request
     * @return
     */
    @Override
    public UpdateAvatarVO updateAvatar(UpdateAvatarDTO request) {
        Long userId = UserContext.getCurrentId();

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //数据库里面的user_id是long类型，所以这里要进行一个转换！
        queryWrapper.eq(UserDataBaseConstant.USER_ID, Long.valueOf(userId));
        User user = this.getOnly(queryWrapper, true);

        if (user == null) {
            throw new UserException(ErrorEnum.NO_USER_ERROR);
        }

        boolean deleteOld = false;

        String oldAvatar = user.getAvatar();
        //注意这里要写&&，不然oldAvatar为null的话会继续判断后面的，而null不可以去判断是否isEmpty
        if (oldAvatar != null && !oldAvatar.isEmpty()) {
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

        UpdateAvatarVO response = new UpdateAvatarVO();
        BeanUtils.copyProperties(user, response);

        return response;
    }

}




