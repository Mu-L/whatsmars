package org.hongxi.whatsmars.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DESUtils 加解密单元测试
 */
public class DESUtilsTest {

    // DES 密钥至少 8 字节
    private static final String KEY = "whatsmars";

    @Test
    public void testEncryptAndDecryptString() {
        String plainText = "Hello DES";
        String encrypted = DESUtils.encrypt(plainText, KEY);

        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);

        String decrypted = DESUtils.decrypt(encrypted, KEY);
        assertEquals(plainText, decrypted);
    }

    @Test
    public void testEncryptAndDecryptChinese() {
        String plainText = "这是一段中文DES加解密测试";
        String encrypted = DESUtils.encrypt(plainText, KEY);

        assertNotNull(encrypted);
        String decrypted = DESUtils.decrypt(encrypted, KEY);
        assertEquals(plainText, decrypted);
    }

    @Test
    public void testEncryptAndDecryptBytes() throws Exception {
        byte[] key = KEY.getBytes("utf-8");
        byte[] data = "byte array test".getBytes("utf-8");

        byte[] encrypted = DESUtils.encrypt(data, key);
        assertNotNull(encrypted);
        assertTrue(encrypted.length > 0);

        byte[] decrypted = DESUtils.decrypt(encrypted, key);
        assertEquals("byte array test", new String(decrypted, "utf-8"));
    }

    @Test
    public void testDifferentKeysProduceDifferentCiphertext() {
        String plainText = "same plaintext";
        String encrypted1 = DESUtils.encrypt(plainText, "key1key1");
        String encrypted2 = DESUtils.encrypt(plainText, "key2key2");

        assertNotEquals(encrypted1, encrypted2, "不同密钥应产生不同密文");
    }

    @Test
    public void testEmptyStringEncryptDecrypt() {
        String plainText = "";
        String encrypted = DESUtils.encrypt(plainText, KEY);
        assertNotNull(encrypted);

        String decrypted = DESUtils.decrypt(encrypted, KEY);
        assertEquals(plainText, decrypted);
    }

    @Test
    public void testLongStringEncryptDecrypt() {
        String plainText = "DES long string test ".repeat(200);

        String encrypted = DESUtils.encrypt(plainText, KEY);
        String decrypted = DESUtils.decrypt(encrypted, KEY);
        assertEquals(plainText, decrypted);
    }

    @Test
    public void testSpecialCharactersEncryptDecrypt() {
        String plainText = "!@#$%^&*()_+-=[]{}|;':\",./<>?`~";
        String encrypted = DESUtils.encrypt(plainText, KEY);
        String decrypted = DESUtils.decrypt(encrypted, KEY);
        assertEquals(plainText, decrypted);
    }

    @Test
    public void testDecryptWithWrongKey() {
        String plainText = "secret data";
        String encrypted = DESUtils.encrypt(plainText, KEY);

        // 用错误密钥解密，返回 null（DESUtils 内部 catch 了异常）
        String wrongDecrypted = DESUtils.decrypt(encrypted, "wrongkey");
        assertNotEquals(plainText, wrongDecrypted);
    }
}
