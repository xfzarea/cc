package com.connection.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface VoiceRecordDao {
	//插入语音信息到语音表，，并插入主键信息到voice
	void saveVoice(Map<String,Object>param);
	HashMap<String,Object>findOne(int id);
	List<HashMap<String,Object>>findAllVoice(@Param("jobId")int jobId,@Param("id")int id,@Param("userId")int userId);
	//根据userId和jobId查询语音
	HashMap<String,Object>checkVoice(@Param("userId")int userId,@Param("jobId")int jobId);
	//获得红包总数和总金额（我抢的）
	HashMap<String,Object>getCount(int userId);
	//获得红包（我抢的）
	List<HashMap<String,Object>>getMyJoin(@Param("userId")int userId,@Param("id")int id);
	//保存证书路径到数据库
	void saveBook(@Param("book_Url")String book_Url,@Param("id")int id);
	
	//查询记录<!-- 这里的state，0_现金提额，1_红包退款，3_红包领取 -->
	List<HashMap<String,Object>>getDetail(@Param("userId")int userId,@Param("startId")int startId);
	//<!-- 	根据用户id和状态=0（voice_record 中状态0是抢红包成功的，1是不成功的）和book——url不为空查询速率最高的1条，获得book_url,rate（速率） -->
	HashMap<String,Object>getHighRate(int userId);
	//<!-- 	获得自己的语音 -->且是最近后一条
	HashMap<String,Object>getMineVoice(@Param("jobId")int jobId,@Param("userId")int userId);
}
