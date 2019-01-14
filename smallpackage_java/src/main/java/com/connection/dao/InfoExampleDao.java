package com.connection.dao;

import java.util.HashMap;
import java.util.List;

public interface InfoExampleDao {
	//这里的id是数据库的levelContextId类似父类id，就是查父类的所有子类
	List<HashMap<String,Object>>getCommand(int id);
}
