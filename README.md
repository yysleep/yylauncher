
## **目录结构说明**

### **java 源码结构**

root package 为 com.yanhuahealth.healthlauncher

下面各个 child package 分别为:

- **api**: 负责与健康云平台之间的通信接口的封装；
- **common**: 应用内通用的类，常量，配置等；
- **model**: 只放实体模型类，不包含业务逻辑，子包又根据不同功能模块分为：
    + **contact**: 主要存放和 联系人，及分组相关的模型类；
    + **controlcenter**: 控制中心模块相关的模型类；
    + **sys**: 主要存放框架和基础能力相关的模型类；
- **sys**: 主要负责与 Lanncher 的框架和基础能力的模块；
- **ui**: 页面组件相关，子包根据不同模块分为：
    + **appmgr**: 所有应用，及应用管理相关；
    + **base**: 主要封装了页面组件中的基础组件，如：YHBaseActivity；
    + **contact**: 联系人，及联系人分组相关的页面组件封装；
    + **controlcenter**: 控制中心；
    + **upgrade**: 系统升级维护相关；
    + **MainActivity**: 主页面；
- **utils**: 主要提供一些常用的工具类；

> 注：如果一级包（如：**api**，**common** 等等）有新增或删除，则需要经过讨论后确定，对于二级包（如：**.ui.appmgr** 等）需要删除，则需要经过讨论后确定，对于新增二级子包如果根据业务模块化可以自行处理；

---------

## **外部应用**

下面列出了各个第三方应用的安装包下载路径

- **[微信](http://www.apk.anzhi.com/data2/apk/201602/03/4b9ee345da2012e5d5ae8a3844c1836e_42287800.apk)**

- **[头条新闻](http://www.apk.anzhi.com/data3/apk/201512/01/dc0470521463089eef2facb7f1e5de1b_32846400.apk)**

> 采用的是 网易新闻

- **[老黄历](http://www.apk.anzhi.com/data2/apk/201602/03/54bcf8cccbe9a57f38a3ce17f5193948_07695000.apk)**

> 采用的是 中华老黄历

---------

## **关于发布**

### **1. 发布内测**

- 版本号更新; (app/build.gradle)
- 添加 bugtags 的 bubble;

    主要更改 YHBaseActivity 中的 needBugtags 开关为 true。

> 未发布内测前，关闭 bugtags，已避免不停上报 bug 消息至 bugtags。

### **2. 正式发布**

- 版本号更新;
- 关闭 bugtags 的 bubble 及开关;
    + HealthLauncherApplication.onCreate()
    + YHBaseActivity.needBugtags
- 关闭调试日志;
    + YHLog.ENABLE_LOG

---------

## **版本功能说明**

对于版本号 x.y.z 的说明：

- **x**: 主要在客户端实现架构发生重大变化时进行递增;
- **y**: 主要根据产品新功能和需求进行迭代时来递增;
- **z**: 主要针对发布出去的版本，如果发现 BUG 时进行修复后递增;

### **v1.0.0**

- 支持版本强制升级;
- 内置缺省 3 个桌面页，以及常用第三方应用(微信，电话，上网，老黄历，相册，短信，相机，联系人);
- 通过 **所有应用** 支持动态添加和移除桌面应用;
- 支持添加 健康小秘书 和 幸福小秘书 的 微信快捷方式;
- 支持动态添加联系人;
- 支持动态移除联系人;(从联系人详情页面触发)
- 支持打电话，发短信;
- **控制中心** 能够打开数据网络，wifi，以及音量等开关;

### **V1.1.0**

- 支持网络状态监测，变化后桌面上的控制中心图标变换；
- 支持外部应用的下载与安装；
- 支持用角标方式提示有新短信和新呼叫记录；
- 删除，编辑，以及搜索联系人等功能；
- 自动定位天气预报功能；



