// pages/voicePlay/voicePlay.js
const innerAudioContext = wx.createInnerAudioContext()
Page({

  /**
   * 页面的初始数据
   */
  data: {
    state:true,
    voice:'',
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    const that = this;
    let url = options.url;
    that.setData({
      voice:url
    })
    that.voiceToPlay();
  },
  changePlay:function(){
    const that = this;
    that.setData({
      state:!that.data.state
    })
    if(that.data.state){
      innerAudioContext.play();
    }else{
      innerAudioContext.pause();
    }
  },
  /**
   * 开始播放
   */
  voiceToPlay:function(){
    const that = this;
    innerAudioContext.autoplay = true;
    innerAudioContext.loop = true;
    innerAudioContext.src = that.data.voice + "?id=" + Math.ceil(Math.random() * 100);
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