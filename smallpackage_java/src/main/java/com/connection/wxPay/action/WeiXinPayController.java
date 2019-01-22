package com.connection.wxPay.action;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.alibaba.fastjson.JSONObject;
import com.connection.dao.AdminDao;
import com.connection.dao.BegJobDao;
import com.connection.dao.JobDao;
import com.connection.service.interfaces.BegJobService;
import com.connection.service.interfaces.JobService;
import com.connection.service.interfaces.UserService;
import com.connection.wxPay.entity.Unifiedorder;
import com.connection.wxPay.util.HttpXmlUtils;
import com.connection.wxPay.util.MyWXPayUtil;
import com.connection.wxPay.util.ParseXMLUtils;
import com.connection.wxPay.util.RandCharsUtils;
import com.connection.wxPay.util.WXSignUtils;
import com.connection.wxPay.util.WeixinConfigUtils;

/**
 * ΢��֧������
 * 
 * @author
 * @date
 */
@Controller
public class WeiXinPayController {
	public static Logger log = Logger.getLogger(WeiXinPayController.class);
	@Autowired
	private JobService jobService;
	@Autowired
	private BegJobService begJobService;
	@Autowired
	private JobDao jobDao;
	@Autowired
	private BegJobDao begJobDao;
	@Autowired
	private AdminDao adminDao;


	/**
	 * ΢��֧������Ԥ���� ��ͳһ�µ���
	 */
	/*
	 * @SuppressWarnings("static-access") public static void main(String[] args)
	 * { goodId goodCount userId points payMoney receipName phoneNumber address
	 */
	@RequestMapping("/createJob")
	public void weixinGenerateOrder(HttpServletRequest request, HttpServletResponse response,
			@RequestParam Map<String, String> getParams) throws IOException {
		PrintWriter out = null;
		out = response.getWriter();
		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		JSONObject json = new JSONObject();
		try {
			//�õ������������ܽ���������ѣ�������ܽ�������
			double totalAward = Double.parseDouble(getParams.get("totalAward"));
			double award = Double.parseDouble(getParams.get("award"));
			int count = Integer.parseInt(getParams.get("totalCount"));
			boolean flag = true;
			if (totalAward < award || award < 0 || totalAward < 0) {
				flag = false;
			}
			if (flag) {
				if ("1".equals(getParams.get("job_type"))) {// ������ͨ���
					double one_award = Double.parseDouble(getParams.get("one_award"));
					if (one_award * count != award) {
						flag = false;//����
					}
				}
				int jobId = jobService.addJob(getParams);//��job��Ϣ��ӵ����ݿ⣬���ص�jobid
				WeixinConfigUtils config = new WeixinConfigUtils();
				// ������ ��Ҫ�ͻ��˴������������У���Ʒ��Ϣ ��Ʒ���� ��Ʒ��� ��ֵ���� ��ֵ�˺�
				String appid = config.appid;// Ӧ��ID
				String mch_id = config.mch_id;// �̻���
				String nonce_str = RandCharsUtils.getRandomString(32);// ����ַ�����������32λ���Ƽ���������ɷ�
				String body = "��������";// ��Ʒ����,��Ʒ���������ֶθ�ʽ���ݲ�ͬ��Ӧ�ó����������¸�ʽ��APP�����贫��Ӧ���г��ϵ�APP����-ʵ����Ʒ���ƣ����찮����-��Ϸ��ֵ��
				String detail = "��������";// ��Ʒ����,��Ʒ��ϸ����������ʹ�õ�Ʒ�Żݵ��̻������ֶα��밴�չ淶�ϴ����������Ʒ�Żݲ���˵����
				String attach = jobId + "";// �������ݣ��ڲ�ѯAPI��֧��֪ͨ��ԭ�����أ����ֶ���Ҫ�����̻�Я���������Զ�������
				String out_trade_no = RandCharsUtils.getRandomStringOrderNum();// �̻������ţ��̻�ϵͳ�ڲ������ţ�Ҫ��32���ַ��ڣ�ֻ�������֡���Сд��ĸ_-|*@
				// ������ͬһ���̻�����Ψһ������̻�������
				// int total_fee =1;// ��λ�Ƿ֣�����0.01Ԫ
				int total_fee = (int) Math.round(Double.parseDouble(getParams.get("totalAward")) * 100); // �ܽ���λ�Ƿ�
				// int total_fee = 1; // �����ܽ���λΪ�֣����֧�����
				String spbill_create_ip = "127.0.0.1";// �ն�ip
														// �û���ʵ��ip
				String time_start = RandCharsUtils.timeStart();// ������ʼʱ��
				String time_expire = RandCharsUtils.timeExpire();// ���׽���ʱ��
				String notify_url = config.notify_url;// ֪ͨ��ַ
														// ����΢��֧���첽֪ͨ�ص���ַ��֪ͨurl����Ϊֱ�ӿɷ��ʵ�url������Я������
				String trade_type = "JSAPI";// �������� ֧������(С����)

				// ��������ʼ����ǩ��
				SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
				parameters.put("appid", appid);
				parameters.put("mch_id", mch_id);
				parameters.put("nonce_str", nonce_str);
				parameters.put("body", body);
				parameters.put("detail", detail);
				parameters.put("attach", attach);
				parameters.put("out_trade_no", out_trade_no);
				parameters.put("total_fee", total_fee);
				parameters.put("time_start", time_start);
				parameters.put("time_expire", time_expire);
				parameters.put("notify_url", notify_url);
				parameters.put("trade_type", trade_type);
				parameters.put("spbill_create_ip", spbill_create_ip);
				parameters.put("openid", getParams.get("openid"));

				// �õ����ɵ�ǩ��
				String sign = WXSignUtils.createSign("UTF-8", parameters);
				// ����ǩ������

				// ��ʼ����Ԥ����
				Unifiedorder unifiedorder = new Unifiedorder();
				unifiedorder.setAppid(appid);
				unifiedorder.setMch_id(mch_id);
				unifiedorder.setNonce_str(nonce_str);
				unifiedorder.setSign(sign);
				unifiedorder.setBody(body);
				unifiedorder.setDetail(detail);
				unifiedorder.setAttach(attach);
				unifiedorder.setOut_trade_no(out_trade_no);
				unifiedorder.setTotal_fee(total_fee);
				unifiedorder.setSpbill_create_ip(spbill_create_ip);
				unifiedorder.setTime_start(time_start);
				unifiedorder.setTime_expire(time_expire);
				unifiedorder.setNotify_url(notify_url);
				unifiedorder.setTrade_type(trade_type);
				unifiedorder.setOpenid(getParams.get("openid"));

				// ����xml����
				String xmlInfo = HttpXmlUtils.xmlInfo(unifiedorder);
				String wxUrl = "https://api.mch.weixin.qq.com/pay/unifiedorder";

				String method = "POST";

				String weixinPost = HttpXmlUtils.httpsRequest(wxUrl, method, xmlInfo).toString();

				// ����Ԥ�������� return_code Ϊsuccessʱ����
				// appID,mch_id,device_info,nonce_str,sign,result_code(ҵ����),err_code(�������),err_code_des(�����������)
				// return_code,result_code��Ϊsuccessʱ
				// ����trade_type(��������),prepay_id(Ԥ֧�����׻Ự��ʶ),code_url(��ά������)
				// ����xml����ý����󷵻ص�ֵ

				Map mapreturn = ParseXMLUtils.jdomParseXml(weixinPost);
				String return_code = null;
				String return_msg;
				String result_code;
				String prepay_id = null;
				/*
				 * for (Object key : map.keySet()) {
				 * System.out.println(key+"/r/n"+map.get(key));
				 */
				return_code = mapreturn.get("return_code").toString();
				return_msg = mapreturn.get("return_msg").toString();
				/* } */
				if (!return_code.equals("FAIL")) {
					result_code = mapreturn.get("result_code").toString();
					if (!result_code.equals("FAIL")) {
						prepay_id = mapreturn.get("prepay_id").toString();

						// ����ǩ��
						SortedMap<Object, Object> finalpackage = new TreeMap<Object, Object>();
						// String timestamp = RandCharsUtils.timeStart();
						long timestamp = System.currentTimeMillis() / 1000;

						finalpackage.put("appId", appid);
						// finalpackage.put("partnerid", mch_id);
						// finalpackage.put("prepayid", prepay_id);
						finalpackage.put("nonceStr", nonce_str);
						finalpackage.put("timeStamp", timestamp);
						finalpackage.put("package", "prepay_id=" + prepay_id);
						finalpackage.put("signType", "MD5");
						String signValue = WXSignUtils.createSign("UTF-8", finalpackage);
						//����ǩ������
						
						
						json.put("appid", appid);
						json.put("partnerid", mch_id);
						json.put("package", "prepay_id=" + prepay_id);
						json.put("noncestr", nonce_str);
						json.put("timestamp", timestamp + "");
						json.put("sign", signValue);
						json.put("prepayid", prepay_id);
						json.put("jobId", jobId);
						//����ǰ����Ϣ������ҪԤ�������ˣ��ÿͻ�ȥ����
						out.print(json.toString());

					} else {
						out.print("123456");
					}

				} else {

					out.print("qianmingcuowu");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

		}

	}
	@RequestMapping("/createBegJob")
	public void weixinGenerateBegOrder(HttpServletRequest request, HttpServletResponse response,
			@RequestParam Map<String, String> getParams) throws IOException {
		PrintWriter out = null;
		out = response.getWriter();
		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		JSONObject json = new JSONObject();
		try {
			//�õ������������ܽ���������ѣ�������ܽ�������
			double totalAward = Double.parseDouble(getParams.get("award"))*0.02+Double.parseDouble(getParams.get("award"));
			getParams.put("totalAward",  totalAward+"");
			double award = Double.parseDouble(getParams.get("award"));
		
			boolean flag = true;
			if (totalAward < award || award < 0 || totalAward < 0) {
				flag = false;
			}
			if (flag) {
				
				int jobId = begJobService.addJob(getParams);//��job��Ϣ��ӵ����ݿ⣬���ص�jobid
				
						json.put("jobId", jobId);
						
						out.print(json.toString());

					
				} else {

					out.print("chuangjiancuowu");
				}
			

		} catch (Exception e) {
			e.printStackTrace();

		}

	}
	//����Ԥ����
	@RequestMapping("/payBegJob")
	public void weixinGeneratepayBegOrder(HttpServletRequest request, HttpServletResponse response,
			@RequestParam Map<String, String> getParams ) throws IOException {
		PrintWriter out = null;
		out = response.getWriter();
		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		JSONObject json = new JSONObject();
		try {
			//�õ������������ܽ���������ѣ�������ܽ�������
			
			int jobId=Integer.parseInt(getParams.get("jobId"));
			int userId=Integer.parseInt(getParams.get("userId"));
			HashMap<String,Object> cc =begJobDao.getPaied(userId, jobId);
			if(cc!=null) {
				out.print("niwanguole");
				return;
			}
			HashMap<String,Object> jobMsg =begJobDao.getBegJobById(jobId);
			
			double totalAward = Double.parseDouble(jobMsg.get("totalAward")+"");
			double award = Double.parseDouble(jobMsg.get("award")+"");
			boolean flag = true;
			if (totalAward < award || award < 0 || totalAward < 0) {
				flag = false;
			}
			String openId =adminDao.getOpenIdByUserId(userId);
			System.out.println(openId);
			System.out.println(getParams.get("openid")+"");
			System.out.println(!openId.equals(getParams.get("openid")+""));
			if(openId==null||!openId.equals(getParams.get("openid")+"")) {
				out.print("nishengfenbudui");
				return;
			}
			if (flag) {
				
				WeixinConfigUtils config = new WeixinConfigUtils();
				// ������ ��Ҫ�ͻ��˴������������У���Ʒ��Ϣ ��Ʒ���� ��Ʒ��� ��ֵ���� ��ֵ�˺�
				String appid = config.appid;// Ӧ��ID
				String mch_id = config.mch_id;// �̻���
				String nonce_str = RandCharsUtils.getRandomString(32);// ����ַ�����������32λ���Ƽ���������ɷ�
				String body = "��������";// ��Ʒ����,��Ʒ���������ֶθ�ʽ���ݲ�ͬ��Ӧ�ó����������¸�ʽ��APP�����贫��Ӧ���г��ϵ�APP����-ʵ����Ʒ���ƣ����찮����-��Ϸ��ֵ��
				String detail = "��������";// ��Ʒ����,��Ʒ��ϸ����������ʹ�õ�Ʒ�Żݵ��̻������ֶα��밴�չ淶�ϴ����������Ʒ�Żݲ���˵����
				String attach = jobId + "";// �������ݣ��ڲ�ѯAPI��֧��֪ͨ��ԭ�����أ����ֶ���Ҫ�����̻�Я���������Զ�������
				String out_trade_no = RandCharsUtils.getRandomStringOrderNum();// �̻������ţ��̻�ϵͳ�ڲ������ţ�Ҫ��32���ַ��ڣ�ֻ�������֡���Сд��ĸ_-|*@
				// ������ͬһ���̻�����Ψһ������̻�������
				// int total_fee =1;// ��λ�Ƿ֣�����0.01Ԫ
				int total_fee = (int) Math.round(totalAward* 100); // �ܽ���λ�Ƿ�
				// int total_fee = 1; // �����ܽ���λΪ�֣����֧�����
				String spbill_create_ip = "127.0.0.1";// �ն�ip
														// �û���ʵ��ip
				String time_start = RandCharsUtils.timeStart();// ������ʼʱ��
				String time_expire = RandCharsUtils.timeExpire();// ���׽���ʱ��
				String notify_url = config.beg_notify_url;// ֪ͨ��ַ
														// ����΢��֧���첽֪ͨ�ص���ַ��֪ͨurl����Ϊֱ�ӿɷ��ʵ�url������Я������
				String trade_type = "JSAPI";// �������� ֧������(С����)

				// ��������ʼ����ǩ��
				SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
				parameters.put("appid", appid);
				parameters.put("mch_id", mch_id);
				parameters.put("nonce_str", nonce_str);
				parameters.put("body", body);
				parameters.put("detail", detail);
				parameters.put("attach", attach);
				parameters.put("out_trade_no", out_trade_no);
				parameters.put("total_fee", total_fee);
				parameters.put("time_start", time_start);
				parameters.put("time_expire", time_expire);
				parameters.put("notify_url", notify_url);
				parameters.put("trade_type", trade_type);
				parameters.put("spbill_create_ip", spbill_create_ip);
				parameters.put("openid", getParams.get("openid"));

				// �õ����ɵ�ǩ��
				String sign = WXSignUtils.createSign("UTF-8", parameters);
				// ����ǩ������

				// ��ʼ����Ԥ����
				Unifiedorder unifiedorder = new Unifiedorder();
				unifiedorder.setAppid(appid);
				unifiedorder.setMch_id(mch_id);
				unifiedorder.setNonce_str(nonce_str);
				unifiedorder.setSign(sign);
				unifiedorder.setBody(body);
				unifiedorder.setDetail(detail);
				unifiedorder.setAttach(attach);
				unifiedorder.setOut_trade_no(out_trade_no);
				unifiedorder.setTotal_fee(total_fee);
				unifiedorder.setSpbill_create_ip(spbill_create_ip);
				unifiedorder.setTime_start(time_start);
				unifiedorder.setTime_expire(time_expire);
				unifiedorder.setNotify_url(notify_url);
				unifiedorder.setTrade_type(trade_type);
				unifiedorder.setOpenid(getParams.get("openid"));

				// ����xml����
				String xmlInfo = HttpXmlUtils.xmlInfo(unifiedorder);
				String wxUrl = "https://api.mch.weixin.qq.com/pay/unifiedorder";

				String method = "POST";

				String weixinPost = HttpXmlUtils.httpsRequest(wxUrl, method, xmlInfo).toString();

				// ����Ԥ�������� return_code Ϊsuccessʱ����
				// appID,mch_id,device_info,nonce_str,sign,result_code(ҵ����),err_code(�������),err_code_des(�����������)
				// return_code,result_code��Ϊsuccessʱ
				// ����trade_type(��������),prepay_id(Ԥ֧�����׻Ự��ʶ),code_url(��ά������)
				// ����xml����ý����󷵻ص�ֵ

				Map mapreturn = ParseXMLUtils.jdomParseXml(weixinPost);
				String return_code = null;
				String return_msg;
				String result_code;
				String prepay_id = null;
				/*
				 * for (Object key : map.keySet()) {
				 * System.out.println(key+"/r/n"+map.get(key));
				 */
				return_code = mapreturn.get("return_code").toString();
				return_msg = mapreturn.get("return_msg").toString();
				/* } */
				if (!return_code.equals("FAIL")) {
					result_code = mapreturn.get("result_code").toString();
					if (!result_code.equals("FAIL")) {
						prepay_id = mapreturn.get("prepay_id").toString();

						// ����ǩ��
						SortedMap<Object, Object> finalpackage = new TreeMap<Object, Object>();
						// String timestamp = RandCharsUtils.timeStart();
						long timestamp = System.currentTimeMillis() / 1000;

						finalpackage.put("appId", appid);
						// finalpackage.put("partnerid", mch_id);
						// finalpackage.put("prepayid", prepay_id);
						finalpackage.put("nonceStr", nonce_str);
						finalpackage.put("timeStamp", timestamp);
						finalpackage.put("package", "prepay_id=" + prepay_id);
						finalpackage.put("signType", "MD5");
						String signValue = WXSignUtils.createSign("UTF-8", finalpackage);
						//����ǩ������
						
						
						json.put("appid", appid);
						json.put("partnerid", mch_id);
						json.put("package", "prepay_id=" + prepay_id);
						json.put("noncestr", nonce_str);
						json.put("timestamp", timestamp + "");
						json.put("sign", signValue);
						json.put("prepayid", prepay_id);
						json.put("jobId", jobId);
						//����ǰ����Ϣ������ҪԤ�������ˣ��ÿͻ�ȥ����
						out.print(json.toString());

					} else {
						out.print("123456");
					}

				} else {

					out.print("qianmingcuowu");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

		}

	}

	/**
	 * ΢��֧�� ��֪ͨ��ַ���ص���
	 */
	@RequestMapping("/weixinConfirmPay")
	public void weixinConfirmPay(HttpServletRequest request, HttpServletResponse response) throws IOException {

		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		InputStream in = request.getInputStream();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = in.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}
		out.close();
		in.close();
		try {
			String msgxml = new String(out.toByteArray(), "utf-8");// xml����
			log.info("���ص�xml����Ϊ=" + msgxml);
			Map map = ParseXMLUtils.jdomParseXml(msgxml);// ����xml����
			log.info("�������ص�xml���ݵõ�=" + map);
			String return_code = map.get("return_code").toString();
			String result_code = (String) map.get("result_code");
			String appid = map.get("appid").toString();
			String mch_id = map.get("mch_id").toString();
			String nonce_str = map.get("nonce_str").toString();
			String sign = (String) map.get("sign");
			String openid = map.get("openid").toString();
			String trade_type = map.get("trade_type").toString();
			String bank_type = map.get("bank_type").toString();
			String total_fee = (String) map.get("total_fee");
			String fee_type = map.get("fee_type").toString();
			String cash_fee = map.get("cash_fee").toString();
			String transaction_id = map.get("transaction_id").toString();
			String out_trade_no = (String) map.get("out_trade_no");
			Double amount = Double.parseDouble(total_fee) / 100;// ��ȡ�������
			String attach = (String) map.get("attach");

			String is_subscribe = map.get("is_subscribe").toString();
			String sn = out_trade_no.split("\\|")[0];// ��ȡ�������
			String time_end = map.get("time_end").toString();

			// ��֤ǩ��
			if (result_code.equals("SUCCESS")) {
				SortedMap<Object, Object> packageParams = new TreeMap<Object, Object>();
				packageParams.put("appid", appid);
				packageParams.put("attach", attach);
				packageParams.put("bank_type", bank_type);
				packageParams.put("cash_fee", cash_fee);
				packageParams.put("fee_type", fee_type);
				packageParams.put("is_subscribe", is_subscribe);
				packageParams.put("mch_id", mch_id);
				packageParams.put("nonce_str", nonce_str);
				packageParams.put("openid", openid);
				packageParams.put("out_trade_no", out_trade_no);
				packageParams.put("result_code", result_code);
				packageParams.put("return_code", return_code);
				packageParams.put("time_end", time_end);
				packageParams.put("total_fee", total_fee);
				packageParams.put("trade_type", trade_type);
				packageParams.put("transaction_id", transaction_id);
				String endsign = WXSignUtils.createSign("UTF-8", packageParams);
				if (endsign.equals(sign)) {
					Map<String, Object> job = jobDao.getJobById1(Integer.parseInt(attach));
					if ((double) job.get("totalAward") > (double) job.get("award")) {// ����һ������
						jobService.payOver(transaction_id, out_trade_no, Integer.parseInt(attach));
						response.getWriter().write(setXml("SUCCESS", "OK")); // ����΢���Ѿ��յ�֪ͨ��
					}
					job = null;
				}
				
			} else {// ��֤ʧ��
				response.getWriter().println("fail");
				log.info("ʧ��");
			}
		} catch (Exception e) {
			response.getWriter().print("fail");
			log.info("ϵͳ����");
			log.info(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * beg΢��֧�� ��֪ͨ��ַ���ص���
	 */
	@RequestMapping("/weixinConfirmPayBeg")
	public void weixinConfirmPayBeg(HttpServletRequest request, HttpServletResponse response) throws IOException {

		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		InputStream in = request.getInputStream();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = in.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}
		out.close();
		in.close();
		try {
			String msgxml = new String(out.toByteArray(), "utf-8");// xml����
			log.info("���ص�xml����Ϊ=" + msgxml);
			Map map = ParseXMLUtils.jdomParseXml(msgxml);// ����xml����
			log.info("�������ص�xml���ݵõ�=" + map);
			String return_code = map.get("return_code").toString();
			String result_code = (String) map.get("result_code");
			String appid = map.get("appid").toString();
			String mch_id = map.get("mch_id").toString();
			String nonce_str = map.get("nonce_str").toString();
			String sign = (String) map.get("sign");
			String openid = map.get("openid").toString();
			String trade_type = map.get("trade_type").toString();
			String bank_type = map.get("bank_type").toString();
			String total_fee = (String) map.get("total_fee");
			String fee_type = map.get("fee_type").toString();
			String cash_fee = map.get("cash_fee").toString();
			String transaction_id = map.get("transaction_id").toString();
			String out_trade_no = (String) map.get("out_trade_no");
			Double amount = Double.parseDouble(total_fee) / 100;// ��ȡ�������
			String attach = (String) map.get("attach");

			String is_subscribe = map.get("is_subscribe").toString();
			String sn = out_trade_no.split("\\|")[0];// ��ȡ�������
			String time_end = map.get("time_end").toString();

			// ��֤ǩ��
			if (result_code.equals("SUCCESS")) {
				SortedMap<Object, Object> packageParams = new TreeMap<Object, Object>();
				packageParams.put("appid", appid);
				packageParams.put("attach", attach);
				packageParams.put("bank_type", bank_type);
				packageParams.put("cash_fee", cash_fee);
				packageParams.put("fee_type", fee_type);
				packageParams.put("is_subscribe", is_subscribe);
				packageParams.put("mch_id", mch_id);
				packageParams.put("nonce_str", nonce_str);
				packageParams.put("openid", openid);
				packageParams.put("out_trade_no", out_trade_no);
				packageParams.put("result_code", result_code);
				packageParams.put("return_code", return_code);
				packageParams.put("time_end", time_end);
				packageParams.put("total_fee", total_fee);
				packageParams.put("trade_type", trade_type);
				packageParams.put("transaction_id", transaction_id);
				String endsign = WXSignUtils.createSign("UTF-8", packageParams);
				if (endsign.equals(sign)) {
					Map<String, Object> job =begJobDao.getJobById1(Integer.parseInt(attach));
					if ((double) job.get("totalAward") > (double) job.get("award")) {// ����һ������
						int userId =begJobService.getUserId(openid);
						begJobService.payOver(transaction_id, out_trade_no, Integer.parseInt(attach),userId,(double) job.get("award"));
						response.getWriter().write(setXml("SUCCESS", "OK")); // ����΢���Ѿ��յ�֪ͨ��
					}
					job = null;
				}
				
			} else {// ��֤ʧ��
				response.getWriter().println("fail");
				log.info("ʧ��");
			}
		} catch (Exception e) {
			response.getWriter().print("fail");
			log.info("ϵͳ����");
			log.info(e);
			throw new RuntimeException(e);
		}
	}
	/**
	 * ΢���˿� url�ص�
	 * 
	 * @throws IOException
	 */
	@RequestMapping("/weixinRefund")
	public void weixinRefund(HttpServletRequest request, HttpServletResponse response) throws IOException {
		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		InputStream in = request.getInputStream();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = in.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}
		out.close();
		in.close();
		try {
			String msgxml = new String(out.toByteArray(), "utf-8");// xml����
			Map map = ParseXMLUtils.jdomParseXml(msgxml);// ����xml����
			log.info("�˿�ص��������ص�xml���ݵõ�=" + map);
			String return_code = map.get("return_code").toString();
			if ("SUCCESS".equals(return_code)) {// �ɹ��ص���
				String req_info = map.get("req_info").toString();
				String key = "Xingbang80301baodating360shengli";
				String result = MyWXPayUtil.getRefundDecrypt(req_info, key);
				Map resultEntity = ParseXMLUtils.jdomParseXml(result);// ������������
				if ("SUCCESS".equals(resultEntity.get("refund_status").toString())) {
					String out_trade_no = resultEntity.get("out_trade_no").toString();
					String transaction_id = resultEntity.get("transaction_id").toString();
					String out_refund_no = resultEntity.get("out_refund_no").toString();
					response.getWriter().write(setXml("SUCCESS", "OK")); // ����΢���Ѿ��յ�֪ͨ��
					// userService.saveRefundNO(out_refund_no, out_trade_no,
					// transaction_id);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}

	private String setXml(String return_code, String return_msg) {
		// TODO Auto-generated method stub
		return "<xml><return_code><![CDATA[" + return_code + "]]></return_code><return_msg><![CDATA[" + return_msg
				+ "]]></return_msg></xml>";
	}

}
