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
     * 用户注册
     * @param user
     * @return
     */
    @PostMapping("/users")
    public JsonResponse<String> addUser(@RequestBody User user){
        System.out.println("User" + user.toString());
        userService.addUser(user);
        return JsonResponse.success();
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
     * 获取用户信息
     * @return
     */
    @GetMapping("/users")
    public JsonResponse<User> getUserInfo(){
        Long userId = userSupport.getCurrentUserId();
        User user = userService.getUserInfo(userId);
        return new JsonResponse<User>(user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/user-infos")
    public JsonResponse<String> updateUserInfos(@RequestBody UserInfo userInfo){
        Long userId = userSupport.getCurrentUserId(); //登录状态下，从token中获取
        userInfo.setUserId(userId);
        userService.updateUserInfos(userInfo);
        return JsonResponse.success();
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
    
    

}
