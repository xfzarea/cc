package com.connection.service.interfaces;

import java.io.IOException;
import java.util.Map;

import com.connection.entity.Admin;

public interface UserService {
	public String sendData(String code,String appId,String secret) throws IOException;
	public Admin saveAdmin(Admin admin);
	public Integer cash(int userId,double money,String openid);
	public void saveFormid(Map<String,Object>param);
	public String sendMsg(String Json);
	public void saveJobTake(Map<String,Object>param);
	public String getCode(String Json);
	public void saveCommand(String command,int userId);
}
