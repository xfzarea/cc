package com.connection.dao;

import java.util.HashMap;

import org.apache.ibatis.annotations.Param;

import com.connection.entity.Admin;

public interface AdminDao {
	//保存新用户
	int saveUser(Admin admin);
	//根据openid查找用户信息，返回用户
	//userId,nickName,avatarUrl,gender,province,city,openid,createTime
	Admin findUserInfo(String openid);
	//用户基本信息发生改变时调用此方法，修改用户基本信息
	void modifyUserBaseInfo(Admin admin);
	//根据用户id，menoy增加award的钱(为什么这两个sql接口功能一样？？)
	void modifyMoney(@Param("award")double award,@Param("userId")int userId);
	void modifyMoney1(@Param("award")double award,@Param("userId")int userId);//在提现的时候用了这个，但应为是钱是负的所以扣除
	//根据用户id查询userId,nickName,avatarUrl,money,money_version 
	HashMap<String,Object>getUserById(int userId);
	//乐观锁，根据用户id和现在钱——版本号，修改版本号为现在版本号+1		
	int checkVersion(@Param("userId")int userId,@Param("money_version")int money_version);
	//根据 openid、头像地址、昵称，查询用户id（这个用来干嘛的？？）
	HashMap<String,Integer>sysFindUser(Admin admin);
	
//根据userId查询openId
	String getOpenIdByUserId(int userId);
}
