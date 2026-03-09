package com.amis.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

/**
 * @author : KwokChichung
 * @description : JWT工具类（无状态版本，密钥通过参数传入）
 * @createDate : 2026/1/4
 */
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    private JwtUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final long DEFAULT_ACCESS_EXPIRATION = 1000 * 60 * 60 * 6;
    private static final long DEFAULT_REFRESH_EXPIRATION = 1000L * 60 * 60 * 24 * 15;

    /**
     * 生成access_token（使用默认过期时间）
     *
     * @param secretKey 密钥
     * @param claims    载荷数据
     * @return access_token
     */
    public static String generateAccessToken(String secretKey, Map<String, Object> claims) {
        return generateAccessToken(secretKey, claims, DEFAULT_ACCESS_EXPIRATION);
    }

    /**
     * 生成access_token（自定义过期时间）
     *
     * @param secretKey  密钥
     * @param claims     载荷数据
     * @param expiration 过期时间（毫秒）
     * @return access_token
     */
    public static String generateAccessToken(String secretKey, Map<String, Object> claims, long expiration) {
        return JWT.create()
                .withClaim("type", "access")
                .withClaim("claims", claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .sign(Algorithm.HMAC256(secretKey));
    }

    /**
     * 生成refresh_token（使用默认过期时间）
     *
     * @param secretKey 密钥
     * @param claims    载荷数据
     * @return refresh_token
     */
    public static String generateRefreshToken(String secretKey, Map<String, Object> claims) {
        return generateRefreshToken(secretKey, claims, DEFAULT_REFRESH_EXPIRATION);
    }

    /**
     * 生成refresh_token（自定义过期时间）
     *
     * @param secretKey  密钥
     * @param claims     载荷数据
     * @param expiration 过期时间（毫秒）
     * @return refresh_token
     */
    public static String generateRefreshToken(String secretKey, Map<String, Object> claims, long expiration) {
        return JWT.create()
                .withClaim("type", "refresh")
                .withClaim("claims", claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .sign(Algorithm.HMAC256(secretKey));
    }

    /**
     * 解析JWT token
     *
     * @param secretKey 密钥
     * @param token     token字符串
     * @return 载荷数据
     */
    public static Map<String, Object> parseToken(String secretKey, String token) {
        if (token == null || token.trim().isEmpty()) {
            String error = "Token cannot be null or empty";
            log.error(error);
            throw new JWTVerificationException(error);
        }

        try {
            return JWT.require(Algorithm.HMAC256(secretKey))
                    .build()
                    .verify(token)
                    .getClaim("claims")
                    .asMap();
        } catch (JWTVerificationException e) {
            log.error("JWT verification failed for token: {}", token, e);
            throw e;
        }
    }

    /**
     * 验证是否为refresh_token
     *
     * @param secretKey 密钥
     * @param token     token字符串
     * @return 是否为refresh_token
     */
    public static boolean isRefreshToken(String secretKey, String token) {
        try {
            DecodedJWT jwt = JWT.require(Algorithm.HMAC256(secretKey))
                    .build()
                    .verify(token);
            return "refresh".equals(jwt.getClaim("type").asString());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从token中获取用户ID
     *
     * @param secretKey  密钥
     * @param token      token字符串
     * @param userIdKey  用户ID在claims中的key
     * @return 用户ID
     */
    public static Long getUserIdFromToken(String secretKey, String token, String userIdKey) {
        Map<String, Object> claims = parseToken(secretKey, token);
        Object userId = claims.get(userIdKey);
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }

    /**
     * 获取token的过期时间
     *
     * @param secretKey 密钥
     * @param token     token字符串
     * @return 过期时间
     */
    public static LocalDateTime getExpirationFromToken(String secretKey, String token) {
        try {
            DecodedJWT jwt = JWT.require(Algorithm.HMAC256(secretKey))
                    .build()
                    .verify(token);
            return LocalDateTime.ofInstant(
                    jwt.getExpiresAt().toInstant(),
                    ZoneId.systemDefault()
            );
        } catch (Exception e) {
            log.error("获取token过期时间失败: {}", token, e);
            throw new RuntimeException("无法解析token过期时间");
        }
    }
}
