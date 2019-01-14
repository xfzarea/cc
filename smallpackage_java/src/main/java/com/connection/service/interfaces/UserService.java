package com.connection.service.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.connection.entity.Admin;

public interface UserService {
	//发送数据（code, appId,secret)到微信服务器换取指定openId
	public String sendData(String code,String appId,String secret) throws IOException;
	//保存用户
	public Admin saveAdmin(Admin admin);
	//提现
	public Integer cash(int userId,double money,String openid);
	//保存userId,formid
	public void saveFormid(Map<String,Object>param);
	//发送模板消息
	public String sendMsg(String Json);
	//<!-- 保存用户id和jobid（红包id） -->，清除福利界面得红包缓存
	public void saveJobTake(Map<String,Object>param);
	//userService.getCode(json)获得二维码主要api（二维码存到了阿里云，用的阿里oss服务）
	public String getCode(String Json);
	public void saveCommand(String command,int userId);
	

}
