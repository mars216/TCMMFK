package com.example.springboot.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.Mapper.UserMapper;
import com.example.springboot.entity.User;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.utils.TokenUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    UserMapper userMapper;

    @Override
    public boolean save(User entity) {
        if (StrUtil.isBlank(entity.getName())) {
            entity.setName(entity.getUsername());
        }
        if (StrUtil.isBlank(entity.getPassword())) {
            entity.setPassword("123");
        }
        if (StrUtil.isBlank(entity.getRole())) {
            entity.setRole("User");
        }
        return super.save(entity);
    }

    public User selectByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return getOne(queryWrapper);
    }

    @Override
    public User login(User user) {
        // Query database by username
        User dbUser = selectByUsername(user.getUsername());
        if (dbUser == null) {
            throw new ServiceException("Account does not exist");
        }
        if (!user.getPassword().equals(dbUser.getPassword())) {
            throw new ServiceException("Username or password is incorrect");
        }
        // Generate token
        String token = TokenUtils.genToken(String.valueOf(dbUser.getId()), dbUser.getPassword());
        dbUser.setToken(token);
        return dbUser;
    }

    public User register(User user) {
        User dbUser = selectByUsername(user.getUsername());
        if (dbUser != null) {
            // Throw a custom exception
            throw new ServiceException("Username already exists");
        }
        user.setName(user.getUsername());
        userMapper.insert(user);
        return user;
    }

    public void resetPassword(User user) {
        User dbUser = selectByUsername(user.getUsername());
        if (dbUser == null) {
            // Throw a custom exception
            throw new ServiceException("User does not exist");
        }
        if (!user.getPhone().equals(dbUser.getPhone())) {
            throw new ServiceException("Verification error");
        }
        dbUser.setPassword("123");   // Reset password
        updateById(dbUser);
    }
}