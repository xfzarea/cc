const urls = require("../../utils/urls.js");
const innerAudioContext = wx.createInnerAudioContext();
const recorderManager = wx.getRecorderManager();
const app = getApp();
Page({

  /**
   * 页面的初始数据
   */
  data: {
    state: false,
    voice: '',
    second:0,
    time:"00:00:00",
    timeOut:'',
    
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    const that = this;
    // let url = options.url;
    // that.setData({
    //   voice: url
    // })
    // that.voiceToPlay();
    that.checkAudio();
  },
  changePlay: function () {
    const that = this;
    that.setData({
      state: !that.data.state
    })
    if (that.data.state) {
      innerAudioContext.play();
    } else {
      innerAudioContext.pause();
    }
  },
  toBeg:function(){
    const that = this;
    let src = that.data.voice;
    let second = that.data.second;
    let jBegInfo = { begType: 3, begInfo: src, second: second};
    app.globalData.jBegInfo = jBegInfo;
    console.log(jBegInfo)
    wx.reLaunch({
      url: '/pages/beg/beg',
    })
  },
  /**
   * 判断录音功能
   */
  checkAudio: function () {
    wx.authorize({
      scope: 'scope.record',
      success: res => {

      },
      error: res => {
        console.log("取消")
        wx.openSetting({
          success: res => {
            console.log(res)
          }
        })
      },
      complete: res => {
        if (res.errMsg == "authorize:ok") {

        } else {
          wx.showModal({
            title: '警告',
            content: '您点击了拒绝授权，将无法使用录音功能，请授权之后再进入!!!',
            showCancel: false,
            confirmText: '返回授权',
            success: function (res) {
              if (res.confirm) {
                wx.openSetting({
                  success: res => {
                    console.log(res)
                  }
                })
              }
            }
          })
        }
      }
    })
  },
  toSay: function () {
    const that = this;
    that.setData({
      state:true,
      second: 0,
    })
    const options = {
      sampleRate: 16000,//采样率
      numberOfChannels: 1,//录音通道数
      encodeBitRate: 96000,//编码码率
      format: 'mp3',//音频格式，有效值 aac/mp3
      frameSize: 50,//指定帧大小，单位 KB
    }
    recorderManager.start(options);
    recorderManager.onStart(() => {
      console.log("开始录音")
      clearInterval(that.data.timeOut);
      that.data.timeOut = setInterval(function () {
        console.log("第一次")
        let second = that.data.second;
        let time = that.data.time;
        second = second+1;
        if(second<10){
          time = "00:00:0"+second;
        }else{
          time = "00:00:"+second;
        }
        that.setData({
          second: second,
          time:time
        });
        that.sayOver();
      }, 1000)
    });

    recorderManager.onStop((res) => {
      const that = this;
      clearInterval(that.data.timeOut);
      var timeLimit = that.data.second;
      const tempFilePath = res.tempFilePath;
      console.log("间隔时间：", timeLimit);
      if (timeLimit >= 1) { //1秒以上进行匹配
        wx.showLoading({
          title: '语音生成中~~',
        })
        wx.uploadFile({
          url: urls.profit + '/app/doImageLoad',
          filePath: tempFilePath,
          name: 'image',
          success: res => {
            wx.hideLoading();
            that.setData({
              voice: res.data,
              state:false,
              
            })
            that.toBeg();
          }
        })
      }
    })
  },

  /**
   * 说完30秒
   */
  sayOver: function () {
    const that = this;
    if (that.data.second >= 30) {
      recorderManager.stop()
      that.setData({
        state: false
      })
      clearInterval(that.data.timeOut);
    }
  },
  sayEnd: function () {
    const that = this;
    that.setData({
      begin: false,
    })
    setTimeout(function () { //延迟关闭录音
      console.log("关闭录音");
      console.log(that.data.second)
      recorderManager.stop();
      clearInterval(that.data.timeOut);
    }, 500)
  },
  /**
   * 开始播放
   */
  voiceToPlay: function () {
    const that = this;
    innerAudioContext.autoplay = true;
    innerAudioContext.loop = true;
    innerAudioContext.src = that.data.voice + "?id=" + Math.ceil(Math.random() * 100);
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
    innerAudioContext.stop();
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