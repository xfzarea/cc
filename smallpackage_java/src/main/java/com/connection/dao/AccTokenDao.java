package com.connection.dao;

import java.util.HashMap;
import java.util.Map;

public interface AccTokenDao {
	void saveAccToken(Map<String,Object>param);
	//查找新的acctoken（id降序）只取一条 
	HashMap<String,Object>findNewAccToken();
	//获取当前时间戳与获取accToken的时间戳之差，时间戳 是自1970年0时0分0秒以来UTC标准时间的秒数
	HashMap<String,Long>findLimit(int id);
}
