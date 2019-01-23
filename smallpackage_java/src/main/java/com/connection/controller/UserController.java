package com.connection.controller;

import java.io.IOException;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.connection.aop.FruitAspectBC;
import com.connection.aop.FruitAspectC;
import com.connection.aop.FruitAspectI;
import com.connection.aop.FruitAspectVE;
import com.connection.aop.FruitAspectVO;
import com.connection.dao.AdminDao;
import com.connection.dao.BegJobDao;
import com.connection.dao.DataDao;
import com.connection.dao.InfoExampleDao;
import com.connection.dao.JobDao;
import com.connection.dao.VoiceRecordDao;
import com.connection.entity.Admin;
import com.connection.service.interfaces.BegJobService;
import com.connection.service.interfaces.RedisService;
import com.connection.service.interfaces.UserService;
import com.connection.tool.HttpUtils;
import com.connection.tool.Result;
import com.connection.tool.Util;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;



@Controller
public class UserController {
	public static Logger log = Logger.getLogger(UserController.class);
	@Value("#{ali.endPoint}")
	private String endPoint;
	@Value("#{ali.accessKeyId}")
	private String accessKeyId;
	@Value("#{ali.accessKeySecret}")
	private String accessKeySecret;
	@Value("#{ali.bucketName}")
	private String bucketName;
	
	@Autowired
	private UserService userService;
	@Value("#{wx.appid}")
	private String appId;
	@Value("#{wx.secret}")
	private String secret;
	@Autowired
	private AdminDao adminDao;
	@Autowired
	private RedisService redis;
	@Autowired
	private DataDao dataDao;
	@Autowired
	private JobDao jobDao;
	@Autowired
	private BegJobService begJobService;
	@Autowired
	private BegJobDao begJobDao;
	@Autowired
	private VoiceRecordDao voiceRecordDao;
	@Autowired
	private InfoExampleDao infoExampleDao;
	
	@ResponseBody
	@RequestMapping("/checkSession")
	public Result checkSession(HttpServletRequest request){
		Result result = null;
		try {
			result = Result.successResult();
			Admin admin = (Admin) request.getSession().getAttribute("admin");
			if(admin == null){
				result.setCode(2);
			}
		} catch (Exception e) {
			
		}
		return result;
	}
	
	/**
	 * �û���¼
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/appLogin")
	public Result appLogin(@RequestParam Map<String,String>params,HttpServletRequest request){
		Result result = null;
		Admin admin = null;
		Map<String,Object>resInfo = null;
		try {
			//Ĭ�ϲ����ɹ�
			result = Result.successResult();
			//���openid
			String resultStr = userService.sendData(params.get("code"), appId, secret);
			JSONObject json = JSON.parseObject(resultStr);
			String openid = json.getString("openid");
			//����admin��Ϣ����Ϣ��ǰ̨���ܵģ�
			admin = new Admin();
			admin.setOpenid(openid);
			request.getSession().setAttribute("admin", admin);
			admin.setGender(Integer.parseInt(params.get("gender")));
			admin.setProvince(params.get("province"));
			admin.setCity(params.get("city"));
			admin.setNickName(params.get("nickName"));
			admin.setAvatarUrl(params.get("avatarUrl"));
			//���ñ����û�����
			admin = userService.saveAdmin(admin);
			//�ѷ���admin��sessionId
			resInfo = new HashMap<String,Object>();
			resInfo.put("admin", admin);
			resInfo.put("sessionId", request.getSession().getId());
			result.setObj(resInfo);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	/**
	 * ��ÿ�������
	 * @param id
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getCommand")
	public Result getCommand(@RequestParam("id") int id,HttpServletRequest request){
		Result result = null;
		Map<String,Object>resInfo = null;
		try {
			result = Result.successResult();
			resInfo = new HashMap<String,Object>();
			//��ѯ����浽map���ٴ浽result
			//�����id�����ݿ��levelContextId���Ƹ���id�����ǲ鸸�����������
			resInfo.put("command", infoExampleDao.getCommand(id));
			result.setObj(resInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
	/**
	 * ��ҳ��õ���������
	 */
	@ResponseBody
	@RequestMapping("/getUserById")
	public Result getUserById(@RequestParam("userId")int userId){
		Result result = null;
		Map<String,Object>response = null;
		try {
			result = Result.successResult();
			response = new HashMap<String,Object>();
			//�����û�id��ѯuserId,nickName,avatarUrl,money,money_version 
			response.put("userInfo", adminDao.getUserById(userId));
			result.setObj(response);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			
		}
		return result;
	}
	
	/**
	 * ����
	 */
	@ResponseBody
	@RequestMapping("/cashHand")
	//��������ǰ�˻�ȡ����ô��ȡ���أ���ʹ��ȫ�ֵ�wwx.getStorageSync("userInfo")������
	public Result cashHand(@RequestParam("userId")int userId,@RequestParam("money")double money,String openid){
		Result result = null;
		Map<String,Object>response = null;
		try {
			result = Result.successResult();
			response = new HashMap<String,Object>();
			//�����response�Ǹ�map���������������ַ�������״̬��
			//0=��������ɹ��� 1=�˺��쳣������ϵƽ̨��ʵ��2=���ֽ�����󣬶������ֹ��ܣ�����ϵƽ̨��ʵ��3=���ֳ������⣬���Ժ�����
			response.put("state",userService.cash(userId, money,openid));
			result.setObj(response);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}finally{
			
		}
		return result;
	}
	
	/**
	 * С���򳣼�����ҳ
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/toMoreQuestion")
	public Result toMoreQuestion(){
		Result result = null;
		Map<String,Object>response = null;
		try {
			result = Result.successResult();
			response = new HashMap<String,Object>();
			response.put("questions", dataDao.getCommon_question());
			result.setObj(response);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			response = null;
		}
		return result;
	}
	
	/**
	 * recordҳ���������
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getTotal")
	//total������������� handType�������ҷ��ĺ�������������ĺ�����������ܽ�
	public Result getTotal(@RequestParam("userId")int userId,@RequestParam("handType")String handType){
		Result result = null;
		Map<String,Object>response = null;
		try {
			result = Result.successResult();
			response = new HashMap<String,Object>();
			if("mine".equals(handType)){
				response.put("total", jobDao.getCount(userId));
			}else if("join".equals(handType)){
				response.put("total", voiceRecordDao.getCount(userId));
			}
			result.setObj(response);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			response = null;
		}
		return result;
	}
	
	/**
	 * �ֺͱ��ּ�¼ҳ�Ļ�����������ܽ��
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getBegTotal")
	//total������������� handType�������ҷ��ĺ�������������ĺ�����������ܽ�
	public Result getBegTotal(@RequestParam("userId")int userId,@RequestParam("tabId")int tabId){
		Result result = null;
		Map<String,Object>response = null;
		try {
			result = Result.successResult();
			response = new HashMap<String,Object>();
			if(tabId==0){
				response.put("total", begJobDao.getBegCount(userId));
			}else if(tabId==1){
				response.put("total", begJobDao.getBegRecordCount(userId));
			}
			result.setObj(response);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			response = null;
		}
		return result;
	}
	
	/**
	 * ����formId//���ܹ���
	 */
	@ResponseBody
	@RequestMapping("/saveFormid")
	public Result saveFormid(@RequestParam Map<String,Object>param){
		Result result = null;
		try {
			result = Result.successResult();
			if(!"the formId is a mock one".equals(param.get("formid"))){
				userService.saveFormid(param);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{

		}
		return result;
	}
	
	/**
	 * ���׼�¼����ҳ������Ǯ��ϸ��	<!-- �����state��0_�ֽ���1_����˿3_�����ȡ -->
	 * ����page��sql����ҳ�Ĳ���
	 */
	@ResponseBody
	@RequestMapping("/getDetail")
	public Result getBalanceList(@RequestParam("userId")int userId,@RequestParam("page")int page){
		Result result = null;
		Map<String,Object>response = null;
		try {
			result = Result.successResult();
			response = new HashMap<String,Object>();
			int startId = (page-1)*10;
			response.put("lists", voiceRecordDao.getDetail(userId, startId));
			result.setObj(response);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			
		}
		return result;
	}
	
	/**
	 * С�����ĺ������
	 */
	@ResponseBody
	@RequestMapping("/toTake")
	public Result toTake(@RequestParam Map<String,Object>param){
		Result result = null;
		Map<String,Object>response = null;
		try {
			result = Result.successResult();
			response = new HashMap<String,Object>();
			//ע������Ƚ���һ��"��"�ڿ����߹����в��ԣ����Եõ���formIdֵΪthe formId is a mock one������������ǿ��Եõ�һ�������ֵ
			if(!"the formId is a mock one".equals(param.get("formid"))){
				//����formid
				userService.saveFormid(param);
			}
//			/<!-- �����û�id��jobid�����id�� -->�������������ú������
			userService.saveJobTake(param);
			result.setObj(response);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			
		}
		return result;
	}
	
	
	/**
	 * С�������дʼ���
	 */
	@ResponseBody
	@RequestMapping("checkWord")
	public Result checkWord(@RequestParam("content")String content){
		Result result = null;
		Map<String,Object>response = null;
		try {
			result = Result.successResult();
			response = new HashMap<String,Object>();
			//������д�,true���������д�
			response.put("flag", HttpUtils.checkWord(content));
			result.setObj(response);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			response = null;
		}
		return result;
	}
	
	/**
	 * С�����ȡ��һ֤�飨�ҵ�ҳ�棩
	 */
	@ResponseBody
	@RequestMapping("getHighRate")
	public Result getHighRate(@RequestParam("userId")int userId){
		Result result = null;
		Map<String,Object>response = null;
		try {
			result = Result.successResult();
			response = new HashMap<String,Object>();
			//<!-- 	�����û�id��״̬=0��voice_record ��״̬0��������ɹ��ģ�1�ǲ��ɹ��ģ���book����url��Ϊ�ղ�ѯ������ߵ�1�������book_url��rate�����ʣ� -->
			response.put("highRate", voiceRecordDao.getHighRate(userId));
			result.setObj(response);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			response = null;
		}
		return result;
	}
	
	/**
	 * ����ҳ��ȡ��ά��
	 */
	@ResponseBody
	@RequestMapping("/getCode")
	public Result getCode(@RequestParam("jobId")String jobId){
		Result result = null;
		Map<String,Object>responseParams = null;
		Map<String,String>response = null;
		try {
			result = Result.successResult();
			//��װ������Ϣ��map
			responseParams = new HashMap<String,Object>();
			//����һ�ַ���������дҳ���������ǰ��ҳ������id����ҳ��
			responseParams.put("scene", jobId);
			//�������Ѿ�������С������ڵ�ҳ�棨���򱨴������� "pages/index/index" ,��·��ǰ��Ҫ���'/',����Я�����������������scene�ֶ�����������д����ֶΣ�Ĭ������ҳ��
			responseParams.put("page", "pages/package/package");
			//�Ƿ���Ҫ͸����ɫ�� is_hyaline Ϊtrueʱ������͸����ɫ��С������	
			responseParams.put("is_hyaline", true);
			//ת��json
			String json = JSON.toJSONString(responseParams);
			response = new HashMap<String,String>();
			//userService.getCode(json)��ö�ά����Ҫapi����ά��浽�˰����ƣ��õİ���oss����
			response.put("codeUrl", userService.getCode(json));
			result.setObj(response);
		} catch (Exception e) {
			e.printStackTrace();
			responseParams = null;
		}
		return result;
	}
	
	/**
	 * ����vip����ҳ���Ӫ���ƹ㣬�ύ������ť
	 */
	@ResponseBody
	@RequestMapping("vipReply")
	public Result vipReply(@RequestParam("name")String name,@RequestParam("tel")String tel){
		Result result = null;
		Map<String,Object>response = null;
		try {
			//����Ҫ�ƹ�������˱��浽���ݿ�
			dataDao.saveVipReply(name, tel);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			
		}
		return result;
	}
	
//	/**
//	 * �Զ���ͷ�񣨽��ܵ�һͼƬ��
//	 * 
//	 * @param request
//	 * @param response
//	 * @return
//	 */
//	@RequestMapping("/app/doImageLoad")
//	public void doFileLoad(HttpServletRequest request, HttpServletResponse response) {
//		response.setContentType("text/html;charset=utf8");
//		MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
//		//��ȡǰ�˵�ֵ
//		MultipartFile file = multiRequest.getFile("image");
//		//��ȡ�ϴ��ļ���ԭ������������replaceAllȥ�ո�
//		String name = file.getOriginalFilename().replaceAll(" ", "");
//		long currentTime = System.currentTimeMillis();
//		/*name.substring(name.lastIndexOf("."), name.length())�����������Ľ������ȥ�Ĵ��ļ�������չ����xxx.exe���صľ���.exe�������㣩 ��
//		 * ȥ��filename���Ӵ�����һ����������ʼindex���ڶ�������������index��ֻ���Ÿ�index��Ӧ���ַ������������Ľ����
//		 */
//		String imagePath = "static/head/" + currentTime + name.substring(name.lastIndexOf("."), name.length());
//		String dbPath = "https://static.yaohoudy.com/static/head/" +currentTime+name.substring(name.lastIndexOf("."), name.length());
//		PrintWriter out = null;
//		InputStream input = null;
//		try {
//			out = response.getWriter();
//			request.setCharacterEncoding("utf-8");
//			input = file.getInputStream();
//			//�浽������
//			Util.ossLoad(endPoint, accessKeyId, accessKeySecret, bucketName, imagePath, input);
//			//�����ַdbPath
//			out.print(dbPath);
//		} catch (Exception e) {
//			log.error("/infoPicLoad:"+dbPath);
//		} finally {
//			out.close();
//			try {
//				input.close();
//			} catch (IOException e) {
//
//			}
//		}
//	}
//	
	/**
	 * С�������û��Զ������
	 */
	@FruitAspectC
	@ResponseBody
	@RequestMapping("getUserCommand")
	public Result getUserCommand(@RequestParam("userId")int userId){
		Result result = null;
		Map<String,Object>response = null;
		try {
			result = Result.successResult();
			response = new HashMap<String, Object>();
			//<!-- 	�����û�id�����state=1������user_command �Ŀ�����û��Լ��ģ� -->
			response.put("commands", dataDao.getUserCommand(userId));
			result.setObj(response);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			response = null;
		}
		return result;
	}
	
	/**
	 * С�������û��Զ���͹�������
	 */
	
	@ResponseBody
	@GetMapping("userSearchCommand")
	public Result userSearchCommand(@RequestParam("text")String text){
		Result result = null;
		Map<String,Object>response = null;
		try {
			result = Result.successResult();
			response = new HashMap<String, Object>();
			//dataDao.searchCommand(text)���ҿͻ��Ŀ���ؼ���������
			response.put("commands", dataDao.searchCommand(text));
			result.setObj(response);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			response = null;
		}
		return result;
	}
	
	/**
	 * �����û������ͨ����
	 */
	@ResponseBody
	@RequestMapping("saveUserCommand")
	@FruitAspectC
	public Result saveUserCommand(@RequestParam("command")String command,@RequestParam("userId")int userId){
		Result result = null;
		try {
			result = Result.successResult();
			userService.saveCommand(command, userId);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			
		}
		return result;
	}
	
	
	
	@RequestMapping("/test")
	@ResponseBody
	public Result test(){
		int id = dataDao.update();
		System.out.println(id);
		return Result.successResult();
	}
	
	/**
	 * ��ù����������ӣ�beg��
	 * @param id
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getBegCommand")
	public Result getBegCommand(@RequestParam("id") int id,HttpServletRequest request){
		Result result = null;
		Map<String,Object>resInfo = null;
		try {
			result = Result.successResult();
			resInfo = new HashMap<String,Object>();
			//��ѯ����浽map���ٴ浽result
			//�����id�����ݿ��levelContextId���Ƹ���id�����ǲ鸸�����������
			resInfo.put("begCommand", redis.getBegCommand(id));
			result.setObj(resInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
	
	/**
	 * ��ù���ͼƬ��������
	 * @param id 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getCommandImage")
	public Result getCommandImage(@RequestParam("id") int id,HttpServletRequest request){
		Result result = null;
		Map<String,Object>resInfo = null;
		try {
			result = Result.successResult();
			resInfo = new HashMap<String,Object>();
			
			resInfo.put("commandImage", redis.getCommandImage(id));
			
			result.setObj(resInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * ��ù���������������
	 * @param id 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getVoiceCommand")
	public Result getVoiceCommand(@RequestParam("id") int id,HttpServletRequest request){
		Result result = null;
		Map<String,Object>resInfo = null;
		try {
			result = Result.successResult();
			resInfo = new HashMap<String,Object>();
			
			resInfo.put("voiceCommand", redis.getVoiceCommand(id));
			
			result.setObj(resInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * ��ù�����Ƶ��������(��Ƶ��ʱ������)
	 * @param id 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getVedioCommand")
	public Result getVedioCommand(@RequestParam("id") int id,HttpServletRequest request){
		Result result = null;
		Map<String,Object>resInfo = null;
		try {
			result = Result.successResult();
			resInfo = new HashMap<String,Object>();
			
			resInfo.put("vedioCommand", redis.getVedioCommand(id));
		
			result.setObj(resInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * �ϴ��Զ���ͼƬ����
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/loadBegCommandImage")
	@FruitAspectI
	public Result loadBegCommandImage(@RequestParam("userId") int userId, HttpServletRequest request) {
		Result result = null;
		Map<String, Object> resInfo = null;
		MultipartFile file = null;
		InputStream input = null;
		try {
			result = Result.successResult();
			resInfo = new HashMap<String, Object>();
			file = ((MultipartHttpServletRequest) request).getFile("image");
			input = file.getInputStream();// ����ļ�������

			resInfo.put("commandImage", begJobService.saveCommandImage(input, userId));
		
			result.setObj(resInfo);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			resInfo = null;
		}
		return result;
		
	}
	/**
	 * �ϴ��Զ�����������
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/uploadVoiceCommand")
	@FruitAspectVO
	public Result uploadVoiceCommand(@RequestParam("userId") int userId
			, HttpServletRequest request) {
		Result result = null;
		Map<String, Object> resInfo = null;
		MultipartFile file = null;
		InputStream input = null;
		try {
			result = Result.successResult();
			resInfo = new HashMap<String, Object>();
					file = ((MultipartHttpServletRequest) request).getFile("file");
					input = file.getInputStream();// ����ļ�������
					int b = input.available();
		            Bitstream bt = new Bitstream(input);
		            Header h = bt.readFrame();
		           int voiceTime= ((int) h.total_ms(b))/1000;//s

					
				resInfo.put("voice", begJobService.saveVoice(input,  userId,voiceTime));
						
					
				
			
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
	 * �ϴ��Զ�����Ƶ����(��Ƶ��ʱ������)
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/loadBegVedio")
	@FruitAspectVE
	public Result loadBegVedio(@RequestParam("userId") int userId, HttpServletRequest request) {
		Result result = null;
		Map<String, Object> resInfo = null;
		MultipartFile file = null;
		InputStream input = null;
		try {
			result = Result.successResult();
			resInfo = new HashMap<String, Object>();
			file = ((MultipartHttpServletRequest) request).getFile("file");
			input = file.getInputStream();// ����ļ�������
	
			resInfo.put("vedio", begJobService.saveVedioCommand(input, userId));
		
			result.setObj(resInfo);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			resInfo = null;
		}
		return result;
		
	}
	/**
	 * С�������û��Զ���ͼƬ����
	 */
	@ResponseBody
	@RequestMapping("/getUserCommandImage")
	@FruitAspectI
	public Result getUserCommandImage(@RequestParam("userId")int userId){
		Result result = null;
		Map<String,Object>response = null;
		try {
			result = Result.successResult();
			response = new HashMap<String, Object>();
			//<!-- 	�����û�id�����state=1������user_command_image �Ŀ�����û��Լ��ģ� -->
			response.put("userCommandImage", dataDao.getUserCommandImage(userId));
			result.setObj(response);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			response = null;
		}
		return result;
	}
	/**
	 * С�������û��Զ�����Ƶ����
	 */
	@ResponseBody
	@RequestMapping("/getUserCommandVedio")
	@FruitAspectVE
	public Result getUserCommandVedio(@RequestParam("userId")int userId){
		Result result = null;
		Map<String,Object>response = null;
		try {
			result = Result.successResult();
			response = new HashMap<String, Object>();
			//<!-- 	�����û�id�����state=1������user_command_vedio�Ŀ�����û��Լ��ģ� -->
			response.put("userCommandVedio", dataDao.getUserCommandVedio(userId));
			result.setObj(response);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			response = null;
		}
		return result;
	}
	/**
	 * С�������û��Զ�����������
	 */
	@ResponseBody
	@RequestMapping("/getUserCommandVoice")
	@FruitAspectVO
	public Result getUserCommandVoice(@RequestParam("userId")int userId){
		Result result = null;
		Map<String,Object>response = null;
		try {
			result = Result.successResult();
			response = new HashMap<String, Object>();
			//<!-- 	�����û�id�����state=1������user_command_vedio�Ŀ�����û��Լ��ģ� -->
			response.put("userCommandVoice", dataDao.getUserCommandVoice(userId));
			result.setObj(response);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			response = null;
		}
		return result;
	}
	
	/**
	 * �ϴ��Զ���beg���ֿ���
	 * 
	 * @param id
	 * @param request
	 * @return
	 */
	
	@ResponseBody
	@RequestMapping("/loadBegCommand")
	@FruitAspectBC
	public Result loadBegCommand(@RequestParam("userId") int userId, String context) {
		Result result = null;
		Map<String, Object> resInfo = null;
		
		try {
			result = Result.successResult();
			resInfo = new HashMap<String, Object>();
			
	
			resInfo.put("BegCommand", begJobService.saveBegCommand( userId,context));
		
			result.setObj(resInfo);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			resInfo = null;
		}
		return result;
		
	}
	
	/**
	 * С�������û��Զ���beg���ֿ���
	 */
	@ResponseBody
	@RequestMapping("/getUserBegCommand")
	@FruitAspectBC
	public Result getUserBegCommand(@RequestParam("userId")int userId){
		Result result = null;
		Map<String,Object>response = null;
		try {
			result = Result.successResult();
			response = new HashMap<String, Object>();
			//<!-- 	�����û�id�����state=1������user_command_vedio�Ŀ�����û��Լ��ģ� -->
			response.put("userBegCommand", dataDao.getUserBegCommand(userId));
			result.setObj(response);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			response = null;
		}
		return result;
	}
	
}
