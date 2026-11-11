# Realme Privacy Guard / 真我隐私卫士

针对部分 realme / OPPO 预置组件的 LSPosed 模块。通过 Java Hook 限制已确认的采集接口和上报入口。

An LSPosed module that restricts identified collection and reporting entry points in selected realme / OPPO components.

- Application ID / 包名：`io.github.caiyi0577.rpg`
- Source / 源码：[caiyi0577/realme-Privacy-Guard](https://github.com/caiyi0577/realme-Privacy-Guard/tree/lsposed-release)
- Support / 反馈：[GitHub Issues](https://github.com/caiyi0577/realme-Privacy-Guard/issues)

## 功能 / Features

- 已确认的位置、Wi-Fi、短信模型和诊断日志上报入口保护。
- 广告与推荐请求、设备标识、定位、应用列表、使用记录及部分安装包元信息保护。
- 九个独立开关、受限的私有配置读取通道。
- 四类自检状态：阻断、阻断失败、未阻断、未运行；失败状态以红色粗体显示。

Blocks identified location/Wi-Fi, SMS-model, and diagnostic uploads; restricts audited ad requests and selected identifier, location, app-list, usage-record, and package-metadata APIs. Includes nine switches, restricted settings access, and four diagnostic states. Failures are red and bold.

## 安装 / Installation

需要兼容的 LSPosed，最低 Android 10 / API 29。已核对的环境为 realme GT8 Pro（RMX5200）、Android 17 / API 37、`RMX5200_17.0.0.106(CN01)`；不保证其他机型或固件兼容。

Requires compatible LSPosed and Android 10 / API 29 or newer. Audited on the realme GT8 Pro (RMX5200), Android 17 / API 37, firmware `RMX5200_17.0.0.106(CN01)`. Other devices and firmware are unverified.

安装签名 APK，在 LSPosed 启用模块并勾选需要的推荐作用域。重点新增目标为智慧建议、智慧推荐和日志服务。RegService 已禁用时继续保持禁用。首次启用或修改拦截规则后重启目标进程或手机，自检需要模块自身的 root 授权。

Install the signed APK, enable the module, and select the relevant recommended scopes. Additional targets include Smart Suggestions, Smart Recommendations, and Logging Service. Keep RegService disabled if already disabled. Restart target processes or the device after first activation or hook changes. Root access for the module app is needed for diagnostics.

## 自检 / Diagnostics

| 状态 / State | 说明 / Meaning |
| --- | --- |
| 阻断 / Blocking | 规则已安装就绪、已有实际命中或组件已禁用。Hooks are ready, hits were observed, or the component is disabled. |
| 阻断失败 / Blocking failed | Hook 安装/初始化报错或关键规则缺失，以红字显示。Hook errors or missing required rules, shown in red. |
| 未阻断 / Not blocking | 尚无有效加载证据、模块未启用或作用域未勾选等。No valid loading evidence, disabled module, or missing scope. |
| 未运行 / Not running | 当前快照未发现运行进程。No running process in the current snapshot. |

命中次数单独显示。多进程覆盖不足、默认配置或接口跳过等情况不会隐藏。Java Hook 不等于全面防火墙，也不保证覆盖原生网络、特权截屏或未来新增通路。

Hit counts are separate. Partial process coverage, default settings, and skipped interfaces remain visible. Java hooks are not a complete firewall and do not guarantee coverage of native networking, privileged screenshots, or new data paths.

## 上架版迁移 / Migration

**独立上架包 `io.github.caiyi0577.rpg`，不能覆盖旧包 `com.caiyi.rpg`。原版配置和作用域不会自动继承。请先停用旧模块，再启用本模块，避免双重 Hook。手机现有模块未被本次发布操作修改。新包已通过构建与规则测试，尚未在手机启用验证。**

**This is the separate `io.github.caiyi0577.rpg` distribution. It cannot update `com.caiyi.rpg` in place or automatically inherit its settings/scope. Disable the legacy module before enabling this one. Publishing does not modify the existing phone installation. This package passed build/policy checks but has not yet been activated on a device.**
