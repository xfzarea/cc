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
import com.connection.dao.AdminDao;
import com.connection.dao.DataDao;
import com.connection.dao.InfoExampleDao;
import com.connection.dao.JobDao;
import com.connection.dao.VoiceRecordDao;
import com.connection.entity.Admin;
import com.connection.service.interfaces.UserService;
import com.connection.tool.HttpUtils;
import com.connection.tool.Result;
import com.connection.tool.Util;



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
	private DataDao dataDao;
	@Autowired
	private JobDao jobDao;
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
	 * 用户登录
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/appLogin")
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
			admin = userService.saveAdmin(admin);
			resInfo = new HashMap<String,Object>();
			resInfo.put("admin", admin);
			resInfo.put("sessionId", request.getSession().getId());
			result.setObj(resInfo);
		} catch (Exception e) {
			throw new RuntimeException();
		}
		return result;
	}
	/**
	 * 获得口令例子
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
			resInfo.put("command", infoExampleDao.getCommand(id));
			result.setObj(resInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 我页面得到个人数据
	 */
	@ResponseBody
	@RequestMapping("/getUserById")
	public Result getUserById(@RequestParam("userId")int userId){
		Result result = null;
		Map<String,Object>response = null;
		try {
			result = Result.successResult();
			response = new HashMap<String,Object>();
			response.put("userInfo", adminDao.getUserById(userId));
			result.setObj(response);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			
		}
		return result;
	}
	
	/**
	 * 提现
	 */
	@ResponseBody
	@RequestMapping("/cashHand")
	public Result cashHand(@RequestParam("userId")int userId,@RequestParam("money")double money,String openid){
		Result result = null;
		Map<String,Object>response = null;
		try {
			result = Result.successResult();
			response = new HashMap<String,Object>();
			response.put("state",userService.cash(userId, money,openid));
			result.setObj(response);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}finally{
			
		}
		return result;
	}
	
	/**
	 * 小程序常见问题页
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
	 * record页获得数量
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getTotal")
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
	 * 保存formId
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
	 * 交易记录
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
	 * 小程序订阅红包提醒
	 */
	@ResponseBody
	@RequestMapping("/toTake")
	public Result toTake(@RequestParam Map<String,Object>param){
		Result result = null;
		Map<String,Object>response = null;
		try {
			result = Result.successResult();
			response = new HashMap<String,Object>();
			if(!"the formId is a mock one".equals(param.get("formid"))){
				userService.saveFormid(param);
			}
			userService.saveJobTake(param);
			result.setObj(response);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			
		}
		return result;
	}
	
	
	/**
	 * 小程序敏感词检索
	 */
	@ResponseBody
	@RequestMapping("checkWord")
	public Result checkWord(@RequestParam("content")String content){
		Result result = null;
		Map<String,Object>response = null;
		try {
			result = Result.successResult();
			response = new HashMap<String,Object>();
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
	 * 小程序获取单一证书（我得页面）
	 */
	@ResponseBody
	@RequestMapping("getHighRate")
	public Result getHighRate(@RequestParam("userId")int userId){
		Result result = null;
		Map<String,Object>response = null;
		try {
			result = Result.successResult();
			response = new HashMap<String,Object>();
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
	 * 传播页获取二维码
	 */
	@ResponseBody
	@RequestMapping("/getCode")
	public Result getCode(@RequestParam("jobId")String jobId){
		Result result = null;
		Map<String,Object>responseParams = null;
		Map<String,String>response = null;
		try {
			result = Result.successResult();
			responseParams = new HashMap<String,Object>();
			responseParams.put("scene", jobId);
			responseParams.put("page", "pages/package/package");
			responseParams.put("is_hyaline", true);
			String json = JSON.toJSONString(responseParams);
			response = new HashMap<String,String>();
			response.put("codeUrl", userService.getCode(json));
			result.setObj(response);
		} catch (Exception e) {
			e.printStackTrace();
			responseParams = null;
		}
		return result;
	}
	
	/**
	 * 小程序获取单一证书（我得页面）
	 */
	@ResponseBody
	@RequestMapping("vipReply")
	public Result vipReply(@RequestParam("name")String name,@RequestParam("tel")String tel){
		Result result = null;
		Map<String,Object>response = null;
		try {
			dataDao.saveVipReply(name, tel);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			
		}
		return result;
	}
	
	/**
	 * 自定义头像（接受单一图片）
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/app/doImageLoad")
	public void doFileLoad(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("text/html;charset=utf8");
		MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
		MultipartFile file = multiRequest.getFile("image");
		String name = file.getOriginalFilename().replaceAll(" ", "");
		long currentTime = System.currentTimeMillis();
		String imagePath = "static/head/" + currentTime + name.substring(name.lastIndexOf("."), name.length());
		String dbPath = "https://static.yaohoudy.com/static/head/" +currentTime+name.substring(name.lastIndexOf("."), name.length());
		PrintWriter out = null;
		InputStream input = null;
		try {
			out = response.getWriter();
			request.setCharacterEncoding("utf-8");
			input = file.getInputStream();
			Util.ossLoad(endPoint, accessKeyId, accessKeySecret, bucketName, imagePath, input);
			out.print(dbPath);
		} catch (Exception e) {
			log.error("/infoPicLoad:"+dbPath);
		} finally {
			out.close();
			try {
				input.close();
			} catch (IOException e) {

			}
		}
	}
	
	/**
	 * 小程序获得用户自定义口令
	 */
	@ResponseBody
	@RequestMapping("getUserCommand")
	public Result getUserCommand(@RequestParam("userId")int userId){
		Result result = null;
		Map<String,Object>response = null;
		try {
			result = Result.successResult();
			response = new HashMap<String, Object>();
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
	 * 小程序获得用户自定义口令
	 */
	@ResponseBody
	@GetMapping("userSearchCommand")
	public Result userSearchCommand(@RequestParam("text")String text){
		Result result = null;
		Map<String,Object>response = null;
		try {
			result = Result.successResult();
			response = new HashMap<String, Object>();
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
	 * 小程序获取单一证书（我得页面）
	 */
	@ResponseBody
	@RequestMapping("saveUserCommand")
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
}
