package com.bilibili.service;

import com.bilibili.constant.UserConstant;
import com.bilibili.dao.UserDao;
import com.bilibili.domain.User;
import com.bilibili.domain.UserInfo;
import com.bilibili.exception.ConditionException;
import com.bilibili.service.util.MD5Util;
import com.bilibili.service.util.RSAUtil;
import com.bilibili.service.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mysql.cj.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Set;


@Service
public class UserService {
    @Autowired
    private UserDao userDao;

    @Transactional
    public void addUser(User user) {
        String phone = user.getPhone();
        if (StringUtils.isNullOrEmpty(phone)) {
            throw new ConditionException("手机号不能为空!");
        }
        User dbUser = this.getUserByPhone(phone);
        if (dbUser != null) {
            throw new ConditionException("该手机号已经注册!");
        }
        Date now = new Date();
        String salt = String.valueOf(now.getTime());
        String password = user.getPassword(); // 前端传过来的密码在前端经过rsa加密
        String rawPassword;
        try{
            rawPassword = RSAUtil.decrypt(password);
        }catch (Exception e){
            throw new ConditionException("密码解密失败！");
        }
        String md5Password = MD5Util.sign(rawPassword, salt, "UTF-8");
        user.setSalt(salt);
        user.setPassword(md5Password);
        user.setCreateTime(now);
        userDao.addUser(user);
        // 注册账号，用户只填写账号、密码；其余的将默认使用初始值
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setNick(UserConstant.DEFAULT_NICK);
        userInfo.setBirth(UserConstant.DEFAULT_BIRTH);
        userInfo.setGender(UserConstant.GENDER_MALE);
        userInfo.setCreateTime(now);
        userDao.addUserInfo(userInfo);
    }

    public User getUserByPhone(String phone){
        return userDao.getUserByPhone(phone);
    }


    public String login(User user) throws Exception{
        String phone = user.getPhone();
        if (StringUtils.isNullOrEmpty(phone)) {
            throw new ConditionException("手机号不能为空!");
        }
        User dbUser = this.getUserByPhone(phone);
        if (dbUser == null) {
            throw new ConditionException("当前用户不存在!");
        }
        String password = user.getPassword(); // 前端传过来的密码在前端经过rsa加密
        String rawPassword;
        try{
            rawPassword = RSAUtil.decrypt(password);
        }catch (Exception e){
            throw new ConditionException("密码解密失败！");
        }
        String salt = dbUser.getSalt();
        String md5Password = MD5Util.sign(rawPassword, salt, "UTF-8");
        if (!md5Password.equals(dbUser.getPassword())) {
            throw new ConditionException("密码错误");
        }
        return TokenUtil.generateToken(dbUser.getId());
    }

    public User getUserInfo(Long userId) {
        User user = userDao.getUserById(userId);
        UserInfo userInfo = userDao.getUserInfoByUserId(userId);
        user.setUserInfo(userInfo);
        return user;
    }

    public void updateUserInfos(UserInfo userInfo) {
        userInfo.setUpdateTime(new Date());
        userDao.updateUserInfos(userInfo);
    }

    public User getUserById(Long followingId) {
        return userDao.getUserById(followingId);
    }

    public List<UserInfo> getUserInfoByUserIds(Set<Long> followingIdSet) {
        return userDao.getUserInfoByUserIds(followingIdSet);
    }
}
