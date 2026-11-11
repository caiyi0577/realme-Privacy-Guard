# Realme Privacy Guard / 真我隐私卫士

> **LSPosed 官方模块仓库 / Official module repository:** [1.2.0 签名 APK / Signed APK](https://github.com/Xposed-Modules-Repo/io.github.caiyi0577.rpg/releases/tag/8-1.2.0) · [模块说明 / Module listing](https://github.com/Xposed-Modules-Repo/io.github.caiyi0577.rpg) · [独立分支 / Source branch](https://github.com/caiyi0577/realme-Privacy-Guard/tree/lsposed-release)
>
> 上架版包名为 `io.github.caiyi0577.rpg`，是独立应用，不能覆盖本分支的旧包 `com.caiyi.rpg`。请先停用旧模块，再启用上架版。官方模块仓库中的说明与签名 APK 已发布；LSPosed 应用和网站索引正在等待刷新。
>
> The public build uses the separate `io.github.caiyi0577.rpg` application ID. It cannot update this branch's legacy `com.caiyi.rpg` package in place. Disable the old module before enabling the new one. The listing and signed APK are published in the official module repository; the LSPosed app/website index refresh is pending.

[![Build](https://github.com/caiyi0577/realme-Privacy-Guard/actions/workflows/build.yml/badge.svg)](https://github.com/caiyi0577/realme-Privacy-Guard/actions/workflows/build.yml)

[中文](#中文) · [English](#english)

## 中文

面向 realme / OPPO 部分预置组件的 LSPosed 模块，通过 Java Hook 限制已确认的采集接口和上报入口。当前版本 **1.1.5**，包名 `com.caiyi.rpg`。

本项目由 realme GT8 Pro（RMX5200）的单机排查与适配发展而来，已核对的系统为 Android 17 / API 37、`RMX5200_17.0.0.106(CN01)`。**不保证适用于其他机型、其他固件或更新后的系统组件。** 应用界面目前为中文。

### 保护内容

| 开关 | 作用 | 可能影响 |
| --- | --- | --- |
| 智慧建议外发保护 | 拦截已确认的位置/Wi-Fi 上传、短信模型上传及位置统计事件 | 云端学习、部分建议 |
| 广告与推荐请求保护 | 拦截已审计版本的广告网络适配器和已知端点 | 广告、推荐更新 |
| 日志上传任务保护 | 拦截三类日志上传及重试任务，保留本地日志服务 | 诊断报告上传 |
| 设备标识与位置 | 过滤设备/SIM/基站信息、定位请求及 SystemHelper 注册入口 | 天气定位、运营商配置 |
| 录音启动与使用记录 | 限制原有目标的录音启动、使用记录和无障碍列表枚举 | 相关音频和统计功能 |
| 应用列表过滤 | 采集型目标返回空列表，功能型目标仅返回系统应用 | 应用扫描 |
| 安装包元信息 | 限制安装包元信息及安装来源查询 | 安装来源识别 |
| 设备状态返回值 | 保留目标进程内的旧版设备状态伪装 | 部分检测逻辑 |
| 已知上报端点保护 | 在 Java DNS / URL 建连前拦截已知端点 | 对应网络请求 |

规则不会更改真实引导状态；Java 网络拦截不覆盖所有原生网络，也不保证阻止特权截屏。代码中发现上传逻辑，不等于证明对应内容已发送到服务器。

### 安装与启用

1. 准备已安装兼容 LSPosed 的设备，以及**使用正确签名的 APK**。模块最低 Android 版本为 10（API 29），使用经典 Xposed Java API 82。
2. 安装 APK，在 LSPosed 中启用模块，选择需要保护的推荐作用域。
3. 新增重点作用域为 **智慧建议** `com.oplus.pantanal.ums`、**智慧推荐** `com.opos.ads`、**日志服务** `com.oplus.olc`。已禁用的 RegService 继续保持禁用。
4. 首次安装或修改拦截规则后，重启目标进程或手机；开关变化同样在目标进程下次加载时生效。
5. 打开模块并运行自检。读取系统状态和模块记录的自检需要给本模块 root 授权。

1.1.3–1.1.5 仅更新自检与显示，沿用 1.1.2 的拦截规则版本；已加载同一规则版本的进程，无需为了界面更新再次软重启。

<details>
<summary>全部 17 个推荐作用域 / All 17 recommended scope packages</summary>

```text
com.mobiletools.systemhelper
com.coloros.regservice
com.oppo.ctautoregist
com.coloros.weather.service
com.nearme.instant.platform
com.oplus.safecenter
com.oplus.securitypermission
com.coloros.securityguard
com.oplus.statistics.rom
com.oplus.deepthinker
com.oplus.metis
com.oplus.obrain
com.oplus.onetrace
com.coloros.remoteguardservice
com.oplus.pantanal.ums
com.opos.ads
com.oplus.olc
```

</details>

### 自检状态

| 状态 | 含义 |
| --- | --- |
| **阻断** | 拦截规则已安装就绪、已有实际命中记录，或组件已禁用。是否触发过上报由命中次数单独说明。 |
| **阻断失败（红色粗体）** | Hook 安装、初始化报错，或关键拦截规则缺失；同时显示具体原因。部分规则仍有拦截时也会注明。 |
| **未阻断** | 模块未启用、作用域未勾选、尚无有效加载证据或安装状态未知等。没有命中记录不会单独造成此状态。 |
| **未运行** | 本次快照中未发现该组件的运行进程，待启动后再验证。 |

自检核对当前 PID、启动标识及兼容规则版本，分别保留初始化与近期命中记录。多进程覆盖不完整、接口跳过、默认配置等情况继续提示；“阻断”不代表所有通路都被覆盖，也不证明服务器是否收到数据。命中日志按 1、64、128…采样，显示的是已记录次数下限。

### 自动编译与下载

推送到 `main`、提交 PR 或手动运行 **Actions → Build**，会在 Linux 和 Windows 上编译并运行 50 项规则与诊断检查。成功后下载 `realme-privacy-guard-unsigned-*` 构建产物，内含 APK、校验信息和测试结果。

**CI 产物是未签名的验证 APK，不能直接安装，也不能直接覆盖手机上的签名版本。** 当前流程不使用或上传签名私钥，不自动发布 Release。需要可安装版本时，使用原签名在本地构建。不要卸载原版来绕过签名不匹配，否则会丢失原配置。

### 本地构建

需要 Python 3.10+、JDK 17，以及 Android SDK `platforms;android-34`、`build-tools;34.0.0`。设置 `JAVA_HOME` 和 `ANDROID_SDK_ROOT`（或 `ANDROID_HOME`）。Windows 和 Linux 使用同一命令：

```sh
python build.py --unsigned
```

编译依赖 Xposed API 82 会从 `dependencies.json` 指定的镜像下载并校验固定 SHA-256，仅用于编译，不打包进 APK。输出位于 `build/日期时间/`。不需要 Gradle 或 Android Studio。

本地签名时，将以下 JSON 保存在**仓库之外**，设置对应密码环境变量后执行：

```json
{
  "keystore": "/absolute/path/to/your-keystore.jks",
  "alias": "your-alias",
  "store_password_env": "RPG_STORE_PASS",
  "key_password_env": "RPG_KEY_PASS"
}
```

```sh
python build.py --signing-config /path/outside/repository/signing.json
```

可增加 `expected_certificate_sha256` 字段，要求签名证书与指定指纹一致。密钥、密码和签名配置不应提交到仓库。

### 配置与兼容性

设置保存在应用私有的设备加密存储，通过受限只读 ContentProvider 向目标 UID 提供九个布尔开关。共享同一系统 UID 的组件共用该访问边界。模块不申请联网权限，不再使用已弃用的 New XSharedPreferences 扩展；这不等于已迁移到 Modern Xposed API。

从早期 New XSharedPreferences 版本升级，需要先迁移旧配置；源码支持读取本模块私有目录中的 `rpg_settings_migration.json`。从 1.1.1 及以后的私有配置版本覆盖更新会保留开关。

SystemHelper 与智慧推荐的混淆入口带有针对已审计 APK 大小的兼容性检查；大小不是可信度证明。系统组件更新后应重新核对。云端测试不替代真机验证，设备日志、提取的系统 APK、签名密钥均不随仓库发布。

## English

Realme Privacy Guard is an LSPosed module for selected preinstalled realme / OPPO components. It uses Java hooks to restrict identified collection APIs and reporting entry points. Current version: **1.1.5**. Package: `com.caiyi.rpg`.

The project grew out of an audit and adaptation for one realme GT8 Pro (RMX5200), running Android 17 / API 37, build `RMX5200_17.0.0.106(CN01)`. **Compatibility with other devices, firmware versions, or updated system components is not guaranteed.** The app UI is currently Chinese.

### Protection switches

| Switch | Behavior | Possible effects |
| --- | --- | --- |
| Smart Suggestions uploads | Blocks identified location/Wi-Fi uploads, SMS-model uploads, and location statistics events | Cloud learning and some suggestions |
| Ads and recommendations | Blocks the audited ad network adapter and known endpoints | Ads and recommendation updates |
| Diagnostic upload jobs | Blocks three upload/retry job types while retaining local logging | Diagnostic report uploads |
| Identifiers and location | Filters device/SIM/cell information, location requests, and identified SystemHelper registration entry points | Weather location and carrier setup |
| Audio startup and usage records | Restricts audio startup, usage queries, and accessibility-service enumeration in legacy targets | Related audio and statistics features |
| Installed-app lists | Returns an empty list to collection-oriented targets and system apps only to functional targets | App scanning |
| APK metadata | Restricts package metadata and installation-source queries | Installer/source detection |
| Device-state values | Retains legacy spoofed return values inside scoped target processes | Some detection logic |
| Known reporting endpoints | Blocks known endpoints before Java DNS resolution or URL connection | Matching network requests |

The module does not alter the real boot state. Java network hooks do not cover all native networking or guarantee prevention of privileged screenshots. Finding upload code does not establish that data was transmitted.

### Installation

1. Use a device with compatible LSPosed installed and obtain an APK signed with the correct key. Minimum Android version is 10 / API 29; the module uses classic Xposed Java API 82.
2. Install the APK, enable it in LSPosed, and select the recommended target scopes you need.
3. The three additional targets are Smart Suggestions (`com.oplus.pantanal.ums`), Smart Recommendations (`com.opos.ads`), and Logging Service (`com.oplus.olc`). Keep RegService disabled if it is already disabled. The complete 17-package scope list appears above.
4. Restart target processes or the phone after initial installation or hook changes. Switch changes apply when target processes next load the module.
5. Open the app and run the self-check. Grant root to the module for reading system state and module diagnostics.

Versions 1.1.3–1.1.5 update diagnostics and presentation while retaining hook revision 1.1.2. Processes already running that hook revision do not need another soft reboot solely for the display update.

### Self-check states

| App label | Meaning |
| --- | --- |
| **阻断 — Blocking** | Blocking hooks are installed and ready, actual hits have been recorded, or the component is disabled. Hit counts separately show whether a rule has been triggered. |
| **阻断失败 — Blocking failed** | Hook installation/initialization failed or required rules are missing. The status line and nonzero failure total are shown in **red and bold**, with a reason. Working rules in other processes remain documented. |
| **未阻断 — Not blocking** | The module/scope is disabled, valid loading evidence is missing, or installation state is unknown. Zero hits alone does not cause this state. |
| **未运行 — Not running** | No running process for the component was found in the current snapshot. Check again after it starts. |

Diagnostics match the current PID, process start identity, and compatible hook revision. Initialization records and recent hits are retained separately. Partial process coverage, skipped interfaces, and default-settings fallbacks remain visible. “Blocking” is not a claim that every data path is covered or that a server received nothing. Hit logs are sampled at 1, 64, 128, …; displayed totals are observed lower bounds.

### CI builds

Pushes to `main`, pull requests, and **Actions → Build → Run workflow** compile the project on Linux and Windows and run 50 policy/diagnostic checks. Download a `realme-privacy-guard-unsigned-*` artifact from a successful run for the APK, build hashes, and test results.

**CI produces an unsigned verification APK. It is not directly installable and cannot directly replace an installed signed APK.** The workflow does not use/upload signing private keys or publish releases. Build locally with the original signing identity for an installable update; do not uninstall the existing app to bypass a certificate mismatch and lose its configuration.

### Local builds

Install Python 3.10+, JDK 17, Android SDK platform 34, and build-tools 34.0.0. Set `JAVA_HOME` and `ANDROID_SDK_ROOT` (or `ANDROID_HOME`). Windows and Linux use the same command:

```sh
python build.py --unsigned
```

The compile-only Xposed API 82 dependency is downloaded from the mirror pinned in `dependencies.json` and checked against its fixed SHA-256. It is never included in the APK. Outputs go to `build/<timestamp>/`; Gradle and Android Studio are not required.

For a signed build, store the signing JSON example shown above outside the repository, set its password environment variables, and run:

```sh
python build.py --signing-config /path/outside/repository/signing.json
```

The optional `expected_certificate_sha256` field enforces a particular signing certificate. Keep keystores, passwords, and signing configuration out of Git.

### Settings and compatibility

The nine boolean settings are kept in private device-protected storage and exposed to authorized target UIDs through a restricted read-only ContentProvider. Components sharing a system UID share that authorization boundary. The module requests no Internet permission and no longer uses the deprecated New XSharedPreferences extension; it still uses the classic hooking API, not the Modern Xposed API.

Upgrades from early New XSharedPreferences builds need their old settings migrated first. The module can import its own private `rpg_settings_migration.json` file. In-place updates from version 1.1.1 onward retain the private settings.

Obfuscated SystemHelper and ad-adapter hooks use audited APK sizes as compatibility guards, not authenticity checks. Re-audit after system-component updates. CI checks do not replace device validation. Device logs, extracted system APKs, and signing materials are not published in this repository.

## References / 参考

- [LSPosed API and preference-sharing documentation](https://github.com/LSPosed/LSPosed/wiki/Develop-Xposed-Modules-Using-Modern-Xposed-API)
- [Using the Xposed Framework API](https://github.com/rovo89/XposedBridge/wiki/Using-the-Xposed-Framework-API)
- [Xposed API reference](https://api.xposed.info/)
- [Android Direct Boot](https://developer.android.com/privacy-and-security/direct-boot)
- [Third-party notices](THIRD_PARTY_NOTICES.md)
