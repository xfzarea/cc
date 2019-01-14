// pages/cash/cash.js
const urls = require("../../utils/urls.js");
Page({

  /**
   * 页面的初始数据
   */
  data: {
    userInfo: '',
    inputMoney: '',
    ifCommit: false,
    money:0.00,
    show:false
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {

  },
  /**
  * 得到用户数据
  */
  getUserById: function () {
    const that = this;
    wx.request({
      url: urls.profit + '/getUserById',
      data: {
        userId: wx.getStorageSync("userInfo").userId,
      },
      success: res => {
        that.setData({
          userInfo: res.data.obj.userInfo,
          money:res.data.obj.userInfo.money
        })
      }
    })
  },
  /**
   * 输入金额
   */
  inputMoney: function (e) {
    const that = this;
    var data = e.detail.value;
    var award = that.data.inputMoney;
    if (/^\d+\.?\d{0,2}$/.test(data)) {
      award = data;
    } else {
      award = data.substring(0, data.length - 1);
    }
    that.setData({
      inputMoney: award
    })
    if (that.data.userInfo.money >= that.data.inputMoney && that.data.inputMoney >=0.3) {
      that.setData({
        ifCommit: true,
        show:false
      })
    } else if (that.data.userInfo.money < that.data.inputMoney){
      that.setData({
        show:true,
        ifCommit: false
      })
    }else {
      that.setData({
        ifCommit: false,
        show:false
      })
    }
  },
  /**
   * 输入完成
   */
  inputOver: function (e) {
    const that = this;
    if (e.currentTarget.dataset.type == 1) {
      that.setData({
        inputMoney: that.data.userInfo.money.toFixed(2)
      })
    }
    if (that.data.userInfo.money >= that.data.inputMoney && that.data.inputMoney >= 0.3) {
      that.setData({
        ifCommit: true,
        show: false
      })
    } else if (that.data.userInfo.money < that.data.inputMoney) {
      that.setData({
        show: true,
        ifCommit:false
      }) 
    }else {
      that.setData({
        ifCommit: false,
        show: false
      })
    }
  },
  toMoreQuestion:function(){
    wx.navigateTo({
      url: '/pages/moreQuestion/moreQuestion',
    })
  },
  /**
   * 提交 提现操作
   */
  commit: function () {
    const that = this;
    setTimeout(function () {
      if (that.data.ifCommit) {
        wx.showModal({
          title: '确认提现？',
          content: '提现不收取任何手续费',
          success: res => {
            if (res.confirm) {
              if (that.data.ifCommit) {
             
                wx.request({
                  url: urls.profit + '/cashHand',
                  data: {
                    userId: wx.getStorageSync("userInfo").userId,
                    money: that.data.inputMoney,
                    openid: wx.getStorageSync("userInfo").openid,
                  },
                  
                  success: res => {
                    
                    that.getUserById();
                    that.setData({
                      inputMoney: '',
                      ifCommit:false
                    })
                    if (res.data.obj.state == 0) {
                      wx.showModal({
                        title: '提现申请成功',
                        content: '1~5个工作日到账',
                      })
                    } else if (res.data.obj.state == 1) {
                      wx.showModal({
                        title: '账号异常，请联系平台核实',
                        content: 'tel:18964569529',
                      })
                    } else if (res.data.obj.state == 2) {
                      wx.showModal({
                        title: '提现金额有误，冻结提现功能，请联系平台核实',
                        content: 'tel:18964569529',
                      })
                    } else if (res.data.obj.state == 3){
                      wx.showModal({
                        title: '提现出现问题',
                        content: '请稍后再试',
                      })
                    }

                  }
                })
              }
            }
          }
        })
      }
    }, 200)
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