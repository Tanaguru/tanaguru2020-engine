package com.tanaguru.helper;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESEncrypt {

    public static String encrypt(String message, String key) {
        try{
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] cryptedValue = cipher.doFinal(message.getBytes());
            return Base64.getEncoder().encodeToString(cryptedValue);
        }catch (Exception e){
            throw new IllegalStateException(e.getMessage());
        }
    }

    public static String decrypt(String encryptedMessage, String key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedValue = Base64.getDecoder().decode(encryptedMessage);
            return new String(cipher.doFinal(decodedValue));
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }
    }
}
