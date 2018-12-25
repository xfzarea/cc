package com.connection.dao;

import java.util.HashMap;
import java.util.Map;

public interface AccTokenDao {
	void saveAccToken(Map<String,Object>param);
	HashMap<String,Object>findNewAccToken();
	HashMap<String,Long>findLimit(int id);
}
