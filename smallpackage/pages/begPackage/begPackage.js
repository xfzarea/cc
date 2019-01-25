// pages/begPackage/begPackage.js
const urls = require("../../utils/urls.js");
Page({

  /**
   * 页面的初始数据
   */
  data: {
    userInfo: wx.getStorageSync("userInfo"),
    state: false, //判断是否是自己淘得红包
    voice_play: false,
    begPackage: '',
    begRecord: [],
    jobId: 0,
    pagFlag: false,
    handType: 0,
    thinkUserId: 0,
    warrantShow: false,
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function(options) {
    const that = this;
    let jobId = options.id;
    var scene = decodeURIComponent(options.scene); //从二维码进来
    if (scene != undefined && scene != "undefined" && scene != null) {
      jobId = scene
    }
    let handType = options.handType;
    if (handType) {
      that.data.handType = handType;
      if (handType == 2) {
        let uid = options.uid;
        that.data.thinkUserId = uid;
      }
    }
    console.log(jobId)
    // jobId = 100123;
    // jobId = 100148;
    that.setData({
      jobId: jobId
    })
    if (!wx.getStorageSync("userInfo")) {
      that.setData({
        warrantShow: true
      })
    } else {
      wx.showLoading({
        title: '客官请稍等~',
      })
      that.toBegPackage(jobId);
      if (that.data.handType != 0) {
        that.setData({
          handType: that.data.handType
        })
      }
    }
  },

  /**
   * 组件内点击
   */
  getWarrant: function(e) {
    const that = this;
    that.setData({
      warrantShow: e.detail,
      userInfo: wx.getStorageSync("userInfo")
    })
    var id = that.data.jobId;
    if (id != undefined && id != "undefined") {
      that.setData({
        id: id
      })
      that.toBegPackage(id)
    }
  },
  preBegImage:function(){
    const that = this;
    wx.previewImage({
      urls: [that.data.begPackage.context],
    })
  },
  hide: function() {
    const that = this;
    that.setData({
      handType: 0,
      thinkUserId: 0
    })
  },
  /**
   * 得到具体红包内容
   */
  toBegPackage: function(jobId) {
    const that = this;
    wx.request({
      url: urls.profit + '/toBegPackage',
      data: {
        id: jobId
      },
      success: res => {
        console.log(res.data);
        let state = that.data.state;
        if (that.data.userInfo.userId == res.data.obj.job.userId) {
          state = true;
        }
        wx.hideLoading();
        that.setData({
          begPackage: res.data.obj.job,
          begRecord: res.data.obj.begJobRecord,
          state: state,
        })
      }
    })
  },
  toVoicePlay: function(e) {
    let src = e.currentTarget.dataset.src;
    wx.navigateTo({
      url: `/pages/voicePlay/voicePlay?url=${src}`,
    })
  },
  /**
   * 跳转首页
   */
  toBeg: function() {
    wx.reLaunch({
      url: '/pages/beg/beg',
    })
  },
  /**
   * 支付
   */
  toPay: function() {
    const that = this;
    let jobId = that.data.jobId;
    let userId = that.data.userInfo.userId;
    let openid = that.data.userInfo.openid;
    console.log(jobId,userId,openid)
    wx.request({
      url: urls.profit + '/payBegJob',
      data: {
        jobId: jobId,
        userId: userId,
        openid: openid
      },
      success: res => {
        console.log(res)
        if("niwanguole" == res.data){
          wx.showToast({
            title: '亲~只允许赏一次哟~',
            icon:'none'
          })
        }else{
          let formid = res.data.prepayid;
          wx.requestPayment({
            'timeStamp': res.data.timestamp + '',
            'nonceStr': res.data.noncestr,
            'package': res.data.package,
            'signType': 'MD5',
            'paySign': res.data.sign,
            'success': function (res) {
              that.toBegPackage(that.data.jobId);
              that.saveFormId(formid);
            },
          })
        }
      }
    })


  },

  saveFormId: function (formId) {
    const that = this;
    wx.request({
      url: urls.profit + '/saveFormid',
      data: {
        formid: formId,
        userId: that.data.userInfo.userId,
      },
      success: res => {

      }
    })
  },
  /**
   * 答谢
   */
  think: function(e) {
    const that = this;
    console.log(e);
    let formid = e.detail.formId;
    let userId = e.detail.target.dataset.userid;
    let jobId = e.detail.target.dataset.jobid;
    let state = e.detail.target.dataset.state;
    that.saveFormId(formid);
    if (state == 0) { //未答谢的状态
      that.setData({
        handType: 1,
        thinkUserId: userId
      })
    }
  },

  toThink: function() {
    const that = this;
    wx.request({
      url: urls.profit + '/thanks',
      data: {
        id: that.data.jobId,
        userId: that.data.thinkUserId,
      },
      success: res => {
        that.hide();
        that.toBegPackage(that.data.jobId)
      }
    })
  },
  saveFormId: function(formId) {
    const that = this;
    wx.request({
      url: urls.profit + '/saveFormid',
      data: {
        formid: formId,
        userId: that.data.userInfo.userId,
      },
      success: res => {

      }
    })
  },
  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady: function() {

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow: function() {

  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide: function() {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload: function() {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh: function() {

  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom: function() {

  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage: function() {

  }
})