package com.connection.controller;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.aliyun.oss.common.comm.ServiceClient.Request;
import com.connection.dao.BegJobDao;
import com.connection.dao.DataDao;
import com.connection.dao.JobDao;
import com.connection.dao.VoiceRecordDao;
import com.connection.service.interfaces.BegJobService;
import com.connection.service.interfaces.JobService;
import com.connection.service.interfaces.RedisService;
import com.connection.service.interfaces.UserService;
import com.connection.tool.Result;
import com.connection.tool.Util;

@Controller
public class JobController {
	public static Logger log = Logger.getLogger(JobController.class);
	@Autowired
	private JobDao jobDao;
	@Autowired
	private JobService jobService;
	@Autowired
	private BegJobService begJobService;
	@Autowired
	private BegJobDao begJobDao;
	@Autowired
	private DataDao dataDao;
	@Autowired
	private VoiceRecordDao voiceRecordDao;
	@Autowired
	private RedisService redis;
	@Autowired
	private UserService userService;

	/**
	 * 获得我自己发布得红包（record）
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
				//我发的
				resInfo.put("jobs", jobDao.getMyPush(userId, id));
			} else {
				//我抢的
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
	 * 获得讨或者被讨的红包（begrecord）
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getBegRecordData")
	public Result getBegCommand(@RequestParam("id") int id , @RequestParam("userId") int userId,
			@RequestParam("tabId") int tabId ,HttpServletRequest request,HttpServletResponse response) {
		Result result = null;
		Map<String, Object> resInfo = null;
		try {
			result = Result.successResult();
			resInfo = new HashMap<String, Object>();
			
			String cc = request.getParameter("header");
			
			if(!"over".equals(cc)&&tabId == 0) {
			
						List<HashMap<String,Object>> list2=begJobDao.getMyBegPush2(userId,id,10);
						resInfo.put("jobs",list2 );	
						result.setObj(resInfo);
						Cookie cookie = new Cookie("go","go");
						response.addCookie(cookie);
						return result;
		}
			if (tabId == 0) {
				//我讨的
				List<HashMap<String,Object>> list=begJobDao.getMyBegPush(userId, id);
				if(list.size()==10) {
				resInfo.put("jobs", list);
				}else {
					List<HashMap<String,Object>> list2=begJobDao.getMyBegPush2(userId,0,10-(list.size()));
					list.addAll(list2);

				
					resInfo.put("jobs",list );	
					Cookie cookie = new Cookie("go","go");
					response.addCookie(cookie);
				

					resInfo.put("jobs",list );

				}
				//resInfo.put("jobs", list);
				
			} else {
				//我被讨的
				resInfo.put("jobs", begJobDao.getMyBeggedPush(userId, id));
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
	 * 获得具体得红包信息（先从缓存，没有从数据库拿）
	 * 我想是不是可以利用数据库的selectKey做文章，但是这个好像只能插入主键
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
	 * 获得具体得tao红包信息（先从缓存，没有从数据库拿）
	 * @param id
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/toBegPackage")
	public Result getBegJobById(@RequestParam("id") int id) {
		Result result = null;
		Map<String, Object> resInfo = null;
		try {
			result = Result.successResult();
			resInfo = new HashMap<String, Object>();
			resInfo.put("job", redis.getBegJobById(id));
			resInfo.put("begJobRecord", redis.getRecord(id));
//			redis.deleteRecord(id);
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
	//second代表时长（秒）
	public Result doVedioLoad(@RequestParam("userId") int userId, @RequestParam("jobId") int id,
			@RequestParam("second") int second, HttpServletRequest request) {
		Result result = null;
		Map<String, Object> resInfo = null;
		MultipartFile file = null;
		InputStream input = null;
		try {
			result = Result.successResult();
			resInfo = new HashMap<String, Object>();
			//<!-- 	判断语音是否可讲 -->，返回map集合
			Map<String, Object> job = jobDao.getJobById2(id);
			//transaction_id没有这个说明没付款成功
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
			//这里的id是前端传过来的count
			if(id == 0){
				mineVoice = redis.getMineVoice(userId, jobId);//从缓存中获得语音（自己的）
				if(mineVoice != null){
					voices.add(mineVoice);
				}
			}
			//除自己以外的（<!-- 这里是考虑下拉刷新 -->）
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
	 * 上传红包分享图（job）
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
	 * 上传红包分享图(beg表)
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/loadBegSharePic")
	public Result loadBegSharePic(@RequestParam("id") int id, HttpServletRequest request) {
		Result result = null;
		Map<String, Object> resInfo = null;
		MultipartFile file = null;
		InputStream input = null;
		try {
			result = Result.successResult();
			resInfo = new HashMap<String, Object>();
			file = ((MultipartHttpServletRequest) request).getFile("image");
			input = file.getInputStream();// 获得文件输入流

			resInfo.put("sharePic", begJobService.saveBegSharePic(input, id));
			redis.deleteRedisBegJob(id);
			result.setObj(resInfo);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			resInfo = null;
		}
		return result;
	}

//	/**
//	 * 福利界面
//	 * 
//	 * @param id
//	 * @param request
//	 * @return
//	 */
//	@ResponseBody
//	@RequestMapping("/getTimerJob")
//	public Result getTimerJob(@RequestParam("id") int id, @RequestParam("userId") int userId) {
//		Result result = null;
//		Map<String, Object> resInfo = null;
//		try {
//			result = Result.successResult();
//			resInfo = new HashMap<String, Object>();
//			resInfo.put("jobs", redis.cachTimerJob(id));
//			resInfo.put("apps", dataDao.getApps());
//			result.setObj(resInfo);
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			resInfo = null;
//		}
//		return result;
//	}
	/**
	 * 获得吉利数值
	 */
	@ResponseBody
	@RequestMapping("/getLuckyNumber")
	public Result  getLuckyNumber() {
		Result result=null;
		List<String> number= redis.getLuckyNumber();
		result = Result.successResult();
		result.setObj(number);
		
		return result;
		
	}
	/**
	 * 添加吉利数值
	 */
	@ResponseBody
	@RequestMapping("/addLuckyNumber")
	public Result  addLuckyNumber(@RequestParam("number")String number) {
		Result result=null;
		//吉利数字判断增加（考虑的又不全面了= =）
		ArrayList<String> list =(ArrayList<String>) redis.getLuckyNumber();
		if(list.contains(number)) {
			result=Result.errorResult();
			result.setMsg("已存在该数字");
			return result;
		}
	
		 if(Double.parseDouble(number)<=200) {
		int cc =begJobDao.addLuckyNumber( Double.parseDouble(number));
		if(cc==1) {
		result = Result.successResult();
		redis.deleteLuckyNumber();
		
		}else {
			result=Result.errorResult();
			result.setMsg("数据库操作失败");
		}
		 }else {
			 result=Result.errorResult();
			 result.setMsg("金额大于200");
		 }
		return result;
		
	}
	/**
	 * 修改答谢状态
	 * 
	 */
	@ResponseBody
	@RequestMapping("/thanks")
	public Result thanks(@RequestParam("id")int id,@RequestParam("userId")int userId ) {
		Result result=null;
	
		 int cc =begJobDao.updataBegRecordState(id,userId);
		 
		if(cc==1) {
			
			//消息通知 发送感谢信
			Map<String,Object>returnParam = dataDao.getFormid(userId);//formid
			Map<String, Object> job =begJobDao.getBegJobById(id);//取该红包
			String msg = Util.getMsg(returnParam, job, 5);
			userService.sendMsg(msg);//前端调用saveFormId保存，这里发出去
			dataDao.updateState((Integer)returnParam.get("id"));
			redis.deleteRecord(id);
			result = Result.successResult();
		}else {
			result = Result.errorResult();
		}
		
		
		return result;
		
	}
}
