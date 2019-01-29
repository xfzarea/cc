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
      that.setData({
        userInfo:wx.getStorageSync("userInfo")
      })
      wx.showLoading({
        title: '客官请稍等~',
      })
      that.getImageInfo(wx.getStorageSync("userInfo").avatarUrl);
      that.toBegPackage(jobId);
      if (that.data.handType != 0) {
        that.setData({
          handType: that.data.handType
        })
      };
      setTimeout(function(){
        that.draw_share_pic_1();
      },1000)
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
      that.getImageInfo(wx.getStorageSync("userInfo").avatarUrl);
      that.toBegPackage(id);
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
              that.setData({
                handType: 4
              })
            },
          })
        }
      }
    })
  },

  /**
   * canvas画转发封面图
   */
  draw_share_pic_1: function () {
    var that = this;
    var rem;
    wx.getSystemInfo({
      success: function (res) {
        rem = res.screenWidth / 750;
      },
    })
    const ctx = wx.createCanvasContext('share_pic_1');
    ctx.drawImage("/images/97.jpg", 0, 0, 420 * rem, 336 * rem);
    let headPic = that.data.headPic;
    if (headPic) {
      that.circleImg(ctx, headPic, 168 * rem, 128 * rem, 45 * rem);
      // ctx.drawImage(headPic, 168 * rem, 128 * rem, 90 * rem, 90 * rem);
    }
    ctx.draw();
    that.downImg_1();
  },

  /**
   * 画圆图
   */
  circleImg: function (ctx, img, x, y, r) {
    ctx.save();
    var d = 2 * r;
    var cx = x + r;
    var cy = y + r;
    // ctx.beginPath();
    ctx.arc(cx, cy, r, 0, 2 * Math.PI);
    ctx.clip();
    ctx.stroke("#fff");
    ctx.drawImage(img, x, y, d, d);
  },
  downImg_1: function (id) {
    const that = this;
    wx.canvasToTempFilePath({
      x: 0,
      y: 0,
      canvasId: 'share_pic_1',
      success: function (res) {
        console.log(res.tempFilePath);
        that.setData({
          share_pic_src_1: res.tempFilePath
        })
      }
    })
    // }, 100))
  },

  /**
   * 头像缓存本地得方法
   */
  getImageInfo: function (url) { //  图片缓存本地的方法
    const that = this;
    if (typeof url === 'string') {
      wx.getImageInfo({ //  小程序获取图片信息API
        src: url,
        success: function (res) {
          that.setData({
            headPic: res.path
          })
        }
      })
    }
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
  toThink1:function(){
    const that = this;
    that.setData({
      handType:1
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
    console.log("分享")
    const that = this;
    let jobId = that.data.jobId;
    return {
      title: "【语音红包】发完红包讨红包，红包玩法欢乐多",
      path: '/pages/begPackage/begPackage?id=' + jobId,
      imageUrl: that.data.share_pic_src_1,
      success: function (res) {
        
      },
      fail: function (res) {
        // 转发失败
      },
    }
  }
})