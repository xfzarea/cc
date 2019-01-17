package com.connection.controller;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.connection.dao.SysAdminDao;
import com.connection.entity.Admin;
import com.connection.service.interfaces.BegJobService;
import com.connection.service.interfaces.RedisService;
import com.connection.service.interfaces.SystemHandler;
import com.connection.service.interfaces.UserService;
import com.connection.tool.HttpUtils;
import com.connection.tool.Result;

@Controller
public class SystemController {
	public static Logger log = Logger.getLogger(UserController.class);
	@Value("#{wx.sysappid}")
	private String appId;
	@Value("#{wx.syssecret}")
	private String secret;
	@Autowired
	private UserService userService;
	@Autowired
	private SystemHandler systemHandler;
	@Autowired
	private SysAdminDao sysAdminDao;
	@Autowired
	private BegJobService begJobService;
	@Autowired
	private RedisService redis;
	/**
	 * ����˵ϵͳ�û���¼
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/system/appLogin")
	public Result appLogin(@RequestParam Map<String,String>params,HttpServletRequest request){
		Result result = null;
		Admin admin = null;
		Map<String,Object>resInfo = null;
		try {
			result = Result.successResult();
			String resultStr = userService.sendData(params.get("code"), appId, secret);
			JSONObject json = JSON.parseObject(resultStr);
			String openid = json.getString("openid");
			admin = new Admin();
			admin.setOpenid(openid);
			request.getSession().setAttribute("admin", admin);
			admin.setGender(Integer.parseInt(params.get("gender")));
			admin.setProvince(params.get("province"));
			admin.setCity(params.get("city"));
			admin.setNickName(params.get("nickName"));
			admin.setAvatarUrl(params.get("avatarUrl"));
			//�����û���Ϣ�����õ���������Ϣ
			admin = systemHandler.saveAdmin(admin);
			resInfo = new HashMap<String,Object>();
			resInfo.put("admin", admin);
			result.setObj(resInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * ����Ա������ʱ���
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/createTimerJob")
	public Result createTimerJob(@RequestParam Map<String,String>params){
		Result result = null;
		Map<String,Object>resInfo = null;
		int id = 0;
		try {
			result = Result.successResult();
			//������ʱ���api
			id = systemHandler.createTimerJob(params);
			resInfo = new HashMap<String,Object>();
			//���ض�ʱ�����id
			resInfo.put("id", id);
			result.setObj(resInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * ����Ա�����û��Զ�����
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/createUserJob")
	public Result createUserJob(@RequestParam Map<String,String>params){
		Result result = null;
		Map<String,Object>resInfo = null;
		try {
			result = Result.successResult();
			int id = systemHandler.createUserJob(params);
			resInfo = new HashMap<String,Object>();
			resInfo.put("id", id);
			result.setObj(resInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * ����Ա��ú���б�
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getJobs")
	public Result getJobs(@RequestParam Map<String,String>params){
		Result result = null;
		List<HashMap<String,Object>>jobs = null;
		Map<String,Object>resInfo = null;
		try {
			result = Result.successResult();
			jobs =  sysAdminDao.getJobs();
			resInfo = new HashMap<String,Object>();
			//���صĵ���list����
			resInfo.put("jobs", jobs);
			result.setObj(resInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			jobs = null;
			resInfo = null;
		}
		return result;
	}
	
	/**
	 * ����Աɾ�����
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/delateJob")
	//�����id�Ǻ��id��openId�ǽ���������ģ���Ϊ�Ǻ�̨ϵͳ�����Է���Ҳû��
	public Result delateJob(@RequestParam("openid")String openid,@RequestParam("id")int id){
		Result result = null;
		List<HashMap<String,Object>>jobs = null;
		Map<String,Object>resInfo = null;
		try {
			result = Result.successResult();
			systemHandler.deleteJob(openid, id);
			resInfo = new HashMap<String,Object>();
			
			result.setObj(resInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			jobs = null;
			resInfo = null;
		}
		return result;
	}
	
	/**
	 * ����Ա��õ�һ���
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/sysGetJobById")
	public Result sysGetJobById(int id,int state){
		Result result = null;
		HashMap<String,Object>job = null;
		Map<String,Object>resInfo = null;
		try {
			result = Result.successResult();
			if(state == 1 || state == 2){
				//���ش�֧������֧���ĺ��
				job=sysAdminDao.getUserJobById(id);
			}else if(state == 4){
				//���ض�ʱ���
				job = sysAdminDao.getTimerJobById(id);
			}
			resInfo = new HashMap<String,Object>();
			resInfo.put("job", job);
			result.setObj(resInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			job = null;
			resInfo = null;
		}
		return result;
	}
	
	
	/**
	 * �ϴ�����ͼƬ����
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/sysCommandImage")
	public Result sysCommandImage(@RequestParam("fatherId") int fatherId, HttpServletRequest request) {
		Result result = null;
		Map<String, Object> resInfo = null;
		MultipartFile file = null;
		InputStream input = null;
		try {
			result = Result.successResult();
			resInfo = new HashMap<String, Object>();
			file = ((MultipartHttpServletRequest) request).getFile("image");
			input = file.getInputStream();// ����ļ�������

			resInfo.put("commandImage", begJobService.saveSysCommandImage(input, fatherId));
			redis.deleteCommandImage(fatherId);//����ʹ��
		
			result.setObj(resInfo);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			resInfo = null;
		}
		return result;
		
	}
	/**
	 * �ϴ�������������
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/sysVoiceCommand")
	
	public Result sysVoiceCommand(@RequestParam("fatherId") int fatherId 
			,@RequestParam("context") String context, HttpServletRequest request) {
		Result result = null;
		Map<String, Object> resInfo = null;
		MultipartFile file = null;
		InputStream input = null;
		try {
			result = Result.successResult();
			resInfo = new HashMap<String, Object>();
					file = ((MultipartHttpServletRequest) request).getFile("file");
					input = file.getInputStream();// ����ļ�������
					
				resInfo.put("voice", begJobService.saveSysVoice(input, fatherId,context));
				redis.deleteVoiceCommand(fatherId);//����ʹ��	
					
				
			
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
	 * �ϴ�������Ƶ���� (�ӿ���Ч������Ƶ��ʱ������)
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	//@ResponseBody
	//@RequestMapping("/sysBegVedio")
	public Result sysBegVedio(@RequestParam("fatherId") int fatherId, HttpServletRequest request) {
		Result result = null;
		Map<String, Object> resInfo = null;
		MultipartFile file = null;
		InputStream input = null;
		try {
			result = Result.successResult();
			resInfo = new HashMap<String, Object>();
			file = ((MultipartHttpServletRequest) request).getFile("file");
			input = file.getInputStream();// ����ļ�������
	
			resInfo.put("vedio", begJobService.saveSysVedioCommand(input, fatherId));
			redis.deleteVedioCommand(fatherId);//����ʹ��
			result.setObj(resInfo);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			resInfo = null;
		}
		return result;
		
	}
	/**
	 * �ϴ����ֿ��� ��//�м�⣨job��
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/sysCommand")
	public Result sysCommand(@RequestParam("contextId") int contextId,@RequestParam("levelContextId") int levelContextId,@RequestParam("context") String context, HttpServletRequest request) {
		Result result = null;
		Map<String, Object> resInfo = null;
		Boolean flag =HttpUtils.checkWord(context);
//		if(flag) {
//			result = Result.errorResult();
//			result.setMsg("�����д�");
//			return result;
//		}
		try {
			result = Result.successResult();
			resInfo = new HashMap<String, Object>();
			

			resInfo.put("command", begJobService.saveSysCommand(contextId, levelContextId,context));
		
			result.setObj(resInfo);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			resInfo = null;
		}
		return result;
		
	}
	/**
	 * �ϴ����ֿ���//�м�⣨beg��
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/sysBegCommand")
	public Result sysBegCommand(@RequestParam("fatherId") int fatherId,@RequestParam("context") String context, HttpServletRequest request) {
		Result result = null;
		Map<String, Object> resInfo = null;
		Boolean flag =HttpUtils.checkWord(context);
//		if(flag) {
//			result = Result.errorResult();
//			result.setMsg("�����д�");
//			return result;
//		}
		try {
			result = Result.successResult();
			resInfo = new HashMap<String, Object>();
			resInfo.put("begCommand", begJobService.saveSysBegCommand(fatherId,context));
		redis.deleteBegCommand(fatherId);
			result.setObj(resInfo);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			resInfo = null;
		}
		return result;
		
	}
}
