package com.connection.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface BegJobDao {
	//����job����
	void addJob(Map<String,Object>param);

	
	int payOver(@Param("transaction_id")String transaction_id,
			@Param("out_trade_no")String out_trade_no,@Param("jobId")int id,@Param("userId")int userId);
//	����ұ��ֻ��ֵ�
	List<HashMap<String,Object>>getMyBegPush(@Param("userId")int userId,@Param("id")int id);
	List<HashMap<String,Object>>getMyBeggedPush(@Param("userId")int userId,@Param("id")int id);
	
	//ͨ��job��ѯ�û�
	
	HashMap<String,Object>getBegJobById(int id);
	HashMap<String,Object>getJobById1(int id);
	//<!-- 	�ж������Ƿ�ɽ� -->
	HashMap<String,Object>getJobById2(int id);
	//һ�������ߺ��֮�󣬸��º����Ϣ
	int modifyCount(@Param("award")double award,@Param("id")int id,@Param("version")int version);
	//��ú���������ܽ��ҷ��ģ�
	HashMap<String,Object>getCount(int userId);
	List<HashMap<String,Object>>getOverdue();
	//�޸ĺ��״̬
	void updateState(@Param("state")int state,@Param("id")int id);
	List<HashMap<String,Object>>getTimerJob(@Param("id")int id);
	//�õ������ĺ����
	List<HashMap<String,Object>>pushTimeDue();
	//�޸��Ƿ�������Ϣ��takepush��0Ӧ����û���͹���1��������
	void updateTakePush(int id);
	void updateBegSharePic(@Param("shareUrl")String sharePic,@Param("id")int id);
	//�û��Զ����ϴ�
	int insertCommandImage(String commandImagePath,int userId);
	int insertVoiceCommand(String commandVoicePath,int userId);
	int insertVedioCommand(String commandVedioPath,int userId);
	//sys�ϴ�
	int sysCommandImage(String commandImagePath, int fatherId);
	int sysVedioCommand(String commandImagePath, int fatherId);
	int sysVoiceCommand(String commandImagePath, int fatherId,String context);
	
	
	public List<String> getLuckyNumber();
	public int addLuckyNumber(Double number);
	
	public int getUserId(String openId);
	
	public int getUserIdByJobId(int jobId);
	
	
	List<HashMap<String,Object>>getRecord( int jobId);
	
	HashMap<String,Object> getPaied(int userId, int jobId);
	
	int updataBegRecordState(int jobId,int userId);
}
