//index.js
//获取应用实例
const app = getApp();
const urls = require("../../utils/urls.js")
Page({
  data: {
    //判断小程序的API，回调，参数，组件等是否在当前版本可用。
    canIUse: wx.canIUse('button.open-type.getUserInfo'),
    page: "home",
  },
  onLoad: function(options) {
    var that = this;
    let page = options.page;
    if (page != undefined) {
      that.data.page = page;
    }
    console.log("page", that.data.page)
  },

  /**
   * 跳转任何页面
   */
  toPage: function(page) {

    if (page == "timer" || page == "admin" || page == "home"||page=="beg") {
      wx.switchTab({
        url: '/pages/' + page + "/" + page,
      })
    } else {
      wx.navigateTo({
        url: '/pages/' + page + "/" + page,
      })
    }

  },
  bindGetUserInfo: function(e) {
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
              success: function(res) {
                //从数据库获取用户信息
                console.log(res.data.obj);
                // wx.setStorageSync("userInfo", res.data.obj.admin);
                wx.setStorageSync("userId", res.data.obj.admin.userId);
                wx.setStorageSync("sessionId", res.data.obj.sessionId);
                wx.setStorage({
                  key: 'userInfo',
                  data: res.data.obj.admin,
                  success: res => {
                    that.toPage(that.data.page)
                  }
                })
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
        success: function(res) {
          if (res.confirm) {
            console.log('用户点击了“返回授权”')
          }
        }
      })
    }
  }
})