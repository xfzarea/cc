package com.connection.dao;

import java.util.HashMap;

import org.apache.ibatis.annotations.Param;

import com.connection.entity.Admin;

public interface AdminDao {
	int saveUser(Admin admin);
	Admin findUserInfo(String openid);
	void modifyUserBaseInfo(Admin admin);
	void modifyMoney(@Param("award")double award,@Param("userId")int userId);
	void modifyMoney1(@Param("award")double award,@Param("userId")int userId);
	HashMap<String,Object>getUserById(int userId);
	int checkVersion(@Param("userId")int userId,@Param("money_version")int money_version);
	HashMap<String,Integer>sysFindUser(Admin admin);
}
