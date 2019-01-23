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
			//默认操作成功
			result = Result.successResult();
			//获得openid
			String resultStr = userService.sendData(params.get("code"), appId, secret);
			JSONObject json = JSON.parseObject(resultStr);
			String openid = json.getString("openid");
			//设置admin信息（信息从前台接受的）
			admin = new Admin();
			admin.setOpenid(openid);
			request.getSession().setAttribute("admin", admin);
			admin.setGender(Integer.parseInt(params.get("gender")));
			admin.setProvince(params.get("province"));
			admin.setCity(params.get("city"));
			admin.setNickName(params.get("nickName"));
			admin.setAvatarUrl(params.get("avatarUrl"));
			//调用保存用户方法
			admin = userService.saveAdmin(admin);
			//把返回admin和sessionId
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
			//查询口令，存到map，再存到result
			//这里的id是数据库的levelContextId类似父类id，就是查父类的所有子类
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
			//根据用户id查询userId,nickName,avatarUrl,money,money_version 
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
	//参数都在前端获取，怎么获取的呢？是使用全局的wwx.getStorageSync("userInfo")。。。
	public Result cashHand(@RequestParam("userId")int userId,@RequestParam("money")double money,String openid){
		Result result = null;
		Map<String,Object>response = null;
		try {
			result = Result.successResult();
			response = new HashMap<String,Object>();
			//这里的response是个map。。。，调用提现方法存入状态码
			//0=提现申请成功， 1=账号异常，请联系平台核实，2=提现金额有误，冻结提现功能，请联系平台核实，3=提现出现问题，请稍后再试
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
	 * record页、获得数量
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getTotal")
	//total总数（这里根据 handType区分是我发的红包，还是我抢的红包总数，和总金额）
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
	 * 讨和被讨记录页的获得总数量和总金额
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getBegTotal")
	//total总数（这里根据 handType区分是我发的红包，还是我抢的红包总数，和总金额）
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
	 * 保存formId//功能关了
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
	 * 交易记录，在页面是零钱明细，	<!-- 这里的state，0_现金提额，1_红包退款，3_红包领取 -->
	 * 参数page是sql语句分页的参数
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
			//注意这里比较有一个"！"在开发者工具中测试，所以得到的formId值为the formId is a mock one。在真机中我们可以得到一个具体的值
			if(!"the formId is a mock one".equals(param.get("formid"))){
				//保存formid
				userService.saveFormid(param);
			}
//			/<!-- 保存用户id和jobid（红包id） -->，清除福利界面得红包缓存
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
			//检测敏感词,true代表有敏感词
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
			//<!-- 	根据用户id和状态=0（voice_record 中状态0是抢红包成功的，1是不成功的）和book——url不为空查询速率最高的1条，获得book_url和rate（速率） -->
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
			//封装请求信息到map
			responseParams = new HashMap<String,Object>();
			//就是一字符串（可以写页面参数），前端页面会根据id生产页面
			responseParams.put("scene", jobId);
			//必须是已经发布的小程序存在的页面（否则报错），例如 "pages/index/index" ,根路径前不要填加'/',不能携带参数（参数请放在scene字段里），如果不填写这个字段，默认跳主页面
			responseParams.put("page", "pages/package/package");
			//是否需要透明底色， is_hyaline 为true时，生成透明底色的小程序码	
			responseParams.put("is_hyaline", true);
			//转成json
			String json = JSON.toJSONString(responseParams);
			response = new HashMap<String,String>();
			//userService.getCode(json)获得二维码主要api（二维码存到了阿里云，用的阿里oss服务）
			response.put("codeUrl", userService.getCode(json));
			result.setObj(response);
		} catch (Exception e) {
			e.printStackTrace();
			responseParams = null;
		}
		return result;
	}
	
	/**
	 * 保存vip，首页点击营销推广，提交合作按钮
	 */
	@ResponseBody
	@RequestMapping("vipReply")
	public Result vipReply(@RequestParam("name")String name,@RequestParam("tel")String tel){
		Result result = null;
		Map<String,Object>response = null;
		try {
			//把想要推广合作的人保存到数据库
			dataDao.saveVipReply(name, tel);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			
		}
		return result;
	}
	
//	/**
//	 * 自定义头像（接受单一图片）
//	 * 
//	 * @param request
//	 * @param response
//	 * @return
//	 */
//	@RequestMapping("/app/doImageLoad")
//	public void doFileLoad(HttpServletRequest request, HttpServletResponse response) {
//		response.setContentType("text/html;charset=utf8");
//		MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
//		//获取前端的值
//		MultipartFile file = multiRequest.getFile("image");
//		//获取上传文件的原名，并且利用replaceAll去空格
//		String name = file.getOriginalFilename().replaceAll(" ", "");
//		long currentTime = System.currentTimeMillis();
//		/*name.substring(name.lastIndexOf("."), name.length())这条代码最后的结果就是去的此文件名的扩展名如xxx.exe返回的就是.exe（包括点） ，
//		 * 去的filename的子串，第一个参数是起始index，第二个参数是最后的index，只是着个index对应的字符不包含在最后的结果中
//		 */
//		String imagePath = "static/head/" + currentTime + name.substring(name.lastIndexOf("."), name.length());
//		String dbPath = "https://static.yaohoudy.com/static/head/" +currentTime+name.substring(name.lastIndexOf("."), name.length());
//		PrintWriter out = null;
//		InputStream input = null;
//		try {
//			out = response.getWriter();
//			request.setCharacterEncoding("utf-8");
//			input = file.getInputStream();
//			//存到阿里云
//			Util.ossLoad(endPoint, accessKeyId, accessKeySecret, bucketName, imagePath, input);
//			//输出地址dbPath
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
	 * 小程序获得用户自定义口令
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
			//<!-- 	根据用户id降序和state=1，查找user_command 的口令（单用户自己的） -->
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
	 * 小程序获得用户自定义和公共口令
	 */
	
	@ResponseBody
	@GetMapping("userSearchCommand")
	public Result userSearchCommand(@RequestParam("text")String text){
		Result result = null;
		Map<String,Object>response = null;
		try {
			result = Result.successResult();
			response = new HashMap<String, Object>();
			//dataDao.searchCommand(text)查找客户的口令（关键字搜索）
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
	 * 保存用户口令，普通方法
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
	 * 获得公共口令例子（beg）
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
			//查询口令，存到map，再存到result
			//这里的id是数据库的levelContextId类似父类id，就是查父类的所有子类
			resInfo.put("begCommand", redis.getBegCommand(id));
			result.setObj(resInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
	
	/**
	 * 获得公共图片口令例子
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
	 * 获得公共语音口令例子
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
	 * 获得公共视频口令例子(视频暂时不开放)
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
	 * 上传自定义图片口令
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
			input = file.getInputStream();// 获得文件输入流

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
	 * 上传自定义语音口令
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
					input = file.getInputStream();// 获得文件输入流
					int b = input.available();
		            Bitstream bt = new Bitstream(input);
		            Header h = bt.readFrame();
		           int voiceTime= ((int) h.total_ms(b))/1000;//s

					
				resInfo.put("voice", begJobService.saveVoice(input,  userId,voiceTime));
						
					
				
			
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
	 * 上传自定义视频口令(视频暂时不开放)
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
			input = file.getInputStream();// 获得文件输入流
	
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
	 * 小程序获得用户自定义图片口令
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
			//<!-- 	根据用户id降序和state=1，查找user_command_image 的口令（单用户自己的） -->
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
	 * 小程序获得用户自定义视频口令
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
			//<!-- 	根据用户id降序和state=1，查找user_command_vedio的口令（单用户自己的） -->
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
	 * 小程序获得用户自定义语音口令
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
			//<!-- 	根据用户id降序和state=1，查找user_command_vedio的口令（单用户自己的） -->
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
	 * 上传自定义beg文字口令
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
	 * 小程序获得用户自定义beg文字口令
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
			//<!-- 	根据用户id降序和state=1，查找user_command_vedio的口令（单用户自己的） -->
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
