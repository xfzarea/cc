const urls = require("../../utils/urls.js");
Page({

  /**
   * 页面的初始数据
   */
  data: {
    userInfo1: wx.getStorageSync("userInfo"),
    userInfo:'',
    money:0.00
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    const that = this;
    that.setData({
      userInfo1: wx.getStorageSync("userInfo")
    })
  },
  /**
   * 提现
   */
  toCash: function () {
    wx.navigateTo({
      url: '/pages/cash/cash',
    })
  },
  toHome:function(){
    wx.reLaunch({
      url: '/pages/home/home',
    })
  },
  toDetail:function(){
    wx.navigateTo({
      url: '/pages/detail/detail',
    })
  },
  /**
   * 得到用户数据
   */
  getUserById: function () {
    const that = this;
    wx.request({
      url: urls.profit + '/getUserById',
      data: {
        userId: that.data.userInfo1.userId,
      },
      success: res => {
        that.setData({
          userInfo: res.data.obj.userInfo,
          money:res.data.obj.userInfo.money
        })
      }
    })
  },
  toMoreQuestion:function(){
    wx.navigateTo({
      url: '/pages/moreQuestion/moreQuestion',
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
    const that = this;
    that.getUserById();
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