package com.connection.wxPay.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//这个类主要作用就是初始化，appid，mch_id，notify_url三个参数，并将sys.properties中的参数给到参数
/**
 * 微信的配置参数
 * @author iYjrg_xiebin
 * @date 2015年11月25日下午4:19:57
 */
@SuppressWarnings("unused")
public class WeixinConfigUtils {

	private static final Log log = LogFactory.getLog(WeixinConfigUtils.class);


	public static String appid;//应用ID，微信开放平台审核通过的应用APPID
	public static String mch_id;//商户号，微信支付分配的商户号
	public static String notify_url;//通知地址，接收微信支付异步通知回调地址，通知URL必须为直接可访问的URL，不能携带参数
	public static String beg_notify_url;//通知地址，接收微信支付异步通知回调地址，通知URL必须为直接可访问的URL，不能携带参数
	public static String book_url;
	static {
		/*ResourceBundle bundle = ResourceBundle.getBundle("resources/sys");
		appid = bundle.getString("appid");
		mch_id = bundle.getString("mch_id");
		notify_url = bundle.getString("notify_url");*/

		try{
			InputStream is = WeixinConfigUtils.class.getResourceAsStream("/sys.properties");
			Properties properties = new Properties();
			properties.load(is);
			
			appid = properties.getProperty("appid");
			mch_id = properties.getProperty("mch_id");
			book_url = properties.getProperty("book_url");
			notify_url = properties.getProperty("notify_url");
			beg_notify_url = properties.getProperty("beg_notify_url");
		}catch(Exception ex){
			log.debug("加载配置文件："+ex.getMessage());
		}
	}


	/*public static void main(String[] args) throws IOException {
		InputStream is = WeixinConfigUtils.class.getResourceAsStream("/sys.properties");

		Properties properties = new Properties();

		properties.load(is);

		is.close();
		String appid = properties.getProperty("appid");
		System.out.println(appid);
	}*/

}
