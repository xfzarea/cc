package com.connection.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.connection.entity.Admin;

public interface SysAdminDao {
	//保存sys用户
	void saveUser(Admin admin);
	//根据openId查询用户是否存在
	HashMap<String,Object>findUserInfo(String openid);
	//跟新用户信息，根据admin中的openId有nickName,avatarUrl ,gender,province ，city
	void modifyUserBaseInfo(Admin admin);
	HashMap<String,Object>getUserById(int userId);
	
	//创建新的语音定时红包（job表中插入一条数据，并给原本的参数里插入id的值）
	void createTimerJob(Map<String,Object>param);
	//<!-- transaction_id = "bxszc" 的红包应该都是在管理员平台发的 -->，查询transaction_id = "bxszc"，并且状态是1，2，4的红包
	List<HashMap<String,Object>>getJobs();
	//而且删除也没返回值，是不是不大好
	void deleteJob(int id);
	HashMap<String,Object>getTimerJobById(int id);
	HashMap<String,Object>getUserJobById(int id);
	//更新定时红包
	void updataTimerJob(Map<String,Object>param);
	Map<String,Integer>getUserIdById(int id);
	void updateAdmin(Admin admin);
}
