// pages/begPackage/begPackage.js
const urls = require("../../utils/urls.js");
Page({

  /**
   * 页面的初始数据
   */
  data: {
    userInfo:wx.getStorageSync("userInfo"),
    state:false,//判断是否是自己淘得红包
    voice_play:false,
    begPackage:'',
    begRecord:[],
    jobId:0,
    pagFlag:false,
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    const that = this;
    let jobId = options.id;
    console.log(jobId)
    // jobId = 100123;
    jobId = 100129;
    that.setData({
      jobId:jobId
    })
    wx.showLoading({
      title: '客官请稍等~',
    })
    that.toBegPackage(jobId)
  },

  /**
   * 得到具体红包内容
   */
  toBegPackage:function(jobId){
    const that = this;
    wx.request({
      url: urls.profit +'/toBegPackage',
      data:{
        id:jobId
      },
      success:res=>{
        console.log(res.data);
        let state = that.data.state;
        if(that.data.userInfo.userId == res.data.obj.job.userId){
          state = true;
        }
        wx.hideLoading();
        that.setData({
          begPackage:res.data.obj.job,
          begRecord: res.data.obj.begJobRecord,
          state:state,
        })
      }
    })
  },
  toVoicePlay:function(e){
    let src = e.currentTarget.dataset.src;
    wx.navigateTo({
      url: `/pages/voicePlay/voicePlay?url=${src}`,
    })
  },
  /**
   * 跳转首页
   */
  toBeg:function(){
    wx.reLaunch({
      url: '/pages/beg/beg',
    })
  },
  /**
   * 支付
   */
  toPay:function(){
    const that = this;
    let payFlag = that.data.payFlag;
    if(!payFlag){
      that.data.payFlag = true;
      let jobId = that.data.jobId;
      let userId = that.data.userInfo.userId;
      let openid = that.data.userInfo.openid;

      wx.request({
        url: urls.profit + '/payBegJob',
        data: {
          jobId: jobId,
          userId: userId,
          openid: openid
        },
        success: res => {
          console.log(res)
          wx.requestPayment({
            'timeStamp': res.data.timestamp + '',
            'nonceStr': res.data.noncestr,
            'package': res.data.package,
            'signType': 'MD5',
            'paySign': res.data.sign,
            'success': function (res) {

            },
            complete:function(){
              that.data.payFlag = false;        
            }
          })
        }
      })
      that.data.payFlag = false;
    }
    
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

  }
})