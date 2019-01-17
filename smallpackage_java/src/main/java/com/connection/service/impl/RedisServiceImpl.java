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

//����ʹ��redis����Ư�����л��治ִ�з�����û����ִ�в�ѯ
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
	 * ��õ�һ��begjob
	 * @param id
	 * @return
	 * 
	 * unless:���������ؿ�ֵʱ���Ͳ��ᱻ��������,�����Ƿ�Ҫ�񶨷������棬���������������ж�
	 */
	@Cacheable(value="begJob",key="'begJob'+#id",unless = "#result == null")
	public HashMap<String,Object>getBegJobById(int id){
		return begJobDao.getBegJobById(id);//ע�⿴�����������ݿ�
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
	 * ��ø��ְ�Ǯ��¼
	 * @param userId
	 * @param id
	 * @return
	 */
	@Cacheable(value="begJobRecord",key="'begJobRecord,jobId:'+#jobId")
	public List<HashMap<String,Object>>getRecord(int jobId){
		return begJobDao.getRecord( jobId);
	}
	/**
	 * ������ְ�Ǯ��¼����
	 * @param userId
	 * @param id
	 * @return
	 */
	@CacheEvict(value="begJobRecord",key="'begJobRecord,jobId:'+#jobId")
	public void deleteRecord(int jobId){
	
	}
	/**
	 * ɾ��job�û���
	 * @param job
	 */
	@CacheEvict(value="Job",key="'job'+#id")
	public void deleteRedisJob(int id){
		
	}
	
	/**
	 * ɾ��begjob�û���
	 * @param job
	 */
	@CacheEvict(value="begJob",key="'BegJob'+#id")
	public void deleteRedisBegJob(int id){
		
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
	/**
	 * �������
	 */
	
	@Cacheable(value="lucky_number",key="'number'")
	public List<String> getLuckyNumber() {
		List<String> list =begJobDao.getLuckyNumber();

		return list ;
	}
	/**
	 * ɾ������
	 */
	@CacheEvict(value="lucky_number",key="'number'")
	public void deleteLuckyNumber(){
		
	}
	/**
	 * ���beg���ֿ���
	 * @param id
	 * @return
	 * 
	 * unless:���������ؿ�ֵʱ���Ͳ��ᱻ��������,�����Ƿ�Ҫ�񶨷������棬���������������ж�
	 */
	@Cacheable(value="BegCommand",key="'BegCommand'+#id",unless = "#result == null")
	public List<HashMap<String,Object>> getBegCommand(int id){
		return dataDao.getBegCommand(id);//ע�⿴�����������ݿ�
	}

	/**
	 * ���ͼƬ����
	 * @param id
	 * @return
	 * 
	 * unless:���������ؿ�ֵʱ���Ͳ��ᱻ��������,�����Ƿ�Ҫ�񶨷������棬���������������ж�
	 */
	@Cacheable(value="CommandImage",key="'CommandImage'+#id",unless = "#result == null")
	public List<HashMap<String,Object>> getCommandImage(int id){
		return dataDao.getCommandImage(id);//ע�⿴�����������ݿ�
	}
	/**
	 * �����������
	 * @param id
	 * @return
	 * 
	 * unless:���������ؿ�ֵʱ���Ͳ��ᱻ��������,�����Ƿ�Ҫ�񶨷������棬���������������ж�
	 */
	@Cacheable(value="VoiceCommand",key="'VoiceCommand'+#id",unless = "#result == null")
	public List<HashMap<String,Object>> getVoiceCommand(int id){
		return dataDao.getVoiceCommand(id);//ע�⿴�����������ݿ�
	}
	/**
	 * �����Ƶ����
	 * @param id
	 * @return
	 * 
	 * unless:���������ؿ�ֵʱ���Ͳ��ᱻ��������,�����Ƿ�Ҫ�񶨷������棬���������������ж�
	 */
	@Cacheable(value="VedioCommand",key="'VedioCommand'+#id",unless = "#result == null")
	public List<HashMap<String,Object>> getVedioCommand(int id){
		return dataDao.getVedioCommand(id);//ע�⿴�����������ݿ�
	}
	//��ʱû�õ����������ˣ�
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
