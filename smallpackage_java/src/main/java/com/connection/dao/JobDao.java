package com.connection.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface JobDao {
	void addJob(Map<String,Object>param);
	void payOver(@Param("transaction_id")String transaction_id,
			@Param("out_trade_no")String out_trade_no,@Param("id")int id);
	List<HashMap<String,Object>>getMyPush(@Param("userId")int userId,@Param("id")int id);
	HashMap<String,Object>getJobById(int id);
	HashMap<String,Object>getJobById1(int id);
	HashMap<String,Object>getJobById2(int id);
	int modifyCount(@Param("award")double award,@Param("id")int id,@Param("version")int version);
	HashMap<String,Object>getCount(int userId);
	List<HashMap<String,Object>>getOverdue();
	void updateState(@Param("state")int state,@Param("id")int id);
	List<HashMap<String,Object>>getTimerJob(@Param("id")int id);
	List<HashMap<String,Object>>pushTimeDue();
	void updateTakePush(int id);
	void updateSharePic(@Param("shareUrl")String sharePic,@Param("id")int id);
}
