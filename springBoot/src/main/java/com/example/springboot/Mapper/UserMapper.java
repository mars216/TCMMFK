package com.example.springboot.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboot.entity.User;

import org.apache.ibatis.annotations.Mapper;


// @Mapper
public interface UserMapper extends BaseMapper<User> {

//    @Insert("INSERT INTO `user` (" +
//            "`username`, " +
//            "`password`, " +
//            "`name`, " +
//            "`phone`, " +
//            "`email`, " +
//            "`address`, " +
//            "`avatar`," +
//            "`role`"+
//            ") VALUES (" +
//            "#{username}, " +
//            "#{password}, " +
//            "#{name}, " +
//            "#{phone}, " +
//            "#{email}, " +
//            "#{address}, " +
//            "#{avatar}," +
//            "#{role})")
//    void insert(User user);
//    @Update("UPDATE `user` SET " +
//            "`username` = #{username}, " +
//            "`password` = #{password}, " +
//            "`name` = #{name}, " +
//            "`phone` = #{phone}, " +
//            "`email` = #{email}, " +
//            "`address` = #{address}, " +
//            "`avatar` = #{avatar} " +
//            "WHERE `id` = #{id}") " )
//    void updateUser(User user);
//    @Delete("DELETE FROM `user` WHERE `id` = #{id}")
//    void deleteUser(Integer id);
//    @Delete("DELETE FROM `user` WHERE `id` in ")
//    void deleteUserList(List<Integer> ids);
//    @Select("SELECT * FROM `user` ORDER BY `id` DESC")
//
//    List<User> selectAllList();
//    @Select("SELECT * FROM `user` WHERE `id` = #{id}")
//    User selectById(Integer id);
//    @Select("SELECT * FROM `user` WHERE `name` = #{name}")
//    List<User> selectByName(String name);
//    @Select({
//            "<script>",
//            "SELECT * FROM `user`",
//            "<where>",
//            "<if test='username != null and username.trim() != \"\"'>",
//            "AND `username` = #{username}",
//            "</if>",
//            "<if test='name != null and name.trim() != \"\"'>",
//            "AND `name` = #{name}",
//            "</if>",
//            "</where>",
//            "</script>"
//    })
//    List<User> selectByMore(@Param("username") String username, @Param("name") String name);
//    @Select("SELECT * FROM `user` WHERE `username` LIKE CONCAT('%', #{username}, '%') AND `name` LIKE CONCAT('%', #{name}, '%')")
//    List<User> selectByMoreFuzzy(@Param("username") String username, @Param("name") String name);
//    @Select("SELECT * FROM `user` WHERE `username` LIKE CONCAT('%', #{username}, '%') AND `name` LIKE CONCAT('%', #{name}, '%') LIMIT #{skipNum}, #{pageSize}")
//    List<User> selectByMorePage(@Param("username") String username, @Param("name")  String name,@Param("skipNum") Integer skipNum,@Param("pageSize") Integer pageSize);
//    @Select("SELECT count(id) FROM `user` WHERE `username` LIKE CONCAT('%', #{username}, '%') AND `name` LIKE CONCAT('%', #{name}, '%')")
//    Integer selectCountByPage(@Param("username") String username, @Param("name") String name);
//    @Select("SELECT * FROM `user` WHERE `username` = #{username}")
//    User selectByUsername(@Param("username")String username);

}
