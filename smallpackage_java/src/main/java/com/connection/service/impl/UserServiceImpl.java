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
	//������������־��־��¼������
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
	//��wx.properties�����ļ���ȡ
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
			// ��ȡ������
			is = conn.getInputStream();
			if (conn.getResponseCode() == 200) {
				int i = -1;
				byte[] b = new byte[1024];
				result = new StringBuffer();
				while ((i = is.read(b)) != -1) {
					//�򵥵���˵����byte����b���±�Ϊn��ʼǰ��m���±����һ�������Ϊ�ַ���item�� 
					//�����ϰ�߻������Լ�֮ǰ�ķ����ɣ��������
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
	 * �����û���Ϣ
	 * @return
	 */
	//����������
	@Transactional
	public Admin saveAdmin(Admin admin){
		//�Ȳ�ѯ��û������û�������openid�����openid�Ǹ��ݴ����admin�õ�
		Admin userInfo  = adminDao.findUserInfo(admin.getOpenid());
		
		if(userInfo == null){
			//userinfo�ǿյģ����ñ������û�����
			int id = adminDao.saveUser(admin);
			if(id!=0){
				//������ؽ����Ϊ0������findUserInfo�������admin���¸�ֵ������ʱ�䣬Ǯ���汾�ŵ��ֶεģ�
				admin = adminDao.findUserInfo(admin.getOpenid());
			}
		}else{
			//userinfo���ǿյľ͵����޸��û�������Ϣ
			adminDao.modifyUserBaseInfo(admin);
			//��ֵ��userid��ǰ̨û�е�
			int userId = userInfo.getUserId();
			admin.setUserId(userId);
		}
		return admin;
	}
	
	/**
	 * ����
	 */
	public synchronized Integer cash(int userId,double money,String openid){
		//�����־�������û�id������ʱ�䣩
		
		//<!-- 	��ѯ���û���������¼���еĽ�����ͣ�����ȥ�����ֽ���е����ּ�¼�ͣ� -->
		Map<String,Double>realMoney = dataDao.getRealMoney(userId);//���֮����ʵ���ڵ�Ǯ
		log.info(userId+"����ʱ��"+System.currentTimeMillis()+"��ʵ��Ǯ="+realMoney);
		int state = 0;
		////�����û�id��ѯuserId,nickName,avatarUrl,money,money_version 
		Map<String,Object>admin = adminDao.getUserById(userId);
		//������û���û�в��õİ��ף�����ͱ������ˣ�ͬʱ�˲�����Ľ���ǲ��Ǹ���
		if(dataDao.findQuestion(userId)!=null||realMoney.get("money")<0){
			state = 1;//�������������������
		}else if(realMoney !=null&&realMoney.get("money")>0){
			if(realMoney.get("money")>=money&&money>0&&realMoney.get("money")>0&&(double)admin.get("money")>0){

				//΢�Ų��ֵĲ��������ֿ������ܴ���200��
				if(money<=200){
					//�ȵ��ø����ֹ����Ĳ���
					int num = adminDao.checkVersion(userId, (Integer)admin.get("money_version"));
					if(num == 1){
						//�ɹ������ֹ�����������
						state = companyPay.pay(userId,openid,money);
					}else if(num == 0){
						//ʧ��
						state = 3;
					}
					
				}else{
					//ʧ��
					state = 3;//��ͣ���ֹ���
				}
			}else{
				//������������
				dataDao.saveQuestion(userId);//������˵�����
				state = 2;
			}
		}
		return state;
	}
	
	/**
	 * ����formid
	 */
	@Transactional
	public void saveFormid(Map<String,Object>param){
		dataDao.saveFormid(param);
	}
	/**
	 * ����formid
	 */
	@Transactional
	public void saveJobTake(Map<String,Object>param){
		//<!-- �����û�id��jobid�����id�� -->
		dataDao.saveJobTake(param);
		//�����������ú������
		redis.deleteTimerCash();
	}
	
	/**
	 * ����΢�ŵ�access_token
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
			// ��ȡ������
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
	 * �õ�AccToken��accToken�ǵ���΢��ĳЩ�ӿ�ʱ����Ҫ��ƾ֤
	 * @return
	 * @throws IOException
	 */
	@Transactional
	public String getToken(){
//		�����µ�acctoken��id����ֻȡһ�� 
		HashMap<String,Object>accToken = accTokenDao.findNewAccToken();
		Map<String,Object>wx = null;
		if(accToken == null){
			//���acctoken�ǿյģ���ȥ΢�������ȡ
			String result = saveToken();
			//�ѻ�ȡ����Ϣ��װ��json�ŵ�map
			wx = (Map<String, Object>) JSON.parse(result.toString());
			accToken = new HashMap<String,Object>();
			accToken.put("expireIn", (Integer)(wx.get("expires_in")));
			accToken.put("accToken", (String)wx.get("access_token"));
			//���浽���ݿ�
			accTokenDao.saveAccToken(accToken);
			//����accToken
			return (String)wx.get("access_token");
		}
		//���ݸ��õ���accToken��Id����ȡ��ǰʱ������ȡaccToken��ʱ���֮�ʱ��� ����1970��0ʱ0��0������UTC��׼ʱ�������
		long timeLimit = accTokenDao.findLimit((Integer)accToken.get("id")).get("timeLimit");
		//������������timeLimit����expireIn����Чʱ�䣬�������������»�ȡ��������
		if(timeLimit > ((Integer)accToken.get("expireIn") - 10)){
			String result = saveToken();
			wx = (Map<String, Object>) JSON.parse(result.toString());
			accToken = new HashMap<String,Object>();
			accToken.put("expireIn", (Integer)(wx.get("expires_in")));
			accToken.put("accToken", (String)wx.get("access_token"));
			accTokenDao.saveAccToken(accToken);
			return (String)wx.get("access_token");
		}
		//���ﾭ�����ɸѡ����ֱ�ӷ���
		return (String)accToken.get("accToken");
	}
	/**
	 * С������������Ϣ
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
			// ����POST�������������������
            conn.setDoOutput(true);
            conn.setDoInput(true);
			conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			// �����������淢������
            if (Json != null) {
                byte[] writebytes = Json.getBytes();
                // �����ļ�����
                conn.setRequestProperty("Content-Length", String.valueOf(writebytes.length));
                conn.setRequestProperty("accept","application/json");
                OutputStream outwritestream = conn.getOutputStream();
                outwritestream.write(Json.getBytes("utf-8"));
                outwritestream.flush();
                outwritestream.close();
            }
			// ��ȡ������
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
	 * ��ô���ҳ�ö�ά��
	 */
	public String getCode(String Json){
		StringBuffer result = null;
		InputStream is = null;
		String dbPath = null;
		try {
			//���ڻ�ȡ��accToken
			String acc_token = getToken();
			URL url = new URL("https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token="+acc_token);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setConnectTimeout(5 * 1000);
			// ����POST�������������������
            conn.setDoOutput(true);
            conn.setDoInput(true);
			conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			// �����������淢������
            if (Json != null) {
                byte[] writebytes = Json.getBytes();
                // �����ļ�����
                conn.setRequestProperty("Content-Length", String.valueOf(writebytes.length));
                conn.setRequestProperty("accept","application/json");
                OutputStream outwritestream = conn.getOutputStream();
                outwritestream.write(Json.getBytes("utf-8"));
                outwritestream.flush();
                outwritestream.close();
            }
			// ��ȡ������
			is = conn.getInputStream();
			if (conn.getResponseCode() == 200) {
				
				long currentTime = System.currentTimeMillis();
				//����ͼƬ·����codeUrl=static/code/��ͼƬ·��Ӧ����static/code/����ʱ��.png��-->�浽������
				String imagePath = codeUrl + currentTime + ".png";
				//���ص�·����ueditorUrl���ǵ�������·��Ϊhttps://static.yaohoudy.com/static/code/����ʱ��.png
				dbPath = ueditorUrl+"static/code/" + currentTime + ".png";
				// �����ƴ洢����
				//Access Key ID��Access Key Secret
				//�û�ע��OSSʱ��ϵͳ����û�����һ��Access Key ID��Access Key Secret����ΪID�ԣ����ڱ�ʶ�û���Ϊ����OSS��ǩ����֤��
				//bucketName��OSS�ϵ������ռ�
				//endPoint�����Ƶ�ַ
				//is����������
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
	 * �����û�����
	 */
	@Transactional
	public void saveCommand(String command,int userId){
		dataDao.saveUserCommand(command, userId);
	}
	

	
	
}
