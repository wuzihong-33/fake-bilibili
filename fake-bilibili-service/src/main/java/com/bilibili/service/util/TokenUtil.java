package com.bilibili.service.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.bilibili.exception.ConditionException;

import java.util.Calendar;
import java.util.Date;

public class TokenUtil {
    private static final String ISSUER = "签发者";

    // 生成用户令牌
    public static String generateToken(Long userId) throws Exception {
        Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(), RSAUtil.getPrivateKey());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, 1); // 表示这个token 1h后过期
        // 1. 唯一标识 这里使用 userid
        // 2. 签发者
        // 3.过期时间
        // 4. sign 生成签名（algorithm表示算法）
        return JWT.create().withKeyId(String.valueOf(userId))
                .withIssuer(ISSUER)
                .withExpiresAt(calendar.getTime())
                .sign(algorithm);
    }

    public static Long verifyToken(String token) {
        // 需要捕获异常，而不能抛出异常，这里有什么原则可以参考呢？
        try {
            Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(), RSAUtil.getPrivateKey());
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            String userId = jwt.getKeyId();
            return Long.valueOf(userId);
        } catch (TokenExpiredException e) {
            throw new ConditionException("555","token过期！");
        } catch (Exception e) {
            throw new ConditionException("非法用户token！");
        }
    }


}
