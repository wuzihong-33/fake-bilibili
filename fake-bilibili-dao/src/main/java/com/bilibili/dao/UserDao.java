package com.bilibili.dao;

import com.bilibili.domain.User;
import com.bilibili.domain.UserInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDao {

    User getUserByPhone(String phone);

    // 返回成功插入的数量
    Integer addUser(User user);
    Integer addUserInfo(UserInfo userInfo);

}
