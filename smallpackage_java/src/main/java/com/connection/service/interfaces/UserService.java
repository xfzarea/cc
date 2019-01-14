package com.connection.service.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.connection.entity.Admin;

public interface UserService {
	//�������ݣ�code, appId,secret)��΢�ŷ�������ȡָ��openId
	public String sendData(String code,String appId,String secret) throws IOException;
	//�����û�
	public Admin saveAdmin(Admin admin);
	//����
	public Integer cash(int userId,double money,String openid);
	//����userId,formid
	public void saveFormid(Map<String,Object>param);
	//����ģ����Ϣ
	public String sendMsg(String Json);
	//<!-- �����û�id��jobid�����id�� -->�������������ú������
	public void saveJobTake(Map<String,Object>param);
	//userService.getCode(json)��ö�ά����Ҫapi����ά��浽�˰����ƣ��õİ���oss����
	public String getCode(String Json);
	public void saveCommand(String command,int userId);
	

}
