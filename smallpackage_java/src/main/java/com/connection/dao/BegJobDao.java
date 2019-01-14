package com.connection.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface BegJobDao {
	//����job����
	void addJob(Map<String,Object>param);
	void payOver(@Param("transaction_id")String transaction_id,
			@Param("out_trade_no")String out_trade_no,@Param("id")int id);
	//	����ҷ����ú�� 
	List<HashMap<String,Object>>getMyPush(@Param("userId")int userId,@Param("id")int id);
	HashMap<String,Object>getJobById(int id);
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
	void updateSharePic(@Param("shareUrl")String sharePic,@Param("id")int id);
}
