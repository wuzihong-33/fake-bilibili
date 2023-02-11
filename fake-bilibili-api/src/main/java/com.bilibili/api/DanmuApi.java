package com.bilibili.api;

import com.bilibili.domain.Danmu;
import com.bilibili.domain.JsonResponse;
import com.bilibili.service.DanmuService;
import com.bilibili.support.UserSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class DanmuApi {
    @Autowired
    private DanmuService danmuService;

    @Autowired
    private UserSupport userSupport;

    @GetMapping("/danmus")
    public JsonResponse<List<Danmu>> getDanmus(@RequestParam Long videoId,
                                               String startTime,
                                               String endTime) throws Exception {
        List<Danmu> list;
        try{
            userSupport.getCurrentUserId();
            //用户登录模式，允许用户进行时间段筛选
            list = danmuService.getDanmus(videoId, startTime, endTime);
        }catch (Exception ignored){
            // 游客模式，不允许用户进行时间段筛选
            list = danmuService.getDanmus(videoId, null, null);
        }
        return new JsonResponse<>(list);
    }
}
