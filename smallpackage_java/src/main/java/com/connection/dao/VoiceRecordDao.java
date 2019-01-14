package com.connection.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface VoiceRecordDao {
	//����������Ϣ����������������������Ϣ��voice
	void saveVoice(Map<String,Object>param);
	HashMap<String,Object>findOne(int id);
	List<HashMap<String,Object>>findAllVoice(@Param("jobId")int jobId,@Param("id")int id,@Param("userId")int userId);
	//����userId��jobId��ѯ����
	HashMap<String,Object>checkVoice(@Param("userId")int userId,@Param("jobId")int jobId);
	//��ú���������ܽ������ģ�
	HashMap<String,Object>getCount(int userId);
	//��ú���������ģ�
	List<HashMap<String,Object>>getMyJoin(@Param("userId")int userId,@Param("id")int id);
	//����֤��·�������ݿ�
	void saveBook(@Param("book_Url")String book_Url,@Param("id")int id);
	
	//��ѯ��¼<!-- �����state��0_�ֽ���1_����˿3_�����ȡ -->
	List<HashMap<String,Object>>getDetail(@Param("userId")int userId,@Param("startId")int startId);
	//<!-- 	�����û�id��״̬=0��voice_record ��״̬0��������ɹ��ģ�1�ǲ��ɹ��ģ���book����url��Ϊ�ղ�ѯ������ߵ�1�������book_url,rate�����ʣ� -->
	HashMap<String,Object>getHighRate(int userId);
	//<!-- 	����Լ������� -->���������һ��
	HashMap<String,Object>getMineVoice(@Param("jobId")int jobId,@Param("userId")int userId);
}
