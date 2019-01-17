package com.connection.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.connection.controller.JobController;
import com.connection.dao.AdminDao;
import com.connection.dao.BegJobDao;
import com.connection.dao.DataDao;
import com.connection.dao.JobDao;
import com.connection.dao.VoiceRecordDao;
import com.connection.service.interfaces.BegJobService;
import com.connection.service.interfaces.JobService;
import com.connection.service.interfaces.RedisService;
import com.connection.service.interfaces.UserService;
import com.connection.tool.EqualStr;
import com.connection.tool.PinYinUtil;
import com.connection.tool.Util;
import com.connection.tool.WeixinMoney;
import com.connection.xunfei.yuyintingxie.FileUtil;
import com.connection.xunfei.yuyintingxie.HttpUtil;
import com.connection.xunfei.yuyintingxie.TestWebIat;

@Service
public class BegJobServiceImpl implements BegJobService {
	public static Logger log = Logger.getLogger(BegJobServiceImpl.class);
	@Autowired
	private BegJobDao begjobDao;
	@Autowired
	private VoiceRecordDao voiceRecordDao;
	@Autowired
	private AdminDao adminDao;
	@Autowired
	private RedisService redis;
	@Value("#{ali.vedioPath}")
	private String dedioPath;
	private String ossVedio = "/static/vedio/";// oss存储位置

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
	@Autowired
	private DataDao dataDao;
	@Autowired
	private UserService userService;

	@Transactional
	public int addJob(Map<String, String> param) {
		Map<String, Object> job = new HashMap<String, Object>();
		job.put("id", 0);
		job.put("userId", Integer.parseInt(param.get("userId")));
		job.put("totalAward", Double.parseDouble(param.get("totalAward")));
		job.put("award", Double.parseDouble(param.get("award")));
		job.put("context", param.get("context"));
		job.put("job_type", Integer.parseInt(param.get("job_type")));
		begjobDao.addJob(job);
		int id = (Integer) job.get("id");
		param = null;
		job = null;
		return id;
	}

	@Transactional
	public void payOver(String transaction_id, String out_trade_no, int jobId,int  userId,double award) {
		int cc= 0;
		cc=	begjobDao.payOver(transaction_id, out_trade_no, jobId, userId);
		
		if(cc==1) {
			redis.deleteRecord(jobId);
			int fauserId =begjobDao.getUserIdByJobId(jobId);
			Map<String,Object>admin = adminDao.getUserById(fauserId);
			int num = adminDao.checkVersion(fauserId, (Integer)admin.get("money_version"));
			if(num == 1){
				adminDao.modifyMoney(award, fauserId);
				
			}else if(num == 0){
				//失败
				log.info("给到用户钱出问题了");
			}
		}
	    
	}

	
	
	
	
	
	/**
	 * 保存证书//voice表
	 * 
	 * @return
	 */
	@Transactional
	public String saveBook(InputStream input, int id) {
		String name = System.currentTimeMillis() + "";
		String dataPath = "";
		try {
			String ossSavePath = "static/book/" + name + ".png";// oss保存路径
			dataPath = "https://static.yaohoudy.com/static/book/" + name + ".png";// 数据库位置
			Util.ossLoad(endPoint, accessKeyId, accessKeySecret, bucketName, ossSavePath, input);
			voiceRecordDao.saveBook(dataPath, id);
		} catch (Exception e) {

		}
		return dataPath;
	}


	/**
	 * 保存分想图//job表
	 * 
	 * @return
	 */
	@Transactional
	public String saveBegSharePic(InputStream input, int id) {
		String name = System.currentTimeMillis() + "";
		String dataPath = "";
		try {
			String ossSavePath = "static/sharePic/" + name + ".png";// oss保存路径
			dataPath = "https://static.yaohoudy.com/static/sharePic/" + name + ".png";// 数据库位置
			Util.ossLoad(endPoint, accessKeyId, accessKeySecret, bucketName, ossSavePath, input);
			begjobDao.updateBegSharePic(dataPath, id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dataPath;
	}

	@Override
	public int getUserId(String openId) {
		// TODO Auto-generated method stub
		return begjobDao.getUserId(openId);
		
		
	
	}
	
	/**
	 * 保存图片口令
	 * 
	 * @return
	 */
	@Transactional
	public String saveCommandImage(InputStream input, int userId) {
		String name = System.currentTimeMillis() + "";
		String dataPath = "";
		try {
			String ossSavePath = "static/sharePic/" + name + ".png";// oss保存路径
			dataPath = "https://static.yaohoudy.com/static/sharePic/" + name + ".png";// 数据库位置
			Util.ossLoad(endPoint, accessKeyId, accessKeySecret, bucketName, ossSavePath, input);
			begjobDao.insertCommandImage(dataPath, userId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dataPath;
	}
	/**
	 * 上传语音口令
	 * 
	 * @param input
	 * @throws IOException
	 */
	public synchronized String  saveVoice(InputStream input, int userId) throws IOException {
		String name = System.currentTimeMillis() + (int) Math.random() * 10 + (int) Math.random() * 10 + "";
		OutputStream os = null;
		File file = null;
		InputStream fileInput = null;
		int len;
		Map<String, Object> job = null;
		String result = "";
		WeixinMoney w = null;
		Map<String, Object> returnParam = null;
		String appid = "5bda9a52";
		String secret = "d2b3b2eec6cc60760c783472ef1b30f8";
		String dataPath=null;
		try {
			//输出到MP3文件
			byte[] bs = new byte[1024];
			String inputFilePath = dedioPath + name + ".mp3";
			os = new FileOutputStream(inputFilePath);
			while ((len = input.read(bs)) != -1) {
				os.write(bs, 0, len);
			}
			//格式转换
			String outputFilePath = dedioPath + name + ".wav";
			boolean flag = Util.Mp3ToWav(inputFilePath, outputFilePath);

			 dataPath = "https://static.yaohoudy.com/static/vedio/" + name + ".wav";// 数据库位置
			String ossSavePath = "static/vedio/" + name + ".wav";// oss保存路径
					file = new File(outputFilePath);// 保存文件以及改变数据
					
					fileInput = new FileInputStream(file);
					begjobDao.insertVoiceCommand(dataPath,userId);// 成功得代表
					Util.ossLoad(endPoint, accessKeyId, accessKeySecret, bucketName, ossSavePath, fileInput);//存到阿里oss
					fileInput.close();

					
					
				

				

				// 删除本地两个文件
				file = new File(inputFilePath);
				file.delete();
				file = new File(outputFilePath);
				file.delete();

			

		} catch (Exception e) {
//			e.printStackTrace();
			log.info("upload内部方法报错");
		} finally {
			os.close();
			input.close();
			file = null;
			job = null;
			w = null;
			returnParam = null;
		}
		return dataPath;
	}
	/**
	 * 上传视频口令
	 * 
	 * @param input
	 * @throws IOException
	 */
	public synchronized String saveVedioCommand(InputStream input, int userId) throws IOException {
		String name = System.currentTimeMillis() + (int) Math.random() * 10 + (int) Math.random() * 10 + "";
		OutputStream os = null;
		File file = null;
		InputStream fileInput = null;
		int len;
		Map<String, Object> job = null;
		String result = "";
		WeixinMoney w = null;
		Map<String, Object> returnParam = null;
		String appid = "5bda9a52";
		String secret = "d2b3b2eec6cc60760c783472ef1b30f8";
		String dataPath=null;
		try {
			//输出到MP3文件
			byte[] bs = new byte[1024];
			String inputFilePath = dedioPath + name + ".mp4";
			os = new FileOutputStream(inputFilePath);
			while ((len = input.read(bs)) != -1) {
				os.write(bs, 0, len);
			}
			//格式转换
			String outputFilePath = dedioPath + name + ".mp4";
			boolean flag = Util.Mp3ToWav(inputFilePath, outputFilePath);

			 dataPath = "https://static.yaohoudy.com/static/vedio/" + name + ".mp4";// 数据库位置
			String ossSavePath = "static/vedio/" + name + ".mp4";// oss保存路径
					file = new File(outputFilePath);// 保存文件以及改变数据
					fileInput = new FileInputStream(file);
				 begjobDao.insertVedioCommand(dataPath,userId);// 成功得代表
					
					Util.ossLoad(endPoint, accessKeyId, accessKeySecret, bucketName, ossSavePath, fileInput);//存到阿里oss
				
					
					

					fileInput.close();
					
				// 删除本地两个文件
				file = new File(inputFilePath);
				file.delete();
				file = new File(outputFilePath);
				file.delete();

			

		} catch (Exception e) {
//			e.printStackTrace();
			log.info("upload内部方法报错");
		} finally {
			os.close();
			input.close();
			file = null;
			job = null;
			w = null;
			returnParam = null;
		}
		return dataPath;
	}
	

	
	
	
	
	
	/**
	 * 保存sys图片口令
	 * 
	 * @return
	 */
	@Transactional
	public String saveSysCommandImage(InputStream input, int fatherId) {
		String name = System.currentTimeMillis() + "";
		String dataPath = "";
		try {
			String ossSavePath = "static/sharePic/" + name + ".png";// oss保存路径
			dataPath = "https://static.yaohoudy.com/static/sharePic/" + name + ".png";// 数据库位置
			Util.ossLoad(endPoint, accessKeyId, accessKeySecret, bucketName, ossSavePath, input);
			begjobDao.sysCommandImage(dataPath, fatherId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dataPath;
	}
	/**
	 * 上传sys视频口令
	 * 
	 * @param input
	 * @throws IOException
	 */
	public synchronized String saveSysVedioCommand(InputStream input, int userId) throws IOException {
		String name = System.currentTimeMillis() + (int) Math.random() * 10 + (int) Math.random() * 10 + "";
		OutputStream os = null;
		File file = null;
		InputStream fileInput = null;
		int len;
		Map<String, Object> job = null;
		String result = "";
		WeixinMoney w = null;
		Map<String, Object> returnParam = null;
		String appid = "5bda9a52";
		String secret = "d2b3b2eec6cc60760c783472ef1b30f8";
		String dataPath=null;
		try {
			//输出到MP3文件
			byte[] bs = new byte[1024];
			String inputFilePath = dedioPath + name + ".mp4";
			os = new FileOutputStream(inputFilePath);
			while ((len = input.read(bs)) != -1) {
				os.write(bs, 0, len);
			}
			//格式转换
			String outputFilePath = dedioPath + name + ".mp4";
			boolean flag = Util.Mp3ToWav(inputFilePath, outputFilePath);

			 dataPath = "https://static.yaohoudy.com/static/vedio/" + name + ".mp4";// 数据库位置
			String ossSavePath = "static/vedio/" + name + ".mp4";// oss保存路径
					file = new File(outputFilePath);// 保存文件以及改变数据
					fileInput = new FileInputStream(file);
				 begjobDao.sysVedioCommand(dataPath,userId);// 成功得代表
					
					Util.ossLoad(endPoint, accessKeyId, accessKeySecret, bucketName, ossSavePath, fileInput);//存到阿里oss
				
					
					

					fileInput.close();
					
				// 删除本地两个文件
				file = new File(inputFilePath);
				file.delete();
				file = new File(outputFilePath);
				file.delete();

			

		} catch (Exception e) {
//			e.printStackTrace();
			log.info("upload内部方法报错");
		} finally {
			os.close();
			input.close();
			file = null;
			job = null;
			w = null;
			returnParam = null;
		}
		return dataPath;
	}
	/**
	 * 上传sys语音口令
	 * 
	 * @param input
	 * @throws IOException
	 */
	public synchronized String  saveSysVoice(InputStream input, int userId,String context) throws IOException {
		String name = System.currentTimeMillis() + (int) Math.random() * 10 + (int) Math.random() * 10 + "";
		OutputStream os = null;
		File file = null;
		InputStream fileInput = null;
		int len;
		Map<String, Object> job = null;
		String result = "";
		WeixinMoney w = null;
		Map<String, Object> returnParam = null;
		String appid = "5bda9a52";
		String secret = "d2b3b2eec6cc60760c783472ef1b30f8";
		String dataPath=null;
		try {
			//输出到MP3文件
			byte[] bs = new byte[1024];
			String inputFilePath = dedioPath + name + ".mp3";
			os = new FileOutputStream(inputFilePath);
			while ((len = input.read(bs)) != -1) {
				os.write(bs, 0, len);
			}
			//格式转换
			String outputFilePath = dedioPath + name + ".wav";
			boolean flag = Util.Mp3ToWav(inputFilePath, outputFilePath);

			 dataPath = "https://static.yaohoudy.com/static/vedio/" + name + ".wav";// 数据库位置
			String ossSavePath = "static/vedio/" + name + ".wav";// oss保存路径
					file = new File(outputFilePath);// 保存文件以及改变数据
					
					fileInput = new FileInputStream(file);
					begjobDao.sysVoiceCommand(dataPath,userId,context);// 成功得代表
					Util.ossLoad(endPoint, accessKeyId, accessKeySecret, bucketName, ossSavePath, fileInput);//存到阿里oss
					fileInput.close();

					
					
				

				

				// 删除本地两个文件
				file = new File(inputFilePath);
				file.delete();
				file = new File(outputFilePath);
				file.delete();

			

		} catch (Exception e) {
//			e.printStackTrace();
			log.info("upload内部方法报错");
		} finally {
			os.close();
			input.close();
			file = null;
			job = null;
			w = null;
			returnParam = null;
		}
		return dataPath;
	}

	@Override
	public String saveSysCommand(int contextId, int levelContextId, String context) {
		// TODO Auto-generated method stub
		begjobDao.sysCommand(contextId, levelContextId, context);// 成功得代表
		return context;
	}
	
}
