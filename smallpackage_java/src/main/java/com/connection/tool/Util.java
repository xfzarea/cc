package com.connection.tool;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;

import javazoom.jl.converter.Converter;
import javazoom.jl.decoder.JavaLayerException;

public class Util {
	public static boolean Mp3ToWav(String inputFilePath, String outputFilePath){
		Converter aConverter = new Converter();
			try { aConverter.convert(inputFilePath, outputFilePath);
		} catch (JavaLayerException e)
			{ e.printStackTrace();
			return false;
			}
			return true;
		}
	
	
	//oss�ϴ�byte
		public static void ossLoadByte(String endPoint,String accessKeyId,String accessKeySecret,String bucketName,
				String keySuffixWithSlash,String content){
			// ����OSSClientʵ��
			OSSClient ossClient = new OSSClient(endPoint, accessKeyId, accessKeySecret);
			// �ϴ��ַ���
			try {
				ossClient.putObject(bucketName, keySuffixWithSlash, new ByteArrayInputStream(content.getBytes("utf8")));
			} catch (OSSException e) {
				
				e.printStackTrace();
			} catch (ClientException e) {
				
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				
				e.printStackTrace();
			}
			// �ر�client
			ossClient.shutdown();
		}
			
		//oss�ϴ��ļ�
		public static void ossLoad(String endPoint,String accessKeyId,String accessKeySecret,String bucketName,
				String keySuffixWithSlash,InputStream inputStream ) throws FileNotFoundException{
			// ����OSSClientʵ��
			OSSClient ossClient = new OSSClient(endPoint, accessKeyId, accessKeySecret);
			ossClient.putObject(bucketName, keySuffixWithSlash, inputStream);
			// �ر�client
			ossClient.shutdown();
		}
		
		public static boolean delAllFile(String path) {
		       boolean flag = false;
		       File file = new File(path);
		       if (!file.exists()) {
		         return flag;
		       }
		       if (!file.isDirectory()) {
		         return flag;
		       }
		       String[] tempList = file.list();
		       File temp = null;
		       for (int i = 0; i < tempList.length; i++) {

		          if (path.endsWith(File.separator)) {
		             temp = new File(path + tempList[i]);
		          } else {
		              temp = new File(path + File.separator + tempList[i]);
		          }
		          if (temp.isFile()) {
		             temp.delete();
		          }
		       }
		       file = null;
		       return flag;
		     }
		
	/**
	 * ΢�ŷ�����Ϣ������Ϣ����������ģ�壩
	 */
	public static String getMsg(Map<String,Object>returnParam,Map<String,Object>job,int type){
		
		Map<String,Object>params = new HashMap<String,Object>();
		Map<String,Object>data = new HashMap<String,Object>();
		
		params.put("touser", returnParam.get("openid"));//��openid��
		
		params.put("page", "pages/package/package?id="+job.get("id"));//��һ�����
		params.put("form_id", returnParam.get("formid"));
		
		Map<String,String>keyWord1 = new HashMap<String,String>();
		Map<String,String>keyWord2 = new HashMap<String,String>();
		Map<String,String>keyWord3 = new HashMap<String,String>();
		
		if(type==1){
			params.put("template_id", "TlDAB-fRWRyodj-OiKWaiQM1mYsZxxoLJAWrxmjAucI");//����ģ��
			keyWord1.put("value", "��������"+job.get("context"+"����24Сʱδ����ȡ��"));
			keyWord1.put("color", "#FF4040");//��һ������
			data.put("keyword1", keyWord1);
			
			keyWord2.put("value", job.get("refundAward")+"Ԫ");//��sqljob�ࣨ����������ж��壩
			keyWord2.put("color", "#FF4040");//��һ������
			data.put("keyword2", keyWord2);
			
			keyWord3.put("value", "С�������˵�˺����");
			keyWord3.put("color", "#FF4040");//�ڶ�������
			data.put("keyword3", keyWord3);
		}else if(type == 2){
			params.put("template_id", "EAWqNS9FQP3skjrFrVQSvCuICwUWi0nafCQPQBRmvCE");//���ģ��
			keyWord1.put("value", "��������");
			keyWord1.put("color", "#FF4040");//��һ������
			data.put("keyword1", keyWord1);
			
			keyWord2.put("value", job.get("context")+"");
			keyWord2.put("color", "#FF4040");//�ڶ�������
			data.put("keyword2", keyWord2);
			
			keyWord3.put("value", "����˴��������ѵ�����");
			keyWord3.put("color", "#FF4040");//����������
			data.put("keyword3", keyWord3);
		}else if(type == 3){
			params.put("template_id", "p8w_7Xqdz_WxZ1W0ShuBCVGnuH43aKrBbBicRgpSpxU");//����ģ��
			keyWord1.put("value", job.get("createTime")+"");
			keyWord1.put("color", "#FF4040");//��һ������
			data.put("keyword1", keyWord1);
			keyWord2.put("value", "�������");
			keyWord2.put("color", "#FF4040");//�ڶ�������
			data.put("keyword2", keyWord2);
		}else if(type == 4){
			params.put("page", "pages/begPackage/begPackage?id="+job.get("id")+"&handType=2&uid="+job.get("uid"));
			params.put("template_id", "tOmEuaDa6ttG-rNp7ItmavCof8oVioUJJ1bzkyBEPz0");//������֧������
			
			keyWord1.put("value", job.get("award")+"Ԫ");
			keyWord1.put("color", "#FF4040");//��һ������
			
			data.put("keyword1", keyWord1);
			keyWord2.put("value", "��˴˴��鿴�������");
			keyWord2.put("color", "#FF4040");//�ڶ�������
			data.put("keyword2", keyWord2);
			
			keyWord3.put("value", job.get("createTime")+"");
			keyWord3.put("color", "#FF4040");//����������
			data.put("keyword3", keyWord3);
		}else if(type == 5){//��л��
			params.put("page", "pages/begPackage/begPackage?id="+job.get("id")+"&handType=3");
			params.put("template_id", "HPtpNqEjA7RGuM9axyg4sFFiB81_WQFm-WuVq_I8Kj4");//��л��
			
			keyWord1.put("value", "�յ�һ����Ѹ�л��");
			keyWord1.put("color", "#FF4040");//��һ������
			data.put("keyword1", keyWord1);
			keyWord2.put("value", job.get("nickName")+"");
			keyWord2.put("color", "#FF4040");//�ڶ�������
			data.put("keyword2", keyWord2);
		}
		
		params.put("data", data);
		params.put("emphasis_keyword", "");
		String json = JSONObject.toJSONString(params);
		params = null;
		data = null;
		keyWord1 = null;
		keyWord2 = null;
		keyWord3 = null;
		return json;
	}
	
	
	
	public static String getIpAddress(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
}
