const urls = require("../../utils/urls.js");
const app = getApp();
const i = 0;
Page({

  /**
   * 页面的初始数据
   */
  data: {
    leftInfos: [],
    rightInfos: [],
    changeId: 1,
    userInfo: wx.getStorageSync("userInfo"),
    myCommand: [],
    textCommand: false,

  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    const that = this;
    that.setData({
      userInfo: wx.getStorageSync("userInfo"),
      textCommand: wx.getStorageSync("textCommand")
    })
    
    that.getLeftInfo();
    
    
  },
  /**
   * 获得口令例子左边菜单
   */
  getLeftInfo: function () {
    const that = this;
    wx.request({
      url: urls.profit + '/getBegCommand',
      data: {
        id: 0
      },
      success: res => {
        console.log(res.data)
        that.setData({
          leftInfos: res.data.obj.begCommand,
          changeId: res.data.obj.begCommand[0].id,
        })
        that.getRightInfo(that.data.changeId);
      }
    })
  },

  getRightInfo: function (id) {
    const that = this;
    wx.request({
      url: urls.profit + '/getBegCommand',
      data: {
        id: id
      },
      success: res => {
        that.setData({
          rightInfos: res.data.obj.begCommand
        })
      }
    })
  },
  
  warnNoOpen: function () {
    const that = this;
    wx.showToast({
      title: '功能暂未开放，敬请期待',
      icon: 'none'
    })
  },
  /**
   * 点击菜单
   */
  changeOrderId: function (e) {
    const that = this;
    var id = e.currentTarget.dataset.contextid;
    
    that.getRightInfo(id);
    
    that.setData({
      changeId: id
    })
  },
  toCustomCommand: function (e) {
    const that = this;
    wx.navigateTo({
      url: '/pages/customCommand/sustomCommand'
    })
  },
  toCustomCommand1: function (e) {
    const that = this;
    wx.navigateTo({
      url: '/pages/customCommand/sustomCommand1?type=1',
    })
  },
  toBeg: function (e) {
    const that = this;
    let context = e.currentTarget.dataset.context;
    let begInfo = {begType:1,begInfo:context}
    app.globalData.jBegInfo = begInfo;
    wx.reLaunch({
      url: '/pages/beg/beg',
    })
    
  },
  /**
   * 跳口令库
   */
  toPasswd: function () {
    wx.navigateTo({
      url: '/pages/passwd/passwd',
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