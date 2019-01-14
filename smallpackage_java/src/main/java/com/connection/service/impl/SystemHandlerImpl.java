package com.connection.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.connection.dao.AdminDao;
import com.connection.dao.SysAdminDao;
import com.connection.entity.Admin;
import com.connection.service.interfaces.RedisService;
import com.connection.service.interfaces.SystemHandler;
@Service
public class SystemHandlerImpl implements SystemHandler {
	@Autowired
	private SysAdminDao sysAdminDao;
	@Autowired
	private AdminDao adminDao;
	@Autowired
	private RedisService redis;
	/**
	 * 保存用户信息，并得到更完整信息
	 * @return
	 */
	public synchronized Admin saveAdmin(Admin admin){
		//先根据openId查询用户是否存在
		Map<String,Object>userInfo = sysAdminDao.findUserInfo(admin.getOpenid());
		if(userInfo == null){
			//不存在，保存系统用户
			sysAdminDao.saveUser(admin);
		}else{
			//存在，先跟新下系统管理员的其他信息（nickName,avatarUrl ,gender,province ，city）
			sysAdminDao.modifyUserBaseInfo(admin);
			//从userInfo中拿到userId，添加到admin中（前端来的时候没有的）
			int userId = (Integer) userInfo.get("userId");
			admin.setUserId(userId);
		}
		return admin;
	}
	/**
	 * 生成定时红包
	 */
	@Transactional
	public int createTimerJob(Map<String,String>param){
		//删除redis中的福利界面得红包缓存
		redis.deleteTimerCash();
		Map<String,Object>realParam = null;
		int id = 0;
		//先查询下管理员是否存在，且有效
		Map<String,Object>admin = sysAdminDao.findUserInfo(param.get("openid"));
		if(admin!=null&&(Integer)admin.get("state")==1){
			realParam = new HashMap<String,Object>();
			double award = Double.parseDouble(param.get("award"));
			realParam.put("id", 0);//字段先设置一下，默认0是需要生成
			realParam.put("headPic",param.get("headPic"));
			realParam.put("userId", 0);
			//String.format("%.2f", award * 1.02)格式化字符串，%f浮点类型，%.2f小数点后两位（这里是包括服务费总金额）
			realParam.put("totalAward", Double.parseDouble(String.format("%.2f", award * 1.02)));
			//这里是直接的红包金额
			realParam.put("award", award);
			realParam.put("totalCount", Integer.parseInt(param.get("totalCount")));
			//口令内容
			realParam.put("context", param.get("context"));
			//创建时间
			realParam.put("createTime", param.get("createTime"));
			//状态码
			realParam.put("state", 4);
			realParam.put("transaction_id", "bxszc");
			realParam.put("title", param.get("title"));
			realParam.put("userName", param.get("userName"));
			realParam.put("timeLimit", 1);
			if(!"null".equals((String)param.get("skipAppid"))){
				realParam.put("skipAppid", param.get("skipAppid"));
				realParam.put("skipUrl", param.get("skipUrl"));
			}
			if(!"null".equals((String)param.get("wxchat_mark"))){
				realParam.put("wxchat_mark", param.get("wxchat_mark"));
			}
			if(!"null".equals((String)param.get("pre_image"))){
				realParam.put("pre_image", param.get("pre_image"));
			}
			if(!"null".equals((String)param.get("tweet"))){
				realParam.put("tweet", param.get("tweet"));
			}
			if(!"null".equals((String)param.get("tweet_title"))){
				realParam.put("tweet_title", param.get("tweet_title"));
			}
			if(Integer.parseInt(param.get("id"))==0){//代表需要生成红包（这里的id应该代表红包编号吧）
				sysAdminDao.createTimerJob(realParam);
				id = (Integer)realParam.get("id");
			}else{//代表修改紅包
				id = Integer.parseInt(param.get("id"));
				redis.deleteRedisJob(id);
				realParam.put("id", id);
				sysAdminDao.updataTimerJob(realParam);
			}
		}
		param = null;
		realParam = null;
		return id;
	}
	/**
	 * 发自定义红包
	 * @return
	 */
	public int createUserJob(Map<String,String>param){
		Map<String,Object>realParam = null;
		Admin admin = null;
		int id = 0;
		//先根据openId查询管理员是否存在，且有效
		Map<String,Object>a = sysAdminDao.findUserInfo(param.get("openid"));
		if(a!=null&&(Integer)a.get("state")==1){
			id = Integer.parseInt(param.get("id"));
			int userId = 0;
			admin = new Admin();
			admin.setNickName(param.get("nickName"));
			admin.setAvatarUrl(param.get("avatarUrl"));
			admin.setOpenid("bxszc");
			//除了这里有区别，基本和上面一样
			if(id==0){//（这里的id应该代表红包编号吧），
				//如果没有发过红包，保存下普通用户
				adminDao.saveUser(admin);
				//得到普通用户的userid
				userId = adminDao.sysFindUser(admin).get("userId");
			}else{
				//如果不是0，从job表获得userId（。。。这命名）
				userId = sysAdminDao.getUserIdById(id).get("userId");
				//设置userId
				admin.setUserId(userId);
				//更新admin表中的用户信息，不是sys表的（前面吓死我了）
				sysAdminDao.updateAdmin(admin);
			}
			realParam = new HashMap<String,Object>();
			double award = Double.parseDouble(param.get("award"));
			realParam.put("userId", userId);
			realParam.put("totalAward", Double.parseDouble(String.format("%.2f", award * 1.02)));
			realParam.put("award", award);
			realParam.put("totalCount", Integer.parseInt(param.get("totalCount")));
			realParam.put("context", param.get("context"));
			realParam.put("state", 4);
			realParam.put("timeLimit", 0);
			realParam.put("state", 1);
			realParam.put("transaction_id", "bxszc");
			if(!"null".equals(param.get("skipAppid"))){
				realParam.put("skipAppid", param.get("skipAppid"));
				realParam.put("skipUrl", param.get("skipUrl"));
			}
			if(!"null".equals((String)param.get("wxchat_mark"))){
				realParam.put("wxchat_mark", param.get("wxchat_mark"));
			}
			if(!"null".equals((String)param.get("pre_image"))){
				realParam.put("pre_image", param.get("pre_image"));
			}
			if(!"null".equals((String)param.get("tweet"))){
				realParam.put("tweet", param.get("tweet"));
			}
			if(!"null".equals((String)param.get("tweet_title"))){
				realParam.put("tweet_title", param.get("tweet_title"));
			}
			if(id==0){//（这里的id应该代表红包编号吧）
				sysAdminDao.createTimerJob(realParam);
				id = (Integer)realParam.get("id");
			}else{
				realParam.put("id", id);
				redis.deleteRedisJob(id);
				sysAdminDao.updataTimerJob(realParam);
			}
		}
		return id;
	}
	/**
	 * 管理员删除红包
	 */
	public void deleteJob(String openid,int id){
		//先验证管理员身份信息
		if(checkInfo(openid)){
			sysAdminDao.deleteJob(id);
			//这里是不是应该加一个redis删除操作
		}
	}
	/**
	 * 判断身份
	 * @return
	 */
	public boolean checkInfo(String openid){
		Map<String,Object>a = sysAdminDao.findUserInfo(openid);
		if(a!=null&&(Integer)a.get("state")==1){
			return true;
		}else{
			return false;
		}
	}
}
