const urls = require("../../utils/urls.js");
const app = getApp();
Page({

  /**
   * 页面的初始数据
   */
  data: {
    userInfo: wx.getStorageSync("userInfo"),
    voices:[]
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    const that = this;
    that.getVoices();
  },
  toBeg:function(e){
    const that = this;
    let src = e.currentTarget.dataset.src;
    let context = e.currentTarget.dataset.context;
    let second = e.currentTarget.dataset.second;
    let jBegInfo = { begType: 3, begInfo: src, second: second};
    app.globalData.jBegInfo = jBegInfo;
    wx.reLaunch({
      url: '/pages/beg/beg',
    })
  },
  getVoices:function(){
    const that = this;
    wx.request({
      url: urls.profit +'/getVoiceCommand',
      data:{
        id:0,
      },
      success:res=>{
        that.setData({
          voices: res.data.obj.voiceCommand
        })
      }
    })
  },

  toVoicePlay1:function(){
    wx.navigateTo({
      url: '/pages/voicePlay/voicePlay1',
    })
  },

  toVoicePlay:function(e){
    const that = this;
    let index = e.currentTarget.dataset.index;
    let voices = that.data.voices;
    let url = voices[index].voiceCommandPath;
    wx.navigateTo({
      url: `/pages/voicePlay/voicePlay?url=${url}`,
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