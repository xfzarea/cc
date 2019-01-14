package com.connection.service.interfaces;

import java.util.HashMap;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;


public interface RedisService {
	
	public HashMap<String,Object>getJobById(int id);
	public HashMap<String,Object>checkVoice(int userId,int id);
	public void deleteRedisJob(int id);
	public void deleteVoiceRedis(int userId,int id);
	public List<HashMap<String,Object>> cachTimerJob(int id);
	//删除定时红包缓存
	public void deleteTimerCash();
	//从缓存中获得语音
	public HashMap<String,Object>getMineVoice(int userId,int jobId);
	public void deleteMineVoice(int userId, int jobId);
}
