/**
 * Function: Token utility for JWT generation and current user retrieval
 * Author: mars
 * Date: 2025/2/17 14:33
 */
package com.example.springboot.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.springboot.Mapper.UserMapper;
import com.example.springboot.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Component
public class TokenUtils {

    private static UserMapper staticUserMapper;

    @Resource
    UserMapper userMapper;

    @PostConstruct
    public void setUserService() {
        staticUserMapper = userMapper;
    }

    /**
     * Generate JWT token
     *
     * @param userId user ID
     * @param sign   signing key (usually password)
     * @return JWT token string
     */
    public static String genToken(String userId, String sign) {
        return JWT.create().withAudience(userId) // Save user id in token as payload
                .withExpiresAt(DateUtil.offsetHour(new Date(), 2)) // Token expires after 2 hours
                .sign(Algorithm.HMAC256(sign)); // Use password as token secret
    }

    /**
     * Get the currently logged-in user
     *
     * @return User object or null if not logged in
     */
    public static User getCurrentUser() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String token = request.getHeader("token");
            if (StrUtil.isNotBlank(token)) {
                String userId = JWT.decode(token).getAudience().get(0);
                return staticUserMapper.selectById(Integer.valueOf(userId));
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}