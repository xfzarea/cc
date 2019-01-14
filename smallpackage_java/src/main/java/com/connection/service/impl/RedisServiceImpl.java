package com.connection.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.connection.dao.JobDao;
import com.connection.dao.VoiceRecordDao;
import com.connection.service.interfaces.RedisService;

//这种使用redis方法漂亮，有缓存不执行方法，没缓存执行查询
@Service
public class RedisServiceImpl implements RedisService {
	@Autowired
	private JobDao jobDao;
	@Autowired
	private VoiceRecordDao voiceRecordDao;
	
	/**
	 * 获得单一得job
	 * @param id
	 * @return
	 * 
	 * unless:当方法返回空值时，就不会被缓存起来,决定是否要否定方法缓存，可以用来做条件判断
	 */
	@Cacheable(value="job",key="'job'+#id",unless = "#result == null")
	public HashMap<String,Object>getJobById(int id){
		return jobDao.getJobById(id);//注意看，这是拿数据库
	}
	/**
	 * 获得自己得语音
	 * @param userId
	 * @param id
	 * @return
	 */
	@Cacheable(value="jobVoice",key="'job_voice,userId:'+#userId+'jobId:'+#id")
	public HashMap<String,Object>checkVoice(int userId,int id){
		return voiceRecordDao.checkVoice(userId, id);
	}
	/**
	 * 删除job得缓存
	 * @param job
	 */
	@CacheEvict(value="job",key="'job'+#id")
	public void deleteRedisJob(int id){
		
	}
	/**
	 * 删除用户语音redis缓存
	 * @param userId
	 * @param id
	 */
	@CacheEvict(value="jobVoice",key="'job_voice,userId:'+#userId+'jobId:'+#id")
	public void deleteVoiceRedis(int userId,int id){
		
	}
	/**
	 * 缓存福利界面得红包
	 * @param id
	 * @return
	 */
	@Cacheable(value="timer",key="'timerJob'")
	public List<HashMap<String,Object>> cachTimerJob(int id){
		List<HashMap<String,Object>>timerJobs = jobDao.getTimerJob(id); 
		return timerJobs;
		
	}
	/**
	 * 清除福利界面得红包缓存
	 * @CacheEvict清除缓存的标注
	 */
	@CacheEvict(value="timer",key="'timerJob'")
	public void deleteTimerCash(){
		
	}
	/**
	 * 缓存用户语音
	 */
	@Override
	@Cacheable(value="mine_voice",key="'mine_voice:jobId='+#jobId+',userId='+#userId")
	public HashMap<String,Object>getMineVoice(int userId, int jobId) {
		return voiceRecordDao.getMineVoice(jobId, userId);
	}
	/**
	 * 删除用户语音
	 */
	@CacheEvict(value="mine_voice",key="'mine_voice:jobId='+#jobId+',userId='+#userId")
	public void deleteMineVoice(int userId, int jobId){
		
	}
}
