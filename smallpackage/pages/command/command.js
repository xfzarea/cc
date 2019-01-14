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
    textCommand:false,
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function(options) {
    const that = this;
    that.setData({
      userInfo: wx.getStorageSync("userInfo"),
      textCommand: wx.getStorageSync("textCommand")
    })
    setTimeout(function(){
      that.setData({
        changeId: 1,
      })
      that.getLeftInfo();
      that.getRightInfo(that.data.changeId);
    },100)
  },
  /**
   * 获得口令例子左边菜单
   */
  getLeftInfo: function() {
    const that = this;
    wx.request({
      url: urls.profit + '/getCommand',
      data: {
        id: 0
      },
      success: res => {
        that.setData({
          leftInfos: res.data.obj.command
        })
      }
    })
  },
  getRightInfo: function(id) {
    const that = this;
    wx.request({
      url: urls.profit + '/getCommand',
      data: {
        id: id
      },
      success: res => {
        that.setData({
          rightInfos: res.data.obj.command
        })
      }
    })
  },
  /**
   * 得到我的口令
   */
  getMyCommand: function() {
    const that = this;
    wx.request({
      url: urls.profit + '/getUserCommand',
      data: {
        userId: that.data.userInfo.userId
      },
      success: res => {
        that.setData({
          rightInfos: res.data.obj.commands
        })
      }
    })
  },
  /**
   * 点击菜单
   */
  changeOrderId: function(e) {
    const that = this;
    var id = e.currentTarget.dataset.contextid;
    if (id == "-1") {
      that.getMyCommand();
    } else {
      that.getRightInfo(id);
    }
    that.setData({
      changeId: id
    })
  },
  toCustomCommand: function(e) {
    const that = this;
    wx.navigateTo({
      url: '/pages/customCommand/sustomCommand'
    })
  },
  toCustomCommand1: function(e) {
    const that = this;
    wx.showModal({
      title: '提示',
      content: '你可以向我们提交你的创意口令，被选中后，会显示在“我的口令”，并可使用',
      showCancel: false,
      confirmText: "知道了",
      success: res => {
        if (res.confirm) {
          wx.navigateTo({
            url: '/pages/customCommand/sustomCommand'
          })
        }
      }
    })
  },
  toHome: function(e) {
    const that = this;
    console.log(that.data.textCommand)
    
    if(that.data.textCommand){
      wx.setStorageSync("textCommand",false);
      const that = this;
      app.globalData.context = e.currentTarget.dataset.context;
      wx.reLaunch({
        url: '/pages/beg/beg',
      })
    }else{
    const that = this;
    app.globalData.context = e.currentTarget.dataset.context;
    wx.reLaunch({
      url: '/pages/home/home',
    })
    }
  },
  /**
   * 跳口令库
   */
  toPasswd: function() {
    wx.navigateTo({
      url: '/pages/passwd/passwd',
    })
  },
  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady: function() {

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow: function() {
    const that = this;
    that.setData({
      changeId: -1
    })
    that.getMyCommand();
  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide: function() {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload: function() {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh: function() {

  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom: function() {

  },


})