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
	
	
	//oss上传byte
		public static void ossLoadByte(String endPoint,String accessKeyId,String accessKeySecret,String bucketName,
				String keySuffixWithSlash,String content){
			// 创建OSSClient实例
			OSSClient ossClient = new OSSClient(endPoint, accessKeyId, accessKeySecret);
			// 上传字符串
			try {
				ossClient.putObject(bucketName, keySuffixWithSlash, new ByteArrayInputStream(content.getBytes("utf8")));
			} catch (OSSException e) {
				
				e.printStackTrace();
			} catch (ClientException e) {
				
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				
				e.printStackTrace();
			}
			// 关闭client
			ossClient.shutdown();
		}
			
		//oss上传文件
		public static void ossLoad(String endPoint,String accessKeyId,String accessKeySecret,String bucketName,
				String keySuffixWithSlash,InputStream inputStream ) throws FileNotFoundException{
			// 创建OSSClient实例
			OSSClient ossClient = new OSSClient(endPoint, accessKeyId, accessKeySecret);
			ossClient.putObject(bucketName, keySuffixWithSlash, inputStream);
			// 关闭client
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
	 * 微信服务消息发送消息
	 */
	public static String getMsg(Map<String,Object>returnParam,Map<String,Object>job,int type){
		
		Map<String,Object>params = new HashMap<String,Object>();
		Map<String,Object>data = new HashMap<String,Object>();
		
		params.put("touser", returnParam.get("openid"));
		
		params.put("page", "pages/package/package?id="+job.get("id"));//第一种情况
		params.put("form_id", returnParam.get("formid"));
		
		
		Map<String,String>keyWord1 = new HashMap<String,String>();
		Map<String,String>keyWord2 = new HashMap<String,String>();
		if(type==1){
			params.put("template_id", "0hOirGlO7QoQdx4-jSLnWPZN3tMNfFiW6b7jAkHWjvI");//过期模板
			
			keyWord1.put("value", job.get("createTime")+"");
			keyWord1.put("color", "#FF4040");//第一个数据
			data.put("keyword1", keyWord1);
			keyWord2.put("value", "红包超过24小时未被领取");
			keyWord2.put("color", "#FF4040");//第二个数据
			data.put("keyword2", keyWord2);
		}else if(type == 2){
			params.put("template_id", "471EChHAUE3JHSlRqW6YUscknL0huk1luBsISCzgv6Q");//完成模板
			keyWord1.put("value", job.get("totalCount")+"");
			keyWord1.put("color", "#FF4040");//第一个数据
			data.put("keyword1", keyWord1);
			
			keyWord2.put("value", "点击查看黄金红包领取详情");
			keyWord2.put("color", "#FF4040");//第一个数据
			data.put("keyword2", keyWord2);
		}else{
			params.put("template_id", "p8w_7Xqdz_WxZ1W0ShuBCVGnuH43aKrBbBicRgpSpxU");//订阅模板
			keyWord1.put("value", job.get("createTime")+"");
			keyWord1.put("color", "#FF4040");//第一个数据
			data.put("keyword1", keyWord1);
			keyWord2.put("value", "红包开抢");
			keyWord2.put("color", "#FF4040");//第二个数据
			data.put("keyword2", keyWord2);
		}
		
		params.put("data", data);
		params.put("emphasis_keyword", "");
		String json = JSONObject.toJSONString(params);
		params = null;
		data = null;
		keyWord1 = null;
		keyWord2 = null;
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
