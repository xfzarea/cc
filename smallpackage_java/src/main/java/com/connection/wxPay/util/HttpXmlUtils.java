package com.connection.wxPay.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import com.connection.wxPay.entity.Unifiedorder;



/**
 * post提交xml格式的参数
 * @author iYjrg_xiebin
 * @date 2015年11月25日下午3:33:38
 */
public class HttpXmlUtils {

	/**
	 * 开始post提交参数到接口
	 * 并接受返回
	 * @param url
	 * @param xml
	 * @param method
	 * @param contentType
	 * @return
	 */
	public static String xmlHttpProxy(String url,String xml,String method,String contentType){
		InputStream is = null;
		OutputStreamWriter os = null;

		try {
			URL _url = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) _url.openConnection();
			conn.setDoInput(true);   
			conn.setDoOutput(true);   
			conn.setRequestProperty("Content-type", "text/xml");
			conn.setRequestProperty("Pragma:", "no-cache");  
			conn.setRequestProperty("Cache-Control", "no-cache");  
			conn.setRequestMethod("POST");
			os = new OutputStreamWriter(conn.getOutputStream());
			os.write(new String(xml.getBytes(contentType)));
			os.flush();

			//返回值
			is = conn.getInputStream();
			return getContent(is, "utf-8");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {
				if(os!=null){os.close();}
				if(is!=null){is.close();}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 解析返回的值
	 * @param is
	 * @param charset
	 * @return
	 */
	public static String getContent(InputStream is, String charset) {
		String pageString = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		StringBuffer sb = null;
		try {
			isr = new InputStreamReader(is, charset);
			br = new BufferedReader(isr);
			sb = new StringBuffer();
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			pageString = sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null){
					is.close();
				}
				if(isr!=null){
					isr.close();
				}
				if(br!=null){
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			sb = null;
		}
		return pageString;
	}

	/**
	 * 构造xml参数
	 * @param xml
	 * @return
	 */
	public static String xmlInfo(Unifiedorder unifiedorder){
		//构造xml参数的时候，至少又是个必传参数
		/*
		 * <xml>
			   <appid>wx2421b1c4370ec43b</appid>
			   <attach>支付测试</attach>
			   <body>JSAPI支付测试</body>
			   <mch_id>10000100</mch_id>
			   <nonce_str>1add1a30ac87aa2db72f57a2375d8fec</nonce_str>
			   <notify_url>http://wxpay.weixin.qq.com/pub_v2/pay/notify.v2.php</notify_url>
			   <openid>oUpF8uMuAJO_M2pxb1Q9zNjWeS6o</openid>
			   <out_trade_no>1415659990</out_trade_no>
			   <spbill_create_ip>14.23.150.211</spbill_create_ip>
			   <total_fee>1</total_fee>
			   <trade_type>JSAPI</trade_type>
			   <sign>0CB01533B8C1EF103065174F50BCA001</sign>
			</xml>
		 */

		if(unifiedorder!=null){
			StringBuffer bf = new StringBuffer();
			bf.append("<xml>");

			bf.append("<appid><![CDATA[");
			bf.append(unifiedorder.getAppid());
			bf.append("]]></appid>");

			bf.append("<mch_id><![CDATA[");
			bf.append(unifiedorder.getMch_id());
			bf.append("]]></mch_id>");

			bf.append("<nonce_str><![CDATA[");
			bf.append(unifiedorder.getNonce_str());
			bf.append("]]></nonce_str>");

			bf.append("<sign><![CDATA[");
			bf.append(unifiedorder.getSign());
			bf.append("]]></sign>");

			bf.append("<body><![CDATA[");
			bf.append(unifiedorder.getBody());
			bf.append("]]></body>");

			bf.append("<detail><![CDATA[");
			bf.append(unifiedorder.getDetail());
			bf.append("]]></detail>");

			bf.append("<attach><![CDATA[");
			bf.append(unifiedorder.getAttach());
			bf.append("]]></attach>");

			bf.append("<out_trade_no><![CDATA[");
			bf.append(unifiedorder.getOut_trade_no());
			bf.append("]]></out_trade_no>");

			bf.append("<total_fee><![CDATA[");
			bf.append(unifiedorder.getTotal_fee());
			bf.append("]]></total_fee>");

			bf.append("<spbill_create_ip><![CDATA[");
			bf.append(unifiedorder.getSpbill_create_ip());
			bf.append("]]></spbill_create_ip>");

			bf.append("<time_start><![CDATA[");
			bf.append(unifiedorder.getTime_start());
			bf.append("]]></time_start>");

			bf.append("<time_expire><![CDATA[");
			bf.append(unifiedorder.getTime_expire());
			bf.append("]]></time_expire>");

			bf.append("<notify_url><![CDATA[");
			bf.append(unifiedorder.getNotify_url());
			bf.append("]]></notify_url>");
			
			bf.append("<openid><![CDATA[");
			bf.append(unifiedorder.getOpenid());
			bf.append("]]></openid>");

			bf.append("<trade_type><![CDATA[");
			bf.append(unifiedorder.getTrade_type());
			bf.append("]]></trade_type>");


			bf.append("</xml>");
			return bf.toString();
		}

		return "";
	}


	/**
	 * 微信退款 构造xml参数
	 * @param xml
	 * @return
	 */
	public static String xmlInfo(Map<Object,Object>param){
		//构造xml参数的时候，至少又是个必传参数
		/*
		 * <xml>
   <appid>wx2421b1c4370ec43b</appid>
   <mch_id>10000100</mch_id>
   <nonce_str>6cefdb308e1e2e8aabd48cf79e546a02</nonce_str> 
   <out_refund_no>1415701182</out_refund_no>
   <out_trade_no>1415757673</out_trade_no>
   <refund_fee>1</refund_fee>
   <total_fee>1</total_fee>
   <transaction_id></transaction_id>
   <sign>FE56DD4AA85C0EECA82C35595A69E153</sign>
</xml>

<xml>
	<appid><![CDATA[wxfa38d01e0f812925]]></appid>
	<mch_id><![CDATA[1509656321]]></mch_id>
	<nonce_str><![CDATA[g2bu0JQpMSNOSNb5vo8Ku18fqD8Coljd]]></nonce_str>
	<sign><![CDATA[3B487CE69A2F8CD6A3029FE24E83EE6D]]></sign>
	<out_refund_no><![CDATA[yhdy20180912110200566]]></out_refund_no>
	<out_trade_no><![CDATA[null]]></out_trade_no>
	<refund_fee><![CDATA[100.0]]></refund_fee>
	<total_fee><![CDATA[100.0]]></total_fee>
	<transaction_id><![CDATA[null]]></transaction_id>
	<notify_url><![CDATA[https://www.yaohoudy.com/connection/weixinRefund]]></notify_url>
</xml>
		 */

		if(param!=null){
			StringBuffer bf = new StringBuffer();
			bf.append("<xml>");
			
			bf.append("<appid><![CDATA[");
			bf.append(param.get("appid"));
			bf.append("]]></appid>");
			
			bf.append("<mch_id><![CDATA[");
			bf.append(param.get("mch_id"));
			bf.append("]]></mch_id>");

			bf.append("<nonce_str><![CDATA[");
			bf.append(param.get("nonce_str"));
			bf.append("]]></nonce_str>");

			bf.append("<sign><![CDATA[");
			bf.append(param.get("sign"));
			bf.append("]]></sign>");

			bf.append("<out_refund_no><![CDATA[");
			bf.append(param.get("out_refund_no"));
			bf.append("]]></out_refund_no>");

			bf.append("<out_trade_no><![CDATA[");
			bf.append(param.get("out_trade_no"));
			bf.append("]]></out_trade_no>");

			bf.append("<refund_fee><![CDATA[");
			bf.append(param.get("refund_fee"));
			bf.append("]]></refund_fee>");

			bf.append("<total_fee><![CDATA[");
			bf.append(param.get("total_fee"));
			bf.append("]]></total_fee>");

			bf.append("<transaction_id><![CDATA[");
			bf.append(param.get("transaction_id"));
			bf.append("]]></transaction_id>");


			bf.append("<notify_url><![CDATA[");
			bf.append(param.get("notify_url"));
			bf.append("]]></notify_url>");
			
			
			bf.append("</xml>");
			return bf.toString();
		}

		return "";
	}

	
	/**
	 * 微信退款 构造xml参数
	 * @param xml
	 * @return
	 */
	public static String createPayXml(Map<Object,Object>param){
//		<xml>
//
//		<mch_appid>wxe062425f740c30d8</mch_appid>
//
//		<mchid>10000098</mchid>
//
//		<nonce_str>3PG2J4ILTKCH16CQ2502SI8ZNMTM67VS</nonce_str>
//
//		<partner_trade_no>100000982014120919616</partner_trade_no>
//
//		<openid>ohO4Gt7wVPxIT1A9GjFaMYMiZY1s</openid>
//
//		<check_name>FORCE_CHECK</check_name>
//
//		<re_user_name>张三</re_user_name>
//
//		<amount>100</amount>
//
//		<desc>节日快乐!</desc>
//
//		<spbill_create_ip>10.2.3.10</spbill_create_ip>
//
//		<sign>C97BDBACF37622775366F38B629F45E3</sign>
//
//		</xml>

		if(param!=null){
			StringBuffer bf = new StringBuffer();
			bf.append("<xml>");
			
			bf.append("<mch_appid><![CDATA[");
			bf.append(param.get("mch_appid"));
			bf.append("]]></mch_appid>");
			
			bf.append("<mchid><![CDATA[");
			bf.append(param.get("mchid"));
			bf.append("]]></mchid>");

			bf.append("<nonce_str><![CDATA[");
			bf.append(param.get("nonce_str"));
			bf.append("]]></nonce_str>");

			bf.append("<partner_trade_no><![CDATA[");
			bf.append(param.get("partner_trade_no"));
			bf.append("]]></partner_trade_no>");

			bf.append("<openid><![CDATA[");
			bf.append(param.get("openid"));
			bf.append("]]></openid>");

			bf.append("<check_name><![CDATA[");
			bf.append(param.get("check_name"));
			bf.append("]]></check_name>");

			bf.append("<amount><![CDATA[");
			bf.append(param.get("amount"));
			bf.append("]]></amount>");

			bf.append("<desc><![CDATA[");
			bf.append(param.get("desc"));
			bf.append("]]></desc>");

			bf.append("<spbill_create_ip><![CDATA[");
			bf.append(param.get("spbill_create_ip"));
			bf.append("]]></spbill_create_ip>");


			bf.append("<sign><![CDATA[");
			bf.append(param.get("sign"));
			bf.append("]]></sign>");
			
			
			bf.append("</xml>");
			return bf.toString();
		}

		return "";
	}
	
	/**
	 * post请求并得到返回结果
	 * @param requestUrl
	 * @param requestMethod
	 * @param output
	 * @return
	 */
	public static String httpsRequest(String requestUrl, String requestMethod, String output) {
		try{
			URL url = new URL(requestUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setRequestMethod(requestMethod);
			if (null != output) {
				OutputStream outputStream = connection.getOutputStream();
				outputStream.write(output.getBytes("UTF-8"));
				outputStream.close();
			}
			// 从输入流读取返回内容
			InputStream inputStream = connection.getInputStream();
			
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String str = null;
			StringBuffer buffer = new StringBuffer();
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
			bufferedReader.close();
			inputStreamReader.close();
			inputStream.close();
			inputStream = null;
			connection.disconnect();
			return buffer.toString();
		}catch(Exception ex){
			ex.printStackTrace();
		}

		return "";
	}

	/**
	 * 退款操作
	 * @return
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public static String refundHand(String url,String data){
		try {
			KeyStore keyStore  = KeyStore.getInstance("PKCS12");

			FileInputStream instream = new FileInputStream(new File("E:/apiclient_cert.p12"));

			 keyStore.load(instream, "1509656321".toCharArray());//杩欓噷鍐欏

			 instream.close();

			SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, "1509656321".toCharArray()).build();

			 @SuppressWarnings("deprecation")
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext,new String[] { "TLSv1" },null,

			 SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);

			CloseableHttpClient httpclient = HttpClients.custom() .setSSLSocketFactory(sslsf) .build();

	        HttpPost httpost = new HttpPost(url); // 璁剧疆鍝嶅簲澶翠俊鎭?

	        httpost.addHeader("Connection", "keep-alive");

	        httpost.addHeader("Accept", "*/*");

	        httpost.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

	        httpost.addHeader("Host", "api.mch.weixin.qq.com");

	        httpost.addHeader("X-Requested-With", "XMLHttpRequest");

	        httpost.addHeader("Cache-Control", "max-age=0");

	        httpost.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0) ");

	        httpost.setEntity(new StringEntity(data, "UTF-8"));

	        CloseableHttpResponse response = httpclient.execute(httpost);

            HttpEntity entity = response.getEntity();

            String jsonStr = EntityUtils.toString(response.getEntity(), "UTF-8");

            EntityUtils.consume(entity);
         
         
            return jsonStr;
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
		return "";
	}
}
