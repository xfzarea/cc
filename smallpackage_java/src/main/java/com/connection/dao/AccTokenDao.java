package com.connection.dao;

import java.util.HashMap;
import java.util.Map;

public interface AccTokenDao {
	void saveAccToken(Map<String,Object>param);
	//�����µ�acctoken��id����ֻȡһ�� 
	HashMap<String,Object>findNewAccToken();
	//��ȡ��ǰʱ������ȡaccToken��ʱ���֮�ʱ��� ����1970��0ʱ0��0������UTC��׼ʱ�������
	HashMap<String,Long>findLimit(int id);
}
