package com.connection.wxPay.action;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.connection.controller.JobController;
import com.connection.dao.AdminDao;
import com.connection.dao.DataDao;
import com.connection.wxPay.util.HttpXmlUtils;
import com.connection.wxPay.util.ParseXMLUtils;
import com.connection.wxPay.util.RandCharsUtils;
import com.connection.wxPay.util.WXSignUtils;
import com.connection.wxPay.util.WeixinConfigUtils;

/**
 * ��ҵ����
 * @author admin
 *
 */
@Service
public class CompanyPay {
	public static Logger log = Logger.getLogger(CompanyPay.class);
	@Autowired
	private AdminDao adminDao;
	@Autowired
	private DataDao dataDao;
	public int pay(int userId,String openid,double money){
		int state = 0;
		try {
			WeixinConfigUtils config = new WeixinConfigUtils();
			// ������ ��Ҫ�ͻ��˴������������У���Ʒ��Ϣ ��Ʒ���� ��Ʒ��� ��ֵ���� ��ֵ�˺�
			String appid = config.appid;// Ӧ��ID
			String mch_id = config.mch_id;// �̻���
			String book_url = config.book_url;//֤��·��
			String nonce_str = RandCharsUtils.getRandomString(32);// ����ַ�����������32λ���Ƽ���������ɷ�
			String partner_trade_no = RandCharsUtils.getRandomStringOrderNum();//�̻�������
			String check_name = "NO_CHECK";//�����ʵ������������д�벻�˲飬NO_CHECK����У����ʵ���� ��FORCE_CHECK��ǿУ����ʵ����
			int amount = (int)(Double.parseDouble(money*100+""));//��λΪ�� ��ȡ2%��������
			String desc = "����˵����";//����
			String spbill_create_ip = "192.168.0.1";//�û�ip
			/**
			 * ��װ����������ǩ��
			 */
			SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
			parameters.put("mch_appid", appid);
			parameters.put("mchid", mch_id);
			parameters.put("nonce_str", nonce_str);
			parameters.put("partner_trade_no", partner_trade_no);;//�̻�������
			parameters.put("openid", openid);//�û���openId
			parameters.put("check_name",check_name);
			parameters.put("amount", amount);
			parameters.put("desc", desc);//utf8�ַ���
			parameters.put("spbill_create_ip", spbill_create_ip);
			
			// �õ����ɵ�ǩ��
			String sign = WXSignUtils.createSign("UTF-8", parameters);
			// ����ǩ������,�ŵ�map��
			parameters.put("sign", sign);
			// ����xml����
			String xmlInfo = HttpXmlUtils.createPayXml(parameters);
			//΢�������ַ
			String wxUrl = "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers";
					//����ȥ���󣬲������ַ���
			String weixinPost = HttpXmlUtils.refundHand(wxUrl, xmlInfo,book_url,mch_id).toString();
			Map mapreturn = ParseXMLUtils.jdomParseXml(weixinPost);
			//���ز���return_codeSUCCESS/FAIL���ֶ���ͨ�ű�ʶ���ǽ��ױ�ʶ�������Ƿ�ɹ���Ҫ�鿴result_code���ж�
			//return_msg����û��ȡ��������Ϣ����ǿգ�Ϊ����ԭ�� ��ǩ��ʧ�� ��������ʽУ�����
			String return_code = mapreturn.get("return_code").toString();
			//SUCCESS/FAIL��ע�⣺��״̬ΪFAILʱ������ҵ����δ��ȷ�������������״̬ΪFAIL������ع�ע������루err_code�ֶΣ���ͨ����ѯ��ѯ�ӿ�ȷ�ϴ˴θ���Ľ����
			String result_code = mapreturn.get("result_code").toString();
			if("SUCCESS".equals(return_code)&&"SUCCESS".equals(result_code)){
				//�˿�����ɹ� �������
//				jobDao.updateState(4,jobId);//�޸�����״̬Ϊ�˿���
				
				//���������ֶζ�����return_code ��result_code��ΪSUCCESS��ʱ���з��� 
				partner_trade_no = mapreturn.get("partner_trade_no").toString();//�̻�������
				String payment_no = mapreturn.get("payment_no").toString();//΢�Ÿ����
				String payment_time = mapreturn.get("payment_time").toString();//����ɹ�ʱ��
//				�������ֲ���
				adminDao.modifyMoney1(0.00-money, userId);
				dataDao.saveCash(userId, money,partner_trade_no,payment_no,payment_time);
				
			}else{
				state = 3;
			}
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException();
		}
		return state;
	}
}
