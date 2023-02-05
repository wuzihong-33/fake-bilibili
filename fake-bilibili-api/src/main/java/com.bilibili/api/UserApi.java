package com.bilibili.api;


import com.bilibili.domain.User;
import com.bilibili.domain.UserInfo;
import com.bilibili.service.UserService;
import com.bilibili.service.util.RSAUtil;
import com.bilibili.domain.JsonResponse;
import com.bilibili.support.UserSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
public class UserApi {
    @Autowired
    private UserService userService;
    @Autowired
    private UserSupport userSupport;

    /**
     * 获取user info
     * @return
     */
    @GetMapping("/users")
    public JsonResponse<User> getUserInfo(){
        Long userId = userSupport.getCurrentUserId();
        User user = userService.getUserInfo(userId);
        return new JsonResponse<>(user);
    }

    /**
     * 获取rsa公钥
     * @return
     */
    @GetMapping("/rsa-pks")
    public JsonResponse<String> getRsaPublicKey() {
        String pk = RSAUtil.getPublicKeyStr();
        return new JsonResponse<String>(pk);
    }

    /**
     * 用户注册
     * @param user
     * @return
     */
    @PostMapping("/users")
    public JsonResponse<String> addUser(@RequestBody User user){
        userService.addUser(user);
        return JsonResponse.success(); // 由于在 userService.addUser(user); 里边已经对各种情况 抛异常，因此能走到最后一步一般是成功的
    }

    /**
     * 用户登录
     * @param user
     * @return
     */
    @PostMapping("/user-tokens")
    public JsonResponse<String> login(@RequestBody User user) throws Exception{
        String token = userService.login(user);
        return new JsonResponse<>(token);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/user-infos")
    public JsonResponse<String> updateUserInfos(@RequestBody UserInfo userInfo){
        Long userId = userSupport.getCurrentUserId(); // userId一般都是从token中获取而非前端直接传递。
        userInfo.setUserId(userId);
        userService.updateUserInfos(userInfo);
        return JsonResponse.success();
    }

}
