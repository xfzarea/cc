// pages/marketing/marketing.js
const urls = require("../../utils/urls.js")
Page({

  /**
   * 页面的初始数据
   */
  data: {
    name:'',
    tel:''
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {

  },
  input:function(e){
    const that = this;
    let t = e.currentTarget.dataset.t;
    if(t=="name"){
      that.setData({
        name:e.detail.value
      })
    }else{
      that.setData({
        tel:e.detail.value
      })
    }
  },
  submit:function(){
    const that = this;
    let name = that.data.name;
    let tel = that.data.tel;
    let flag = true;
    if (tel.length == 11 && name != '' && (/^1[34578]\d{9}$/.test(tel))) {
        console.log("成功")
        wx.request({
          url: urls.profit +'/vipReply',
          data:{
            name:name,
            tel:tel
          },
          success:res=>{
            
            wx.showModal({
              title: '提示',
              content: '提交成功，稍后商务人员会和您联系',
              showCancel: false,
              confirmText: '知道了',
              success: function (res) {

              }
            })
            that.setData({
              name:'',
              tel:''
            })
          }
        })
    }else if(tel!=''&&name!=''){
      wx.showModal({
        title: '提示',
        content: '填写信息有误，请检查后重新申请!!!',
        showCancel: false,
        confirmText: '知道了',
        success: function (res) {
        }
      })
    }
  },
  checkSubmit: function () {
    const that = this;
    let name = that.data.name;
    let tel = that.data.tel;
    let flag = false;
    if (tel.length == 11&&name!='') {
      if ((/^1[34578]\d{9}$/.test(phone))) {
        flag = true;
      } 
    }
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