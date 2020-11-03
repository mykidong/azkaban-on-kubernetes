package io.mykidong.util;

import org.jasypt.util.text.AES256TextEncryptor;

public class EncryptionUtils {

    private static AES256TextEncryptor getEncryptor(String encryptionKey) {
        AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
        textEncryptor.setPassword(encryptionKey);
        return textEncryptor;
    }

    public static String encrypt(String encryptionKey, String text) {
        AES256TextEncryptor textEncryptor = getEncryptor(encryptionKey);
        return textEncryptor.encrypt(text);
    }

    public static String decrypt(String encryptionKey, String text) {
        AES256TextEncryptor textEncryptor = getEncryptor(encryptionKey);
        return textEncryptor.decrypt(text);
    }

    public static String encyptAndEncodeBase64(String encryptionKey, String text) {
        String encrypted = encrypt(encryptionKey, text);
        return StringUtils.base64Encode(encrypted);
    }

    public static String decodeBase64AndDecrypt(String encryptionKey, String text) {
        String base64Decoded = StringUtils.base64Decode(text);
        return decrypt(encryptionKey, base64Decoded);
    }
}
