const urls = require("../../utils/urls.js");
Page({

  /**
   * 页面的初始数据
   */
  data: {
    userInfo:wx.getStorageSync("userInfo"),
    fresh:true,
    jobs:[],
    id:0,
    apps:[]
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    const that = this;
    if (!wx.getStorageSync("userInfo")) {
      wx.redirectTo({
        url: '/pages/index/index?page=timer'
      })
    } else {
      that.setData({
        userInfo: wx.getStorageSync("userInfo")
      })
      that.getTimerJob();
    }
  },
  /**
   * 获得定时红包
   */
  getTimerJob:function(){
    const that = this;
    if (that.data.fresh) {
      that.data.fresh = false;
      wx.request({
        url: urls.profit + '/getTimerJob',
        data: {
          id: that.data.id,
          userId:that.data.userInfo.userId
        },
        success: res => {
          var jobs = that.data.jobs;
          if (res.data.obj.jobs!=null&&res.data.obj.jobs.length != 0) {
            jobs = jobs.concat(res.data.obj.jobs);
            that.setData({
              jobs: jobs,
              id: jobs[jobs.length - 1].id,
              apps:res.data.obj.apps
            })
            console.log("jobs",jobs)
          }
          that.data.fresh = true;
        }
      })
    }
  },

  /**
     * 打开小程序
     */
  openWx: function (e) {
    var appid = e.currentTarget.dataset.skipappid;
    wx.navigateToMiniProgram({
      appId: appid,
      success(res) {
        // 打开成功
      }
    })
  },
  /**
   * 下拉刷新
   */
  freshJobs: function () {
    const that = this;
    that.getTimerJob();
  },

  toPackage:function(e){
    const that = this;
    var id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: '/pages/package/package?id='+id,
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
    console.log("分享")
    const that = this;
    return {
      path: "/pages/timer/timer",
      success: function (res) {

      },
      fail: function (res) {
        // 转发失败
      }
    }
  }
})