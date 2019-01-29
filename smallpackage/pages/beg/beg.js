const urls = require("../../utils/urls.js");
const app = getApp();
Page({

  /**
   * 页面的初始数据
   */

  data: {
    userInfo: wx.getStorageSync("userInfo"),
    context: "可设置文字/语音/图片",
    award: 0.00,
    choose_award: 0.00,
    submit_award: 0.00,
    count: 1,
    submit: false,
    charge: 0.00,
    warrantShow: false,
    show: false,
    warn_show: false,
    job_type: 0,
    one_award: 0.00,
    num: -1,
    showModal: false,
    handType: 0, //我用来区分卡片的
    default_award: [5.20, 66.6, 8.88, 99.9, 10.00, 11.00],
    jobId: 0,
    jBegInfo: {},
    begSubmit: false
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    const that = this;
    // 查看是否授权
    if (!wx.getStorageSync("userInfo")) {
      wx.redirectTo({
        url: '/pages/index/index?page=beg'
      })
    } else {
      that.setData({
        userInfo: wx.getStorageSync("userInfo"),
        jBegInfo: app.globalData.jBegInfo
      })
      console.log(app.globalData.jBegInfo)
      app.globalData.jBegInfo = { begType: 0, begInfo: '' };
      that.getImageInfo(that.data.userInfo.avatarUrl);
      //从自定义口令页面过来
      if (app.globalData.context != '') {
        that.setData({
          context: app.globalData.context
        })
        // var jobContexts = wx.getStorageSync("jobContexts");
        // if (jobContexts == '') {
        //   jobContexts = [];
        // }
        // jobContexts.splice(0, 0, app.globalData.context);
        // wx.setStorageSync("jobContexts", jobContexts)
        app.globalData.context = '';
        

      }
    }
  },

  /**
   * canvas画分享图
   */
  draw_share_pic: function () {
    var that = this;
    var rem;
    wx.getSystemInfo({
      success: function (res) {
        rem = res.screenWidth / 750;
      },
    })
    const ctx = wx.createCanvasContext('share_pic');
    ctx.drawImage("/images/83.jpg", 0, 0, 600 * rem, 842 * rem);
    let codeUrl = that.data.codeUrl;
    if (codeUrl) {
      ctx.drawImage(codeUrl, 155 * rem, 388 * rem, 290 * rem, 290 * rem);
    }
    ctx.draw();
    that.downImg();
  },
  
  /**
   * canvas画转发封面图
   */
  draw_share_pic_1: function () {
    var that = this;
    var rem;
    wx.getSystemInfo({
      success: function (res) {
        rem = res.screenWidth / 750;
      },
    })
    const ctx = wx.createCanvasContext('share_pic_1');
    ctx.drawImage("/images/97.jpg", 0, 0, 420 * rem, 336 * rem);
    let headPic = that.data.headPic;
    if (headPic) {
      that.circleImg(ctx,headPic,168*rem,128*rem,45*rem);
      // ctx.drawImage(headPic, 168 * rem, 128 * rem, 90 * rem, 90 * rem);
    }
    ctx.draw();
    that.downImg_1();
  },

  /**
   * 画圆图
   */
  circleImg: function (ctx, img, x, y, r) {
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
  shareImageToLoad:function(url){
    console.log("code:", url)
    const that = this;
    if (typeof url === 'string') {
      wx.getImageInfo({ //  小程序获取图片信息API
        src: url,
        success: function (res) {
          that.setData({
            codeUrl: res.path
          })
        },
        fail: function (res) {
          console.log(res);
          wx.getImageInfo({
            src: url,
            success: function (res) {
              that.setData({
                codeUrl: res.path
              })
            }
          })
        }
      })
    }
  },

  /**
   * 头像缓存本地得方法
   */
  getImageInfo: function (url) { //  图片缓存本地的方法
    const that = this;
    if (typeof url === 'string') {
      wx.getImageInfo({ //  小程序获取图片信息API
        src: url,
        success: function (res) {
          that.setData({
            headPic: res.path
          })
        }
      })
    }
  },

  getCode: function (id) {
    const that = this;
    wx.request({
      url: urls.profit + '/getCode',
      data: {
        jobId: id,
        type: 'beg'
      },
      success: res => {
        console.log(res)
        let url = res.data.obj.codeUrl;
        console.log("code:", url)
        if (typeof url === 'string') {
          wx.getImageInfo({ //  小程序获取图片信息API
            src: url,
            success: function (res) {
              that.setData({
                codeUrl: res.path
              })
            },
            fail: function (res) {
              console.log(res);
              wx.getImageInfo({
                src: url,
                success: function (res) {
                  that.setData({
                    codeUrl: res.path
                  })
                }
              })
            }
          })
        }
      }
    })
  },
  downImg: function (id) {
    const that = this;
    wx.canvasToTempFilePath({
      x: 0,
      y: 0,
      canvasId: 'share_pic',
      success: function (res) {
        console.log(res.tempFilePath);
        that.setData({
          share_pic_src: res.tempFilePath
        })
      }
    })
    // }, 100))
  },

  downImg_1: function (id) {
    const that = this;
    wx.canvasToTempFilePath({
      x: 0,
      y: 0,
      canvasId: 'share_pic_1',
      success: function (res) {
        console.log(res.tempFilePath);
        that.setData({
          share_pic_src_1: res.tempFilePath
        })
      }
    })
    // }, 100))
  },
  toBegRecord:function(){
    wx.navigateTo({
      url: '/pages/begRecord/begRecord',
    })
  },
  /**
   * 保存到相册
   */
  saveSharePic: function () {
    const that = this;
    wx.saveImageToPhotosAlbum({
      filePath: that.data.share_pic_src,
      success: function (res) {
        wx.showToast({
          title: '保存图片完成',
          success: function () {
            setTimeout(function () {
              that.setData({
                handType: 0,
              })
              wx.showTabBar({
                animation: false //是否需要过渡动画
              })
            }, 2000)
          }
        })
      }
    })
  },
  /**
   * 生成讨要红包
   */
  createJob: function () {
    const that = this;
    that.begSubmit();
    let begSubmit = that.data.begSubmit;
    let jBegInfo = that.data.jBegInfo;
    let job_type = 0;
    if (begSubmit && jBegInfo.begType != 0) {
      if (jBegInfo.begType == 3) {
        job_type = 1;
      }
      if (jBegInfo.begType == 2) {
        job_type = 2;
      }
      let context = jBegInfo.begInfo;
      wx.showLoading({
        title: '红包生成中~',
      })
      wx.request({
        url: urls.profit + '/createBegJob',
        data: {
          award: that.data.submit_award,
          totalAward: parseFloat(that.data.award) + parseFloat(Math.round(that.data.award) * 2 / 100),
          userId: that.data.userInfo.userId,
          context: context,
          job_type: job_type
        },
        success: res => {
          wx.hideLoading();
          console.log("红包,",res.data)
          that.setData({
            jobId: res.data.jobId,
            begSubmit: false,
            submit_award: 0.00,
            jBegInfo: { begType: 0, begInfo: '' },
            handType: 1
          })
          wx.hideTabBar({
            animation: false
          })
          // that.getCode(res.data.jobId);
          that.shareImageToLoad(res.data.codeUrl);
          that.draw_share_pic_1();
          setTimeout(function () {
            that.draw_share_pic();
          }, 1500)
        }
      })
    }
  },

  create_share_pic: function () {
    const that = this;
    that.setData({
      handType: 2
    })
  },

  load: function (e) {
    console.log("公众号组件", e)
  },

  changeOil: function (e) {
    const that = this;
    let num = e.currentTarget.dataset.num;
    let award = that.data.default_award[num];
    this.setData({
      num: e.target.dataset.num,
      choose_award: award,
      submit_award: award,
      award: 0.00
    })
    that.checkSubmit();
  },

  /**
  
  /**
   * 创建
   */
  toChanges: function () {
    wx.navigateTo({
      url: '/pages/changes/changes',
    })
  },
  input: function (e) {
    const that = this;
    var type1 = e.currentTarget.dataset.type;
    let data = e.detail.value;
    let award = that.data.award;
    that.setData({
      num: -1,
    })
    if(data == 0||data == ""){
      that.data.award = data;
      that.setData({
        submit: false
      })
      return;
    }
    if (type1 == "award") {
      if (/^\d+\.?\d{0,2}$/.test(data)) {
        award = data;
      } else {
        award = data.substring(0, data.length - 1);
      }

      that.setData({
        award: award,
        choose_award: 0.00,
        submit_award: award,
        charge: Math.round(award) * 2 / 100
      })

    }
    that.checkSubmit();
  },
  //判断是否可以塞钱进红包
  begSubmit: function () {
    const that = this;
    let jBegInfo = that.data.jBegInfo;
    let submit_award = that.data.submit_award;
    let flag = false;
    if (jBegInfo.begType != 0 && submit_award > 0) {
      flag = true;
    } else {
      flag = false
    }
    that.setData({
      begSubmit: flag
    })
  },

  checkSubmit: function () {
    const that = this;
    let flag = false;
    let award = that.data.submit_award;
    if (award <= 0||award == '') {
      flag = false;
    } else {
      flag = true;
    }
    that.setData({
      submit: flag
    })
  },
  //卡片区分
  changeHandType: function () {
    wx.hideTabBar({
      animation: true //是否需要过渡动画
    })
    const that = this;
    that.setData({
      handType: 1
    })
  },
  /**
   * 隐藏 卡片
   */
  hide: function () {
    const that = this;
    that.setData({
      code: 0,
      handType: 0
    })
    wx.showTabBar({
      animation: false //是否需要过渡动画
    })
  },
  
  toRecord: function () {
    wx.navigateTo({
      url: '/pages/record/record',
    })
  },
  submit_b: function () {
    wx.hideTabBar({
      animation: true //是否需要过渡动画
    })
    this.setData({
      showModal: true


    })

  },

  preventTouchMove: function () {

  },

  saveFormId: function (formId) {
    const that = this;
    wx.request({
      url: urls.profit + '/saveFormid',
      data: {
        formid: formId,
        userId: that.data.userInfo.userId,
      },
      success: res => {

      }
    })
  },
  go: function (e) {
    let submit = this.data.submit;
    let formid = e.detail.formId;
    this.saveFormId(formid);
    if (submit) {
      this.setData({
        showModal: false,
        award: 0,
        choose_award: 0.00,
        num: -1,
        submit: false
      })
      this.begSubmit();
      wx.showTabBar({
        animation: true //是否需要过渡动画
      })
    }
  },
  go1: function () {
    this.setData({
      showModal: false,
      award: 0,
      choose_award: 0.00,
      num: -1,
      submit: false
    })
    this.begSubmit();
    wx.showTabBar({
      animation: true //是否需要过渡动画
    })
  },
  closeWarn: function (e) {
    const that = this;
    that.setData({
      warn_show: false
    })
  },
  toPlayRed: function () {
    // wx.navigateTo({
    //   url: '/pages/playRed/playRed',
    // })
    const that = this;
    that.setData({
      warn_show: true
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
    // const that = this;
    // that.setData({
    //   show: false
    // })
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
  onShareAppMessage: function (e) {
    console.log("分享")
    const that = this;
    let jobId = that.data.jobId;
    if (e.from == 'button') {//点击按钮来的 menu
      return {
        title: "【语音红包】发完红包讨红包，红包玩法欢乐多",
        path: '/pages/begPackage/begPackage?id=' + jobId,
        imageUrl: that.data.share_pic_src_1,
        success: function (res) {
          that.setData({
            handType: 0
          })
        },
        fail: function (res) {
          // 转发失败
        },
      }
    }
    that.setData({
      handType: 0
    })
  }
})