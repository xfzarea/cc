package com.connection.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.connection.dao.BegJobDao;
import com.connection.dao.DataDao;
import com.connection.dao.JobDao;
import com.connection.dao.VoiceRecordDao;
import com.connection.service.interfaces.RedisService;

//这种使用redis方法漂亮，有缓存不执行方法，没缓存执行查询
@Service
public class RedisServiceImpl implements RedisService {
	@Autowired
	private JobDao jobDao;
	@Autowired
	private BegJobDao begJobDao;
	@Autowired
	private VoiceRecordDao voiceRecordDao;
	@Autowired
	private DataDao dataDao;
	
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
	 * 获得单一得begjob
	 * @param id
	 * @return
	 * 
	 * unless:当方法返回空值时，就不会被缓存起来,决定是否要否定方法缓存，可以用来做条件判断
	 */
	@Cacheable(value="begJob",key="'begJob'+#id",unless = "#result == null")
	public HashMap<String,Object>getBegJobById(int id){
		return begJobDao.getBegJobById(id);//注意看，这是拿数据库
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
	 * 获得付讨包钱记录
	 * @param userId
	 * @param id
	 * @return
	 */
	@Cacheable(value="begJobRecord",key="'begJobRecord,jobId:'+#jobId")
	public List<HashMap<String,Object>>getRecord(int jobId){
		return begJobDao.getRecord( jobId);
	}
	/**
	 * 清除付讨包钱记录缓存
	 * @param userId
	 * @param id
	 * @return
	 */
	@CacheEvict(value="begJobRecord",key="'begJobRecord,jobId:'+#jobId")
	public void deleteRecord(int jobId){
	
	}
	/**
	 * 删除job得缓存
	 * @param job
	 */
	@CacheEvict(value="Job",key="'job'+#id")
	public void deleteRedisJob(int id){
		
	}
	
	/**
	 * 删除begjob得缓存
	 * @param job
	 */
	@CacheEvict(value="begJob",key="'BegJob'+#id")
	public void deleteRedisBegJob(int id){
		
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
	/**
	 * 获得数字
	 */
	
	@Cacheable(value="lucky_number",key="'number'")
	public List<String> getLuckyNumber() {
		List<String> list =begJobDao.getLuckyNumber();

		return list ;
	}
	/**
	 * 删除数字
	 */
	@CacheEvict(value="lucky_number",key="'number'")
	public void deleteLuckyNumber(){
		
	}
	/**
	 * 获得beg文字口令
	 * @param id
	 * @return
	 * 
	 * unless:当方法返回空值时，就不会被缓存起来,决定是否要否定方法缓存，可以用来做条件判断
	 */
	@Cacheable(value="BegCommand",key="'BegCommand'+#id",unless = "#result == null")
	public List<HashMap<String,Object>> getBegCommand(int id){
		return dataDao.getBegCommand(id);//注意看，这是拿数据库
	}

	/**
	 * 获得图片口令
	 * @param id
	 * @return
	 * 
	 * unless:当方法返回空值时，就不会被缓存起来,决定是否要否定方法缓存，可以用来做条件判断
	 */
	@Cacheable(value="CommandImage",key="'CommandImage'+#id",unless = "#result == null")
	public List<HashMap<String,Object>> getCommandImage(int id){
		return dataDao.getCommandImage(id);//注意看，这是拿数据库
	}
	/**
	 * 获得语音口令
	 * @param id
	 * @return
	 * 
	 * unless:当方法返回空值时，就不会被缓存起来,决定是否要否定方法缓存，可以用来做条件判断
	 */
	@Cacheable(value="VoiceCommand",key="'VoiceCommand'+#id",unless = "#result == null")
	public List<HashMap<String,Object>> getVoiceCommand(int id){
		return dataDao.getVoiceCommand(id);//注意看，这是拿数据库
	}
	/**
	 * 获得视频口令
	 * @param id
	 * @return
	 * 
	 * unless:当方法返回空值时，就不会被缓存起来,决定是否要否定方法缓存，可以用来做条件判断
	 */
	@Cacheable(value="VedioCommand",key="'VedioCommand'+#id",unless = "#result == null")
	public List<HashMap<String,Object>> getVedioCommand(int id){
		return dataDao.getVedioCommand(id);//注意看，这是拿数据库
	}
	//暂时没用到啊（别忘了）
	@CacheEvict(value="CommandImage",key="'CommandImage'+#id")
	public void deleteCommandImage(int id){
		
	}
	@CacheEvict(value="VoiceCommand",key="'VoiceCommand'+#id")
	public void deleteVoiceCommand(int id){
		
	}
	@CacheEvict(value="VedioCommand",key="'VedioCommand'+#id")
	public void deleteVedioCommand(int id){
		
	}
	@CacheEvict(value="BegCommand",key="'BegCommand'+#id")
	public void deleteBegCommand(int id){
		
	}
	
}
