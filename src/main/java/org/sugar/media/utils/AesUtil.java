package org.sugar.media.utils;

import cn.hutool.core.lang.Console;
import cn.hutool.crypto.symmetric.AES;


/**
 * Date:2025/03/05 14:41:08
 * Author：Tobin
 * Description:
 */
public class AesUtil {

    private static String aes_key = "0123456789ABHAEQ";
    private static String aes_iv = "DYgjCEIMVrj2W9xN";

    public static String aesEncrypt(String content) {


        AES aes = new AES("CBC", "PKCS7Padding",
                // 密钥，可以自定义
                aes_key.getBytes(),
                // iv加盐，按照实际需求添加
                aes_iv.getBytes());
//加密
//        byte[] encrypt = aes.encrypt(content);
////解密
//        byte[] decrypt = aes.decrypt(encrypt);

//加密为16进制表示
        String encryptHex = aes.encryptHex(content);
        Console.log(encryptHex);

        return encryptHex;
    }

    public static String aesDecrypt(String encryptedHex) {
        AES aes = new AES("CBC", "PKCS7Padding",
                // 密钥，可以自定义
                aes_key.getBytes(),
                // iv加盐，按照实际需求添加
                aes_iv.getBytes());

        // 解密16进制字符串并转为原始文本
        byte[] decryptedBytes = aes.decrypt(encryptedHex);
        String decryptedContent = new String(decryptedBytes);
        Console.log("Decrypted Content: " + decryptedContent);

        return decryptedContent;
    }

}
