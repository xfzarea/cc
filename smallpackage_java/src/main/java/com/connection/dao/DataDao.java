package com.connection.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface DataDao {
	void saveFormid(Map<String,Object>param);
	//����user_formid���state���1������id
	void updateState(int id);
	//��ѯ a.id,a.formid,a.userId,b.openid������ userId ��state ��TIMESTAMPDIFF(HOUR,insertTime,NOW()) &lt; 167 LIMIT 1�����뵽���ڵ�ʱ��С��167��Сʱ��
	HashMap<String,Object>getFormid(int userId);
	//�������ֲ�����¼userId,money,������������΢�ŷ��ظ��ģ�partner_trade_no,payment_no,payment_time
	void saveCash(@Param("userId")int userId,@Param("money")double money,@Param("partner_trade_no")String partner_trade_no,
			@Param("payment_no")String payment_no,@Param("payment_time")String payment_time);
	//�õ���ʵ��Ǯ�����û�id
	HashMap<String,Double>getRealMoney(int userId);
	
	
	void saveQuestion(int userId);
	HashMap<String,Integer>findQuestion(int userId);
	
	List<HashMap<String,Object>>findBadWord();
	//�õ��������⣨ȫ�飩
	List<HashMap<String,Object>>getCommon_question();
	
	void refund(@Param("userId")int userId,@Param("money")double money,@Param("jobId")int jobId);
	//
	void saveJobTake(Map<String,Object>param);
	//�õ���������������FormId
	List<HashMap<String,Object>>getTimerFormId(int jobId);
	
//	�õ�Ѷ�ɵ��˺�
	HashMap<String,Object>getXunfei();
	//Ѷ��inCount + 1
	void updateXunfei(int id);
	HashMap<String,Object>checkCash(int jobId);
	List<HashMap<String,Object>>getApps();
	//����Ҫ�ƹ�������˱��浽���ݿ⣬���ֵ绰
	void saveVipReply(@Param("name")String name,@Param("tel")String tel);
	
	//�����û�����
	void saveUserCommand(@Param("command")String command,@Param("userId")int userId);
	//<!-- 	�����û�id�����state=1������user_command �Ŀ�����û��Լ��ģ� -->
	List<HashMap<String,Object>>getUserCommand(int userId);
	List<HashMap<String,Object>>getUserCommandImage(int userId);
	List<HashMap<String,Object>>getUserCommandVedio(int userId);
	List<HashMap<String,Object>>getUserCommandVoice(int userId);
	
	//���ҿͻ��͹����Ŀ���ؼ����������ܣ�
	List<HashMap<String,String>>searchCommand(@Param("text")String text);
	
	//����û�õ�
	int update();
	
	void saveIp(String ip);
	HashMap<String,Object>checkSecond(String ip);
	//�õ�ͼƬ����
	List<HashMap<String,Object>>getCommandImage(int id);	
	//�õ���������
	List<HashMap<String,Object>>getVoiceCommand(int id);
	//�õ���Ƶ����
		List<HashMap<String,Object>>getVedioCommand(int id);
		//�õ�beg���ֿ���
				List<HashMap<String,Object>>getBegCommand(int id);	

}
