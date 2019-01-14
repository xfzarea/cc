package com.connection.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.connection.entity.Admin;

public interface SysAdminDao {
	//����sys�û�
	void saveUser(Admin admin);
	//����openId��ѯ�û��Ƿ����
	HashMap<String,Object>findUserInfo(String openid);
	//�����û���Ϣ������admin�е�openId��nickName,avatarUrl ,gender,province ��city
	void modifyUserBaseInfo(Admin admin);
	HashMap<String,Object>getUserById(int userId);
	
	//�����µ�������ʱ�����job���в���һ�����ݣ�����ԭ���Ĳ��������id��ֵ��
	void createTimerJob(Map<String,Object>param);
	//<!-- transaction_id = "bxszc" �ĺ��Ӧ�ö����ڹ���Աƽ̨���� -->����ѯtransaction_id = "bxszc"������״̬��1��2��4�ĺ��
	List<HashMap<String,Object>>getJobs();
	//����ɾ��Ҳû����ֵ���ǲ��ǲ����
	void deleteJob(int id);
	HashMap<String,Object>getTimerJobById(int id);
	HashMap<String,Object>getUserJobById(int id);
	//���¶�ʱ���
	void updataTimerJob(Map<String,Object>param);
	Map<String,Integer>getUserIdById(int id);
	void updateAdmin(Admin admin);
}
