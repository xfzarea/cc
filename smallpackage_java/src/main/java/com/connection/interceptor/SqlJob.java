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
 * ����һ����ʱ������
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
		jobs = jobDao.getOverdue();// �㶨���ڵ�����
		if (jobs != null && jobs.size() > 0) {

			for (Map<String, Object> job : jobs) {
				jobId = (int) job.get("id");
				userId = (int) job.get("userId");
				state = (int) job.get("state");
				if((double)job.get("totalAward")>(double)job.get("award")&&job.get("transaction_id")!=null){
					money = (double) job.get("award") - (double) job.get("alreadyAward");
					double charge = Double.parseDouble(String.format("%.2f", money * 0.02));
					money = money + charge;//Ǯ��������
					if (state == 1) {// �����������״̬
						if (dataDao.checkCash(jobId) == null) {// ��ֹ���ֶ����ύ����Ϊ�˿���Ļ����������뵽cash���������ֲ�����jobId��
							
							
							jobDao.updateState(3, jobId);// �ı�job״̬
							dataDao.refund(userId, 0.00 - money, jobId);// cash���������
							adminDao.modifyMoney(money, userId);// �û��������
							
							
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

		dues = jobDao.pushTimeDue();// �������ѣ��õ����������ĺ����
		if (dues != null && dues.size() > 0) {
			for (Map<String, Object> due : dues) {
				forms = dataDao.getTimerFormId((int) due.get("id"));//�õ��Ķ��ĵ�����
				jobDao.updateTakePush((int) due.get("id"));// �޸� takePush���޸��Ƿ�������Ϣ��takepush��0Ӧ����û���͹���1��������
				if (forms != null && forms.size() > 0) {
					for (Map<String, Object> form : forms) {
						String msg = Util.getMsg(form, due, 3);//�õ���Ϣģ��
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
