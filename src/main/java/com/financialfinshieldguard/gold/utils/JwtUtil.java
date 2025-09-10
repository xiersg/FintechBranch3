package com.financialfinshieldguard.gold.utils;

import com.financialfinshieldguard.gold.constants.config.ConfigEnum;
import com.financialfinshieldguard.gold.constants.config.TimeOutEnum;
import com.financialfinshieldguard.gold.constants.user.ErrorEnum;
import com.financialfinshieldguard.gold.exception.UserException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Date;

public class JwtUtil {

    /**
     * 过期时间目前设置成2天，这个配置随业务需求而定
     */
    private final static Duration expiration = Duration.ofHours(TimeOutEnum.JWT_TIME_OUT.getTimeOut());


    /**
     * 生成JWT
     *
     * @param id 手机号
     * @return JWT
     */
    public static String generate(String id) {
        // 过期时间
        Date expiryDate = new Date(System.currentTimeMillis() + expiration.toMillis());


        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS512, ConfigEnum.TOKEN_SECRET_KEY.getValue()) // 设置加密算法和秘钥
                .setExpiration(expiryDate)  // 设置过期时间
                .setSubject(id) // 将id放进JWT
                .setIssuedAt(new Date()) // 设置JWT签发时间
                .compact();//将构建好的 JWT 序列化为一个紧凑的字符串。
    }

    /**
     * 解析JWT
     *
     * @param token JWT字符串
     * @return 解析成功返回Claims对象，解析失败返回null
     * <p>
     * Claims 是 JWT 中的 “声明”，它包含了一系列的键值对，这些键值对可以携带以下类型的信息：
     * 用户信息：如用户 ID、用户名、角色等。
     * 元数据：如 JWT 的签发时间、过期时间等。
     * 其他自定义信息：如用户权限、用户状态等。
     */
    public static Claims parse(String token) {
        // 如果是空字符串直接返回null
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        // 这个Claims对象包含了许多属性，比如签发时间、过期时间以及存放的数据等
        Claims claims = null;
        // 解析失败了会抛出异常，所以我们要捕捉一下。token过期、token非法都会导致解析失败
//        try {

        //这里如果异常会直接抛出给调用者，我在那一边处理！
        claims = Jwts.parser()
                .setSigningKey(ConfigEnum.TOKEN_SECRET_KEY.getValue()) // 设置秘钥,跟生成JWT的时候的密钥要一样！
                .parseClaimsJws(token)
                .getBody();//拿到JWT令牌第二部分内容(自定义部分内容)

//        } catch (JwtException e) {
//            System.err.println("解析失败！");
//            //直接抛出异常，不可继续进行！
//            throw new UserException(ErrorEnum.JWT_ERROR);
//        }
        return claims;
    }


}
