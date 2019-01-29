const urls = require("../../utils/urls.js")
const innerAudioContext = wx.createInnerAudioContext()
const recorderManager = wx.getRecorderManager()
Page({

  /**
   * 页面的初始数据
   */
  data: {
    userInfo: wx.getStorageSync("userInfo"),
    context: "老龙恼怒闹老农老农恼怒闹老农怒龙恼农更怒老龙恼怒闹老农老农恼",
    begin: false,
    id: 0,
    job: '',
    second: 29,
    timeOut: '',
    fresh: true,
    voices: [],
    count: 0,
    showId: 0,
    doSay: true,
    code: 0,
    voice: '',
    headPic: '',
    timerShow: 2, //初始化
    time: '--时--分--秒后开始',
    IntervalTime: '',
    handType: 0,
    codeUrl: '',
    sharePic: '',
    warrantShow: false,
    warn_show: false,
    getCode: -1,
    show: false,
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function(options) {
    const that = this;
    var id = options.id;
    // id = 100110;
    var scene = decodeURIComponent(options.scene); //从二维码进来
    if (scene != undefined && scene != "undefined" && scene != null) {
      id = scene
    }
    console.log(scene)
    console.log("跳转jobId", id)
    that.data.id = id;
    that.getCode(id);
    if (options.handType != undefined && options.handType != "undefined") {
      that.setData({
        handType: options.handType
      })
    }
    
    //改版的
    if (!wx.getStorageSync("userInfo")) {
      that.setData({
        warrantShow: true
      })
    } else {
      wx.showLoading({
        title: '客官请稍等~',
      })
      that.setData({
        userInfo: wx.getStorageSync("userInfo")
      })
      if (id != undefined && id != "undefined") {
        that.setData({
          id: id
        })
        that.toPackage(id);
        that.getVoices(id, that.data.count);
        that.getImageInfo(that.data.userInfo.avatarUrl);
        that.checkAudio();
      }
    }
  },
  toHome:function(){
    wx.switchTab({
      url: '/pages/home/home',
    })
  },
  /**
   * 判断录音功能
   */
  checkAudio: function () {
    wx.authorize({
      scope: 'scope.record',
      success:res=>{
        
      },
      error:res=>{
        console.log("取消")
        wx.openSetting({
          success:res=>{
            console.log(res)
          }
        })
      },
      complete:res=>{
        if (res.errMsg == "authorize:ok"){

        }else{
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
  /**
   * 判断是否出来warn组件
   */
  checkWarnShow: function(e) {
    const that = this;
    let warnShow = false;
    if(e&&e.currentTarget.dataset.code){
      warnShow = true;
    }
    // var getCode = -1;
    // let voice = that.data.voice;
    // let job = that.data.job;
    // if (voice == null || voice.state == 1) { //未领过
    //   if (job.state == 2) {
    //     warnShow = true;
    //     getCode = 1; //没领红包但红包领完了
    //   } else if (job.state == 3) {
    //     warnShow = true;
    //     getCode = 2; //没领红包但红包过期了
    //   } else if (e&&e.currentTarget.dataset.code){
    //     warnShow = true;
    //     getCode = 3; //没领过红包正常状态
    //   }else if (job.state == 1 || job.state == 4) {
    //     warnShow = false;
    //     getCode = 3; //没领过红包正常状态
    //   }
    // } else { //领过了
    //   warnShow = true;
    //   getCode = 4; //领过了红包
    // }
    // if (that.data.handType == 1) {
    //   warnShow = false;
    // }
    that.setData({
      warn_show: warnShow,
      // getCode: getCode
    })

  },
  /**
   * 关闭warn组件
   */
  closeWarn: function(e) {
    const that = this;
    that.setData({
      warn_show: false
    })
  },
  /**
   * 组件内点击
   */
  getWarrant: function(e) {
    const that = this;
    that.setData({
      warrantShow: e.detail,
      userInfo: wx.getStorageSync("userInfo")
    })
    var id = that.data.id;
    if (id != undefined && id != "undefined") {
      that.setData({
        id: id
      })
      that.toPackage(id);
      that.getVoices(id, that.data.count);
      that.getImageInfo(that.data.userInfo.avatarUrl);
      that.checkAudio();
    }
  },
  /**
   * 打开小程序
   */
  openWx: function(e) {
    const that = this;
    var appid = e.currentTarget.dataset.skipappid;
    if (appid == 0) { //跳卡片
      that.setData({
        warn_show:true
      })
    } else if (appid == 1) {//跳福利
      wx.reLaunch({
        url: '/pages/timer/timer',
      })
    }else if(appid == 2){//预览图
      wx.previewImage({
        urls: [that.data.job.pre_image],
      })
    } else if(appid == 3){
      wx.navigateTo({
        url: '/pages/tweet/tweet?title='+that.data.job.tweet_title+"&tweet="+that.data.job.tweet,
      })
    }
    else {
      wx.navigateToMiniProgram({
        appId: appid,
        success(res) {
          // 打开成功
        }
      })
    }
  },
  /**
   * 获得二维码
   */
  getCode: function(id) {
    const that = this;
    wx.request({
      url: urls.profit + '/getCode',
      data: {
        jobId: id
      },
      success: res => {
        var url = res.data.obj.codeUrl;
        console.log("code:", url)
        if (typeof url === 'string') {
          wx.getImageInfo({ //  小程序获取图片信息API
            src: url,
            success: function(res) {
              that.setData({
                codeUrl: res.path
              })
            },
            fail: function(res) {
              console.log(res);
              wx.getImageInfo({
                src: url,
                success: function(res) {
                  that.setData({
                    url: url,
                    codeUrl: res.path
                  })
                }
              })
            }
          })
        }
      },
      fail: function(res) {
        console.log(res)
      }
    })
  },

  /**
   *  保存分享图
   */
  saveCanvas: function() {
    const that = this;
    wx.canvasToTempFilePath({
      x: 0,
      y: 0,
      canvasId: 'card_canvas',
      quality: 1,
      success: function(res) {
        wx.saveImageToPhotosAlbum({
          filePath: res.tempFilePath,
          success: function(res) {
            wx.showToast({
              title: '保存图片完成',
              success: function() {
                setTimeout(function() {
                  that.setData({
                    handType: 0,
                    code: 0
                  })
                }, 1000)
              }
            })
          }
        })
      }
    })
  },
  /**
   * 生成分享图按钮
   */
  createPic: function() {
    const that = this;
    that.setData({
      handType: 2
    })
    that.drawPostC();
  },
  /**
   * 画圆图
   */
  circleImg: function(ctx, img, x, y, r) {
    ctx.save();
    var d = 2 * r;
    var cx = x + r;
    var cy = y + r;
    // ctx.beginPath();
    ctx.arc(cx, cy, r, 0, 2 * Math.PI);
    ctx.clip();
    ctx.stroke("#fff");
    ctx.drawImage(img, x, y, d, d);
  },

  /**
   * canvas画分享图
   */
  drawPostC: function() {
    var that = this;

    var rem;
    wx.getSystemInfo({
      success: function(res) {
        rem = res.screenWidth / 750;
      },
    })

    const ctx = wx.createCanvasContext('card_canvas');

    ctx.drawImage("/images/41.png", 0, 0, 395 * rem, 533 * rem);

    ctx.setTextAlign('center'); // 文字居中
    ctx.setFillStyle('#f6e0b7'); // 文字颜色：黑色
    ctx.setFontSize(16 * rem); // 文字字号：22px
    var nickName;
    if (that.data.job.state != 4) {
      nickName = that.data.job.nickName;
    } else if (that.data.job.state == 4) {
      nickName = that.data.job.userName;
    }
    if (nickName.length > 6) {
      nickName = nickName.substring(0, 6) + "...";
    }
    ctx.fillText(nickName + "的语音红包", 395 * rem / 2, 120 * rem);

    ctx.setFontSize(28 * rem); // 文字字号：22px
    var context = that.data.job.context;
    if (context.length < 12) {
      ctx.fillText(context, 395 * rem / 2, 174 * rem);
    } else if (context.length < 21) {
      ctx.fillText(context.substring(0, 12), 395 * rem / 2, 174 * rem);
      ctx.fillText(context.substring(12, 21), 395 * rem / 2, 210 * rem);
    } else {
      ctx.fillText(context.substring(0, 12), 395 * rem / 2, 174 * rem);
      ctx.fillText(context.substring(12, 21) + "...", 395 * rem / 2, 210 * rem);
    }
    var codeUrl = that.data.codeUrl;
    console.log(codeUrl);
    if (codeUrl) {
      ctx.drawImage(codeUrl, 285 * rem / 2, 265 * rem, 110 * rem, 110 * rem);
    }
    that.circleImg(ctx, that.data.headPic, 337 * rem / 2, 26 * rem, 58 * rem / 2);
    ctx.draw();
  },
  /**
   * 倒计时
   */
  checkTime: function(insertTime) {
    const that = this;

    insertTime = insertTime.replace(/-/g, '/');
    var d1 = new Date().getTime();
    var d2 = new Date(insertTime);
    var totalSecond = parseInt(d2 - d1);

    var time = "";

    if (totalSecond <= 0) {
      that.setData({
        timerShow: 2 //时间到了 去掉倒计时
      })
      clearInterval(that.data.IntervalTime);
    } else {
      var second1 = (parseInt)(totalSecond / 1000);
      // var second1 = 10000;
      var second = second1 % 60;
      var minute = (second1 - second) / 60 % 60;
      var hour = (second1 - second - minute * 60) / (60 * 60) % 24;
      var day = (second1 - second - minute * 60 - 3600 * hour) / (60 * 60 * 24);
      if (day > 0) {
        time += day + "天";
      }
      if (hour > 9) {
        time += hour + "时";
      } else {
        time += "0" + hour + "时";
      }
      if (minute > 9) {
        time += minute + "分";
      } else {
        time += "0" + minute + "分";
      }
      if (second > 9) {
        time += second + "秒后开始";
      } else {
        time += "0" + second + "秒后开始";
      }
      that.setData({
        time: time
      })
    }

  },
  /**
   * 订阅
   */
  take: function(e) {
    const that = this;
    console.log(e)
    wx.request({
      url: urls.profit + '/toTake',
      data: {
        userId: that.data.userInfo.userId,
        jobId: that.data.job.id,
        formid: e.detail.formId
      },
      success: res => {
        console.log(res)
        wx.showToast({
          title: '订阅成功',
        })
      }
    })
  },
  changeHandType: function() {
    const that = this;
    that.setData({
      handType: 1
    })
  },
  /**
   * 获得单一得红包内容
   */
  toPackage: function(id) {
    const that = this;
    wx.request({
      url: urls.profit + '/toPackage',
      data: {
        id: id,
        userId: that.data.userInfo.userId
      },
      success: res => {
        wx.hideLoading();
        var doSay = that.data.doSay;
        console.log("topackage数据：", res.data.obj)
        if (res.data.obj.voice != null) {
          doSay = false;
        }
        that.setData({
          job: res.data.obj.job,
          doSay: doSay,
          voice: res.data.obj.voice,
        })
        if (res.data.obj.job.shareUrl == null) {
          const job = res.data.obj.job;
          var url = job.avatarUrl + "";
          if (job.state == 4) {
            url = job.headPic + ""
          }
          if (typeof url == "string") {
            wx.getImageInfo({
              src: url,
              success: res => {
                that.drawShare(job, res.path);
              }
            })
          }
        }
        if (that.data.job.state == 4) {
          that.setData({
            timerShow: 1 //代表开启倒计时
          })
          that.checkTime(that.data.job.createTime)

          IntervalTime: setInterval(function() {
            that.checkTime(that.data.job.createTime)
          }, 1000)
        }
        // that.checkWarnShow();
      }
    })
  },
  /**
   * 获得语音
   */
  getVoices: function(id, count) {
    const that = this;
    if (that.data.fresh) {
      that.data.fresh = false;
      wx.request({
        url: urls.profit + '/packageGetVoice',
        data: {
          jobId: id,
          id: count,
          userId: that.data.userInfo.userId
        },
        success: res => {
          console.log(res)
          var voices = that.data.voices;

          if (res.data.obj.voices.length != 0) {
            voices = voices.concat(res.data.obj.voices);
            that.setData({
              voices: voices,
              count: voices[voices.length - 1].id,
              fresh: true
            })
          }
        }
      })
    }
  },
  /**
   * 下拉刷新
   */
  freshJobs: function() {
    const that = this;
    that.getVoices(that.data.id, that.data.count);
  },
  /**
   * 隐藏 卡片
   */
  hide: function() {
    const that = this;
    that.setData({
      code: 0,
      handType: 0
    })
  },

  toSay: function() {
    const that = this;
    // wx.authorize({
    //   scope: 'scope.record',
      // success: res => {
        // const options = {
        //   sampleRate: 16000,//采样率
        //   numberOfChannels: 1,//录音通道数
        //   encodeBitRate: 96000,//编码码率
        //   format: 'mp3',//音频格式，有效值 aac/mp3
        //   frameSize: 50,//指定帧大小，单位 KB
        // }
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
          that.data.timeOut = setInterval(function() {
            console.log("第一次")
            that.setData({
              second: that.data.second - 1
            });
            that.sayOver();
          }, 1000)
        });

        recorderManager.onStop((res) => {
          const that = this;
          clearInterval(that.data.timeOut);
          var timeLimit = 29 - that.data.second;
          const tempFilePath = res.tempFilePath;
          console.log("间隔时间：", timeLimit);
          if (timeLimit >= 1) { //1秒以上进行匹配
            wx.showLoading({
              title: '识别中',
            })
            wx.uploadFile({
              url: urls.profit + '/upload',
              filePath: tempFilePath,
              name: 'file',
              formData: {
                userId: that.data.userInfo.userId,
                jobId: that.data.job.id,
                second: timeLimit
              },
              success: res => {
                var jj = JSON.parse(res.data);
                console.log(jj)
                var voices = that.data.voices;
                var code = jj.obj.code;
                var voice = jj.obj.voice;
                wx.hideLoading();
                console.log("code:",code);
                if (code == "error") {
                  that.toPackage(that.data.id);
                } else if (code == "haveNoChance") {
                  that.toPackage(that.data.id);
                } else if (code == "fail") {
                  that.setData({
                    code: 1
                  })
                } else if (code == "success") {
                  var job = that.data.job;
                  console.log("success")
                  job.alreadyCount++;
                  that.drawPost(voice);
                  that.setData({
                    job: job,
                    code: 2,
                    voice: voice,
                  })

                }
                setTimeout(function() {
                  wx.hideLoading();
                }, 1000)
                if (voice != null) {
                  voices.splice(0, 0, voice); //添加一个元素
                  that.setData({
                    voices: voices,
                  })
                  if (voice.state == 0) {
                    that.setData({
                      doSay: false
                    })
                  }
                }
              }
            })
            that.setData({
              begin: false,
              second: 29,
            })
          }
        })
      // },
      // fail:res=>{
      //   console.log(res)
      //   wx.openSetting({
      //     success:res=>{
      //       console.log(res)
      //     }
      //   })
      // }
    // })
    that.setData({
      begin: true
    })
  },
  sayEnd: function() {
    const that = this;
    that.setData({
      begin: false,
    })
    setTimeout(function() { //延迟关闭录音
      console.log("关闭录音");
      console.log(that.data.second)

      recorderManager.stop();
      clearInterval(that.data.timeOut);
    }, 500)
  },
  /**
   * 点击播放
   */
  audioPlay: function(e) {
    const that = this;
    var index = e.currentTarget.dataset.index;
    var voices = that.data.voices;
    var path = voices[index].voice_path;
    var showId = that.data.showId;

    if (voices[index].play == 0) {
      voices[showId].play = 0;
      voices[index].play = 1;
      that.setData({
        voices: voices,
        showId: index
      })
      that.toPlay(path);
    } else if (showId == index && voices[index].play == 1) {
      innerAudioContext.pause();
      voices[index].play = 0;
      that.setData({
        voices: voices
      })
    }

  },
  /**
   * 播放
   */
  toPlay: function(file) {
    const that = this;
    innerAudioContext.autoplay = true;
    innerAudioContext.src = file + "?id=" + Math.ceil(Math.random() * 100);
    innerAudioContext.onPlay((res) => {
      console.log('开始播放', res)
    })
    innerAudioContext.onError((res) => {
      console.log(res.errMsg)
      console.log(res.errCode)
    })
    innerAudioContext.onEnded((res) => {
      console.log("播放完成", res);
      const voices = that.data.voices;
      voices[that.data.showId].play = 0;
      that.setData({
        voices: voices
      })
    })
  },
  /**
   * 说完30秒
   */
  sayOver: function() {
    const that = this;
    if (that.data.second <= 0) {
      recorderManager.stop()
      that.setData({
        begin: false
      })
      clearInterval(that.data.timeOut);
    }
  },
  /**
   * 查看证书
   */
  searchBook: function() {
    const that = this;
    console.log(that.data.voice)
    that.preview(that.data.voice.book_url);
    that.setData({
      code: 0
    })
  },
  /**
   * 上传证书
   */
  loadBook: function(id, file) {
    const that = this;
    wx.uploadFile({
      url: urls.profit + '/loadBook',
      filePath: file,
      name: 'image',
      formData: {
        id: id
      },
      success: res => {
        var jj = JSON.parse(res.data);
        console.log(res.data);
        var voice = that.data.voice;
        var voices = that.data.voices;
        voice.book_url = jj.obj.bookPath;
        voices[0].book_url = jj.obj.bookPath;
        that.setData({
          voice: voice,
          voices: voices,

        })
      }
    })
  },
  drawPost: function(voice) {
    var that = this;
    var rem;
    wx.getSystemInfo({
      success: function(res) {
        rem = res.screenWidth / 750;
      },
    })
    const ctx = wx.createCanvasContext('myCanvas');
    ctx.drawImage("/images/28.png", 0, 0, 750 * rem, 673 * rem);
    ctx.drawImage("/images/29.png", 210 * rem, 262 * rem, 90 * rem, 90 * rem);
    var headPic = that.data.headPic;
    ctx.drawImage(headPic, 76 * rem, 262 * rem, 90 * rem, 90 * rem);
    ctx.setFillStyle('#303030'); // 文字颜色：黑色
    ctx.setFontSize(20 * rem); // 文字字号：22px
    var nickName = that.data.userInfo.nickName;
    if (nickName.length > 6) {
      nickName = nickName.substring(0, 6) + "..."
    }
    ctx.fillText(nickName, 530 * rem, 166 * rem);
    var gender = that.data.userInfo.gender;
    if (gender == 1) {
      ctx.fillText("男", 530 * rem, 207 * rem)
    } else {
      ctx.fillText("女", 530 * rem, 207 * rem)
    }
    var now = new Date();
    ctx.fillText(now.getFullYear() + "年" + (now.getMonth() + 1) + "月" + now.getDate() + "日", 530 * rem, 248 * rem)
    ctx.fillText(voice.rate + "分", 530 * rem, 286 * rem)
    var rate = voice.rate;
    var level;
    if (rate < 86) {
      level = "二级乙等";
    } else if (rate < 91) {
      level = "二级甲等"
    } else if (rate < 96) {
      level = "一级乙等"
    } else {
      level = "一级甲等";
    }
    ctx.fillText(level, 530 * rem, 326 * rem)
    var num = (Math.ceil(Math.random() * 8) + 1) + "" + Math.ceil(Math.random() * 10) + Math.ceil(Math.random() * 10) + Math.ceil(Math.random() * 10) + Math.ceil(Math.random() * 10) + Math.ceil(Math.random() * 10) + Math.ceil(Math.random() * 10) + Math.ceil(Math.random() * 10) + Math.ceil(Math.random() * 10);
    ctx.fillText(num, 530 * rem, 410 * rem)

    ctx.fillText(now.getFullYear() + " 年 " + (now.getMonth() + 1) + " 月 " + now.getDate() + " 日", 526 * rem, 550 * rem)
    ctx.draw();
    that.downImg(voice.id);
  },
  downImg: function(id) {
    const that = this;
    wx.canvasToTempFilePath({
      x: 0,
      y: 0,
      canvasId: 'myCanvas',
      success: function(res) {
        that.loadBook(id, res.tempFilePath);
      }
    })
    // }, 100))
  },
  preview: function(file) {
    const that = this;
    wx.previewImage({
      urls: [file]
    })
  },

  /**
   * 头像缓存本地得方法
   */
  getImageInfo: function(url) { //  图片缓存本地的方法
    const that = this;
    if (typeof url === 'string') {
      wx.getImageInfo({ //  小程序获取图片信息API
        src: url,
        success: function(res) {
          that.setData({
            headPic: res.path
          })
        }
      })
    }
  },
  toHome: function() {
    wx.reLaunch({
      url: '/pages/home/home',
    })
  },
  toWallet: function() {
    wx.navigateTo({
      url: '/pages/wallet/wallet',
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
    that.setData({
      show: false
    })
    if (!that.data.warrantShow){
      that.checkAudio();
    }
  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide: function() {
    const that = this;
    var showId = that.data.showId;
    var voices = that.data.voices;
    voices[showId].play = 0;
    innerAudioContext.pause();
    that.setData({
      voices: voices
    })
  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload: function() {
    const that = this;
    var showId = that.data.showId;
    var voices = that.data.voices;
    if (voices != '') {
      voices[showId].play = 0;
    }
    innerAudioContext.pause();
    that.setData({
      voices: voices
    })
    clearInterval(that.data.IntervalTime);
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
    console.log("用户上拉操作")
  },

  /**
   * canvas画分享图
   */
  drawShare: function(job, url) {
    console.log("生成分享图")
    const that = this;
    let rem;
    wx.getSystemInfo({
      success: function(res) {
        rem = res.screenWidth / 750;
      },
    })

    const ctx = wx.createCanvasContext('canvas_2');

    ctx.drawImage("/images/42_1.jpg", 0, 0, 603 * rem, 481 * rem);
    ctx.drawImage(url, 262 * rem, 20 * rem, 80 * rem, 80 * rem);
    ctx.setTextAlign('center'); // 文字居中
    ctx.setFillStyle('#FFFFFF'); // 文字颜色：黑色
    ctx.setFontSize(42 * rem); // 文字字号：22px

    // ctx.font = 'bold ' + 42*rem+' arial';
    var context = job.context;
    if (context.length < 12) {
      ctx.fillText(context, 603 * rem / 2, 202 * rem);
    } else if (context.length < 21) {
      ctx.fillText(context.substring(0, 12), 603 * rem / 2, 202 * rem);
      ctx.fillText(context.substring(12, 21), 603 * rem / 2, 250 * rem);
    } else {
      ctx.fillText(context.substring(0, 12), 603 * rem / 2, 202 * rem);
      ctx.fillText(context.substring(12, 21) + "...", 603 * rem / 2, 250 * rem);
    }

    ctx.draw();
    setTimeout(function() {
      that.downShare(job);
    }, 200)

  },
  downShare: function(job) {
    const that = this;
    wx.canvasToTempFilePath({
      x: 0,
      y: 0,
      canvasId: 'canvas_2',
      success: function(res) {
        that.loadShare(job.id, res.tempFilePath, job);
        // that.preview()
      },
      complete: res => {
        console.log(res)
      }
    })
  },
  /**
   * 上传分享图
   */
  loadShare: function(id, file, job) {
    const that = this;
    wx.uploadFile({
      url: urls.profit + '/loadSharePic',
      filePath: file,
      name: 'image',
      formData: {
        id: id,
      },
      success: res => {
        var jj = JSON.parse(res.data);
        console.log(res.data);
        job.shareUrl = jj.obj.sharePic;
        that.setData({
          job: job
        })
      }
    })
  },
  /**
   * 用户点击右上角分享
   */
  onShareAppMessage: function() {
    console.log("分享")
    const that = this;
    var id = that.data.job.id;

    console.log("sharePic1", that.data.sharePic)

    return {
      title: "【语音红包】说对口令，领取红包",
      path: "/pages/package/package?id=" + id,
      imageUrl: that.data.job.shareUrl,
      // imageUrl:"/images/57.jpg",
      success: function(res) {
        that.setData({
          code: 0
        })
      },
      fail: function(res) {
        // 转发失败
      }
    }
  }
})