package com.connection.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface BegJobDao {
	//增加job数据
	void addJob(Map<String,Object>param);
	void payOver(@Param("transaction_id")String transaction_id,
			@Param("out_trade_no")String out_trade_no,@Param("id")int id);
	//	获得我发布得红包 
	List<HashMap<String,Object>>getMyPush(@Param("userId")int userId,@Param("id")int id);
	HashMap<String,Object>getJobById(int id);
	HashMap<String,Object>getJobById1(int id);
	//<!-- 	判断语音是否可讲 -->
	HashMap<String,Object>getJobById2(int id);
	//一个人抢走红包之后，跟新红包信息
	int modifyCount(@Param("award")double award,@Param("id")int id,@Param("version")int version);
	//获得红包总数和总金额（我发的）
	HashMap<String,Object>getCount(int userId);
	List<HashMap<String,Object>>getOverdue();
	//修改红包状态
	void updateState(@Param("state")int state,@Param("id")int id);
	List<HashMap<String,Object>>getTimerJob(@Param("id")int id);
	//得到被订阅红包们
	List<HashMap<String,Object>>pushTimeDue();
	//修改是否推送消息的takepush，0应该是没推送过，1是推送了
	void updateTakePush(int id);
	void updateSharePic(@Param("shareUrl")String sharePic,@Param("id")int id);
}
