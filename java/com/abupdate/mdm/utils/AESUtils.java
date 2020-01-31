package com.abupdate.mdm.utils;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/*
 * @date   : 2019/09/20
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class AESUtils {
    private static final String ALGORITHM_SHORT_NAME = "AES";
    private static final String ALGORITHM_LONG_NAME = "AES/CFB/NoPadding";//使用CFB加密，需要设置IV
    private static final int IV_LENGTH = 16; //16 =>128, 24 => 192, 32 => 256
    private static final String PASSWORD = String.valueOf(new char[]{'a', 'e', 's',  '_', 'l', 'o', 'c', 'k', '_', 'p', 'a', 's', 's', 'w', 'o', 'r', 'd'});//"aes_lock_password";

    private static SecretKeySpec genKeySpec(String password) {
        if (password == null) {
            password = "";
        }
        StringBuilder sb = new StringBuilder(IV_LENGTH);
        sb.append(password);
        while (sb.length() < IV_LENGTH) {
            sb.append("0");
        }

        if (sb.length() > IV_LENGTH) {
            sb.setLength(IV_LENGTH);
        }

        byte[] data = null;
        try {
            data = sb.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return new SecretKeySpec(data, ALGORITHM_SHORT_NAME);
    }

    /**解密
     * @param content  待解密内容
     * @param password 解密密钥
     * @return
     * @throws Exception
     */
    private static byte[] decrypt(byte[] content, String password) throws Exception {
        SecretKeySpec key = genKeySpec(password);
        Cipher cipher = Cipher.getInstance(ALGORITHM_LONG_NAME);
        cipher.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(
                new byte[cipher.getBlockSize()]));
        return cipher.doFinal(content); // 解密
    }

    /**
     * 加密
     *
     * @param content 需要加密的内容
     * @param password  加密密码
     * @return
     * @throws NoSuchProviderException
     */
    private static byte[] encrypt(String content, String password) throws Exception {
        SecretKeySpec key = genKeySpec(password);
        Cipher cipher = Cipher.getInstance(ALGORITHM_LONG_NAME);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(
                new byte[cipher.getBlockSize()]));
        return cipher.doFinal(content.getBytes("utf-8")); // 加密
    }

    public static String encrypt(String content) {
        try {
            byte[] encrypt = encrypt(content, PASSWORD);
            return Base64Utils.encode(encrypt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String decrypt(String content) {
        try {
            byte[] decryptFrom = Base64Utils.decode(content);
            byte[] decryptResult = decrypt(decryptFrom, PASSWORD);
            return new String(decryptResult,"utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}