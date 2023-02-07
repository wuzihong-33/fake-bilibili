package com.bilibili.service;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bilibili.constant.UserMomentsConstant;
import com.bilibili.dao.UserMomentsDao;
import com.bilibili.domain.UserMoment;
import com.bilibili.service.util.RocketMQUtil;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class UserMomentsService {
    @Autowired
    private UserMomentsDao userMomentsDao;
    
    @Autowired
    private DefaultMQProducer momentsProducer;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 用户发了一条动态，关注了的用户就能看到该动态
     * @param userMoment
     * @throws Exception
     */
    public void addUserMoments(UserMoment userMoment) throws Exception {
        userMoment.setCreateTime(new Date());
        userMomentsDao.addUserMoments(userMoment);
        Message msg = new Message(UserMomentsConstant.TOPIC_MOMENTS, JSONObject.toJSONString(userMoment).getBytes(StandardCharsets.UTF_8));
        RocketMQUtil.syncSendMsg(momentsProducer, msg);
    }

    
    public List<UserMoment> getUserSubscribedMoments(Long userId) {
        String key = "subscribed-" + userId;
        String listStr = redisTemplate.opsForValue().get(key);
        return JSONArray.parseArray(listStr, UserMoment.class);
    }
    
    
}
