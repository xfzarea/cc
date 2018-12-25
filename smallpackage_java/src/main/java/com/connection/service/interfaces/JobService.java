package com.connection.service.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public interface JobService {
	public int addJob(Map<String,String>param);
	public void payOver(String transaction_id,String out_trade_no,int id);
	public String saveVedio(InputStream input,int id,int userId,int second) throws IOException;
	public String saveBook(InputStream input,int id);
	public String saveSharePic(InputStream input,int id);
	
}
