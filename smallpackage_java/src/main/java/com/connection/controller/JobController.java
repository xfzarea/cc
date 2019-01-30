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
	 * ������Լ������ú����record��
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
				//�ҷ���
				resInfo.put("jobs", jobDao.getMyPush(userId, id));
			} else {
				//������
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
	 * ����ֻ��߱��ֵĺ����begrecord��
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
				//���ֵ�
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
				//�ұ��ֵ�
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
	 * ��þ���ú����Ϣ���ȴӻ��棬û�д����ݿ��ã�
	 * �����ǲ��ǿ����������ݿ��selectKey�����£������������ֻ�ܲ�������
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
	 * ��þ����tao�����Ϣ���ȴӻ��棬û�д����ݿ��ã�
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
	 * �ϴ�¼���ļ�
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/upload")
	//second����ʱ�����룩
	public Result doVedioLoad(@RequestParam("userId") int userId, @RequestParam("jobId") int id,
			@RequestParam("second") int second, HttpServletRequest request) {
		Result result = null;
		Map<String, Object> resInfo = null;
		MultipartFile file = null;
		InputStream input = null;
		try {
			result = Result.successResult();
			resInfo = new HashMap<String, Object>();
			//<!-- 	�ж������Ƿ�ɽ� -->������map����
			Map<String, Object> job = jobDao.getJobById2(id);
			//transaction_idû�����˵��û����ɹ�
			if (job != null && job.get("transaction_id") != null) {
				if ((Integer) job.get("state") == 4 && (Long) job.get("timeLimit") < 0) {
					
				} else {
					file = ((MultipartHttpServletRequest) request).getFile("file");
					input = file.getInputStream();// ����ļ�������
					String str = jobService.saveVedio(input, id, userId, second);
					if ("error".equals(str) || "haveNoChance".equals(str)) {
						resInfo.put("code", str);
						resInfo.put("voice", null);
					} else if (str.indexOf(",") != -1) {
						log.info("������д���:" + str);
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
			log.info("upload�ӿڴ���");
		} finally {
			resInfo = null;
		}
		return result;
	}

	
	/**
	 * ��ø������������������
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
			//�����id��ǰ�˴�������count
			if(id == 0){
				mineVoice = redis.getMineVoice(userId, jobId);//�ӻ����л���������Լ��ģ�
				if(mineVoice != null){
					voices.add(mineVoice);
				}
			}
			//���Լ�����ģ�<!-- �����ǿ�������ˢ�� -->��
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
	 * �ϴ�֤��
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
			input = file.getInputStream();// ����ļ�������

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
	 * �ϴ��������ͼ��job��
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
			input = file.getInputStream();// ����ļ�������

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
	 * �ϴ��������ͼ(beg��)
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
			input = file.getInputStream();// ����ļ�������

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
//	 * ��������
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
	 * ��ü�����ֵ
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
	 * ��Ӽ�����ֵ
	 */
	@ResponseBody
	@RequestMapping("/addLuckyNumber")
	public Result  addLuckyNumber(@RequestParam("number")String number) {
		Result result=null;
		//���������ж����ӣ����ǵ��ֲ�ȫ����= =��
		ArrayList<String> list =(ArrayList<String>) redis.getLuckyNumber();
		if(list.contains(number)) {
			result=Result.errorResult();
			result.setMsg("�Ѵ��ڸ�����");
			return result;
		}
	
		 if(Double.parseDouble(number)<=200) {
		int cc =begJobDao.addLuckyNumber( Double.parseDouble(number));
		if(cc==1) {
		result = Result.successResult();
		redis.deleteLuckyNumber();
		
		}else {
			result=Result.errorResult();
			result.setMsg("���ݿ����ʧ��");
		}
		 }else {
			 result=Result.errorResult();
			 result.setMsg("������200");
		 }
		return result;
		
	}
	/**
	 * �޸Ĵ�л״̬
	 * 
	 */
	@ResponseBody
	@RequestMapping("/thanks")
	public Result thanks(@RequestParam("id")int id,@RequestParam("userId")int userId ) {
		Result result=null;
	
		 int cc =begJobDao.updataBegRecordState(id,userId);
		 
		if(cc==1) {
			
			//��Ϣ֪ͨ ���͸�л��
			Map<String,Object>returnParam = dataDao.getFormid(userId);//formid
			Map<String, Object> job =begJobDao.getBegJobById(id);//ȡ�ú��
			String msg = Util.getMsg(returnParam, job, 5);
			userService.sendMsg(msg);//ǰ�˵���saveFormId���棬���﷢��ȥ
			dataDao.updateState((Integer)returnParam.get("id"));
			redis.deleteRecord(id);
			result = Result.successResult();
		}else {
			result = Result.errorResult();
		}
		
		
		return result;
		
	}
}
