package com.connection.dao;

import java.util.HashMap;
import java.util.List;

public interface InfoExampleDao {
	List<HashMap<String,Object>>getCommand(int id);
}
