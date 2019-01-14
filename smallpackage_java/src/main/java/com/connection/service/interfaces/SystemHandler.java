package com.connection.service.interfaces;

import java.util.Map;

import com.connection.entity.Admin;

public interface SystemHandler {
	//保存用户信息，并得到更完整信息
	public Admin saveAdmin(Admin admin);
	//发布定时红包
	public int createTimerJob(Map<String,String>param);
	//管理员发布用户自定义红包
	public int createUserJob(Map<String,String>param);
	//删除自定义红包
	public void deleteJob(String openid,int id);
}
