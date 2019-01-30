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
 * 企业付款
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
			// 参数组 需要客户端传过来的数据有：商品信息 商品描述 商品金额 充值类型 充值账号
			String appid = config.appid;// 应用ID
			String mch_id = config.mch_id;// 商户号
			String book_url = config.book_url;//证书路径
			String nonce_str = RandCharsUtils.getRandomString(32);// 随机字符串，不长于32位，推荐随机数生成法
			String partner_trade_no = RandCharsUtils.getRandomStringOrderNum();//商户订单号
			String check_name = "NO_CHECK";//检查真实姓名不？这里写入不核查，NO_CHECK：不校验真实姓名 、FORCE_CHECK：强校验真实姓名
			int amount = (int)(Double.parseDouble(money*100+""));//单位为分 收取2%的手续费
			String desc = "包开说提现";//描述
			String spbill_create_ip = "192.168.0.1";//用户ip
			/**
			 * 封装参数，参数签名
			 */
			SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
			parameters.put("mch_appid", appid);
			parameters.put("mchid", mch_id);
			parameters.put("nonce_str", nonce_str);
			parameters.put("partner_trade_no", partner_trade_no);;//商户订单号
			parameters.put("openid", openid);//用户得openId
			parameters.put("check_name",check_name);
			parameters.put("amount", amount);
			parameters.put("desc", desc);//utf8字符集
			parameters.put("spbill_create_ip", spbill_create_ip);
			
			// 得到生成的签名
			String sign = WXSignUtils.createSign("UTF-8", parameters);
			// 生成签名结束,放到map里
			parameters.put("sign", sign);
			// 构造xml参数
			String xmlInfo = HttpXmlUtils.createPayXml(parameters);
			//微信请求地址
			String wxUrl = "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers";
					//这里去请求，并返回字符串
			String weixinPost = HttpXmlUtils.refundHand(wxUrl, xmlInfo,book_url,mch_id).toString();
			Map mapreturn = ParseXMLUtils.jdomParseXml(weixinPost);
			//返回参数return_codeSUCCESS/FAIL此字段是通信标识，非交易标识，交易是否成功需要查看result_code来判断
			//return_msg这里没获取，返回信息，如非空，为错误原因 、签名失败 、参数格式校验错误
			String return_code = mapreturn.get("return_code").toString();
			//SUCCESS/FAIL，注意：当状态为FAIL时，存在业务结果未明确的情况。如果如果状态为FAIL，请务必关注错误代码（err_code字段），通过查询查询接口确认此次付款的结果。
			String result_code = mapreturn.get("result_code").toString();
			if("SUCCESS".equals(return_code)&&"SUCCESS".equals(result_code)){
				//退款申请成功 任务过期
//				jobDao.updateState(4,jobId);//修改任务状态为退款中
				
				//以下三个字段都是在return_code 和result_code都为SUCCESS的时候有返回 
				partner_trade_no = mapreturn.get("partner_trade_no").toString();//商户订单号
				String payment_no = mapreturn.get("payment_no").toString();//微信付款单号
				String payment_time = mapreturn.get("payment_time").toString();//付款成功时间
//				进行提现操作
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
