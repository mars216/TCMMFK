package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.AuthAccess;
import com.example.springboot.common.Result;
import com.example.springboot.entity.User;
import com.example.springboot.service.UserService;
import com.example.springboot.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserServiceImpl userService;

    @PostMapping("/add")
    public Result add(@RequestBody User user) {
        try {
            userService.save(user);
            return Result.success();
        } catch (Exception e) {
            if (e instanceof DuplicateKeyException) {
                return Result.error("Database insertion error");
            } else {
                return Result.error("System error");
            }
        }
    }

    @PutMapping("/update")
    public Result update(@RequestBody User user) {
        try {
            userService.updateById(user);
            return Result.success();
        } catch (Exception e) {
            if (e instanceof DuplicateKeyException) {
                return Result.error("Database insertion error");
            } else {
                return Result.error("System error");
            }
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        userService.removeById(id);
        return Result.success();
    }

    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        userService.removeBatchByIds(ids);
        return Result.success();
    }

    @AuthAccess
    @GetMapping("/selectAll")
    public Result selectAll() {
        List<User> users = userService.list(new QueryWrapper<User>().orderByAsc("id"));
        return Result.success(users);
    }

    @GetMapping("/selectById/{id}") // Query user information by ID
    public Result selectById(@PathVariable Integer id) {
        User users = userService.getById(id);
        return Result.success(users);
    }

//    @GetMapping("/selectByName/{name}") // Query user information by Name
//    public Result selectByName(@PathVariable String name) {
//        List<User> users = userService.selectByName(name);
//        return Result.success(users);
//    }

    @GetMapping("/selectByMorePage") // Multi-condition fuzzy query
    public Result selectByPage(@RequestParam Integer pageNum,
                               @RequestParam Integer pageSize,
                               @RequestParam String username,
                               @RequestParam String name) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>().orderByDesc("id");
        queryWrapper.like(StrUtil.isNotBlank(username), "username", username);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        // select * from user where username like '%#{username}%' and name like '%#{name}%'
        Page<User> page = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        return Result.success(page);
    }
}