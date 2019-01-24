package com.connection.service.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface BegJobService {
	//����job����
	public int addJob(Map<String,String>param);
	public void payOver(String transaction_id,String out_trade_no,int jobId,int  userId,double award);
	//����Vedio
	
	//����֤��
	public String saveBook(InputStream input,int id);
	
	public String saveBegSharePic(InputStream input,int id);
	
	public List<Integer> getUserId(String openid);
	//�û��Զ����ϴ�
	public String saveCommandImage(InputStream input,int id);
	public String saveVedioCommand(InputStream input,int id) throws IOException ;
	public String saveVoice(InputStream input,int userId,int voiceTime) throws IOException;
	public String saveBegCommand(int userId,String context) throws IOException;
	//sys�ϴ�
	public String saveSysCommandImage(InputStream input,int id);
	public String saveSysVedioCommand(InputStream input,int id) throws IOException ;
	public String saveSysVoice(InputStream input,int id,String context,int voiceTime) throws IOException ;
	public String saveSysCommand(int contextId,int  levelContextId,String context);
	public String saveSysBegCommand(int fatherId,String context);
	
}
