# SmartGlasses

## 项目简介
本项目用于演示在Android平台上与Venus智能眼镜进行交互，通过VenusSdk快速实现对设备的搜索连接、状态管理、个性化设置，以及翻译、导航、通知、题词、AI助手等常用功能控制。

## 功能

### 设备管理
通过bluetoothAdapter.startDiscovery()搜索Venus智能眼镜设备，搜索到BluetoothDevice后调用VenusSDK.connect()与设备进行连接，参考Scan2Activity。
设备连接成功后，在DeviceManager中初始化设备、自动重连、开启上行消息监听，实时反馈设备电量状态、工作状态。

### 个性化设置
App支持设置亮度、字体、语言、佩戴检测等配置项，VenusSDK提供SystemConfig读写接口，参考DeviceInfoActivity。

### 转写翻译
App支持将语音转文字(Speech to Text, STT)结果通过VenusSDK.updateAsr()、VenusSDK.updateTrans()接口实时同步到眼镜设备。
项目默认Mock引擎用于演示转写翻译与眼镜设备的同步效果，同时兼容集成了Azure、iFlyTek、AiSpeech等语音引擎，替换有效Key后即可体验。
相关实现请参考SttManager。

### 导航
App支持将实时导航信息通过VenusSDK.updateNav()同步到眼镜设备。项目集成了高德导航用于演示，替换有效Key后即可体验。

### 通知提醒
App支持将Android设备上接收到的通知流转至智能眼镜设备，通过VenusSDK.addNotification()，VenusSDK.removeNotification()与设备交互。

### 健康
App支持将实时健康信息通过VenusSDK.updateHealth()同步到眼镜设备。

### 提词器
App支持通过VenusSDK.updatePrompter()将提词内容同步到眼镜设备，项目提供了导入txt格式文件内容用于演示。

### AI助手
开发者可以接入任意大模型AI助手，并通过VenusSDK接口将实时交互同步到眼镜端。

## 反馈建议
在您体验App和SDK过程中遇到任何问题，或有任何建议，可以通过以下方式联系我们：
- https://github.com/jueyuantech/SmartGlasses-Android