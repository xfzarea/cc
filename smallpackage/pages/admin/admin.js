
Page({

  /**
   * 页面的初始数据
   */
  data: {
    userInfo:wx.getStorageSync("userInfo")
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    const that = this;
    //改版的
    if (!wx.getStorageSync("userInfo")) {
      wx.redirectTo({
        url: '/pages/index/index?page=admin'
      })
    } else {
      that.setData({
        userInfo: wx.getStorageSync("userInfo")
      })
    }
  },
  toRecord:function(){
    wx.navigateTo({
      url: '/pages/record/record',
    })
  },
  toComplain:function(e){
    wx.navigateTo({
      url: '/pages/complain/complain',
    })
  },
  toWallet:function(){
    wx.navigateTo({
      url: '/pages/wallet/wallet',
    })
  },
  toMoreQuestion:function(){
    wx.navigateTo({
      url: '/pages/moreQuestion/moreQuestion',
    })
  },
  toSearchBook:function(){
    wx.navigateTo({
      url: '/pages/searchBook/searchBook',
    })
  },
  /**
   * 营销服务
   */
  toMarketing: function () {
    wx.navigateTo({
      url: '/pages/marketing/marketing',
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
      path: "/pages/admin/admin",
      success: function (res) {
        
      },
      fail: function (res) {
        // 转发失败
      }
    }
  
  }
})