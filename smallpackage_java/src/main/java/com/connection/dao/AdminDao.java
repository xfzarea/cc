package com.connection.dao;

import java.util.HashMap;

import org.apache.ibatis.annotations.Param;

import com.connection.entity.Admin;

public interface AdminDao {
	//�������û�
	int saveUser(Admin admin);
	//����openid�����û���Ϣ�������û�
	//userId,nickName,avatarUrl,gender,province,city,openid,createTime
	Admin findUserInfo(String openid);
	//�û�������Ϣ�����ı�ʱ���ô˷������޸��û�������Ϣ
	void modifyUserBaseInfo(Admin admin);
	//�����û�id��menoy����award��Ǯ(Ϊʲô������sql�ӿڹ���һ������)
	void modifyMoney(@Param("award")double award,@Param("userId")int userId);
	void modifyMoney1(@Param("award")double award,@Param("userId")int userId);//�����ֵ�ʱ�������������ӦΪ��Ǯ�Ǹ������Կ۳�
	//�����û�id��ѯuserId,nickName,avatarUrl,money,money_version 
	HashMap<String,Object>getUserById(int userId);
	//�ֹ����������û�id������Ǯ�����汾�ţ��޸İ汾��Ϊ���ڰ汾��+1		
	int checkVersion(@Param("userId")int userId,@Param("money_version")int money_version);
	//���� openid��ͷ���ַ���ǳƣ���ѯ�û�id�������������ģ�����
	HashMap<String,Integer>sysFindUser(Admin admin);
	
//����userId��ѯopenId
	String getOpenIdByUserId(int userId);
}
