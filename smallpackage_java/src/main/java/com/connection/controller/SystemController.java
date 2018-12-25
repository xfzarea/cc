package com.connection.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.connection.dao.SysAdminDao;
import com.connection.entity.Admin;
import com.connection.service.interfaces.SystemHandler;
import com.connection.service.interfaces.UserService;
import com.connection.tool.Result;

@Controller
public class SystemController {
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
	/**
	 * 包享说系统用户登录
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
	 * 管理员发布定时红包
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
			id = systemHandler.createTimerJob(params);
			resInfo = new HashMap<String,Object>();
			resInfo.put("id", id);
			result.setObj(resInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 管理员发布用户自定义红包
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
	 * 管理员获得红包列表
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
	 * 管理员删除红包
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/delateJob")
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
	 * 管理员获得单一红包
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
				job=sysAdminDao.getUserJobById(id);
			}else if(state == 4){
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
}
