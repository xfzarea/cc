const urls = require("../../utils/urls.js");
const app = getApp();
Page({

  /**
   * 页面的初始数据
   */

  data: {
    userInfo: wx.getStorageSync("userInfo"),
    context: "恭喜发财，大吉大利",
    award: 0.00,
    count: 0,
    submit: false,
    charge: 0.00,
    warrantShow: false,
    show: false,
    warn_show: false,
    job_type: 0,
    one_award: 0.00,
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    const that = this;
    // 查看是否授权
    if (!wx.getStorageSync("userInfo")) {
      wx.redirectTo({
        url: '/pages/index/index?page=home'
      })
    } else {
      that.setData({
        userInfo: wx.getStorageSync("userInfo")
      })
      //从自定义口令页面过来
      if (app.globalData.context != '') {
        that.setData({
          context: app.globalData.context
        })
        var jobContexts = wx.getStorageSync("jobContexts");
        if (jobContexts == '') {
          jobContexts = [];
        }
        jobContexts.splice(0, 0, app.globalData.context);
        wx.setStorageSync("jobContexts", jobContexts)
        app.globalData.context = '';
      }
    }
  },
  load: function (e) {
    console.log("公众号组件", e)
  },

  /**
   * 选择普通红包
   */
  changeType: function () {
    const that = this;
    let job_type = that.data.job_type;
    if (job_type == 0) {
      job_type = 1;
    } else {
      job_type = 0;
    }
    that.setData({
      job_type: job_type,
      award: 0.00,
      count: 0,
      one_award: 0.00,
      submit: false
    })
  },
  /**
   * 去红包页面
   */
  toRedPackage: function (jobId) {
    setTimeout(function () {
      wx.navigateTo({
        url: '/pages/package/package?id=' + jobId,
      })
    }, 200)
  },
  /**
   * 营销服务
   */
  toMarketing: function () {
    wx.navigateTo({
      url: '/pages/marketing/marketing',
    })
  },
  /**
   * 创建
   */
  toCommand: function () {
    wx.setStorageSync("textCommand", false);
    wx.navigateTo({
      url: '/pages/command/command',
    })
  },
  input: function (e) {
    const that = this;
    var type1 = e.currentTarget.dataset.type;
    var data = e.detail.value;
    var award = that.data.award;
    let count = that.data.count;
    let job_type = that.data.job_type;
    let one_award = that.data.one_award;
    if (type1 == "award") {

      if (data == 0 || data == "") {
        that.data.award = data;
        that.setData({
          submit: false
        })
        return;
      }

      if (/^\d+\.?\d{0,2}$/.test(data)) {
        if (job_type == 0) {
          that.setData({
            award: data,
            charge: Math.round(data) * 2 / 100,
          })
        } else {
          that.setData({
            one_award: data,
            award: (data * count).toFixed(2),
            charge: Math.round(data * count) * 2 / 100,
          })
        }
      } else {
        if (job_type == 0) {
          that.setData({
            award: data.substring(0, data.length - 1),
            charge: Math.round(data.substring(0, data.length - 1)) * 2 / 100,
          })
        } else {
          that.setData({
            one_award: data.substring(0, data.length - 1),
            // award : data.substring(0, data.length - 1) * count,
            // charge: Math.round(data.substring(0, data.length - 1)*count) * 2 / 100,
          })
        }
      }
    }
    if (type1 == "count") {
      if (job_type == 0) {
        that.setData({
          count: data
        })
      } else {
        that.setData({
          count: data,
          award: one_award * data,
          charge: Math.round(one_award * data) * 2 / 100
        })
      }
    }
    
    that.checkSubmit();
  },
  //判断是否可以塞钱进红包
  checkSubmit: function () {
    const that = this;
    let job_type = that.data.job_type;
    var flag = true;
    if (that.data.award == '' || that.data.count == '' || that.data.award == 0 || that.data.count == 0) {
      flag = false;
    }
    if (job_type == 1) {
      if (that.data.one_award == '' || that.data.one_award == 0.00) {
        flag = false; 
      }
    }else{
      if(that.data.count * 0.01 > that.data.award){
        flag = false;
      }
    }
    that.setData({
      submit: flag  
    })
  },
  /**
   * 唤醒支付以及生成红包
   */
  createJob: function () {
    const that = this;
    var userId = that.data.userInfo.userId;
    var openid = that.data.userInfo.openid;
    var totalAward = (parseFloat(that.data.award) + parseFloat(that.data.charge)).toFixed(2);
    let flag = true;
    if (totalAward < that.data.award) {
      flag = false;
    } 
    var jobId;
    if (flag) {
      wx.request({
        url: urls.profit + '/createJob',
        data: {
          userId: userId,
          openid: openid,
          totalAward: totalAward,
          award: that.data.award,
          totalCount: that.data.count,
          context: that.data.context,
          job_type: that.data.job_type,//是普通，还是拼手气红包
          one_award: that.data.one_award
        },
        success: res => {
          console.log(res.data);
          jobId = res.data.jobId;
          wx.requestPayment({
            'timeStamp': res.data.timestamp + '',
            'nonceStr': res.data.noncestr,
            'package': res.data.package,
            'signType': 'MD5',
            'paySign': res.data.sign,
            'success': function (res) {
              console.log(123);
              app.globalData.tags = '';
              app.globalData.jobContext = '';
              that.setData({
                context: "恭喜发财，大吉大利",
                award: '',
                count: '',
                charge: 0.00
              })
              setTimeout(function () {
                wx.navigateTo({
                  url: '/pages/package/package?id=' + jobId + "&handType=1"
                })
              }, 1000)
            },
            'fail': function (res) { }
          })
        }
      })
    }
  },
  closeWarn: function (e) {
    const that = this;
    that.setData({
      warn_show: false
    })
  },
  toRecord: function () {
    wx.navigateTo({
      url: '/pages/record/record',
    })
  },
  toPlayRed: function () {
    // wx.navigateTo({
    //   url: '/pages/playRed/playRed',
    // })
    const that = this;
    that.setData({
      warn_show: true
    })
  },
  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady: function () {

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow: function () {
    const that = this;
    that.setData({
      show: false
    })
  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide: function () {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload: function () {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh: function () {

  },
  /**
  * 红包记录
  */
  toRecord: function () {
    wx.navigateTo({
      url: '/pages/record/record',
    })
  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom: function () {

  },
 

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage: function () {
    console.log("分享")
    const that = this;
    return {
      path: "/pages/home/home",
      success: function (res) {

      },
      fail: function (res) {
        // 转发失败
      }
    }
  }
})