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
 * 微信支付测试
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
	 * 微信支付生成预付单 （统一下单）
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
			//得到三个参数，总金额（包含服务费），红包总金额，总条数
			double totalAward = Double.parseDouble(getParams.get("totalAward"));
			double award = Double.parseDouble(getParams.get("award"));
			int count = Integer.parseInt(getParams.get("totalCount"));
			boolean flag = true;
			if (totalAward < award || award < 0 || totalAward < 0) {
				flag = false;
			}
			if (flag) {
				if ("1".equals(getParams.get("job_type"))) {// 代表普通红包
					double one_award = Double.parseDouble(getParams.get("one_award"));
					if (one_award * count != award) {
						flag = false;//？？
					}
				}
				int jobId = jobService.addJob(getParams);//把job信息添加到数据库，返回的jobid
				WeixinConfigUtils config = new WeixinConfigUtils();
				// 参数组 需要客户端传过来的数据有：商品信息 商品描述 商品金额 充值类型 充值账号
				String appid = config.appid;// 应用ID
				String mch_id = config.mch_id;// 商户号
				String nonce_str = RandCharsUtils.getRandomString(32);// 随机字符串，不长于32位，推荐随机数生成法
				String body = "发布任务";// 商品描述,商品描述交易字段格式根据不同的应用场景按照以下格式：APP——需传入应用市场上的APP名字-实际商品名称，天天爱消除-游戏充值。
				String detail = "发布任务";// 商品详情,商品详细描述，对于使用单品优惠的商户，改字段必须按照规范上传，详见“单品优惠参数说明”
				String attach = jobId + "";// 附加数据，在查询API和支付通知中原样返回，该字段主要用于商户携带订单的自定义数据
				String out_trade_no = RandCharsUtils.getRandomStringOrderNum();// 商户订单号，商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|*@
				// ，且在同一个商户号下唯一。详见商户订单号
				// int total_fee =1;// 单位是分，即是0.01元
				int total_fee = (int) Math.round(Double.parseDouble(getParams.get("totalAward")) * 100); // 总金额，单位是分
				// int total_fee = 1; // 订单总金额，单位为分，详见支付金额
				String spbill_create_ip = "127.0.0.1";// 终端ip
														// 用户端实际ip
				String time_start = RandCharsUtils.timeStart();// 交易起始时间
				String time_expire = RandCharsUtils.timeExpire();// 交易结束时间
				String notify_url = config.notify_url;// 通知地址
														// 接收微信支付异步通知回调地址，通知url必须为直接可访问的url，不能携带参数
				String trade_type = "JSAPI";// 交易类型 支付类型(小程序)

				// 参数：开始生成签名
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

				// 得到生成的签名
				String sign = WXSignUtils.createSign("UTF-8", parameters);
				// 生成签名结束

				// 开始生成预付单
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

				// 构造xml参数
				String xmlInfo = HttpXmlUtils.xmlInfo(unifiedorder);
				String wxUrl = "https://api.mch.weixin.qq.com/pay/unifiedorder";

				String method = "POST";

				String weixinPost = HttpXmlUtils.httpsRequest(wxUrl, method, xmlInfo).toString();

				// 生成预付单结束 return_code 为success时返回
				// appID,mch_id,device_info,nonce_str,sign,result_code(业务结果),err_code(错误代码),err_code_des(错误代码描述)
				// return_code,result_code都为success时
				// 返回trade_type(交易类型),prepay_id(预支付交易会话标识),code_url(二维码链接)
				// 解析xml并获得解析后返回的值

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

						// 二次签名
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
						//二次签名结束
						
						
						json.put("appid", appid);
						json.put("partnerid", mch_id);
						json.put("package", "prepay_id=" + prepay_id);
						json.put("noncestr", nonce_str);
						json.put("timestamp", timestamp + "");
						json.put("sign", signValue);
						json.put("prepayid", prepay_id);
						json.put("jobId", jobId);
						//返给前端信息，最主要预付单有了，让客户去付款
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
			//得到三个参数，总金额（包含服务费），红包总金额，总条数
			double totalAward = Double.parseDouble(getParams.get("award"))*0.02+Double.parseDouble(getParams.get("award"));
			getParams.put("totalAward",  totalAward+"");
			double award = Double.parseDouble(getParams.get("award"));
		
			boolean flag = true;
			if (totalAward < award || award < 0 || totalAward < 0) {
				flag = false;
			}
			if (flag) {
				
				int jobId = begJobService.addJob(getParams);//把job信息添加到数据库，返回的jobid
				
						json.put("jobId", jobId);
						
						out.print(json.toString());

					
				} else {

					out.print("chuangjiancuowu");
				}
			

		} catch (Exception e) {
			e.printStackTrace();

		}

	}
	//返回预付单
	@RequestMapping("/payBegJob")
	public void weixinGeneratepayBegOrder(HttpServletRequest request, HttpServletResponse response,
			@RequestParam Map<String, String> getParams ) throws IOException {
		PrintWriter out = null;
		out = response.getWriter();
		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		JSONObject json = new JSONObject();
		try {
			//得到三个参数，总金额（包含服务费），红包总金额，总条数
			
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
				// 参数组 需要客户端传过来的数据有：商品信息 商品描述 商品金额 充值类型 充值账号
				String appid = config.appid;// 应用ID
				String mch_id = config.mch_id;// 商户号
				String nonce_str = RandCharsUtils.getRandomString(32);// 随机字符串，不长于32位，推荐随机数生成法
				String body = "发布任务";// 商品描述,商品描述交易字段格式根据不同的应用场景按照以下格式：APP——需传入应用市场上的APP名字-实际商品名称，天天爱消除-游戏充值。
				String detail = "发布任务";// 商品详情,商品详细描述，对于使用单品优惠的商户，改字段必须按照规范上传，详见“单品优惠参数说明”
				String attach = jobId + "";// 附加数据，在查询API和支付通知中原样返回，该字段主要用于商户携带订单的自定义数据
				String out_trade_no = RandCharsUtils.getRandomStringOrderNum();// 商户订单号，商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|*@
				// ，且在同一个商户号下唯一。详见商户订单号
				// int total_fee =1;// 单位是分，即是0.01元
				int total_fee = (int) Math.round(totalAward* 100); // 总金额，单位是分
				// int total_fee = 1; // 订单总金额，单位为分，详见支付金额
				String spbill_create_ip = "127.0.0.1";// 终端ip
														// 用户端实际ip
				String time_start = RandCharsUtils.timeStart();// 交易起始时间
				String time_expire = RandCharsUtils.timeExpire();// 交易结束时间
				String notify_url = config.beg_notify_url;// 通知地址
														// 接收微信支付异步通知回调地址，通知url必须为直接可访问的url，不能携带参数
				String trade_type = "JSAPI";// 交易类型 支付类型(小程序)

				// 参数：开始生成签名
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

				// 得到生成的签名
				String sign = WXSignUtils.createSign("UTF-8", parameters);
				// 生成签名结束

				// 开始生成预付单
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

				// 构造xml参数
				String xmlInfo = HttpXmlUtils.xmlInfo(unifiedorder);
				String wxUrl = "https://api.mch.weixin.qq.com/pay/unifiedorder";

				String method = "POST";

				String weixinPost = HttpXmlUtils.httpsRequest(wxUrl, method, xmlInfo).toString();

				// 生成预付单结束 return_code 为success时返回
				// appID,mch_id,device_info,nonce_str,sign,result_code(业务结果),err_code(错误代码),err_code_des(错误代码描述)
				// return_code,result_code都为success时
				// 返回trade_type(交易类型),prepay_id(预支付交易会话标识),code_url(二维码链接)
				// 解析xml并获得解析后返回的值

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

						// 二次签名
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
						//二次签名结束
						
						
						json.put("appid", appid);
						json.put("partnerid", mch_id);
						json.put("package", "prepay_id=" + prepay_id);
						json.put("noncestr", nonce_str);
						json.put("timestamp", timestamp + "");
						json.put("sign", signValue);
						json.put("prepayid", prepay_id);
						json.put("jobId", jobId);
						//返给前端信息，最主要预付单有了，让客户去付款
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
	 * 微信支付 （通知地址、回调）
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
			String msgxml = new String(out.toByteArray(), "utf-8");// xml数据
			log.info("返回的xml数据为=" + msgxml);
			Map map = ParseXMLUtils.jdomParseXml(msgxml);// 解析xml数据
			log.info("解析返回的xml数据得到=" + map);
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
			Double amount = Double.parseDouble(total_fee) / 100;// 获取订单金额
			String attach = (String) map.get("attach");

			String is_subscribe = map.get("is_subscribe").toString();
			String sn = out_trade_no.split("\\|")[0];// 获取订单编号
			String time_end = map.get("time_end").toString();

			// 验证签名
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
					if ((double) job.get("totalAward") > (double) job.get("award")) {// 在做一波处理
						jobService.payOver(transaction_id, out_trade_no, Integer.parseInt(attach));
						response.getWriter().write(setXml("SUCCESS", "OK")); // 告诉微信已经收到通知了
					}
					job = null;
				}
				
			} else {// 验证失败
				response.getWriter().println("fail");
				log.info("失敗");
			}
		} catch (Exception e) {
			response.getWriter().print("fail");
			log.info("系统错误");
			log.info(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * beg微信支付 （通知地址、回调）
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
			String msgxml = new String(out.toByteArray(), "utf-8");// xml数据
			log.info("返回的xml数据为=" + msgxml);
			Map map = ParseXMLUtils.jdomParseXml(msgxml);// 解析xml数据
			log.info("解析返回的xml数据得到=" + map);
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
			Double amount = Double.parseDouble(total_fee) / 100;// 获取订单金额
			String attach = (String) map.get("attach");

			String is_subscribe = map.get("is_subscribe").toString();
			String sn = out_trade_no.split("\\|")[0];// 获取订单编号
			String time_end = map.get("time_end").toString();

			// 验证签名
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
					if ((double) job.get("totalAward") > (double) job.get("award")) {// 在做一波处理
						int userId =begJobService.getUserId(openid);
						begJobService.payOver(transaction_id, out_trade_no, Integer.parseInt(attach),userId,(double) job.get("award"));
						response.getWriter().write(setXml("SUCCESS", "OK")); // 告诉微信已经收到通知了
					}
					job = null;
				}
				
			} else {// 验证失败
				response.getWriter().println("fail");
				log.info("失敗");
			}
		} catch (Exception e) {
			response.getWriter().print("fail");
			log.info("系统错误");
			log.info(e);
			throw new RuntimeException(e);
		}
	}
	/**
	 * 微信退款 url回调
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
			String msgxml = new String(out.toByteArray(), "utf-8");// xml数据
			Map map = ParseXMLUtils.jdomParseXml(msgxml);// 解析xml数据
			log.info("退款回调解析返回的xml数据得到=" + map);
			String return_code = map.get("return_code").toString();
			if ("SUCCESS".equals(return_code)) {// 成功回调了
				String req_info = map.get("req_info").toString();
				String key = "Xingbang80301baodating360shengli";
				String result = MyWXPayUtil.getRefundDecrypt(req_info, key);
				Map resultEntity = ParseXMLUtils.jdomParseXml(result);// 解析加密数据
				if ("SUCCESS".equals(resultEntity.get("refund_status").toString())) {
					String out_trade_no = resultEntity.get("out_trade_no").toString();
					String transaction_id = resultEntity.get("transaction_id").toString();
					String out_refund_no = resultEntity.get("out_refund_no").toString();
					response.getWriter().write(setXml("SUCCESS", "OK")); // 告诉微信已经收到通知了
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
