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
		String appid = "5c2f2717";
		String secret = "b8472321c07517d13536c258d5c34285";
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

			String dataPath = "https://static.yaohoudy.com/static/vedio/" + name + ".wav";// 数据库位置
			String ossSavePath = "static/vedio/" + name + ".wav";// oss保存路径
			if (flag) {
				//拿到讯飞的账号
				Map<String, Object> xunfei = dataDao.getXunfei();
				if (xunfei != null) {
					appid = (String) xunfei.get("appid");
					secret = (String) xunfei.get("secret");
					//讯飞inCount + 1
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
					//把数据库中的context变成拼音
					rightAnswer = PinYinUtil.getFullSpell(rightAnswer);

					double num = EqualStr.getSimilarityRatio(data, rightAnswer);// 准确率

					
					file = new File(outputFilePath);// 保存文件以及改变数据
					fileInput = new FileInputStream(file);
					Util.ossLoad(endPoint, accessKeyId, accessKeySecret, bucketName, ossSavePath, fileInput);//存到阿里oss
					fileInput.close();
					//查询语音信息并放入缓存中
					Map<String, Object> voice = redis.checkVoice(userId, id);
					log.info("jobService中缓存得voice:"+voice);
					if (num >= 80 && voice == null) {// 代表通过
						int people = (int) job.get("totalCount") - (int) job.get("alreadyCount");
						double award = 0.00;
						int job_type = (Integer) job.get("job_type");
						double money = 0.00;
						if (job_type == 0) {//0是拼手气
							money = (Double) job.get("award") - (Double) job.get("alreadyAward");
							if (money > 0) {
								//这是算法参数的构造器，new
								w = new WeixinMoney(people, money);
								//使用算法，因为至少得出0.01，所以加上0.29肯定在0.3之上
								award = WeixinMoney.getMoney(w);
							} else {
								return "error";
							}
						} else if (job_type == 1) {//job_type =1是普通
							award = (Double) job.get("one_award");
						}
						
						if (award > 0) {
							result = saveVoice(userId, id, dataPath, award, num, 0, second,(Integer) job.get("version"), "success");// 成功得代表
							if (result.indexOf("success") != -1) {// 代表乐观锁起作用了(通过)
								if (people == 1 && (int) job.get("state") == 1) {
									jobDao.updateState(2, id);//设置红包已完成
									// 消息推送 红包已完成
									returnParam = dataDao.getFormid(userId);	//查询 user_formid.id,user_formid.formid,user_formid.userId,admin.openid，
																				//根据 userId 、state 、TIMESTAMPDIFF(HOUR,insertTime,NOW()) &lt; 167 LIMIT 1（插入到现在的时间小于167个小时）
									if (returnParam != null) {
										String msg = Util.getMsg(returnParam, job, 2);
										userService.sendMsg(msg);
										dataDao.updateState((int) returnParam.get("id"));//更新user_formid表的state变成1，根据id
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
			int num = jobDao.modifyCount(award, jobId, version);//这个sql语句使用了乐观锁（version），//一个人抢走红包之后，跟新红包信息
			if (num != 0) {// 表示可行
				adminDao.modifyMoney(award, userId);//根据用户id，menoy增加award的钱
				voiceRecordDao.saveVoice(voice);//插入语音信息到语音表，并插入主键信息到voice
				redis.deleteVoiceRedis(userId, jobId);//删除语音缓存
				redis.deleteRedisJob(jobId);//删除红包缓存
				id = voice.get("id") + "";
				result = "success," + id;
			} else {
				voice.put("award", 0);
				voice.put("state", 1);//这里是语音表中的state，1是没抢成功的语音状态，0是成功的
				voice.put("rate", 79);
				voiceRecordDao.saveVoice(voice);//插入语音信息到语音表，，并插入主键信息到voice
				id = voice.get("id") + "";
				result = "fail," + id;
			}
		} else {
			voiceRecordDao.saveVoice(voice);//插入语音信息到语音表，，并插入主键信息到voice
			id = voice.get("id") + "";
			result = "fail," + id;
		}
		voice = null;
		return result;
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
