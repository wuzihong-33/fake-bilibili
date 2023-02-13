package com.bilibili.dao;

import com.alibaba.fastjson.JSONObject;
import com.bilibili.domain.RefreshTokenDetail;
import com.bilibili.domain.User;
import com.bilibili.domain.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;
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

    Integer pageCountUserInfos(Map<String, Object> params);

    List<UserInfo> pageListUserInfos(JSONObject params);

    Integer updateUsers(User user);

    Integer deleteRefreshTokenByUserId(Long userId);

    String getRefreshTokenByUserId(Long userId);

    RefreshTokenDetail getRefreshTokenDetail(String refreshToken);

    Integer addRefreshToken(@Param("refreshToken")String refreshToken,
                            @Param("userId") Long userId,
                            @Param("createTime") Date createTime);
    
    Integer deleteRefreshToken(@Param("refreshToken") String refreshToken,
                               @Param("userId") Long userId);


    User getUserByPhoneOrEmail(@Param("phone") String phone, @Param("email") String email);

}
