const urls = require("../../utils/urls.js");
Page({

  /**
   * 页面的初始数据
   */
  data: {
    questions: [],
    userInfo: wx.getStorageSync("userInfo")
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    const that = this;
    that.getData();
    that.setData({
      userInfo: wx.getStorageSync("userInfo")
    })
  },
  toShow: function (e) {
    const that = this;
    var index = e.currentTarget.dataset.index;
    var questions = that.data.questions;
    if (questions[index].isShow == 0) {
      questions[index].isShow = 1
    } else {
      questions[index].isShow = 0
    }
    that.setData({
      questions: questions
      
    })
    
  },
  /**
   * 得到数据
   */
  getData: function () {
    const that = this;
    wx.request({
      url: urls.profit + '/toMoreQuestion',
      success: res => {
        that.setData({
          questions: res.data.obj.questions
        })
      }
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
})