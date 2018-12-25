package com.connection.wxPay.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;

public class MyWXPayUtil {

    public static String getRefundDecrypt(String reqInfoSecret, String key) {
        String result = "";
        try {
            Security.addProvider(new BouncyCastleProvider());
            sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
            byte[] bt = decoder.decodeBuffer(reqInfoSecret);
//            byte[] bt = Base64Utils.decode(reqInfoSecret);
            String md5key = MD5Util.MD5Encode(key, "utf8").toLowerCase();
            SecretKey secretKey = new SecretKeySpec(md5key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] resultbt = cipher.doFinal(bt);
            result = new String(resultbt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) {
        String A = "igoAhzACg7oeXby06Jn9gDMt45nN3YWp09kLgnXortCZn1GdcEPVdtvchNEa72c/L6h5KJh0fNfMB6+vRdojQW5NyoEWqgx/KFhDxV1+6ZZOV+cizt5be67pCG0Bgpy02rRgtTGSa+cQctLjU0o5RyTjpcpo/7glrICbGBmCq6B796S+kBfiXyq2Dwk1mMvi9xqbC3Y2czjlZVhUUpHchv7tW9WZhSAgDbUYVrdLw+zyWcbmaDhVYWrpkJVauM265jnXpq9+aVpwMk0HOzmYs0aONQu2MWN1daVTBOBe+vEnoP1mIFxQu7MZ+FuJvpIC2w6l7hETmjZKBLZi/ci+lGyX6dr7icTwpBmbzzhcNtGi9ei2Vl77KeXCERpp98yzcy5R4sfnzCBPIOsqtYv7mUz9VNsaTajLvQc5jxW+Tz4wIaE0cOfV2/uSGXimEAvxfto0vKQ9jZ5UwjVSjGbSUD+8ajumMBgzNVsCkMhJDuMR066FbjASAPILiFmgpEowRE3pGy0xcjJ3OJk5rAvbmosIgbShdfSAt2bqX8I5k1mYLc1vqH2tXWxrBa9kGfLV/0ovdq2X/60jjKOAuDCLJ2RhStXUu3xmQxSfC6kKPtmVgOIcF7RIOiSIO4URgVNczv7Ys2fZ+CEgC5eE6xGJqeXbvHiR+8XHMojYvdH3Va7QBWAoyTCpY0Nz62cy7U0NYY6CPEI+OD5O3Lmt+a01fyJFWpnMnVEGWHruK0d3/Mbg5R+UPkib1FtEqzIfwUlNtJoqjfoxogDJLYDdktuoBn7TqDZM8g/v7b85a+dTHuwRucSbUSPL+asBwFIErg6xgjg0kgLpQrIBvU87yu1+7vLoam3Z8Cv3GwJVG55HeJ2Rg79TViaN/XzIn4+IRL9I6DxBMKQ8HEiUhCieTI06tLdDHbwesVM+KkbP+q5ujvnXcvQ3FOYrzL/g/wN4iaP14/wNRPl1M3i8vReVI92mJX1nlMA9XwBjyTd3V9FzzdDJtxY3kFDv0d5Qg60MI4WpNh5xFSA3/4/M77zZC2SaD2uE1mCy/OiF9YSaVnltywCevKiTcREHjgVOd1ngqt/D";
        String key = "Xingbang80301baodating360shengli";
        String B = getRefundDecrypt(A, key);
        System.out.println(B);
    }
}