package com.connection.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.connection.controller.UserController;
import com.connection.dao.AccTokenDao;
import com.connection.dao.AdminDao;
import com.connection.dao.DataDao;
import com.connection.entity.Admin;
import com.connection.service.interfaces.RedisService;
import com.connection.service.interfaces.UserService;
import com.connection.tool.Util;
import com.connection.wxPay.action.CompanyPay;
@Service
public class UserServiceImpl implements UserService{
	public static Logger log = Logger.getLogger(UserServiceImpl.class);
	@Autowired
	private AdminDao adminDao;
	@Autowired
	private DataDao dataDao;
	@Autowired
	private CompanyPay companyPay;
	@Autowired
	private AccTokenDao accTokenDao;
	@Autowired
	private RedisService redis;
	@Value("#{wx.appid}")
	private String appId;
	@Value("#{wx.secret}")
	private String secret;
	
	@Value("#{ali.endPoint}")
	private String endPoint;
	@Value("#{ali.accessKeyId}")
	private String accessKeyId;
	@Value("#{ali.accessKeySecret}")
	private String accessKeySecret;
	@Value("#{ali.bucketName}")
	private String bucketName;
	@Value("#{ali.ueditorUrl}")
	private String ueditorUrl;
	@Value("#{ali.codeUrl}")
	private String codeUrl;
	public String sendData(String code,String appId,String secret) throws IOException {
		StringBuffer result = null;
		InputStream is = null;
		try {
			URL url = new URL("https://api.weixin.qq.com/sns/jscode2session?appid="+appId+"&secret="+secret+"&js_code="+code+"&grant_type=authorization_code");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5 * 1000);
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "text/html; charset=utf-8");
			// 获取输入流
			is = conn.getInputStream();
			if (conn.getResponseCode() == 200) {
				int i = -1;
				byte[] b = new byte[1024];
				result = new StringBuffer();
				while ((i = is.read(b)) != -1) {
					result.append(new String(b, 0, i));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(is!=null){
				is.close();
			}
		}
		return result.toString();
	}
	/**
	 * 保存用户信息
	 * @return
	 */
	@Transactional
	public Admin saveAdmin(Admin admin){
		Admin userInfo  = adminDao.findUserInfo(admin.getOpenid());
		if(userInfo == null){
			int id = adminDao.saveUser(admin);
			if(id!=0){
				admin = adminDao.findUserInfo(admin.getOpenid());
			}
		}else{
			adminDao.modifyUserBaseInfo(admin);
			int userId = userInfo.getUserId();
			admin.setUserId(userId);
		}
		return admin;
	}
	
	/**
	 * 提现
	 */
	public synchronized Integer cash(int userId,double money,String openid){
		log.info(userId+"提现时间"+System.currentTimeMillis());
		Map<String,Double>realMoney = dataDao.getRealMoney(userId);//风控之后真实存在的钱
		int state = 0;
		Map<String,Object>admin = adminDao.getUserById(userId);
		if(dataDao.findQuestion(userId)!=null||realMoney.get("money")<0){
			state = 1;//此人在提现这块有问题
		}else if(realMoney !=null&&realMoney.get("money")>0){
			if(realMoney.get("money")>=money&&money>0&&realMoney.get("money")>0&&(double)admin.get("money")>0){

				//微信部分的操作
				if(money<=200){
					int num = adminDao.checkVersion(userId, (Integer)admin.get("money_version"));
					if(num == 1){
						state = companyPay.pay(userId,openid,money);
					}else if(num == 0){
						state = 3;
					}
					
				}else{
					state = 3;//暂停提现功能
				}
			}else{
				dataDao.saveQuestion(userId);//保存此人的问题
				state = 2;
			}
		}
		return state;
	}
	
	/**
	 * 保存formid
	 */
	@Transactional
	public void saveFormid(Map<String,Object>param){
		dataDao.saveFormid(param);
	}
	/**
	 * 保存formid
	 */
	@Transactional
	public void saveJobTake(Map<String,Object>param){
		dataDao.saveJobTake(param);
		redis.deleteTimerCash();
	}
	
	/**
	 * 保存微信的access_token
	 * @param code
	 * @param appId
	 * @param secret
	 * @return
	 * @throws IOException
	 */
	public String saveToken() {
		StringBuffer result = null;
		InputStream is = null;
		try {
			URL url = new URL("https://api.weixin.qq.com/cgi-bin/token?appid="+appId+"&secret="+secret+"&grant_type=client_credential");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5 * 1000);
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "text/html; charset=utf-8");
			// 获取输入流
			is = conn.getInputStream();
			if (conn.getResponseCode() == 200) {
				int i = -1;
				byte[] b = new byte[1024];
				result = new StringBuffer();
				while ((i = is.read(b)) != -1) {
					result.append(new String(b, 0, i));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(is!=null){
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result.toString();
	}
	
	/**
	 * 得到AccToken
	 * @return
	 * @throws IOException
	 */
	@Transactional
	public String getToken(){
		HashMap<String,Object>accToken = accTokenDao.findNewAccToken();
		Map<String,Object>wx = null;
		if(accToken == null){
			String result = saveToken();
			wx = (Map<String, Object>) JSON.parse(result.toString());
			accToken = new HashMap<String,Object>();
			accToken.put("expireIn", (Integer)(wx.get("expires_in")));
			accToken.put("accToken", (String)wx.get("access_token"));
			accTokenDao.saveAccToken(accToken);
			return (String)wx.get("access_token");
		}
		long timeLimit = accTokenDao.findLimit((Integer)accToken.get("id")).get("timeLimit");
		if(timeLimit > ((Integer)accToken.get("expireIn") - 10)){
			String result = saveToken();
			wx = (Map<String, Object>) JSON.parse(result.toString());
			accToken = new HashMap<String,Object>();
			accToken.put("expireIn", (Integer)(wx.get("expires_in")));
			accToken.put("accToken", (String)wx.get("access_token"));
			accTokenDao.saveAccToken(accToken);
			return (String)wx.get("access_token");
		}
		return (String)accToken.get("accToken");
	}
	/**
	 * 小程序发送推送消息
	 * @param Json
	 * @return
	 */
	public String sendMsg(String Json){
		StringBuffer result = null;
		InputStream is = null;
		try {
			String acc_token = getToken();
			URL url = new URL("https://api.weixin.qq.com/cgi-bin/message/wxopen/template/send?access_token="+acc_token);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setConnectTimeout(5 * 1000);
			// 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
			conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			// 往服务器里面发送数据
            if (Json != null) {
                byte[] writebytes = Json.getBytes();
                // 设置文件长度
                conn.setRequestProperty("Content-Length", String.valueOf(writebytes.length));
                conn.setRequestProperty("accept","application/json");
                OutputStream outwritestream = conn.getOutputStream();
                outwritestream.write(Json.getBytes("utf-8"));
                outwritestream.flush();
                outwritestream.close();
            }
			// 获取输入流
			is = conn.getInputStream();
			if (conn.getResponseCode() == 200) {
				int i = -1;
				byte[] b = new byte[1024];
				result = new StringBuffer();
				while ((i = is.read(b)) != -1) {
					result.append(new String(b, 0, i));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}finally{
			try {
				if(is!=null){
					is.close();
				}
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}
		return result.toString();
	}
	
	
	
	/**
	 * 获得传播页得二维码
	 */
	public String getCode(String Json){
		StringBuffer result = null;
		InputStream is = null;
		String dbPath = null;
		try {
			String acc_token = getToken();
			URL url = new URL("https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token="+acc_token);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setConnectTimeout(5 * 1000);
			// 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
			conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			// 往服务器里面发送数据
            if (Json != null) {
                byte[] writebytes = Json.getBytes();
                // 设置文件长度
                conn.setRequestProperty("Content-Length", String.valueOf(writebytes.length));
                conn.setRequestProperty("accept","application/json");
                OutputStream outwritestream = conn.getOutputStream();
                outwritestream.write(Json.getBytes("utf-8"));
                outwritestream.flush();
                outwritestream.close();
            }
			// 获取输入流
			is = conn.getInputStream();
			if (conn.getResponseCode() == 200) {
				
				long currentTime = System.currentTimeMillis();
				String imagePath = codeUrl + currentTime + ".png";
				dbPath = ueditorUrl+"static/code/" + currentTime + ".png";
				Util.ossLoad(endPoint, accessKeyId, accessKeySecret, bucketName, imagePath, is);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}finally{
			try {
				if(is!=null){
					is.close();
				}
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}
		return dbPath;
	}
	/**
	 * 保存用户口令
	 */
	@Transactional
	public void saveCommand(String command,int userId){
		dataDao.saveUserCommand(command, userId);
	}
}
