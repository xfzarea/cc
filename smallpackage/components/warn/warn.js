// components/warn/warn.js
Component({
  /**
   * 组件的属性列表
   */
  properties: {
    show:Boolean,
    getCode:Number,
    mark:String
  },

  /**
   * 组件的初始数据
   */
  data: {
    userInfo:wx.getStorageSync("userInfo"),
  },

  /**
   * 组件的方法列表
   */
  methods: {
    
    _changeMsg:function(){
      const that = this;
      that.setData({
        show: true
      })
    },
    closeWarn:function(){
      const that = this;
      that.triggerEvent('closeWarn', false);
    },
  },
  attached:function(){
    const that = this;
    that.setData({
      userInfo:wx.getStorageSync("userInfo")
    })
  }
})
