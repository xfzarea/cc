package com.connection.wxPay.action;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.connection.wxPay.util.HttpXmlUtils;
import com.connection.wxPay.util.ParseXMLUtils;
import com.connection.wxPay.util.RandCharsUtils;
import com.connection.wxPay.util.WXSignUtils;
import com.connection.wxPay.util.WeixinConfigUtils;

@Service
public class RefundService {
//	@Autowired
//	private JobDao jobDao;
	@Transactional
	public void refund(Map<String,Object>job){
		try {
			WeixinConfigUtils config = new WeixinConfigUtils();
			// 参数组 需要客户端传过来的数据有：商品信息 商品描述 商品金额 充值类型 充值账号
			String appid = config.appid;// 应用ID
			String mch_id = config.mch_id;// 商户号
			String nonce_str = RandCharsUtils.getRandomString(32);// 随机字符串，不长于32位，推荐随机数生成法
			String out_refund_no = RandCharsUtils.getRandomStringOrderNum();//退款单号
			String out_trade_no = (String)job.get("out_trade_no");
			String refund_fee = (Double)job.get("award")*100+"";
			String total_fee = (Double)job.get("award")*100+"";
			String transaction_id = (String)job.get("transaction_id");
			String notify_url = "https://www.yaohoudy.com/connection/weixinRefund";
			int jobId = (Integer)job.get("id");
			/**
			 * 封装参数，参数签名
			 */
			SortedMap<Object, Object> parameters = new TreeMap<Object, Object>();
			parameters.put("appid", appid);
			parameters.put("mch_id", mch_id);
			parameters.put("nonce_str", nonce_str);
			parameters.put("out_refund_no", out_refund_no);
			parameters.put("out_trade_no", out_trade_no);
			parameters.put("refund_fee", refund_fee.substring(0,refund_fee.indexOf(".")));
			parameters.put("total_fee", total_fee.substring(0,total_fee.indexOf(".")));
			parameters.put("transaction_id", transaction_id);
			parameters.put("notify_url", notify_url);
			
			// 得到生成的签名
			String sign = WXSignUtils.createSign("UTF-8", parameters);
			// 生成签名结束
			parameters.put("sign", sign);
			
			// 构造xml参数
			String xmlInfo = HttpXmlUtils.xmlInfo(parameters);
			String wxUrl = "https://api.mch.weixin.qq.com/secapi/pay/refund";

			String weixinPost = HttpXmlUtils.refundHand(wxUrl, xmlInfo).toString();
			
			Map mapreturn = ParseXMLUtils.jdomParseXml(weixinPost);
			String return_code = mapreturn.get("return_code").toString();
			String result_code = mapreturn.get("result_code").toString();
			if("SUCCESS".equals(return_code)&&"SUCCESS".equals(result_code)){
				//退款申请成功 任务过期
//				jobDao.updateState(4,jobId);//修改任务状态为退款中
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
}
