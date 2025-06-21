package com.nonu1l.msgboxservice.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CryptoUtils {

    // 密钥和IV，必须与前端保持一致
    private static final String KEY = "1234567890123456";
    private static final String IV = "1234567890123456";
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    /**
     * AES 解密
     * @param encryptedText Base64编码的加密字符串
     * @return 解密后的原始字符串
     */
    public static String decrypt(String encryptedText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] original = cipher.doFinal(decodedBytes);
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 在实际应用中，这里应该记录日志
            e.printStackTrace();
            throw new RuntimeException("解密失败", e);
        }
    }
} 