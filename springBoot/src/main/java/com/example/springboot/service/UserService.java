package com.example.springboot.service;

import com.example.springboot.entity.User;

public interface UserService {
//    void insertUser(User User);
//
//    void updateUser(User user);
//
//    void deleteUser(Integer id);
//
//    void deleteUserList(List<Integer> ids);
//
//    List<User> selectAllList();
//
//    User selectById(Integer id);
//
//    List<User> selectByName(String name);
//
//    List<User> selectByMore(String username, String name);
//
//    List<User> selectByMoreFuzzy(String username, String name);
//
//    Page<User> selectByMorePage(String username, String name, Integer pageNum, Integer pageSize);

    User login(User user);

    User register(User user);

    void resetPassword(User user);
}
