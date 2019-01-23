// pages/change/changes.js
Page({

  /**
   * 页面的初始数据
   */
  data: {

  },
  /**
 * 文字
 */
  toCommand1:function(){
    wx.navigateTo({
      url: '/pages/command/command1',
    })
  },
  /**
 * 图片
 */
  toBegPicInfo: function () {
    wx.navigateTo({
      url: '/pages/begPicInfo/begPicInfo',
    })
  },
  /**
 * 语音
 */
  toBegVoiceInfo: function () {
    wx.navigateTo({
      url: '/pages/begVoiceInfo/begVoiceInfo',
    })
  },
  /**
   * 视频
   */
  toBegVidoInfo:function(){
    wx.navigateTo({
      url: '/pages/begVideoInfo/begVideoInfo',
    })
  },
  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
  
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

})