package com.hanhuy.android.bluetooth.keyguard;

import com.google.common.io.BaseEncoding;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;

public class CryptoUtils {
    private final static String OBFUSCATION_KEY =
            "com.hanhuy.android.bluetooth.keyguard.OBFUS_KEY";
    private final static String SALT = "0123456789ABCDEF";
    private final static BaseEncoding BASE16 = BaseEncoding.base16();
    private final static SecretKey KEY;
    private final static String ALG = "AES";
    private final static String CIPHER_ALG = ALG + "/CBC/PKCS5Padding";
    private final static MessageDigest SHA1;

    static {
        try {
            SecretKeyFactory fac = SecretKeyFactory.getInstance(
                    "PBKDF2WithHmacSHA1");
            PBEKeySpec spec = new PBEKeySpec(
                    OBFUSCATION_KEY.toCharArray(), SALT.getBytes("utf-8"), 1000, 128);
            SecretKey k = fac.generateSecret(spec);
            KEY = new SecretKeySpec(k.getEncoded(), ALG);
            SHA1 = MessageDigest.getInstance("SHA1");
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

    }

    public static synchronized String hmac(String input) {
        try {
            byte[] digest = SHA1.digest(input.getBytes("utf-8"));
            return BASE16.encode(digest);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String encrypt(String input) {
        try {
            Cipher c = Cipher.getInstance(CIPHER_ALG);
            c.init(Cipher.ENCRYPT_MODE, KEY, new SecureRandom());
            return BASE16.encode(c.getParameters().getEncoded()) + ":" +
                    BASE16.encode(c.doFinal(input.getBytes("utf-8")));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String decrypt(String input) {
        try {
            String[] inp = input.split(":");
            byte[] params = BASE16.decode(inp[0]);
            byte[] encrypted = BASE16.decode(inp[1]);
            AlgorithmParameters p = AlgorithmParameters.getInstance(ALG);
            p.init(params);
            Cipher c = Cipher.getInstance(CIPHER_ALG);
            c.init(Cipher.DECRYPT_MODE, KEY, p);
            return new String(c.doFinal(encrypted));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
