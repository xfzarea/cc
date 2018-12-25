package com.connection.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface VoiceRecordDao {
	void saveVoice(Map<String,Object>param);
	HashMap<String,Object>findOne(int id);
	List<HashMap<String,Object>>findAllVoice(@Param("jobId")int jobId,@Param("id")int id,@Param("userId")int userId);
	HashMap<String,Object>checkVoice(@Param("userId")int userId,@Param("jobId")int jobId);
	HashMap<String,Object>getCount(int userId);
	List<HashMap<String,Object>>getMyJoin(@Param("userId")int userId,@Param("id")int id);
	void saveBook(@Param("book_Url")String book_Url,@Param("id")int id);
	List<HashMap<String,Object>>getDetail(@Param("userId")int userId,@Param("startId")int startId);
	HashMap<String,Object>getHighRate(int userId);
	HashMap<String,Object>getMineVoice(@Param("jobId")int jobId,@Param("userId")int userId);
}
