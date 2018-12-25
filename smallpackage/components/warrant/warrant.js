// components/warrant/warrant.js
//获取应用实例
const app = getApp();
const urls = require("../../utils/urls.js")
Component({
  /**
   * 组件的属性列表
   */
  properties: {
    
  },

  /**
   * 组件的初始数据
   */
  data: {
    canIUse: wx.canIUse('button.open-type.getUserInfo'),
  },

  /**
   * 组件的方法列表
   */
  methods: {
    bindGetUserInfo: function (e) {
      if (e.detail.userInfo) {
        //用户按了允许授权按钮
        var that = this;
        console.log(e.detail.userInfo);
        wx.login({
          success(res) {
            console.log(res)
            if (res.code) {
              //插入登录的用户的相关信息到数据库
              wx.request({
                url: urls.profit + '/appLogin',
                data: {
                  code: res.code,
                  nickName: e.detail.userInfo.nickName,
                  avatarUrl: e.detail.userInfo.avatarUrl,
                  province: e.detail.userInfo.province,
                  city: e.detail.userInfo.city,
                  gender: e.detail.userInfo.gender
                },
                method: 'post',
                header: {
                  'content-type': 'application/x-www-form-urlencoded'
                },
                success: function (res) {
                  //从数据库获取用户信息
                  console.log(res.data.obj);
                  wx.setStorageSync("userInfo", res.data.obj.admin);
                  wx.setStorageSync("userId", res.data.obj.admin.userId);
                  wx.setStorageSync("sessionId", res.data.obj.sessionId);
                  wx.setStorage({
                    key: 'userInfo',
                    data: res.data.obj.admin,
                  })
                  that.triggerEvent('getWarrant', false);
                }
              });
            }
          }
        })
      } else {
        //用户按了拒绝按钮
        wx.showModal({
          title: '警告',
          content: '您点击了拒绝授权，将无法进入小程序，请授权之后再进入!!!',
          showCancel: false,
          confirmText: '返回授权',
          success: function (res) {
            if (res.confirm) {
              console.log('用户点击了“返回授权”')
            }
          }
        })
      }
    }
  }
})
