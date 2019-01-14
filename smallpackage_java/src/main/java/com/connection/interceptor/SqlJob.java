package com.connection.interceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.connection.dao.AdminDao;
import com.connection.dao.DataDao;
import com.connection.dao.JobDao;
import com.connection.service.interfaces.UserService;
import com.connection.tool.Util;
import com.connection.wxPay.action.RefundService;
/**
 * 这是一个定时任务类
 * @author 86136
 *
 */
@Service
public class SqlJob {
	@Autowired
	private JobDao jobDao;
	@Autowired
	private DataDao dataDao;
	@Autowired
	private AdminDao adminDao;
	@Autowired
	private UserService userService;

	@Transactional
	public void execute() {
		int jobId = 0;
		int userId = 0;
		int state = 0;
		List<HashMap<String, Object>> jobs = null;
		double money = 0.00;
		Map<String, Object> returnParam = null;
		List<HashMap<String, Object>> dues = null;
		List<HashMap<String, Object>> forms = null;
		jobs = jobDao.getOverdue();// 搞定过期的任务
		if (jobs != null && jobs.size() > 0) {

			for (Map<String, Object> job : jobs) {
				jobId = (int) job.get("id");
				userId = (int) job.get("userId");
				state = (int) job.get("state");
				if((double)job.get("totalAward")>(double)job.get("award")&&job.get("transaction_id")!=null){
					money = (double) job.get("award") - (double) job.get("alreadyAward");
					double charge = Double.parseDouble(String.format("%.2f", money * 0.02));
					money = money + charge;//钱加手续费
					if (state == 1) {// 红包处于正常状态
						if (dataDao.checkCash(jobId) == null) {// 防止出现二次提交（因为退款过的话，下面会插入到cash，正常提现不会有jobId）
							
							
							jobDao.updateState(3, jobId);// 改变job状态
							dataDao.refund(userId, 0.00 - money, jobId);// cash表插入数据
							adminDao.modifyMoney(money, userId);// 用户余额增加
							
							
							returnParam = dataDao.getFormid(userId);
							if (returnParam != null && returnParam.get("openid") != null) {
								String msg = Util.getMsg(returnParam, job, 1);
								userService.sendMsg(msg);
								dataDao.updateState((int) returnParam.get("id"));
							}
						}
					}
				}
			}
		}

		dues = jobDao.pushTimeDue();// 订阅提醒，得到符合条件的红包们
		if (dues != null && dues.size() > 0) {
			for (Map<String, Object> due : dues) {
				forms = dataDao.getTimerFormId((int) due.get("id"));//得到的订阅的人们
				jobDao.updateTakePush((int) due.get("id"));// 修改 takePush，修改是否推送消息的takepush，0应该是没推送过，1是推送了
				if (forms != null && forms.size() > 0) {
					for (Map<String, Object> form : forms) {
						String msg = Util.getMsg(form, due, 3);//拿到消息模板
						userService.sendMsg(msg);
						if (form.get("formid") != null) {
							dataDao.updateState((int) form.get("id"));
						}
					}
				}
			}
		}
	}
}
