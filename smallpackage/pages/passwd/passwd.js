// pages/passwd/passwd.js
const urls = require("../../utils/urls.js");
const app = getApp();
Page({

  /**
   * 页面的初始数据
   */
  data: {
    commands:[],
    text:''
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {

  },
  /**
   * 搜索口令
   */
  toSearchCommand:function(){
    const that = this;
    wx.request({
      url: urls.profit + '/userSearchCommand',
      data:{
        text:that.data.text
      },
      success:res=>{
        that.setData({
          commands: res.data.obj.commands
        })
      }
    })
  },
  inputText:function(e){
    const that = this;
    that.setData({
      text:e.detail.value
    })
  },
  toHome: function (e) {
    const that = this;
    app.globalData.context = e.currentTarget.dataset.context;
    wx.reLaunch({
      url: '/pages/home/home',
    })
  },
  toCustomCommand:function(){
    const that = this;
    wx.navigateTo({
      url: '/pages/customCommand/sustomCommand',
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