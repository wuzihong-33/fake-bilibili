package com.bilibili.api;

import com.bilibili.constant.AuthRoleConstant;
import com.bilibili.domain.JsonResponse;
import com.bilibili.domain.UserMoment;
import com.bilibili.domain.annotation.ApiLimitedRole;
import com.bilibili.service.UserMomentsService;
import com.bilibili.support.UserSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class UserMomentApi {
    @Autowired
    private UserMomentsService userMomentsService;

    @Autowired
    private UserSupport userSupport;


    /**
     * 用户发布一条动态（视频、直播、专栏动态）
     * @param userMoment
     * @return
     * @throws Exception
     */
    @ApiLimitedRole(limitedRoleCodeList = {AuthRoleConstant.ROLE_LV0}) // 限制lv0的用户不允许发布动态
//    @DataLimited
    @PostMapping("/user-moments")
    public JsonResponse<String> addUserMoments(@RequestBody UserMoment userMoment) throws Exception {
        Long userId = userSupport.getCurrentUserId();
        userMoment.setUserId(userId);
        userMomentsService.addUserMoments(userMoment);
        return JsonResponse.success();
    }

    /**
     * 获取用户关注的up的动态信息
     * @return
     */
    @GetMapping("/user-subscribed-moments")
    public JsonResponse<List<UserMoment>> getUserSubscribedMoments(){
        Long userId = userSupport.getCurrentUserId();
        List<UserMoment> list = userMomentsService.getUserSubscribedMoments(userId);
        return new JsonResponse<>(list);
    }
    
    
}
