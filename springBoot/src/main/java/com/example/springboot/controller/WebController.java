/**
 * Function: Provide API interfaces and return data
 * Author: mars
 * Date: 2024/12/30 15:30
 */
package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.example.springboot.common.AuthAccess;
import com.example.springboot.common.Result;
import com.example.springboot.entity.User;
import com.example.springboot.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController()
public class WebController {
    @Resource
    UserService userService;

    @AuthAccess
    @GetMapping("/")
    public Result hello() {
        return Result.success("success");
    }

//   @PostMapping("/post") // URL parameters
//    public Result post(objClass obj){
//        return Result.success(obj);
//    }
//    @PostMapping("/json")
//    public Result json(@RequestBody objClass obj){
//        return Result.success(obj);
//    }
//    @PutMapping("/putjson")
//    public Result putJson(@RequestBody objClass obj){
//        return Result.success(obj);
//    }
//    @DeleteMapping("/delete/{id}")
//    public Result Delete(@PathVariable Integer id){
//        return Result.success(id);
//    }
//    @DeleteMapping("/delete1") // Batch delete
//    public Result DeleteList(@RequestBody List<Integer>  ids){
//        return Result.success(ids);
//    }

    @AuthAccess
    @PostMapping("login")
    public Result logIn(@RequestBody User user) {
        if (StrUtil.isBlank(user.getUsername()) || StrUtil.isBlank(user.getPassword())) {
            return Result.error("Invalid input data");
        }
//        user = userService.login(user);
//        return Result.success(user);
        User dbUser = userService.login(user);
        return Result.success(dbUser);  // Login successful
    }

    @AuthAccess
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        if (StrUtil.isBlank(user.getUsername()) || StrUtil.isBlank(user.getPassword()) || StrUtil.isBlank(user.getRole())) {
            return Result.error("Invalid input data");
        }
        if (user.getUsername().length() > 10 || user.getPassword().length() > 20) {
            return Result.error("Invalid input data");
        }
        user = userService.register(user);
        return Result.success(user);
    }

    /**
     * Controller: Reset password
     */
    @AuthAccess
    @PutMapping("/password")
    public Result password(@RequestBody User user) {
        if (StrUtil.isBlank(user.getUsername()) || StrUtil.isBlank(user.getPhone())) {
            return Result.error("Invalid input data");
        }
        userService.resetPassword(user);
        return Result.success();
    }
}