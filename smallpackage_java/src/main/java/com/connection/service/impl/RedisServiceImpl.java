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

//����ʹ��redis����Ư�����л��治ִ�з�����û����ִ�в�ѯ
@Service
public class RedisServiceImpl implements RedisService {
	@Autowired
	private JobDao jobDao;
	@Autowired
	private VoiceRecordDao voiceRecordDao;
	
	/**
	 * ��õ�һ��job
	 * @param id
	 * @return
	 * 
	 * unless:���������ؿ�ֵʱ���Ͳ��ᱻ��������,�����Ƿ�Ҫ�񶨷������棬���������������ж�
	 */
	@Cacheable(value="job",key="'job'+#id",unless = "#result == null")
	public HashMap<String,Object>getJobById(int id){
		return jobDao.getJobById(id);//ע�⿴�����������ݿ�
	}
	/**
	 * ����Լ�������
	 * @param userId
	 * @param id
	 * @return
	 */
	@Cacheable(value="jobVoice",key="'job_voice,userId:'+#userId+'jobId:'+#id")
	public HashMap<String,Object>checkVoice(int userId,int id){
		return voiceRecordDao.checkVoice(userId, id);
	}
	/**
	 * ɾ��job�û���
	 * @param job
	 */
	@CacheEvict(value="job",key="'job'+#id")
	public void deleteRedisJob(int id){
		
	}
	/**
	 * ɾ���û�����redis����
	 * @param userId
	 * @param id
	 */
	@CacheEvict(value="jobVoice",key="'job_voice,userId:'+#userId+'jobId:'+#id")
	public void deleteVoiceRedis(int userId,int id){
		
	}
	/**
	 * ���渣������ú��
	 * @param id
	 * @return
	 */
	@Cacheable(value="timer",key="'timerJob'")
	public List<HashMap<String,Object>> cachTimerJob(int id){
		List<HashMap<String,Object>>timerJobs = jobDao.getTimerJob(id); 
		return timerJobs;
		
	}
	/**
	 * �����������ú������
	 * @CacheEvict�������ı�ע
	 */
	@CacheEvict(value="timer",key="'timerJob'")
	public void deleteTimerCash(){
		
	}
	/**
	 * �����û�����
	 */
	@Override
	@Cacheable(value="mine_voice",key="'mine_voice:jobId='+#jobId+',userId='+#userId")
	public HashMap<String,Object>getMineVoice(int userId, int jobId) {
		return voiceRecordDao.getMineVoice(jobId, userId);
	}
	/**
	 * ɾ���û�����
	 */
	@CacheEvict(value="mine_voice",key="'mine_voice:jobId='+#jobId+',userId='+#userId")
	public void deleteMineVoice(int userId, int jobId){
		
	}
}
