package com.connection.controller;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.connection.dao.DataDao;
import com.connection.dao.JobDao;
import com.connection.dao.VoiceRecordDao;
import com.connection.service.interfaces.JobService;
import com.connection.service.interfaces.RedisService;
import com.connection.tool.Result;

@Controller
public class JobController {
	public static Logger log = Logger.getLogger(JobController.class);
	@Autowired
	private JobDao jobDao;
	@Autowired
	private JobService jobService;
	@Autowired
	private DataDao dataDao;
	@Autowired
	private VoiceRecordDao voiceRecordDao;
	@Autowired
	private RedisService redis;

	/**
	 * 获得我自己发布得红包
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getRecordData")
	public Result getCommand(@RequestParam("id") int id, @RequestParam("userId") int userId,
			@RequestParam("tabId") int tabId) {
		Result result = null;
		Map<String, Object> resInfo = null;
		try {
			result = Result.successResult();
			resInfo = new HashMap<String, Object>();
			if (tabId == 0) {
				resInfo.put("jobs", jobDao.getMyPush(userId, id));
			} else {
				resInfo.put("jobs", voiceRecordDao.getMyJoin(userId, id));
			}

			result.setObj(resInfo);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			resInfo = null;
		}
		return result;
	}

	/**
	 * 获得具体得红包信息
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/toPackage")
	public Result getJobById(@RequestParam("id") int id, @RequestParam("userId") int userId) {
		Result result = null;
		Map<String, Object> resInfo = null;
		try {
			result = Result.successResult();
			resInfo = new HashMap<String, Object>();
			resInfo.put("job", redis.getJobById(id));
			resInfo.put("voice", redis.checkVoice(userId, id));
			result.setObj(resInfo);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			resInfo = null;
		}
		return result;
	}

	/**
	 * 上传录音文件
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/upload")
	public Result doVedioLoad(@RequestParam("userId") int userId, @RequestParam("jobId") int id,
			@RequestParam("second") int second, HttpServletRequest request) {
		Result result = null;
		Map<String, Object> resInfo = null;
		MultipartFile file = null;
		InputStream input = null;
		try {
			result = Result.successResult();
			resInfo = new HashMap<String, Object>();

			Map<String, Object> job = jobDao.getJobById2(id);
			if (job != null && job.get("transaction_id") != null) {
				if ((Integer) job.get("state") == 4 && (Long) job.get("timeLimit") < 0) {
					
				} else {
					file = ((MultipartHttpServletRequest) request).getFile("file");
					input = file.getInputStream();// 获得文件输入流
					String str = jobService.saveVedio(input, id, userId, second);
					if ("error".equals(str) || "haveNoChance".equals(str)) {
						resInfo.put("code", str);
						resInfo.put("voice", null);
					} else if (str.indexOf(",") != -1) {
						log.info("语音读写完成:" + str);
						String[] strArr = str.split(",");
						resInfo.put("code", strArr[0]);
						resInfo.put("voice", voiceRecordDao.findOne(Integer.parseInt(strArr[1])));
						
					}
				}
			}else{
				resInfo.put("code", "haveNoChance");
				resInfo.put("voice", null);
			}
			result.setObj(resInfo);
		} catch (Exception e) {
//			e.printStackTrace();
			log.info("upload接口错误");
		} finally {
			resInfo = null;
		}
		return result;
	}

	/**
	 * 获得该任务下面得所有语音
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/packageGetVoice")
	public Result packageGetVoice(@RequestParam("jobId") int jobId, @RequestParam("id") int id,
			@RequestParam("userId") int userId) {
		Result result = null;
		Map<String, Object> resInfo = null;
		List<HashMap<String,Object>>voices = null;
		HashMap<String,Object>mineVoice = null;
		try {
			result = Result.successResult();
			voices = new ArrayList<HashMap<String,Object>>();
			resInfo = new HashMap<String, Object>();
			if(id == 0){
				mineVoice = redis.getMineVoice(userId, jobId);//从缓存中获得语音
				if(mineVoice != null){
					voices.add(mineVoice);
				}
			}
			voices.addAll(voiceRecordDao.findAllVoice(jobId, id, userId));
			resInfo.put("voices", voices);
			result.setObj(resInfo);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			resInfo = null;
			voices = null;
			mineVoice = null;
		}
		return result;
	}

	/**
	 * 上传证书
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/loadBook")
	public Result loadBook(@RequestParam("id") int id, HttpServletRequest request) {
		Result result = null;
		Map<String, Object> resInfo = null;
		MultipartFile file = null;
		InputStream input = null;
		try {
			result = Result.successResult();
			resInfo = new HashMap<String, Object>();
			file = ((MultipartHttpServletRequest) request).getFile("image");
			input = file.getInputStream();// 获得文件输入流

			resInfo.put("bookPath", jobService.saveBook(input, id));
			result.setObj(resInfo);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			resInfo = null;
		}
		return result;
	}

	/**
	 * 上传红包分享图
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/loadSharePic")
	public Result loadSharePic(@RequestParam("id") int id, HttpServletRequest request) {
		Result result = null;
		Map<String, Object> resInfo = null;
		MultipartFile file = null;
		InputStream input = null;
		try {
			result = Result.successResult();
			resInfo = new HashMap<String, Object>();
			file = ((MultipartHttpServletRequest) request).getFile("image");
			input = file.getInputStream();// 获得文件输入流

			resInfo.put("sharePic", jobService.saveSharePic(input, id));
			result.setObj(resInfo);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			resInfo = null;
		}
		return result;
	}

	/**
	 * 福利界面
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getTimerJob")
	public Result getTimerJob(@RequestParam("id") int id, @RequestParam("userId") int userId) {
		Result result = null;
		Map<String, Object> resInfo = null;
		try {
			result = Result.successResult();
			resInfo = new HashMap<String, Object>();
			resInfo.put("jobs", redis.cachTimerJob(id));
			resInfo.put("apps", dataDao.getApps());
			result.setObj(resInfo);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			resInfo = null;
		}
		return result;
	}
}
