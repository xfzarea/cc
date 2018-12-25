const app = getApp();
const urls = require("../../utils/urls.js");
Page({

  /**
   * 页面的初始数据
   */
  data: {
    context: '',
    over: false,
    count: 0,
    userInfo: {},
    jobContexts: wx.getStorageSync("jobContexts").slice(0, 3),
    code: 0,
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function(options) {
    const that = this;
    that.setData({
      userInfo: wx.getStorageSync("userInfo")
    })
  },
  input: function(e) {
    const that = this;
    var count = e.detail.cursor;
    var context = e.detail.value;
    var over;
    if (count == 0) {
      over = false;
    } else {
      var str = context.substring(context.length - 1, context.length);
      if (/^[0-9]*$/.test(str)) {
        context = context.substring(0, context.length - 1);
        count--;
        wx.showModal({
          title: '提示',
          content: '只能输入中文或部分标点符号',
        })
      }
      if (count == 0) {
        over = false;
      } else {
        over = true;
      }
    }
    that.setData({
      context: context,
      count: count,
      over: over
    })
  },
  cancel: function() {
    const that = this;
    that.setData({
      context: '',
      over: false,
      count: 0
    })
  },
  submit: function() {
    const that = this;
    that.setData({
      code: 1
    })
    setTimeout(function() {
      that.setData({
        code:0
      })
      wx.request({
        url: urls.profit + '/checkWord',
        data: {
          content: that.data.context,
        },
        success: res => {
          if (res.data.obj.flag == true) {
            wx.showModal({
              title: '检测结果',
              content: '口令中包含敏感词汇,请修改后重新创建',
            })
          } else {
            that.saveUserCommand(that.data.context);
          }
        }
      })
    }, 8000)
  },
  /**
   * 保存用户自行发文
   */
  saveUserCommand: function(command) {
    const that = this;
    wx.request({
      url: urls.profit + '/saveUserCommand',
      data: {
        command: command,
        userId: that.data.userInfo.userId
      },
      success: res => {
        wx.showModal({
          title: '提示',
          content: '恭喜！你提交得口令已被我们收录，可进入“我的口令”查看，已经可以使用。非常感谢！',
          showCancel: false,
          confirmText: '知道了',
          success: res => {
            wx.navigateBack({
              delta: 1
            })
            // app.globalData.context = that.data.context
            // wx.reLaunch({
            //   url: '/pages/home/home',
            // })
          }
        })
      }
    })
  },
  /**
   * 清空历史记录
   */
  clearHistory: function() {
    const that = this;
    wx.setStorage({
      key: 'jobContexts',
      data: [],
    })
    that.setData({
      jobContexts: []
    })
  },
  /**
   * 快速创建
   */
  click: function(e) {
    console.log(e)
    const that = this;
    var context = e.currentTarget.dataset.context;
    var count = context.length;
    that.setData({
      context: context,
      count: count,
      over: true
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
    wx.getStorage({
      key: 'jobContexts',
      success: function(res) {
        console.log(res)
        if (res.data) {
          that.setData({
            jobContexts: res.data.slice(0, 3)
          })
        }
      },
    })
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