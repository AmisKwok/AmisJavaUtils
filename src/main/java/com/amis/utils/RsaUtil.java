package com.amis.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * @author : KwokChichung
 * @description : RSA加解密工具类（无状态版本，密钥通过参数传入）
 * @createDate : 2026/2/9
 */
public class RsaUtil {

    private static final Logger log = LoggerFactory.getLogger(RsaUtil.class);
    private static final String RSA_ALGORITHM = "RSA";
    private static final String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding";

    private RsaUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 使用私钥解密
     *
     * @param privateKey    私钥对象
     * @param encryptedData 加密后的数据
     * @return 解密后的明文
     */
    public static String decrypt(PrivateKey privateKey, String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("RSA解密失败", e);
            throw new RuntimeException("密码解密失败");
        }
    }

    /**
     * 使用公钥加密
     *
     * @param publicKey 公钥对象
     * @param data      明文数据
     * @return 加密后的数据
     */
    public static String encrypt(PublicKey publicKey, String data) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedBytes = cipher.doFinal(dataBytes);
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("RSA加密失败", e);
            throw new RuntimeException("密码加密失败");
        }
    }

    /**
     * 从字符串获取私钥（支持 PEM 和 Base64 格式）
     *
     * @param keyStr 私钥字符串
     * @return PrivateKey对象
     */
    public static PrivateKey getPrivateKey(String keyStr) throws Exception {
        String cleanedKey = cleanPemKey(keyStr);
        byte[] keyBytes = Base64.getDecoder().decode(cleanedKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 从字符串获取公钥（支持 PEM 和 Base64 格式）
     *
     * @param keyStr 公钥字符串
     * @return PublicKey对象
     */
    public static PublicKey getPublicKey(String keyStr) throws Exception {
        String cleanedKey = cleanPemKey(keyStr);
        byte[] keyBytes = Base64.getDecoder().decode(cleanedKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 清理 PEM 格式的密钥（移除头尾标记）
     *
     * @param key 密钥字符串
     * @return 清理后的密钥字符串
     */
    public static String cleanPemKey(String key) {
        if (key == null) {
            return key;
        }
        key = key.replaceAll("\\s", "");
        key = key.replace("-----BEGINPUBLICKEY-----", "")
                 .replace("-----ENDPUBLICKEY-----", "")
                 .replace("-----BEGINPRIVATEKEY-----", "")
                 .replace("-----ENDPRIVATEKEY-----", "")
                 .replace("-----BEGINRSAPRIVATEKEY-----", "")
                 .replace("-----ENDRSAPRIVATEKEY-----", "");
        return key;
    }

    /**
     * 生成RSA密钥对（用于初始化配置）
     *
     * @return 包含私钥和公钥的字符串数组 [privateKey, publicKey]
     */
    public static String[] generateKeyPair() throws Exception {
        java.security.KeyPairGenerator keyPairGenerator = java.security.KeyPairGenerator.getInstance(RSA_ALGORITHM);
        keyPairGenerator.initialize(2048);
        java.security.KeyPair keyPair = keyPairGenerator.generateKeyPair();

        String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

        return new String[]{privateKey, publicKey};
    }
}
