package com.connection.service.interfaces;

import java.util.Map;

import com.connection.entity.Admin;

public interface SystemHandler {
	//�����û���Ϣ�����õ���������Ϣ
	public Admin saveAdmin(Admin admin);
	//������ʱ���
	public int createTimerJob(Map<String,String>param);
	//����Ա�����û��Զ�����
	public int createUserJob(Map<String,String>param);
	//ɾ���Զ�����
	public void deleteJob(String openid,int id);
}
