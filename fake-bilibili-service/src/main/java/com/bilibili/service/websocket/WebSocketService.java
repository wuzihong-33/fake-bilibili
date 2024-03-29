package com.bilibili.service.websocket;

import com.alibaba.fastjson.JSONObject;
import com.bilibili.constant.MQConstant;
import com.bilibili.domain.Danmu;
import com.bilibili.service.DanmuService;
import com.bilibili.service.util.RocketMQUtil;
import com.bilibili.service.util.TokenUtil;
import io.netty.util.internal.StringUtil;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;


/**
 * 由@ServerEndpoint负责声明一个websocket服务
 * 多例、有状态
 */
@Component
@ServerEndpoint("/imserver")
public class WebSocketService {
    private final Logger logger =  LoggerFactory.getLogger(this.getClass());

    private static final AtomicInteger ONLINE_COUNT = new AtomicInteger(0);

    public static final ConcurrentHashMap<String, WebSocketService> WEBSOCKET_MAP = new ConcurrentHashMap<>();
    
    private static DanmuService danmuService ;
    
    private static DefaultMQProducer danmuProducer ;
    
    private Session session;

    private String sessionId;

    private Long userId;
    
    private static ApplicationContext APPLICATION_CONTEXT;
    
    public static void initService(ApplicationContext applicationContext) {
        APPLICATION_CONTEXT = applicationContext;
        danmuService = (DanmuService) APPLICATION_CONTEXT.getBean("danmuService");
        danmuProducer = (DefaultMQProducer) APPLICATION_CONTEXT.getBean("danmusProducer");
    }
    
//    @OnOpen
//    public void openConnection(Session session,  @PathParam("token") String token) {
//        logger.info("用户连接成功, sessionId: {} , id: {}", session.getId(), ONLINE_COUNT.incrementAndGet());
//    }
    
    
    @OnOpen
    public void openConnection(Session session, @PathParam("token") String token) {
        try{
            this.userId = TokenUtil.verifyToken(token);
        }catch (Exception ignored){} // 没有登录也能发弹幕
        this.session = session;
        this.sessionId = session.getId();
        if (WEBSOCKET_MAP.contains(sessionId)) {
            WEBSOCKET_MAP.remove(sessionId);
            WEBSOCKET_MAP.put(sessionId, this);
        } else {
            WEBSOCKET_MAP.put(sessionId, this);
            ONLINE_COUNT.getAndIncrement();
        }
        logger.info("用户连接成功, sessionId: {} , 当前在线人数: {}", sessionId, ONLINE_COUNT.get());
        try {
            this.sendMessage("0"); // 向前端发送连接成功的确认信息
        } catch (Exception e) {
            logger.error("连接异常");
        }
    }

    @OnClose
    public void closeConnection() {
        if (WEBSOCKET_MAP.containsKey(sessionId)) {
            WEBSOCKET_MAP.remove(sessionId);
            ONLINE_COUNT.getAndDecrement();
        }
        logger.info("用户退出: sessionId: {}, 当前在线人数: {}", sessionId, ONLINE_COUNT.get());
    }

    // 接收到弹幕推送
    @OnMessage
    public void onMessage(String message) {
        if (StringUtil.isNullOrEmpty(message)) return;
        broadCastMessage(message);
        if (userId != null) {
            Danmu danmu = JSONObject.parseObject(message, Danmu.class);
            danmu.setCreateTime(new Date());
            danmu.setUserId(userId);
            storeDanmu(danmu);
        }
    }
    
    @OnError
    public void onError(Throwable error) {
        closeConnection();
    }

    //或直接指定时间间隔，例如：5秒
    @Scheduled(fixedRate=5000)
    private void noticeOnlineCount() throws IOException {
        for(Map.Entry<String, WebSocketService> entry : WebSocketService.WEBSOCKET_MAP.entrySet()){
            WebSocketService webSocketService = entry.getValue();
            if(webSocketService.session.isOpen()){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("onlineCount", ONLINE_COUNT.get());
                jsonObject.put("msg", "当前在线人数为" + ONLINE_COUNT.get());
                webSocketService.sendMessage(jsonObject.toJSONString());
            }
        }
    }
    
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    public Session getSession() {
        return session;
    }

    public String getSessionId() {
        return sessionId;
    }
    
    private static synchronized void storeDanmu(Danmu danmu) {
        // 异步保存弹幕到db(也可以将操作保存到mq中)
        danmuService.asyncAddDanmuToDb(danmu);
        // 保存弹幕到redis
        danmuService.addDanmusToRedis(danmu);   
    }
    
    private void broadCastMessage(String message) {
        for(Map.Entry<String, WebSocketService> entry : WEBSOCKET_MAP.entrySet()){
            WebSocketService webSocketService = entry.getValue();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message", message);
            jsonObject.put("sessionId", webSocketService.getSessionId());
            Message msg = new Message(MQConstant.TOPIC_DANMUS, jsonObject.toJSONString().getBytes(StandardCharsets.UTF_8));
            try {
//                    RocketMQUtil.syncSendMsg(danmusProducer, msg);
                RocketMQUtil.asyncSendMsg(danmuProducer, msg);
            } catch (Exception e) {
                logger.error("保存弹幕信息到MQ失败");
                e.printStackTrace();
            }
        }
    }
}
