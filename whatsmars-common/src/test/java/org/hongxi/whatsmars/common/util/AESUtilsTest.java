package org.hongxi.whatsmars.common.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AESUtils 加解密单元测试
 */
public class AESUtilsTest {

    // 16 字节（128 位）AES 密钥
    private static final String KEY_16 = "1234567890abcdef";

    @Test
    public void testEncryptAndDecryptBytes() {
        byte[] key = KEY_16.getBytes(StandardCharsets.UTF_8);
        byte[] data = "Hello AES".getBytes(StandardCharsets.UTF_8);

        byte[] encrypted = AESUtils.encrypt(data, key);
        assertNotNull(encrypted);
        assertNotEquals(0, encrypted.length);

        byte[] decrypted = AESUtils.decrypt(encrypted, key);
        assertEquals("Hello AES", new String(decrypted, StandardCharsets.UTF_8));
    }

    @Test
    public void testEncryptToBase64AndDecryptFromBase64() {
        String plainText = "这是一段中文AES加密测试";
        String encrypted = AESUtils.encryptToBase64(plainText, KEY_16);

        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);

        String decrypted = AESUtils.decryptFromBase64(encrypted, KEY_16);
        assertEquals(plainText, decrypted);
    }

    @Test
    public void testEncryptWithKeyBase64AndDecryptWithKeyBase64() {
        // Base64 编码的密钥（原始密钥仍为 16 字节）
        String base64Key = new Base64().encodeToString(KEY_16.getBytes(StandardCharsets.UTF_8));
        String plainText = "Base64 key test";

        String encrypted = AESUtils.encryptWithKeyBase64(plainText, base64Key);
        assertNotNull(encrypted);

        String decrypted = AESUtils.decryptWithKeyBase64(encrypted, base64Key);
        assertEquals(plainText, decrypted);
    }

    @Test
    public void testGenerateRandomKey() {
        byte[] randomKey = AESUtils.generateRandomKey();
        assertNotNull(randomKey);
        assertTrue(randomKey.length == 16 || randomKey.length == 32,
                "AES 密钥长度应为 16 字节 (AES-128) 或 32 字节 (AES-256)，实际: " + randomKey.length);
    }

    @Test
    public void testGenerateRandomKeyWithBase64() {
        String base64Key = AESUtils.generateRandomKeyWithBase64();
        assertNotNull(base64Key);
        assertFalse(base64Key.isEmpty());

        // 生成的随机密钥可能为 32 字节，AESUtils 只支持 16 字节，截取前 16 字节验证加解密
        byte[] keyBytes = new Base64().decode(base64Key);
        byte[] key16 = Arrays.copyOf(keyBytes, 16);
        byte[] data = "random key test".getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = AESUtils.encrypt(data, key16);
        byte[] decrypted = AESUtils.decrypt(encrypted, key16);
        assertEquals("random key test", new String(decrypted, StandardCharsets.UTF_8));
    }

    @Test
    public void testInvalidKeyLength() {
        byte[] shortKey = "short".getBytes(StandardCharsets.UTF_8); // 5 字节，不合法
        byte[] data = "test".getBytes(StandardCharsets.UTF_8);

        assertThrows(RuntimeException.class, () -> AESUtils.encrypt(data, shortKey));
        assertThrows(RuntimeException.class, () -> AESUtils.decrypt(data, shortKey));
    }

    @Test
    public void testDifferentKeysProduceDifferentCiphertext() {
        String key1 = "1234567890abcdef";
        String key2 = "abcdef1234567890";
        String plainText = "same plaintext";

        String encrypted1 = AESUtils.encryptToBase64(plainText, key1);
        String encrypted2 = AESUtils.encryptToBase64(plainText, key2);

        assertNotEquals(encrypted1, encrypted2, "不同密钥应产生不同密文");
    }

    @Test
    public void testEmptyDataEncryptDecrypt() {
        byte[] key = KEY_16.getBytes(StandardCharsets.UTF_8);
        byte[] emptyData = new byte[0];

        byte[] encrypted = AESUtils.encrypt(emptyData, key);
        byte[] decrypted = AESUtils.decrypt(encrypted, key);

        assertEquals(0, decrypted.length);
    }

    @Test
    public void testLongDataEncryptDecrypt() {
        // 超过一个 AES block (16 bytes) 的数据
        String plainText = "long data test ".repeat(100);

        String encrypted = AESUtils.encryptToBase64(plainText, KEY_16);
        String decrypted = AESUtils.decryptFromBase64(encrypted, KEY_16);
        assertEquals(plainText, decrypted);
    }
}
