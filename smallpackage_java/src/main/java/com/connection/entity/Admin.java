package com.connection.entity;
//����ok�����ݿ�ʵ���ֶ���
//����������ʵ������menoy��menoy_version
public class Admin {
	private int userId;
	private String nickName;
	private String avatarUrl;//ͷ���ַ
	private int gender;//�Ա�0 δ֪ ��1�У�2Ů
	private String province;//�û�����ʡ��
	private String city;//����
	private String openid;//�û�Ψһ��ʶ
	private String createTime;//����ʱ��
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getAvatarUrl() {
		return avatarUrl;
	}
	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}
	public int getGender() {
		return gender;
	}
	public void setGender(int gender) {
		this.gender = gender;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getOpenid() {
		return openid;
	}
	public void setOpenid(String openid) {
		this.openid = openid;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	
}
