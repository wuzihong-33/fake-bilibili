package com.bilibili.domain;

import java.util.Date;

public class Danmu {
    private Long id; // 对于数据库主键

    private Long userId; // 弹幕的创建者的userId

    private Long videoId; // 该条弹幕所对应的视频id

    private String content; // 弹幕内容

    private String danmuTime; // 弹幕应该出现的时间点

    private Date createTime; // 弹幕的创建时间

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getDanmuTime() {
        return danmuTime;
    }

    public void setDanmuTime(String danmuTime) {
        this.danmuTime = danmuTime;
    }
}
