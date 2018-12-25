package com.connection.wxPay.action;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
			String nonce_str = RandCharsUtils.getRandomString(32);// 随机字符串，不长于32位，推荐随机数生成法
			String partner_trade_no = RandCharsUtils.getRandomStringOrderNum();//商户订单号
			String check_name = "NO_CHECK";
			int amount = (int)(Double.parseDouble(money*100+""));//单位为分 收取2%的手续费
			String desc = "包享说提现";
			String spbill_create_ip = "192.168.0.1";
			/**
			 * 封装参数，参数签名
			 */
			SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
			parameters.put("mch_appid", appid);
			parameters.put("mchid", mch_id);
			parameters.put("nonce_str", nonce_str);
			parameters.put("partner_trade_no", partner_trade_no);
			parameters.put("openid", openid);
			parameters.put("check_name",check_name);
			parameters.put("amount", amount);
			parameters.put("desc", desc);//utf8字符集
			parameters.put("spbill_create_ip", spbill_create_ip);
			
			// 得到生成的签名
			String sign = WXSignUtils.createSign("UTF-8", parameters);
			// 生成签名结束
			parameters.put("sign", sign);
			
			// 构造xml参数
			String xmlInfo = HttpXmlUtils.createPayXml(parameters);
			String wxUrl = "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers";

			String weixinPost = HttpXmlUtils.refundHand(wxUrl, xmlInfo).toString();
			
			Map mapreturn = ParseXMLUtils.jdomParseXml(weixinPost);
			String return_code = mapreturn.get("return_code").toString();
			String result_code = mapreturn.get("result_code").toString();
			if("SUCCESS".equals(return_code)&&"SUCCESS".equals(result_code)){
				//退款申请成功 任务过期
//				jobDao.updateState(4,jobId);//修改任务状态为退款中
				partner_trade_no = mapreturn.get("partner_trade_no").toString();
				String payment_no = mapreturn.get("payment_no").toString();
				String payment_time = mapreturn.get("payment_time").toString();
//				进行提现操作
				adminDao.modifyMoney1(0.00-money, userId);
				dataDao.saveCash(userId, money,partner_trade_no,payment_no,payment_time);
				
			}else{
				state = 3;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		return state;
	}
}
