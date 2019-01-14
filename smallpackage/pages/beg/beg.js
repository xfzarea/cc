const urls = require("../../utils/urls.js");
const app = getApp();
Page({

  /**
   * 页面的初始数据
   */

  data: {
    userInfo: wx.getStorageSync("userInfo"),
    context: "可设置文字/语音/图片",
    award: 0.00,
    count: 1,
    submit: false,
    charge: 0.00,
    warrantShow: false,
    show: false,
    warn_show: false,
    job_type: 0,
    one_award: 0.00,
    num: 0,
    showModal: false,
    handType: 0, //我用来区分卡片的
    jobId:0
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

  changeOil: function (e) {
    console.log(e);
   
    this.setData({
      num: e.target.dataset.num
    })
    if (this.data.num==1){
      this.setData({
        award:0,
        award:5.20
      })
    }
    if (this.data.num == 2) {
      this. setData({
        award: 0,
        award: 66.66
      })
    }
    if (this.data.num==3){
      this.setData({
        award: 0,
        award: 8.88
      })
    }

    if (this.data.num == 4) {
      this.setData({
        award: 0,
        award: 99.99
      })
    }
    if (this.data.num == 5) {
      this.setData({
        award: 0,
      
      })
    }
    const that = this;
    that.checkSubmit();
    console.log(this.data.award)
    console.log(this.data.count)
  },

  /**
  
  /**
   * 创建
   */
  toChanges: function () {
    wx.navigateTo({
      url: '/pages/changes/changes',
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
        
        })
      } else {
        that.setData({
         
          award: one_award * data,
          charge: Math.round(one_award * data) * 2 / 100
        })
      }
    }
    if (that.data.award > 200 || that.data.one_award > 200 || that.data.one_award * that.data.count > 200) {
      wx.showModal({
        title: '提示',
        content: '单个红包金额最高200元，如需发大额红包，请联系客服，回复‘8’即可',
        cancelText: '知道了',
        cancelColor: '#59ce40',
        confirmText: '更多服务',
        success: function (res) {
          if (res.confirm) {
            that.setData({
              award: 0.00,
              charge: 0.00,
              one_award: 0.00,
              submit: false
            })
            wx.navigateTo({
              url: '/pages/marketing/marketing',
            })
          } else {
            that.setData({
              award: 0.00,
              charge: 0.00,
              one_award: 0.00,
              submit: false
            })
          }
        }
      })
    }
    that.checkSubmit();
  },
  //判断是否可以塞钱进红包
  checkSubmit: function () {
    const that = this;
    let job_type = that.data.job_type;
    var flag = true;
    if (that.data.award == ''  || that.data.award == 0 ) {
      flag = false;
    }
    if (job_type == 1) {
      if (that.data.one_award == '' || that.data.one_award == 0.00) {
        flag = false;
      }
    }
    if (flag) {
      if (that.data.award / that.data.count < 1) {
        wx.showModal({
          title: '提醒',
          content: '平均每个人获得的红包不低于1元',
        })
        that.setData({
          submit: false
        })
      } else {
        that.setData({
          submit: true
        })
      }
    } else {
      that.setData({
        submit: false
      })
    }
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
    if (that.data.award > 200) {
      flag = false;
    }
    if (that.data.award / that.data.count < 1) {
      flag = false;
    }
   
    if (flag) {
      wx.request({
        url: urls.profit + '/createBegJob',
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
          
         
                that.setData({
                 context: "可设置文字/语音/图片",
                 award: '',
                 charge: 0.00,
                 num:0, 
                 jobId : res.data.jobId
              })
              that.checkSubmit();
          that.changeHandType();
          // wx.requestPayment({
          //   'timeStamp': res.data.timestamp + '',
          //   'nonceStr': res.data.noncestr,
          //   'package': res.data.package,
          //   'signType': 'MD5',
          //   'paySign': res.data.sign,
          //   'success': function (res) {
          //     console.log(123);
          //     app.globalData.tags = '';
          //     app.globalData.jobContext = '';
          //     that.setData({
          //       context: "可设置文字/语音/图片",
          //       award: '',
          //       count: '',
          //       charge: 0.00
          //     })
          //     setTimeout(function () {
          //       wx.navigateTo({
          //         url: '/pages/package/package?id=' + jobId + "&handType=1"
          //       })
          //     }, 1000)
          //   },
          //   'fail': function (res) { }
          // })
        }
      })
    }
  },
  //卡片区分
  changeHandType: function () {
    wx.hideTabBar({
      animation: true //是否需要过渡动画
    })
    const that = this;
    that.setData({
      handType: 1
    })
  },
  /**
   * 隐藏 卡片
   */
  hide: function () {
    const that = this;
    that.setData({
      code: 0,
      handType: 0
    })
    wx.showTabBar({
      animation: true //是否需要过渡动画
    })
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
  submit_b: function () {
    wx.hideTabBar({
      animation: true //是否需要过渡动画
    })
    this.setData({
      showModal: true


    })
  
  },
  
  preventTouchMove: function () {
  },
  go: function () {
    
    this.setData({
      showModal: false
    })
    wx.showTabBar({
      animation: true //是否需要过渡动画
    })
  },
  go1: function () {
    this.setData({
      showModal: false,
      award:0,
      num:0
    })
    const that =this;
    that.checkSubmit();
    
    this.setData({
      showModal: false
    })
    wx.showTabBar({
      animation: true //是否需要过渡动画
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
    const jobId = that.data.jobId;
    return {
     
      path: '/pages/begPackage/begPackage?id=' + jobId + " & handType=1",
    
      success: function (res) {

      },
      fail: function (res) {
        // 转发失败
      }
    }
  }
 
})