package com.connection.dao;

import java.util.HashMap;
import java.util.List;

public interface InfoExampleDao {
	//�����id�����ݿ��levelContextId���Ƹ���id�����ǲ鸸�����������
	List<HashMap<String,Object>>getCommand(int id);
}
