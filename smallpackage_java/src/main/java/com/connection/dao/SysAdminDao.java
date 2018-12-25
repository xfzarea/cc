package com.connection.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.connection.entity.Admin;

public interface SysAdminDao {
	void saveUser(Admin admin);
	HashMap<String,Object>findUserInfo(String openid);
	void modifyUserBaseInfo(Admin admin);
	HashMap<String,Object>getUserById(int userId);
	
	void createTimerJob(Map<String,Object>param);
	List<HashMap<String,Object>>getJobs();
	void deleteJob(int id);
	HashMap<String,Object>getTimerJobById(int id);
	HashMap<String,Object>getUserJobById(int id);
	void updataTimerJob(Map<String,Object>param);
	Map<String,Integer>getUserIdById(int id);
	void updateAdmin(Admin admin);
}
