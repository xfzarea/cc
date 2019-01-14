package com.connection.service.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public interface BegJobService {
	//增加job数据
	public int addJob(Map<String,String>param);
	public void payOver(String transaction_id,String out_trade_no,int id);
	//保存Vedio
	public String saveVedio(InputStream input,int id,int userId,int second) throws IOException;
	//保存证书
	public String saveBook(InputStream input,int id);
	
	public String saveSharePic(InputStream input,int id);
	
}
