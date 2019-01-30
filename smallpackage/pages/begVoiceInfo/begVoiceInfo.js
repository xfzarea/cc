const urls = require("../../utils/urls.js");
const app = getApp();
const innerAudioContext = wx.createInnerAudioContext();
Page({

  /**
   * 页面的初始数据
   */
  data: {
    userInfo: wx.getStorageSync("userInfo"),
    voices:[],
    select_id:0,
    isPlay:true
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    const that = this;
    that.getVoices();
    that.checkUserH("userCommandVoice");
  },

  /**
  * 是否开启自定义窗口
  */
  checkUserH: function (customName) {
    const that = this;
    wx.request({
      url: urls.profit + '/CheckCustom',
      data: {
        customName: customName
      },
      success: res => {
        console.log(res.data)
        that.setData({
          userH: res.data.obj.state
          // userH: 1
        })
      }
    })
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
  /**
   * 播放语音
   */
  voicePlay:function(e){
    const that = this;
    let index = e.currentTarget.dataset.index;
    let voices = that.data.voices;
    let id = voices[index].id;
    let src = voices[index].voiceCommandPath;
    let select_id = that.data.select_id;
    if(select_id != id){
      that.setData({
        select_id:id,
        isPlay:true
      })
      that.voiceToPlay(src)
    }
  },
  /**
   * 语音暂停与否
   */
  doIsPlay:function(){
    const that = this;
    let isPlay = that.data.isPlay;
    if(isPlay){
      that.setData({
        isPlay:false
      })
      innerAudioContext.pause();
    }else{
      that.setData({
        isPlay:true
      })
      innerAudioContext.play();
    }

  },

  /**
   * 开始播放
   */
  voiceToPlay: function (src) {
    const that = this;
    innerAudioContext.autoplay = true;
    innerAudioContext.loop = false;
    innerAudioContext.src = src + "?id=" + Math.ceil(Math.random() * 100);

    innerAudioContext.onEnded(res => {
      const that = this;
      console.log(123)
      that.setData({
        select_id: 0
      })
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
    innerAudioContext.stop();
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