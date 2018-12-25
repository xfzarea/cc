const urls = require("../../utils/urls.js");
Page({

  /**
   * 页面的初始数据
   */
  data: {
    page:1,
    userInfo:wx.getStorageSync("userInfo"),
    ifFresh: true,
    lists: []
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    const that = this;
    that.setData({
      userInfo: wx.getStorageSync("userInfo")
    })
  },
  /**
   * 获得交易记录
   */
  getBalanList: function () {
    const that = this;
    console.log("刷新")
    if (that.data.ifFresh) {
      that.data.ifFresh = false;
      wx.request({
        url: urls.profit + '/getDetail',
        data: {
          userId: that.data.userInfo.userId,
          page:that.data.page
        },
        success: res => {
          var lists = that.data.lists.concat(res.data.obj.lists);
          console.log(lists)
          if (lists.length > 0) {
            that.setData({
              lists: lists,
              page: ++that.data.page
            })
            that.data.ifFresh = true;
          }
        }
      })
    }
  },
  /**
   * 去任务详情页
   */
  toJobInfo: function (e) {
    const that = this;
    var jobId = e.currentTarget.dataset.jobid;
    wx.navigateTo({
      url: '/pages/jobInfo/jobInfo?id=' + jobId,
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
    that.setData({
      uid: 0,
      lists: []
    })
    that.getBalanList();
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
    console.log("分享")
    const that = this;
    return {
      path: "/pages/home/home?page=detail",
      success: function (res) {
        
      },
      fail: function (res) {
        // 转发失败
      }
    }

  }
 
})