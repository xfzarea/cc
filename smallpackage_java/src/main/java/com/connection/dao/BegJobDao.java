package com.connection.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface BegJobDao {
	//增加job数据
	void addJob(Map<String,Object>param);

	
	int payOver(@Param("transaction_id")String transaction_id,
			@Param("out_trade_no")String out_trade_no,@Param("jobId")int id,@Param("userId")int userId);
//	获得我被讨或讨的
	List<HashMap<String,Object>>getMyBegPush(@Param("userId")int userId,@Param("id")int id);
	List<HashMap<String,Object>>getMyBeggedPush(@Param("userId")int userId,@Param("id")int id);
	
	//通过job查询用户
	
	HashMap<String,Object>getBegJobById(int id);
	HashMap<String,Object>getJobById1(int id);
	//<!-- 	判断语音是否可讲 -->
	HashMap<String,Object>getJobById2(int id);
	//一个人抢走红包之后，跟新红包信息
	int modifyCount(@Param("award")double award,@Param("id")int id,@Param("version")int version);
	//获得红包总数和总金额（我讨的）
	HashMap<String,Object>getBegCount(int userId);
	//获得红包总数和总金额（我被讨的）
	HashMap<String,Object>getBegRecordCount(int userId);
	List<HashMap<String,Object>>getOverdue();
	//修改红包状态
	void updateState(@Param("state")int state,@Param("id")int id);
	List<HashMap<String,Object>>getTimerJob(@Param("id")int id);
	//得到被订阅红包们
	List<HashMap<String,Object>>pushTimeDue();
	//修改是否推送消息的takepush，0应该是没推送过，1是推送了
	void updateTakePush(int id);
	void updateBegSharePic(@Param("shareUrl")String sharePic,@Param("id")int id);
	//用户自定义上传
	int insertCommandImage(String commandImagePath,int userId);
	int insertVoiceCommand(String commandVoicePath,int userId,int voiceTime);
	int insertVedioCommand(String commandVedioPath,int userId);
	int saveBegCommand(int userId,String context);
	//sys上传
	int sysCommandImage(String commandImagePath, int fatherId);
	int sysVedioCommand(String commandImagePath, int fatherId);
	int sysVoiceCommand(String commandImagePath, int fatherId,String context, int voiceTime);
	int sysCommand(int contextId, int levelContextId,String context);
	int sysBegCommand(int fatherId,String context);
	
	public List<String> getLuckyNumber();
	
	public int addLuckyNumber(Double number);
	
	public List<Integer> getUserId(@Param("openid")String openid);
	
	public List<Integer> getUserIdByJobId(int jobId);
	
	List<HashMap<String,Object>>getRecord( @Param("jobId")int jobId);
	
	HashMap<String,Object> getPaied(int userId, int jobId);
	
	int updataBegRecordState(int jobId,int userId);
}
