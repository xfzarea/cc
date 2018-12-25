// pages/complain/complain.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    content: [{"context":"欺诈","check":false},
      { "context": "色情", "check": false },
      { "context": "政治谣言", "check": false },
      { "context": "常识性谣言", "check": false },
      { "context": "诱导分享", "check": false },
      { "context": "恶意营销", "check": false },
      { "context": "隐私信息收集", "check": false },
      { "context": "其他侵权类（冒名/诽谤/抄袭）", "check": false },],
      phone:'',
      commit:false,
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {

  },
  checkClick:function(e){
    const that = this;
    var index = e.currentTarget.dataset.index;
    var content = that.data.content;
    content[index].check = !content[index].check;
    that.setData({
      content:content
    })
    that.checkSubmit();
  },
  input:function(e){
    
    const that = this;
    that.setData({
      phone:e.detail.value
    })
    that.checkSubmit();
  },
  checkSubmit:function(){
    const that = this;
    var phone = that.data.phone;
    var content = that.data.content;
    var flag = false;
    for(let i = 0;i<8;i++){
      if(content[i].check){
        flag = true;
      }
    } 
    if(phone.length==11&&flag){
      if ((/^1[34578]\d{9}$/.test(phone))) {
        that.setData({
          commit:true
        })
      } else{
        that.setData({
          commit:false
        })
        consoe.log("失败：",phone)
      }
    }else{
      that.setData({
        commit:false
      })
    }
  },
  /**
   *  提交 
   */
  toCommit:function(){
    const that = this;
    wx.showToast({
      title: '提交成功',
    })
    that.setData({
      phone:'',
      content: [{ "context": "欺诈", "check": false },
        { "context": "色情", "check": false },
        { "context": "政治谣言", "check": false },
        { "context": "常识性谣言", "check": false },
        { "context": "诱导分享", "check": false },
        { "context": "恶意营销", "check": false },
        { "context": "隐私信息收集", "check": false },
        { "context": "其他侵权类（冒名/诽谤/抄袭）", "check": false }]
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