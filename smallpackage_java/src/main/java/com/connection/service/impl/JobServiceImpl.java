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
import com.connection.dao.DataDao;
import com.connection.dao.JobDao;
import com.connection.dao.VoiceRecordDao;
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
public class JobServiceImpl implements JobService {
	public static Logger log = Logger.getLogger(JobServiceImpl.class);
	@Autowired
	private JobDao jobDao;
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
		job.put("totalCount", Integer.parseInt(param.get("totalCount")));
		job.put("context", param.get("context"));
		job.put("job_type", Integer.parseInt(param.get("job_type")));
		job.put("one_award", Double.parseDouble(param.get("one_award")));
		jobDao.addJob(job);
		int id = (Integer) job.get("id");
		param = null;
		job = null;
		return id;
	}

	@Transactional
	public void payOver(String transaction_id, String out_trade_no, int id) {
		jobDao.payOver(transaction_id, out_trade_no, id);
	}

	/**
	 * 保存录音文件并且得到内容
	 * 
	 * @param input
	 * @throws IOException
	 */
	public synchronized String saveVedio(InputStream input, int id, int userId, int second) throws IOException {
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
		try {
			byte[] bs = new byte[1024];
			String inputFilePath = dedioPath + name + ".mp3";
			os = new FileOutputStream(inputFilePath);
			while ((len = input.read(bs)) != -1) {
				os.write(bs, 0, len);
			}
			String outputFilePath = dedioPath + name + ".wav";
			boolean flag = Util.Mp3ToWav(inputFilePath, outputFilePath);

			String dataPath = "https://static.yaohoudy.com/static/vedio/" + name + ".wav";// 数据库位置
			String ossSavePath = "static/vedio/" + name + ".wav";// oss保存路径
			if (flag) {
				Map<String, Object> xunfei = dataDao.getXunfei();
				if (xunfei != null) {
					appid = (String) xunfei.get("appid");
					secret = (String) xunfei.get("secret");
					dataDao.updateXunfei((int) xunfei.get("id"));
				}
				String data = TestWebIat.parse(outputFilePath, appid, secret);// 得到语音中得内容
																				// 拼音
				job = redis.getJobById(id);// 乐观锁
				log.info("jobService中缓存得job:"+job);
				if (job.get("alreadyAward") == job.get("award") && job.get("totalCount") == job.get("alreadyCount")) {// 说明没有机会了
					result = "haveNoChance";
				} else if (job.get("alreadyCount") != job.get("award")
						&& job.get("totalCount") != job.get("alreadyCount")) {// 成功
					String rightAnswer = (String) job.get("context");
					rightAnswer = PinYinUtil.getFullSpell(rightAnswer);

					double num = EqualStr.getSimilarityRatio(data, rightAnswer);// 准确路

					file = new File(outputFilePath);// 保存文件以及改变数据
					fileInput = new FileInputStream(file);
					Util.ossLoad(endPoint, accessKeyId, accessKeySecret, bucketName, ossSavePath, fileInput);
					fileInput.close();
					Map<String, Object> voice = redis.checkVoice(userId, id);
					log.info("jobService中缓存得voice:"+voice);
					if (num >= 80 && voice == null) {// 代表通过
						int people = (int) job.get("totalCount") - (int) job.get("alreadyCount");
						double award = 0.00;
						int job_type = (Integer) job.get("job_type");
						double money = 0.00;
						double money1 = 0.00;
						if (job_type == 0) {
							money = (Double) job.get("award") - (Double) job.get("alreadyAward");
							money1 = money - Double.parseDouble(String.format("%.2f", 0.29 * people));// 保证
																												// 领取的红包
																												// 在0.3元以上
							if (money1 > 0) {
								w = new WeixinMoney(people, money1);
								award = WeixinMoney.getMoney(w) + 0.29;
							} else {
								return "error";
							}
						} else if (job_type == 1) {
							award = (Double) job.get("one_award");
						}
						
						if (award > 0) {
							result = saveVoice(userId, id, dataPath, award, num, 0, second,
									(Integer) job.get("version"), "success");// 成功得代表
							if (result.indexOf("success") != -1) {// 代表乐观锁起作用了(通过)
								if (people == 1 && (int) job.get("state") == 1) {
									jobDao.updateState(2, id);
									// 消息推送 红包已完成
									returnParam = dataDao.getFormid(userId);
									if (returnParam != null) {
										String msg = Util.getMsg(returnParam, job, 2);
										userService.sendMsg(msg);
										dataDao.updateState((int) returnParam.get("id"));
									}
								}
							}
						}
					} else {// 代表不通过
						result = saveVoice(userId, id, dataPath, 0.00, num, 1, second, (Integer) job.get("version"),
								"fail");// 失败得代表;
					}
					voice = null;

				} else {
					result = "error";
				}

				// 删除本地两个文件
				file = new File(inputFilePath);
				file.delete();
				file = new File(outputFilePath);
				file.delete();

			}

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
		return result;
	}
	
	
	/**
	 * 读完 正确之后开始保存
	 */
	public String saveVoice(int userId, int jobId, String voicePath, double award, double rate, int state, int second,
			int version, String code) {
		Map<String, Object> voice = new HashMap<String, Object>();
		redis.deleteMineVoice(userId,jobId);//删除缓存
		String result = "";
		String id = "";
		voice.put("userId", userId);
		voice.put("jobId", jobId);
		voice.put("voice_path", voicePath);
		voice.put("award", award);
		voice.put("rate", rate);
		voice.put("state", state);
		voice.put("second", second);
		if ("success".equals(code)) {
			int num = jobDao.modifyCount(award, jobId, version);
			if (num != 0) {// 表示可行
				adminDao.modifyMoney(award, userId);
				voiceRecordDao.saveVoice(voice);
				redis.deleteVoiceRedis(userId, jobId);//删除缓存
				redis.deleteRedisJob(jobId);//删除缓存
				id = voice.get("id") + "";
				result = "success," + id;
			} else {
				voice.put("award", 0);
				voice.put("state", 1);
				voice.put("rate", 79);
				voiceRecordDao.saveVoice(voice);
				id = voice.get("id") + "";
				result = "fail," + id;
			}
		} else {
			voiceRecordDao.saveVoice(voice);
			id = voice.get("id") + "";
			result = "fail," + id;
		}
		voice = null;
		return result;
	}
	
	/**
	 * 保存证书
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
	 * 保存证书
	 * 
	 * @return
	 */
	@Transactional
	public String saveSharePic(InputStream input, int id) {
		String name = System.currentTimeMillis() + "";
		String dataPath = "";
		try {
			String ossSavePath = "static/sharePic/" + name + ".png";// oss保存路径
			dataPath = "https://static.yaohoudy.com/static/sharePic/" + name + ".png";// 数据库位置
			Util.ossLoad(endPoint, accessKeyId, accessKeySecret, bucketName, ossSavePath, input);
			jobDao.updateSharePic(dataPath, id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dataPath;
	}

}
