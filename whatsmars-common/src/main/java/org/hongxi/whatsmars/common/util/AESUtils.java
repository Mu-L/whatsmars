package org.hongxi.whatsmars.common.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;

/**
 * Created by shenhongxi on 15/5/27.
 */
public class AESUtils {

    private static final String PADDING = "AES/ECB/PKCS5Padding";

    /**
     * 加密
     *
     * @param data 需要加密的内容
     * @param key 加密秘钥
     * @return
     */
    public static byte[] encrypt(byte[] data, byte[] key) {
        if (key.length != 16) {
            throw new RuntimeException("Invalid AES key length (must be 16 bytes)");
        }
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance(PADDING);// 创建密码器
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);// 初始化
            return cipher.doFinal(data); // 加密
        } catch (Exception e) {
            throw new RuntimeException("encrypt failed!", e);
        }
    }

    /**
     * 解密
     *
     * @param data 待解密内容
     * @param key 解密密钥
     * @return
     */
    public static byte[] decrypt(byte[] data, byte[] key) {
        if(key.length!=16){
            throw new RuntimeException("Invalid AES key length (must be 16 bytes)");
        }
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance(PADDING);// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);// 初始化
            return cipher.doFinal(data); // 解密
        } catch (Exception e){
            throw new RuntimeException("decrypt failed!", e);
        }
    }

    public static String encryptToBase64(String data, String key){
        byte[] valueByte = encrypt(data.getBytes(StandardCharsets.UTF_8), key.getBytes(StandardCharsets.UTF_8));
        return new Base64().encodeToString(valueByte);
    }

    public static String decryptFromBase64(String data, String key){
        byte[] originalData = new Base64().decode(data.getBytes());
        byte[] valueByte = decrypt(originalData, key.getBytes(StandardCharsets.UTF_8));
        return new String(valueByte, StandardCharsets.UTF_8);
    }

    public static String encryptWithKeyBase64(String data, String key){
        byte[] valueByte = encrypt(data.getBytes(StandardCharsets.UTF_8), new Base64().decode(key.getBytes()));
        return new String(new Base64().encode(valueByte));
    }

    public static String decryptWithKeyBase64(String data, String key){
        byte[] originalData = new Base64().decode(data.getBytes());
        byte[] valueByte = decrypt(originalData, new Base64().decode(key.getBytes()));
        return new String(valueByte, StandardCharsets.UTF_8);
    }

    public static byte[] generateRandomKey(){
        KeyGenerator keygen;
        try {
            keygen = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("generate RandomKey failed!", e);
        }
        SecureRandom random = new SecureRandom();
        keygen.init(random);
        Key key = keygen.generateKey();
        return key.getEncoded();
    }

    public static String generateRandomKeyWithBase64(){
        return new String(new Base64().encode(generateRandomKey()));
    }
}
