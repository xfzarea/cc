package com.connection.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface DataDao {
	void saveFormid(Map<String,Object>param);
	//更新user_formid表的state变成1，根据id
	void updateState(int id);
	//查询 a.id,a.formid,a.userId,b.openid，根据 userId 、state 、TIMESTAMPDIFF(HOUR,insertTime,NOW()) &lt; 167 LIMIT 1（插入到现在的时间小于167个小时）
	HashMap<String,Object>getFormid(int userId);
	//保存提现操作记录userId,money,（后面三个由微信返回给的）partner_trade_no,payment_no,payment_time
	void saveCash(@Param("userId")int userId,@Param("money")double money,@Param("partner_trade_no")String partner_trade_no,
			@Param("payment_no")String payment_no,@Param("payment_time")String payment_time);
	//得到真实的钱根据用户id
	HashMap<String,Double>getRealMoney(int userId);
	
	
	void saveQuestion(int userId);
	HashMap<String,Integer>findQuestion(int userId);
	
	List<HashMap<String,Object>>findBadWord();
	//得到常见问题（全查）
	List<HashMap<String,Object>>getCommon_question();
	
	void refund(@Param("userId")int userId,@Param("money")double money,@Param("jobId")int jobId);
	//
	void saveJobTake(Map<String,Object>param);
	//得到订阅了这个红包的FormId
	List<HashMap<String,Object>>getTimerFormId(int jobId);
	
//	得到讯飞的账号
	HashMap<String,Object>getXunfei();
	//讯飞inCount + 1
	void updateXunfei(int id);
	HashMap<String,Object>checkCash(int jobId);
	List<HashMap<String,Object>>getApps();
	//把想要推广合作的人保存到数据库，名字电话
	void saveVipReply(@Param("name")String name,@Param("tel")String tel);
	
	//保存用户口令
	void saveUserCommand(@Param("command")String command,@Param("userId")int userId);
	//<!-- 	根据用户id降序和state=1，查找user_command 的口令（单用户自己的） -->
	List<HashMap<String,Object>>getUserCommand(int userId);
	List<HashMap<String,Object>>getUserCommandImage(int userId);
	List<HashMap<String,Object>>getUserCommandVedio(int userId);
	List<HashMap<String,Object>>getUserCommandVoice(int userId);
	
	//查找客户和公共的口令（关键字搜索功能）
	List<HashMap<String,String>>searchCommand(@Param("text")String text);
	
	//测试没用的
	int update();
	
	void saveIp(String ip);
	HashMap<String,Object>checkSecond(String ip);
	//得到图片口令
	List<HashMap<String,Object>>getCommandImage(int id);	
	//得到语音口令
	List<HashMap<String,Object>>getVoiceCommand(int id);
	//得到视频口令
		List<HashMap<String,Object>>getVedioCommand(int id);
		//得到beg文字口令
				List<HashMap<String,Object>>getBegCommand(int id);	

}
