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
	//生成这个类的日志日志记录器对象
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
	//从wx.properties属性文件获取
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
					//简单的来说就是byte数组b从下标为n开始前进m个下标的那一段数组变为字符串item。 
					//如果不习惯还是用自己之前的方法吧，反正差不多
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
	//打上事务标记
	@Transactional
	public Admin saveAdmin(Admin admin){
		//先查询有没有这个用户，根据openid，这个openid是根据传入的admin得到
		Admin userInfo  = adminDao.findUserInfo(admin.getOpenid());
		
		if(userInfo == null){
			//userinfo是空的，调用保存新用户方法
			int id = adminDao.saveUser(admin);
			if(id!=0){
				//如果返回结果不为0，调用findUserInfo给传入的admin重新赋值（多了时间，钱，版本号等字段的）
				admin = adminDao.findUserInfo(admin.getOpenid());
			}
		}else{
			//userinfo不是空的就调用修改用户基本信息
			adminDao.modifyUserBaseInfo(admin);
			//赋值下userid，前台没有的
			int userId = userInfo.getUserId();
			admin.setUserId(userId);
		}
		return admin;
	}
	
	/**
	 * 提现
	 */
	public synchronized Integer cash(int userId,double money,String openid){
		//输出日志（包括用户id和提现时间）
		
		//<!-- 	查询（用户在语音记录表中的奖励金和），减去，（现金表中的提现记录和） -->
		Map<String,Double>realMoney = dataDao.getRealMoney(userId);//风控之后真实存在的钱
		log.info(userId+"提现时间"+System.currentTimeMillis()+"真实的钱="+realMoney);
		int state = 0;
		////根据用户id查询userId,nickName,avatarUrl,money,money_version 
		Map<String,Object>admin = adminDao.getUserById(userId);
		//查这个用户有没有不好的案底，有你就别想玩了，同时核查上面的结果是不是负的
		if(dataDao.findQuestion(userId)!=null||realMoney.get("money")<0){
			state = 1;//此人在提现这块有问题
		}else if(realMoney !=null&&realMoney.get("money")>0){
			if(realMoney.get("money")>=money&&money>0&&realMoney.get("money")>0&&(double)admin.get("money")>0){

				//微信部分的操作（提现看来不能大于200）
				if(money<=200){
					//先调用跟新乐观锁的操作
					int num = adminDao.checkVersion(userId, (Integer)admin.get("money_version"));
					if(num == 1){
						//成功更新乐观锁。，提现
						state = companyPay.pay(userId,openid,money);
					}else if(num == 0){
						//失败
						state = 3;
					}
					
				}else{
					//失败
					state = 3;//暂停提现功能
				}
			}else{
				//给她留个案底
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
		//<!-- 保存用户id和jobid（红包id） -->
		dataDao.saveJobTake(param);
		//清除福利界面得红包缓存
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
	 * 得到AccToken，accToken是调用微信某些接口时所需要的凭证
	 * @return
	 * @throws IOException
	 */
	@Transactional
	public String getToken(){
//		查找新的acctoken（id降序）只取一条 
		HashMap<String,Object>accToken = accTokenDao.findNewAccToken();
		Map<String,Object>wx = null;
		if(accToken == null){
			//如果acctoken是空的，就去微信那里获取
			String result = saveToken();
			//把获取的信息封装成json放到map
			wx = (Map<String, Object>) JSON.parse(result.toString());
			accToken = new HashMap<String,Object>();
			accToken.put("expireIn", (Integer)(wx.get("expires_in")));
			accToken.put("accToken", (String)wx.get("access_token"));
			//保存到数据库
			accTokenDao.saveAccToken(accToken);
			//返回accToken
			return (String)wx.get("access_token");
		}
		//根据刚拿到的accToken的Id，获取当前时间戳与获取accToken的时间戳之差，时间戳 是自1970年0时0分0秒以来UTC标准时间的秒数
		long timeLimit = accTokenDao.findLimit((Integer)accToken.get("id")).get("timeLimit");
		//如果计算出来的timeLimit大于expireIn（有效时间，秒数），则重新获取，并返回
		if(timeLimit > ((Integer)accToken.get("expireIn") - 10)){
			String result = saveToken();
			wx = (Map<String, Object>) JSON.parse(result.toString());
			accToken = new HashMap<String,Object>();
			accToken.put("expireIn", (Integer)(wx.get("expires_in")));
			accToken.put("accToken", (String)wx.get("access_token"));
			accTokenDao.saveAccToken(accToken);
			return (String)wx.get("access_token");
		}
		//这里经过层层筛选可以直接返回
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
			//终于获取了accToken
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
				//设置图片路径，codeUrl=static/code/（图片路径应该是static/code/创建时间.png）-->存到阿里云
				String imagePath = codeUrl + currentTime + ".png";
				//返回的路径。ueditorUrl我们的域名，路径为https://static.yaohoudy.com/static/code/创建时间.png
				dbPath = ueditorUrl+"static/code/" + currentTime + ".png";
				// 阿里云存储服务
				//Access Key ID、Access Key Secret
				//用户注册OSS时，系统会给用户分配一对Access Key ID和Access Key Secret，称为ID对，用于标识用户，为访问OSS做签名验证。
				//bucketName是OSS上的命名空间
				//endPoint阿里云地址
				//is流的引用名
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
