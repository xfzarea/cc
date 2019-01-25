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
	private String ossVedio = "/static/vedio/";// oss�洢λ��

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
	 * ����¼���ļ����ҵõ�����
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
			//�����MP3�ļ�
			byte[] bs = new byte[1024];
			String inputFilePath = dedioPath + name + ".mp3";
			os = new FileOutputStream(inputFilePath);
			while ((len = input.read(bs)) != -1) {
				os.write(bs, 0, len);
			}
			//��ʽת��
			String outputFilePath = dedioPath + name + ".wav";
			boolean flag = Util.Mp3ToWav(inputFilePath, outputFilePath);

			String dataPath = "https://static.yaohoudy.com/static/vedio/" + name + ".wav";// ���ݿ�λ��
			String ossSavePath = "static/vedio/" + name + ".wav";// oss����·��
			if (flag) {
				//�õ�Ѷ�ɵ��˺�
				Map<String, Object> xunfei = dataDao.getXunfei();
				if (xunfei != null) {
					appid = (String) xunfei.get("appid");
					secret = (String) xunfei.get("secret");
					//Ѷ��inCount + 1
					dataDao.updateXunfei((int) xunfei.get("id"));
				}
				String data = TestWebIat.parse(outputFilePath, appid, secret);// �õ������е�����
																				// ƴ��
				job = redis.getJobById(id);// �ֹ���
				log.info("jobService�л����job:"+job);
				if (job.get("alreadyAward") == job.get("award") && job.get("totalCount") == job.get("alreadyCount")) {// ˵��û�л�����
					result = "haveNoChance";
				} else if (job.get("alreadyCount") != job.get("award")
						&& job.get("totalCount") != job.get("alreadyCount")) {// �ɹ�
					String rightAnswer = (String) job.get("context");
					//�����ݿ��е�context���ƴ��
					rightAnswer = PinYinUtil.getFullSpell(rightAnswer);

					double num = EqualStr.getSimilarityRatio(data, rightAnswer);// ׼ȷ��

					
					file = new File(outputFilePath);// �����ļ��Լ��ı�����
					fileInput = new FileInputStream(file);
					Util.ossLoad(endPoint, accessKeyId, accessKeySecret, bucketName, ossSavePath, fileInput);//�浽����oss
					fileInput.close();
					//��ѯ������Ϣ�����뻺����
					Map<String, Object> voice = redis.checkVoice(userId, id);
					log.info("jobService�л����voice:"+voice);
					if (num >= 80 && voice == null) {// ����ͨ��
						int people = (int) job.get("totalCount") - (int) job.get("alreadyCount");
						double award = 0.00;
						int job_type = (Integer) job.get("job_type");
						double money = 0.00;
						if (job_type == 0) {//0��ƴ����
							money = (Double) job.get("award") - (Double) job.get("alreadyAward");
							if (money > 0) {
								//�����㷨�����Ĺ�������new
								w = new WeixinMoney(people, money);
								//ʹ���㷨����Ϊ���ٵó�0.01�����Լ���0.29�϶���0.3֮��
								award = WeixinMoney.getMoney(w);
							} else {
								return "error";
							}
						} else if (job_type == 1) {//job_type =1����ͨ
							award = (Double) job.get("one_award");
						}
						
						if (award > 0) {
							result = saveVoice(userId, id, dataPath, award, num, 0, second,(Integer) job.get("version"), "success");// �ɹ��ô���
							if (result.indexOf("success") != -1) {// �����ֹ�����������(ͨ��)
								if (people == 1 && (int) job.get("state") == 1) {
									jobDao.updateState(2, id);//���ú�������
									// ��Ϣ���� ��������
									returnParam = dataDao.getFormid(userId);	//��ѯ user_formid.id,user_formid.formid,user_formid.userId,admin.openid��
																				//���� userId ��state ��TIMESTAMPDIFF(HOUR,insertTime,NOW()) &lt; 167 LIMIT 1�����뵽���ڵ�ʱ��С��167��Сʱ��
									if (returnParam != null) {
										String msg = Util.getMsg(returnParam, job, 2);
										userService.sendMsg(msg);
										dataDao.updateState((int) returnParam.get("id"));//����user_formid���state���1������id
									}
								}
							}
						}
					} else {// ����ͨ��
						result = saveVoice(userId, id, dataPath, 0.00, num, 1, second, (Integer) job.get("version"),
								"fail");// ʧ�ܵô���;
					}
					voice = null;

				} else {
					result = "error";
				}

				// ɾ�����������ļ�
				file = new File(inputFilePath);
				file.delete();
				file = new File(outputFilePath);
				file.delete();

			}

		} catch (Exception e) {
//			e.printStackTrace();
			log.info("upload�ڲ���������");
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
	 * ���� ��ȷ֮��ʼ����
	 */
	public String saveVoice(int userId, int jobId, String voicePath, double award, double rate, int state, int second,
			int version, String code) {
		Map<String, Object> voice = new HashMap<String, Object>();
		redis.deleteMineVoice(userId,jobId);//ɾ������
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
			int num = jobDao.modifyCount(award, jobId, version);//���sql���ʹ�����ֹ�����version����//һ�������ߺ��֮�󣬸��º����Ϣ
			if (num != 0) {// ��ʾ����
				adminDao.modifyMoney(award, userId);//�����û�id��menoy����award��Ǯ
				voiceRecordDao.saveVoice(voice);//����������Ϣ��������������������Ϣ��voice
				redis.deleteVoiceRedis(userId, jobId);//ɾ����������
				redis.deleteRedisJob(jobId);//ɾ���������
				id = voice.get("id") + "";
				result = "success," + id;
			} else {
				voice.put("award", 0);
				voice.put("state", 1);//�������������е�state��1��û���ɹ�������״̬��0�ǳɹ���
				voice.put("rate", 79);
				voiceRecordDao.saveVoice(voice);//����������Ϣ����������������������Ϣ��voice
				id = voice.get("id") + "";
				result = "fail," + id;
			}
		} else {
			voiceRecordDao.saveVoice(voice);//����������Ϣ����������������������Ϣ��voice
			id = voice.get("id") + "";
			result = "fail," + id;
		}
		voice = null;
		return result;
	}
	
	/**
	 * ����֤��//voice��
	 * 
	 * @return
	 */
	@Transactional
	public String saveBook(InputStream input, int id) {
		String name = System.currentTimeMillis() + "";
		String dataPath = "";
		try {
			String ossSavePath = "static/book/" + name + ".png";// oss����·��
			dataPath = "https://static.yaohoudy.com/static/book/" + name + ".png";// ���ݿ�λ��
			Util.ossLoad(endPoint, accessKeyId, accessKeySecret, bucketName, ossSavePath, input);
			voiceRecordDao.saveBook(dataPath, id);
		} catch (Exception e) {

		}
		return dataPath;
	}


	/**
	 * �������ͼ//job��
	 * 
	 * @return
	 */
	@Transactional
	public String saveSharePic(InputStream input, int id) {
		String name = System.currentTimeMillis() + "";
		String dataPath = "";
		try {
			String ossSavePath = "static/sharePic/" + name + ".png";// oss����·��
			dataPath = "https://static.yaohoudy.com/static/sharePic/" + name + ".png";// ���ݿ�λ��
			Util.ossLoad(endPoint, accessKeyId, accessKeySecret, bucketName, ossSavePath, input);
			jobDao.updateSharePic(dataPath, id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dataPath;
	}

}
