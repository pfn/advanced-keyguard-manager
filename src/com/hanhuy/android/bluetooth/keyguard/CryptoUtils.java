package com.hanhuy.android.bluetooth.keyguard;

import android.content.Context;
import android.util.Log;
import com.google.common.io.BaseEncoding;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class CryptoUtils {
    private final static String TAG = "CryptoUtils";
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
            PBEKeySpec spec = new PBEKeySpec(OBFUSCATION_KEY.toCharArray(),
                    SALT.getBytes("utf-8"), 1000, 128);
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
            if (inp.length < 2)
                return null;

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

    public static boolean isPIN(Context c) {
        boolean isPIN = false;
        Settings s = Settings.getInstance(c);
        String saved = s.get(Settings.PASSWORD);
        try {
            if (saved != null) {
                String pw = decrypt(saved);
                if (pw != null) {
                    Integer.parseInt(decrypt(saved));
                    isPIN = true;
                }
            }
        } catch (NumberFormatException e) {  } // ignore

        return isPIN;
    }

    public static boolean verifyPassword(Context c, String pass) {
        // have to decrypt saved password: encrypted is different each time
        Settings s = Settings.getInstance(c);
        String saved = s.get(Settings.PASSWORD);
        String decrypted = saved == null ? null : decrypt(saved);
        return decrypted == null || decrypted.equals(pass);
    }

    public static boolean isPasswordSaved(Context c) {
        Settings s = Settings.getInstance(c);
        boolean isSaved = false;
        String saved = s.get(Settings.PASSWORD);
        String hash = s.get(Settings.PASSWORD_HASH);
        if (saved != null && hash != null) {
            String pass = decrypt(saved);
            isSaved = hmac(pass).equals(hash);
        }
        return isSaved;
    }

    public static String getPassword(Context c) {
        Settings s = Settings.getInstance(c);
        boolean isSaved = false;
        String saved = s.get(Settings.PASSWORD);
        String hash = s.get(Settings.PASSWORD_HASH);
        if (saved != null && hash != null) {
            String pass = decrypt(saved);
            if (hmac(pass).equals(hash))
                return pass;
        }
        return null;
    }
}
