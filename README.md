# Venus 智能眼镜 Android SDK 示例项目

## 项目简介
本项目是一个基于 Android 平台的示例应用，旨在演示如何通过 **VenusSDK** 与 **Venus 智能眼镜**进行交互。开发者可以参考本项目快速进行设备连接、状态管理、个性化配置，以及实现翻译、导航、通知、提词器、AI 助手等场景的信息推送。

## 主要功能

### 设备管理
本模块负责 Venus 智能眼镜的发现、连接以及后续的状态维护。
- **设备发现与连接**：通过蓝牙搜索附近的 Venus 智能眼镜，使用 `VNCommon.connect()` 方法建立连接。详细流程请参考 `ScanActivity.java`。
- **设备状态维护**：成功连接设备后，`DeviceManager.java` 将接管设备的初始化、实时状态（如电量、工作模式）的监控、连接断开后的自动重连机制，以及处理来自眼镜的上行消息。

### 个性化设置
允许用户根据个人使用习惯调整眼镜的各项显示与行为参数，例如屏幕亮度、文本字号、系统语言以及配置佩戴检测、静置休眠功能等。**VenusSDK** 提供了 `SystemConfig` 相关的读写接口，方便开发者进行参数配置。相关实现逻辑可参见 `DeviceConfigActivity.java`。

### 语音转写与翻译消息推送
用于演示转写和翻译内容如何实时显示在眼镜屏幕上。项目中预置了Microsoft Azure、科大讯飞 (iFlyTek)、思必驰 (AiSpeech) 等主流语音服务提供商的 SDK。开发者在代码中配置`STT_ENGINE_DEFAULT`，并将`mSttEngine.initParam("")`替换为自己有效的 API Key，即可启用相应的语音服务，相关实现逻辑可参见 `SttWorker.java`。

- 语音识别结果通过 `VNCommon.updateTranscribe()` 接口同步至眼镜端。
- 翻译结果则通过 `VNCommon.updateTranslate()` 接口进行同步。

### 导航信息推送
本项目集成了高德地图SDK作为导航数据源示例，应用能够将实时的导航指令信息通过 `VNCommon.updateNav()` 接口发送到智能眼镜，为用户提供便捷的视觉导航体验，相关实现逻辑可参见 `NaviActivity.java`。

### 通知推送
能够将 Android 智能手机接收到的应用程序通知（例如短信、来电、社交应用消息等）实时流转并显示在智能眼镜的屏幕上。此功能主要通过 `VNCommon.addNotification()` 和 `VNCommon.removeNotification()` 接口与眼镜进行交互，实现通知的添加与移除。

### 固件升级
应用支持对 Venus 智能眼镜进行固件升级 (OTA)。用户可以通过 App 内的 OTA 功能模块，选择并上传最新的固件文件到眼镜端，从而完成设备的固件更新迭代。具体实现细节及交互逻辑，请参考 `DeviceOtaActivity.java`：
- **升级流程**：检查设备当前固件版本、从服务器下载或选择本地固件包、通过 `VNCommon.initOta()`将固件包通过SDK初始化给设备、通过 `VNCommon.startOta()`执行升级等步骤。
- **注意事项**：OTA 升级过程中，请确保眼镜电量充足（建议不低于40%），并保持手机与眼镜的稳定连接，避免中途断开连接或退出应用，以免导致升级失败。


