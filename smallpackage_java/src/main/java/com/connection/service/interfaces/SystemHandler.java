package com.connection.service.interfaces;

import java.util.Map;

import com.connection.entity.Admin;

public interface SystemHandler {
	public Admin saveAdmin(Admin admin);
	public int createTimerJob(Map<String,String>param);
	public int createUserJob(Map<String,String>param);
	public void deleteJob(String openid,int id);
}
