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
  toCommand: function () {
    wx.setStorageSync("textCommand",true);
    wx.navigateTo({
      url: '/pages/command/command',
    })
  },
  /**
 * 语音
 */
  toCommandImage: function () {
    wx.setStorageSync("commandImaged", true);
    wx.navigateTo({
      url: '/pages/changes/changes',
    })
  },
  /**
 * 图片
 */
  toVoiceCommand: function () {
    wx.setStorageSync("voiceCommand", true);
    wx.navigateTo({
      url: '/pages/changes/changes',
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