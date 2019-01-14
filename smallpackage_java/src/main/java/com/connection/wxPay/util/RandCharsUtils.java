package com.connection.wxPay.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
//这个主要生成随机字符串，按照一定要求，订单号，开始时间结束时间等
/**
 * nonce_str随即字符串
 * @author iYjrg_xiebin
 * @date 2015年11月25日下午5:10:32
 */
public class RandCharsUtils {
	private static SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");

	public static String getRandomString(int length) { //length表示生成字符串的长度
		String base = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";   
		Random random = new Random();   
		StringBuffer sb = new StringBuffer();
		int number = 0;
		for (int i = 0; i < length; i++) {   
			number = random.nextInt(base.length());   
			sb.append(base.charAt(number));   
		}   
		return sb.toString();   
	}   
	//生成订单号
	public static String getRandomStringOrderNum() {
		// 自增8位数 00000001
		return "bxs" + DatetimeUtil.formatDate(new Date(), DatetimeUtil.TIME_STAMP_PATTERN);
	}
	
	
	/*
	 * 订单开始交易的时间
	 */
	public static String timeStart(){
		return df.format(new Date());
	}

	/*
	 * 订单结束交易的时间
	 */
	public static String timeExpire(){
		Calendar now=Calendar.getInstance();
		now.add(Calendar.MINUTE,30);
		return df.format(now.getTimeInMillis());
	}

	
	/*public static void main(String[] args) {*/
		/*for (int i = 0; i < 10; i++) {
			System.out.println("第"+i+"次是："+getRandomString(32));
		}*/
		

		/*System.out.println("开始时间是："+timeStart());
		System.out.println("开始时间是："+timeExpire());
	}*/

}
