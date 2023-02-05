package com.bilibili.dao;

import com.bilibili.domain.User;
import com.bilibili.domain.UserInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Set;

@Mapper
public interface UserDao {

    User getUserByPhone(String phone);

    // 返回成功插入的数量
    Integer addUser(User user);

    User getUserById(Long id);

    UserInfo getUserInfoByUserId(Long userId);

    Integer addUserInfo(UserInfo userInfo);

    Integer updateUserInfos(UserInfo userInfo);

    List<UserInfo> getUserInfoByUserIds(Set<Long> followingIdSet);
}
