const urls = require("utils/urls.js");
App({
  onLaunch: function () {    
    // 展示本地存储能力
    var logs = wx.getStorageSync('logs') || []
    logs.unshift(Date.now())
    wx.setStorageSync('logs', logs)
    // 登录
    // wx.login({
    //   success: res => {
    //     // 发送 res.code 到后台换取 openId, sessionKey, unionId
    //   }
    // })
    // 获取用户信息
    // wx.getSetting({
    //   success: res => {
    //     if (res.authSetting['scope.userInfo']) {
    //       // 已经授权，可以直接调用 getUserInfo 获取头像昵称，不会弹框
    //       wx.getUserInfo({
    //         success: res => {
    //           // 可以将 res 发送给后台解码出 unionId
    //           this.globalData.userInfo = res.userInfo

    //           // 由于 getUserInfo 是网络请求，可能会在 Page.onLoad 之后才返回
    //           // 所以此处加入 callback 以防止这种情况
    //           if (this.userInfoReadyCallback) {
    //             this.userInfoReadyCallback(res)
    //           }
    //         }
    //       })
    //     }
    //   }
    // })
    if(wx.getStorageSync("sessionId") ){
      wx.request({
        url: urls.profit + '/checkSession',
        header: {
          'content-type': 'application/x-www-form-urlencoded',
          'cookie': 'JSESSIONID=' + wx.getStorageSync("sessionId")
        },
        success(res) {
            //说明拦截了
          if(res.data.code == 2){
            wx.getUserInfo({
              success(res) {
                wx.login({
                  success(res1){
                    if(res1.code){
                      wx.request({
                        url: urls.profit + '/appLogin',
                        data: {
                          code: res1.code,
                          nickName: res.userInfo.nickName,
                          avatarUrl: res.userInfo.avatarUrl,
                          province: res.userInfo.province,
                          city: res.userInfo.city,
                          gender: res.userInfo.gender
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
                        }
                      });
                    }
                  }
                })
              }
            })
          }
        } 
      })
    }
  },
  globalData: {
    userInfo: null,
    context:'',
    jobId:0,
    page:'',
    jBegInfo:{begType:0,begInfo:''}
  }
})