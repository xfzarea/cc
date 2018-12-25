// pages/diploma/diploma.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    userInfo:wx.getStorageSync("userInfo"),
    headPic:''
  },
 
  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    const that =  this;
    that.setData({
      userInfo: wx.getStorageSync("userInfo")
    })
    that.imageInfo(wx.getStorageSync("userInfo").avatarUrl);
  },

  /**
    * 头像缓存本地得方法
    */
  imageInfo: function (url) {    //  图片缓存本地的方法
    const that = this;
    wx.showLoading({
      title: 'tupian',
    })
    console.log(url)
      wx.getImageInfo({   //  小程序获取图片信息API
        src: "https://wx.qlogo.cn/mmopen/vi_32/Fovw346E6XwJ8E2qaMowrDnUJicTF99UgMChQxqXdOBpDgw8a8JSKe0zGibN1v76NfQR9u1vrD2rk1sP4FyeOCDQ/132",
        success:res=> {
          that.setData({
            headPic: res.path
          })
          wx.showModal({
            title: '123',
            content: res.path,
          })
        },
        fail:res=>{
          wx.showModal({
            title: '456',
            content: JSON.stringify(res)+""
          })

        },
        complete:res=>{
          // wx.showModal({
          //   title: '789',
          //   content: JSON.stringify(res)+""
          // })
        }
      })
  },

  drawPost: function () {
    const that = this;
    var rem;
    wx.getSystemInfo({
      success: function (res) {
        rem = res.screenWidth / 750;
      },
    })
    wx.showLoading({
      title: '1',
    })
    var headPic = that.data.headPic;
    const ctx = wx.createCanvasContext('myCanvas');
   
    ctx.drawImage("/images/28.png", 0, 0, 750 * rem, 673 * rem);
    ctx.drawImage(that.data.userInfo.avatarUrl, 76 * rem, 262 * rem, 90 * rem, 90 * rem);
    ctx.drawImage("/images/29.png", 210 * rem, 262 * rem, 90 * rem, 90 * rem);
    
    
    
    wx.showModal({
      title: '123',
      content: headPic,
    })
    ctx.drawImage(headPic, 76 * rem, 262 * rem, 90 * rem, 90 * rem);
    ctx.setFillStyle('#303030', 'STSongti-SC-Regular'); // 文字颜色：黑色
    ctx.setFontSize(20 * rem);     // 文字字号：22px
    ctx.fillText("由于木", 530 * rem, 166 * rem);
    var gender = that.data.userInfo.gender;
    if (gender == 1) {
      ctx.fillText("男", 530 * rem, 207 * rem)
    } else {
      ctx.fillText("女", 530 * rem, 207 * rem)
    }
    ctx.fillText("2018年09月09日", 530 * rem, 248 * rem)
    ctx.fillText("96分", 530 * rem, 286 * rem)
    ctx.fillText("一级甲等", 530 * rem, 326 * rem)
    var num = (Math.ceil(Math.random() * 8) + 1) + "" + Math.ceil(Math.random() * 10) + Math.ceil(Math.random() * 10) + Math.ceil(Math.random() * 10) + Math.ceil(Math.random() * 10) + Math.ceil(Math.random() * 10) + Math.ceil(Math.random() * 10) + Math.ceil(Math.random() * 10) + Math.ceil(Math.random() * 10);
    ctx.fillText(num, 530 * rem, 410 * rem)
    ctx.fillText("2018 年 09 月 09 日", 526 * rem, 550 * rem)
    ctx.draw(false, () => {
      that.downImg();
    });
  },
  downImg: function () {
    const that = this;
    wx.hideLoading();
    wx.showLoading({
      title: 'downImg',
    })
    wx.canvasToTempFilePath({
      x: 0,
      y: 0,
      canvasId: 'myCanvas',
      success: function (res) {
        that.setData({
          file: res.tempFilePath
        })
        that.preview();
      }
    })
    
    // }, 100))
  },
  preview:function(){
    const that = this;
    var file = that.data.file;
    console.log("3",file)
    wx.previewImage({
      urls:[file]
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