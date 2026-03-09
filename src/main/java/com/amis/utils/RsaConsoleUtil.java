package com.amis.utils;

import javax.crypto.Cipher;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Scanner;

/**
 * @author : KwokChichung
 * @description : RSA加解密控制台工具类，用于手动执行加解密操作
 * @createDate : 2026/3/5
 */
public class RsaConsoleUtil {

    private static final String RSA_ALGORITHM = "RSA";
    private static final String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding";

    private static PrivateKey privateKey;
    private static PublicKey publicKey;
    private static String privateKeyStr;
    private static String publicKeyStr;

    /**
     * 主菜单
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n========================================");
        System.out.println("    RSA 加解密控制台工具");
        System.out.println("========================================\n");

        while (true) {
            System.out.println("请选择操作：");
            System.out.println("1. 显示当前密钥");
            System.out.println("2. 加密数据");
            System.out.println("3. 解密数据");
            System.out.println("4. 生成新密钥对");
            System.out.println("0. 退出");
            System.out.print("\n请输入选项: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    showCurrentKeys();
                    break;
                case "2":
                    System.out.print("请输入要加密的数据: ");
                    String dataToEncrypt = scanner.nextLine().trim();
                    encryptData(dataToEncrypt);
                    break;
                case "3":
                    System.out.print("请输入要解密的数据（Base64格式）: ");
                    String dataToDecrypt = scanner.nextLine().trim();
                    decryptData(dataToDecrypt);
                    break;
                case "4":
                    generateNewKeyPair();
                    break;
                case "0":
                    System.out.println("\n感谢使用，再见！");
                    scanner.close();
                    return;
                default:
                    System.out.println("无效选项，请重新选择！\n");
            }
        }
    }

    /**
     * 静态初始化块，从配置文件加载密钥
     */
    static {
        loadKeysFromConfig();
    }

    /**
     * 从配置文件加载密钥
     */
    private static void loadKeysFromConfig() {
        String configPath = "src/main/resources/application.yml";

        try {
            String content = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(configPath)), Charset.forName("UTF-8"));
            
            String[] lines = content.split("\n");
            boolean inRsaSection = false;
            
            for (String line : lines) {
                line = line.trim();
                
                if (line.startsWith("rsa:")) {
                    inRsaSection = true;
                    continue;
                }
                
                if (inRsaSection) {
                    if (line.startsWith("private-key:")) {
                        privateKeyStr = line.substring("private-key:".length()).trim();
                    } else if (line.startsWith("public-key:")) {
                        publicKeyStr = line.substring("public-key:".length()).trim();
                    } else if (line.startsWith("#") || line.isEmpty()) {
                        continue;
                    } else if (!line.startsWith(" ") && !line.startsWith("\t")) {
                        break;
                    }
                }
            }

            if (privateKeyStr != null && !privateKeyStr.isEmpty() && !privateKeyStr.equals("your-rsa-private-key-here")) {
                privateKey = getPrivateKey(privateKeyStr);
            }

            if (publicKeyStr != null && !publicKeyStr.isEmpty() && !publicKeyStr.equals("your-rsa-public-key-here")) {
                publicKey = getPublicKey(publicKeyStr);
            }

            if (privateKey != null && publicKey != null) {
                System.out.println("已从配置文件加载RSA密钥\n");
            } else {
                System.out.println("提示：配置文件中未找到有效的RSA密钥\n");
            }

        } catch (Exception e) {
            System.out.println("提示：未找到配置文件或密钥配置\n");
        }
    }

    /**
     * 显示当前密钥
     */
    private static void showCurrentKeys() {
        System.out.println("\n========================================");
        System.out.println("当前密钥状态");
        System.out.println("========================================\n");

        if (publicKeyStr != null) {
            System.out.println("公钥：");
            System.out.println(publicKeyStr);
        } else {
            System.out.println("公钥：未设置");
        }

        System.out.println();

        if (privateKeyStr != null) {
            System.out.println("私钥：");
            System.out.println(privateKeyStr);
        } else {
            System.out.println("私钥：未设置");
        }

        System.out.println("\n========================================\n");
    }

    /**
     * 加密数据
     *
     * @param data 明文数据
     */
    private static void encryptData(String data) {
        if (publicKey == null) {
            System.out.println("公钥未初始化，请先生成密钥对！\n");
            return;
        }

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedBytes = cipher.doFinal(dataBytes);
            String encryptedData = Base64.getEncoder().encodeToString(encryptedBytes);

            System.out.println("\n========================================");
            System.out.println("加密成功！");
            System.out.println("========================================\n");
            System.out.println("原始数据: " + data);
            System.out.println("加密结果: " + encryptedData);
            System.out.println("\n========================================\n");

        } catch (Exception e) {
            System.out.println("加密失败: " + e.getMessage() + "\n");
        }
    }

    /**
     * 解密数据
     *
     * @param encryptedData 加密数据（Base64格式）
     */
    private static void decryptData(String encryptedData) {
        if (privateKey == null) {
            System.out.println("私钥未初始化，请先生成密钥对！\n");
            return;
        }

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            String decryptedData = new String(decryptedBytes, StandardCharsets.UTF_8);

            System.out.println("\n========================================");
            System.out.println("解密成功！");
            System.out.println("========================================\n");
            System.out.println("加密数据: " + encryptedData);
            System.out.println("解密结果: " + decryptedData);
            System.out.println("\n========================================\n");

        } catch (Exception e) {
            System.out.println("解密失败: " + e.getMessage() + "\n");
        }
    }

    /**
     * 生成新的密钥对
     */
    private static void generateNewKeyPair() {
        try {
            String[] keyPair = RsaUtil.generateKeyPair();
            privateKeyStr = keyPair[0];
            publicKeyStr = keyPair[1];
            privateKey = getPrivateKey(privateKeyStr);
            publicKey = getPublicKey(publicKeyStr);

            System.out.println("\n========================================");
            System.out.println("密钥对生成成功！");
            System.out.println("========================================\n");
            System.out.println("将以下配置添加到 application.yml：\n");
            System.out.println("rsa:");
            System.out.println("  private-key: " + privateKeyStr);
            System.out.println("  public-key: " + publicKeyStr);
            System.out.println("\n========================================\n");

        } catch (Exception e) {
            System.out.println("生成密钥对失败: " + e.getMessage() + "\n");
        }
    }

    /**
     * 从字符串获取私钥
     *
     * @param keyStr 私钥字符串
     * @return PrivateKey对象
     */
    private static PrivateKey getPrivateKey(String keyStr) throws Exception {
        String cleanedKey = RsaUtil.cleanPemKey(keyStr);
        byte[] keyBytes = Base64.getDecoder().decode(cleanedKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 从字符串获取公钥
     *
     * @param keyStr 公钥字符串
     * @return PublicKey对象
     */
    private static PublicKey getPublicKey(String keyStr) throws Exception {
        String cleanedKey = RsaUtil.cleanPemKey(keyStr);
        byte[] keyBytes = Base64.getDecoder().decode(cleanedKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }
}
