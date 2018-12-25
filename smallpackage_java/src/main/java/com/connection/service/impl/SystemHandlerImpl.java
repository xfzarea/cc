package com.connection.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.connection.dao.AdminDao;
import com.connection.dao.SysAdminDao;
import com.connection.entity.Admin;
import com.connection.service.interfaces.RedisService;
import com.connection.service.interfaces.SystemHandler;
@Service
public class SystemHandlerImpl implements SystemHandler {
	@Autowired
	private SysAdminDao sysAdminDao;
	@Autowired
	private AdminDao adminDao;
	@Autowired
	private RedisService redis;
	/**
	 * 保存用户信息
	 * @return
	 */
	public synchronized Admin saveAdmin(Admin admin){
		Map<String,Object>userInfo = sysAdminDao.findUserInfo(admin.getOpenid());
		if(userInfo == null){
			sysAdminDao.saveUser(admin);
		}else{
			sysAdminDao.modifyUserBaseInfo(admin);
			int userId = (Integer) userInfo.get("userId");
			admin.setUserId(userId);
		}
		return admin;
	}
	/**
	 * 生成定时红包
	 */
	@Transactional
	public int createTimerJob(Map<String,String>param){
		redis.deleteTimerCash();
		Map<String,Object>realParam = null;
		int id = 0;
		Map<String,Object>admin = sysAdminDao.findUserInfo(param.get("openid"));
		if(admin!=null&&(Integer)admin.get("state")==1){
			realParam = new HashMap<String,Object>();
			double award = Double.parseDouble(param.get("award"));
			realParam.put("id", 0);
			realParam.put("headPic",param.get("headPic"));
			realParam.put("userId", 0);
			realParam.put("totalAward", Double.parseDouble(String.format("%.2f", award * 1.02)));
			realParam.put("award", award);
			realParam.put("totalCount", Integer.parseInt(param.get("totalCount")));
			realParam.put("context", param.get("context"));
			realParam.put("createTime", param.get("createTime"));
			realParam.put("state", 4);
			realParam.put("transaction_id", "bxszc");
			realParam.put("title", param.get("title"));
			realParam.put("userName", param.get("userName"));
			realParam.put("timeLimit", 1);
			if(!"null".equals((String)param.get("skipAppid"))){
				realParam.put("skipAppid", param.get("skipAppid"));
				realParam.put("skipUrl", param.get("skipUrl"));
			}
			if(!"null".equals((String)param.get("wxchat_mark"))){
				realParam.put("wxchat_mark", param.get("wxchat_mark"));
			}
			if(!"null".equals((String)param.get("pre_image"))){
				realParam.put("pre_image", param.get("pre_image"));
			}
			if(!"null".equals((String)param.get("tweet"))){
				realParam.put("tweet", param.get("tweet"));
			}
			if(!"null".equals((String)param.get("tweet_title"))){
				realParam.put("tweet_title", param.get("tweet_title"));
			}
			if(Integer.parseInt(param.get("id"))==0){//代表需要生成红包
				sysAdminDao.createTimerJob(realParam);
				id = (Integer)realParam.get("id");
			}else{//代表修改紅包
				id = Integer.parseInt(param.get("id"));
				redis.deleteRedisJob(id);
				realParam.put("id", id);
				sysAdminDao.updataTimerJob(realParam);
			}
		}
		param = null;
		realParam = null;
		return id;
	}
	/**
	 * 发自定义红包
	 * @return
	 */
	public int createUserJob(Map<String,String>param){
		Map<String,Object>realParam = null;
		Admin admin = null;
		int id = 0;
		Map<String,Object>a = sysAdminDao.findUserInfo(param.get("openid"));
		if(a!=null&&(Integer)a.get("state")==1){
			id = Integer.parseInt(param.get("id"));
			int userId = 0;
			admin = new Admin();
			admin.setNickName(param.get("nickName"));
			admin.setAvatarUrl(param.get("avatarUrl"));
			admin.setOpenid("bxszc");
			if(id==0){
				adminDao.saveUser(admin);
				userId = adminDao.sysFindUser(admin).get("userId");
			}else{
				userId = sysAdminDao.getUserIdById(id).get("userId");
				admin.setUserId(userId);
				sysAdminDao.updateAdmin(admin);
			}
			realParam = new HashMap<String,Object>();
			double award = Double.parseDouble(param.get("award"));
			realParam.put("userId", userId);
			realParam.put("totalAward", Double.parseDouble(String.format("%.2f", award * 1.02)));
			realParam.put("award", award);
			realParam.put("totalCount", Integer.parseInt(param.get("totalCount")));
			realParam.put("context", param.get("context"));
			realParam.put("state", 4);
			realParam.put("timeLimit", 0);
			realParam.put("state", 1);
			realParam.put("transaction_id", "bxszc");
			if(!"null".equals(param.get("skipAppid"))){
				realParam.put("skipAppid", param.get("skipAppid"));
				realParam.put("skipUrl", param.get("skipUrl"));
			}
			if(!"null".equals((String)param.get("wxchat_mark"))){
				realParam.put("wxchat_mark", param.get("wxchat_mark"));
			}
			if(!"null".equals((String)param.get("pre_image"))){
				realParam.put("pre_image", param.get("pre_image"));
			}
			if(!"null".equals((String)param.get("tweet"))){
				realParam.put("tweet", param.get("tweet"));
			}
			if(!"null".equals((String)param.get("tweet_title"))){
				realParam.put("tweet_title", param.get("tweet_title"));
			}
			if(id==0){
				sysAdminDao.createTimerJob(realParam);
				id = (Integer)realParam.get("id");
			}else{
				realParam.put("id", id);
				redis.deleteRedisJob(id);
				sysAdminDao.updataTimerJob(realParam);
			}
		}
		return id;
	}
	/**
	 * 管理员删除红包
	 */
	public void deleteJob(String openid,int id){
		if(checkInfo(openid)){
			sysAdminDao.deleteJob(id);
		}
	}
	/**
	 * 判断身份
	 * @return
	 */
	public boolean checkInfo(String openid){
		Map<String,Object>a = sysAdminDao.findUserInfo(openid);
		if(a!=null&&(Integer)a.get("state")==1){
			return true;
		}else{
			return false;
		}
	}
}
