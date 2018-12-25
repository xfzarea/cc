package com.connection.xunfei.yuyintingxie;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import com.alibaba.fastjson.JSON;
import com.connection.tool.PinYinUtil;
import com.sun.xml.internal.fastinfoset.Decoder;

import sun.nio.cs.UnicodeEncoder;

	public class TestWebIat {
		// ��дwebapi�ӿڵ�ַ
	private static final String WEBIAT_URL = "http://api.xfyun.cn/v1/service/v1/iat";
	// ����Ӧ��ID
	private static final String TEST_APPID = "5bda9a52";
	// ���Խӿ���Կ
	private static final String TEST_API_KEY = "d2b3b2eec6cc60760c783472ef1b30f8";
	// ������Ƶ�ļ����λ��
	
	private static final String AUDIO_FILE_PATH = "E:/10.wav";

	/**
	 * ��װhttp����ͷ
	 * 
	 * @param aue
	 * @param engineType
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws ParseException
	 */
	private static Map<String, String> constructHeader(String aue, String engineType,String appid,String secret) throws UnsupportedEncodingException, ParseException {
		// ϵͳ��ǰʱ���
		String X_CurTime = System.currentTimeMillis() / 1000L + "";
		// ҵ�����
		String param = "{\"aue\":\""+aue+"\""+",\"engine_type\":\"" + engineType + "\"}";
		String X_Param = new String(Base64.encodeBase64(param.getBytes("UTF-8")));
		// �ӿ���Կ
		String apiKey = secret;
		// Ѷ�ɿ���ƽ̨Ӧ��ID
		String X_Appid = appid;
		// ��������
		String X_CheckSum = DigestUtils.md5Hex(apiKey + X_CurTime + X_Param);

		// ��װ����ͷ
		Map<String, String> header = new HashMap<String, String>();
		header.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
		header.put("X-Param", X_Param);
		header.put("X-CurTime", X_CurTime);
		header.put("X-CheckSum", X_CheckSum);
		header.put("X-Appid", X_Appid);
		return header;
	}
	
	
	public static String parse(String file,String appid,String secret) throws ParseException, IOException{
		Map<String, String> header = constructHeader("raw", "sms16k",appid,secret);
		// ��ȡ��Ƶ�ļ���ת���������飬Ȼ��Base64����
		byte[] audioByteArray = FileUtil.read2ByteArray(file);
		String audioBase64 = new String(Base64.encodeBase64(audioByteArray), "UTF-8");
		String bodyParam = "audio=" + audioBase64;
		String result = HttpUtil.doPost(WEBIAT_URL, header, bodyParam);
		Map<String, Object>r = JSON.parseObject(result);
		String data = (String)r.get("data");
		String data1 = PinYinUtil.getFullSpell(data);
		return data1;
	}

	public static String format(String s){ 
		String str=s.replaceAll("[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~��@#��%����& amp;*��������+|{}������������������������|-]", "");
		return str;
	}
	
	public static String toChinese(String string) {
        String[] s1 = { "��", "һ", "��", "��", "��", "��", "��", "��", "��", "��" };
        String[] s2 = { "ʮ", "��", "ǧ", "��", "ʮ", "��", "ǧ", "��", "ʮ", "��", "ǧ" };

        String result = "";

        int n = string.length();
        for (int i = 0; i < n; i++) {

            int num = string.charAt(i) - '0';

            if (i != n - 1 && num != 0) {
                result += s1[num] + s2[n - 2 - i];
            } else {
                result += s1[num];
            }
        }
        return result;
    }
	
	public static void main(String[] args) throws Exception{
		Map<String, String> header = constructHeader("raw", "sms16k","5bda9a52","d2b3b2eec6cc60760c783472ef1b30f8");
		// ��ȡ��Ƶ�ļ���ת���������飬Ȼ��Base64����
		byte[] audioByteArray = FileUtil.read2ByteArray(AUDIO_FILE_PATH);
		String audioBase64 = new String(Base64.encodeBase64(audioByteArray), "UTF-8");
		String bodyParam = "audio=" + audioBase64;
		String result = HttpUtil.doPost(WEBIAT_URL, header, bodyParam);
		
		Map<String, Object>r = JSON.parseObject(result);
		System.out.println(r);
		String data = (String)r.get("data");
		
		data = format(data);
		
		String data1 = PinYinUtil.getFullSpell(data);
		System.out.println(data1);
		String data2 = PinYinUtil.getFullSpell("ʮһ�¿���");
		System.out.println(data2);
		System.out.println(toChinese("21"));
	}
}

