package com.connection.tool;
import java.io.Serializable;

/**
 * Ajax����Json��Ӧ���ģ��.
 *
 */
@SuppressWarnings("serial")
public class Result implements Serializable {

	/**
	 * �ɹ�
	 */
	public static final int SUCCESS = 1;
	/**
	 * ����
	 */
	public static final int WARN = 2;
	/**
	 * ʧ��
	 */
	public static final int ERROR = 0;
	
	/**
	 * �ɹ���Ϣ
	 */
	public static final String SUCCESS_MSG = "�����ɹ���";
	/**
	 * ʧ����Ϣ
	 */
	public static final String ERROR_MSG = "����ʧ��:����δ֪�쳣��";

	/**
	 * ���״̬��(���Զ�����״̬��) 1:�����ɹ� 0:����ʧ��
	 */
	private int code;
	/**
	 * ��Ӧ�������
	 */
	private String msg;
	/**
	 * ����������Ϣ��������ת��ַ��
	 */
	private Object obj;

	public Result() {
		super();
	}

	/**
	 * 
	 * @param code
	 *            ���״̬��(���Զ�����״̬�����ʹ���ڲ���̬���� 1:�����ɹ� 0:����ʧ�� 2:����)
	 * @param msg
	 *            ��Ӧ�������
	 * @param obj
	 *            ����������Ϣ��������ת��ַ��
	 */
	public Result(int code, String msg, Object obj) {
		super();
		this.code = code;
		this.msg = msg;
		this.obj = obj;
	}

	/**
	 * Ĭ�ϲ����ɹ����.
	 */
	public static Result successResult() {
		return new Result(SUCCESS, SUCCESS_MSG, null);
	}

	/**
	 * Ĭ�ϲ���ʧ�ܽ��.
	 */
	public static Result errorResult() {
		return new Result(ERROR, ERROR_MSG, null);
	}

	/**
	 * ���״̬��(���Զ�����״̬��) 1:�����ɹ� 0:����ʧ��
	 */
	public int getCode() {
		return code;
	}

	/**
	 * ���ý��״̬��(Լ�� 1:�����ɹ� 0:����ʧ�� 2:����)
	 * 
	 * @param code
	 *            ���״̬��
	 */
	public Result setCode(int code) {
		this.code = code;
        return this;
	}

	/**
	 * ��Ӧ�������
	 */
	public String getMsg() {
		return msg;
	}

	/**
	 * ������Ӧ�������
	 * 
	 * @param msg
	 *            ��Ӧ�������
	 */
	public Result setMsg(String msg) {
		this.msg = msg;
        return this;
	}

	/**
	 * ����������Ϣ��������ת��ַ��
	 */
	public Object getObj() {
		return obj;
	}

	/**
	 * ��������������Ϣ��������ת��ַ��
	 * 
	 * @param obj
	 *            ����������Ϣ��������ת��ַ��
	 */
	public Result setObj(Object obj) {
		this.obj = obj;
        return this;
	}

	
}

