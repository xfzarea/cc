package com.connection.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface DataDao {
	void saveFormid(Map<String,Object>param);
	void updateState(int id);
	HashMap<String,Object>getFormid(int userId);
	void saveCash(@Param("userId")int userId,@Param("money")double money,@Param("partner_trade_no")String partner_trade_no,
			@Param("payment_no")String payment_no,@Param("payment_time")String payment_time);
	HashMap<String,Double>getRealMoney(int userId);
	void saveQuestion(int userId);
	HashMap<String,Integer>findQuestion(int userId);
	
	List<HashMap<String,Object>>findBadWord();
	
	List<HashMap<String,Object>>getCommon_question();
	void refund(@Param("userId")int userId,@Param("money")double money,@Param("jobId")int jobId);
	void saveJobTake(Map<String,Object>param);
	List<HashMap<String,Object>>getTimerFormId(int jobId);
	
	HashMap<String,Object>getXunfei();
	void updateXunfei(int id);
	HashMap<String,Object>checkCash(int jobId);
	List<HashMap<String,Object>>getApps();
	void saveVipReply(@Param("name")String name,@Param("tel")String tel);
	
	void saveUserCommand(@Param("command")String command,@Param("userId")int userId);
	List<HashMap<String,Object>>getUserCommand(int userId);
	List<HashMap<String,String>>searchCommand(@Param("text")String text);
	
	int update();
	
	void saveIp(String ip);
	HashMap<String,Object>checkSecond(String ip);
}
