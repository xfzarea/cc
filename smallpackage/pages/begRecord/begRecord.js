// pages/record/record.js
const urls = require("../../utils/urls.js");
Page({

  /**
   * 
   * 页面的初始数据
   */
  data: {
    userInfo: wx.getStorageSync("userInfo"),
    jobs: [],
    id: 0,
    fresh: true,
    tabId: 0,//用来区分是我发的还是我强的
    totalAward: 0.00,
    totalCount: 0
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    const that = this;
    that.setData({
      userInfo: wx.getStorageSync("userInfo")
    })
    that.getData(that.data.tabId);//函数是自己的在下面,得到数据
    that.getTotal(that.data.tabId);//得到总数
  },
  /**
   * 保存formid
   */
  saveFormid: function (formid) {
    const that = this;
    wx.request({
      url: urls.profit + '/saveFormid',
      data: {
        userId: that.data.userInfo.userId,
        formid: formid
      },
      success: res => {

      }
    })
  },
  toBegPackage:function(e){
    let jobId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/begPackage/begPackage?id=${jobId}`,
    })
  },
  /**
   * 获得我讨得红包
   */
  getData: function (tabId) {
    const that = this;
    if (that.data.fresh) {
      that.data.fresh = false;
      wx.request({
        url: urls.profit + '/getBegRecordData',
        data: {
          userId: that.data.userInfo.userId,
          id: that.data.id,
          tabId: tabId
        },
        success: res => {
          var jobs = that.data.jobs;
          if (res.data.obj.jobs.length != 0) {
            jobs = jobs.concat(res.data.obj.jobs);
            var id;
            if (tabId == 0) {
              id = jobs[jobs.length - 1].id;
            } else {
              id = jobs[jobs.length - 1].id;
            }
            that.setData({
              jobs: jobs,
              id: id
            })
          }
          that.data.fresh = true;
        }
      })
    }
  },

  /**
   * 获得总数
   */
  getTotal: function (tabId) {
    const that = this;
    var handType;
    if (tabId == 0) {
      handType = "mine"
    } else {
      handType = "join"
    }
    wx.request({
      url: urls.profit + '/getBegTotal',
      data: {
        userId: that.data.userInfo.userId,
        tabId:tabId
      },
      success: res => {
        that.setData({
          totalAward: res.data.obj.total.totalAward,
          totalCount: res.data.obj.total.totalCount
        })
      }
    })

  },
  changeTab: function (e) {
    const that = this;
    var tabId = e.currentTarget.dataset.tabid;
    if (tabId != that.data.tabId) {
      that.setData({
        tabId: tabId,
        id: 0,
        jobs: []
      })
      that.getTotal(that.data.tabId);
      that.getData(that.data.tabId);
    }
  },
  /**
   * 下拉刷新
   */
  freshJobs: function () {
    const that = this;
    that.getData(that.data.tabId);
  },
  toPackage: function (e) {
    const that = this;
    // that.saveFormid(e.detail.formId);
    var id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: '/pages/package/package?id=' + id,
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
      path: "/pages/home/home?page=record",
      success: function (res) {

      },
      fail: function (res) {
        // 转发失败
      }
    }
  }
})