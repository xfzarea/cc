package com.connection.wxPay.util;



import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Encrypt {
    public static boolean initialized = false;  

    public static final String ALGORITHM = "AES/ECB/PKCS7Padding";  

    /** 
     * @param  String str  Ҫ�����ܵ��ַ��� 
     * @param  byte[] key  ��/����Ҫ�õĳ���Ϊ32���ֽ����飨256λ����Կ 
     * @return byte[]  ���ܺ���ֽ����� 
     */  
    public static byte[] Aes256Encode(String str, byte[] key){  
        initialize();  
        byte[] result = null;  
        try{  
            Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");  
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES"); //���ɼ��ܽ�����Ҫ��Key  
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);  
            result = cipher.doFinal(str.getBytes("UTF-8"));  
        }catch(Exception e){  
            e.printStackTrace();  
        }  
        return result;  
    }  

    /** 
     * @param  byte[] bytes  Ҫ�����ܵ��ֽ����� 
     * @param  byte[] key    ��/����Ҫ�õĳ���Ϊ32���ֽ����飨256λ����Կ 
     * @return String  ���ܺ���ַ��� 
     */  
    public static String Aes256Decode(byte[] bytes, byte[] key){  
        initialize();  
        String result = null;  
        try{  
            Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");  
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES"); //���ɼ��ܽ�����Ҫ��Key  
            cipher.init(Cipher.DECRYPT_MODE, keySpec);  
            byte[] decoded = cipher.doFinal(bytes);  
            result = new String(decoded, "UTF-8");  
        }catch(Exception e){  
            e.printStackTrace();  
        }  
        return result;  
    }  

    public static void initialize(){  
        if (initialized) return;  
        //Security.addProvider(new BouncyCastleProvider());  
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); 
        initialized = true;  
    }  
}