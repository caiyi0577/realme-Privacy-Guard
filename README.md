# Realme Privacy Guard / ������˽��ʿ

[![Build](https://github.com/caiyi0577/realme-Privacy-Guard/actions/workflows/build.yml/badge.svg)](https://github.com/caiyi0577/realme-Privacy-Guard/actions/workflows/build.yml)

[����](#����) �� [English](#english)

## ����

���� realme / OPPO ����Ԥ������� LSPosed ģ�飬ͨ�� Java Hook ������ȷ�ϵĲɼ��ӿں��ϱ���ڡ���ǰ�汾 **1.1.5**������ `com.caiyi.rpg`��

����Ŀ�� realme GT8 Pro��RMX5200���ĵ����Ų������䷢չ�������Ѻ˶Ե�ϵͳΪ Android 17 / API 37��`RMX5200_17.0.0.106(CN01)`��**����֤�������������͡������̼�����º��ϵͳ�����** Ӧ�ý���ĿǰΪ���ġ�

### ��������

| ���� | ���� | ����Ӱ�� |
| --- | --- | --- |
| �ǻ۽����ⷢ���� | ������ȷ�ϵ�λ��/Wi-Fi �ϴ�������ģ���ϴ���λ��ͳ���¼� | �ƶ�ѧϰ�����ֽ��� |
| ������Ƽ����󱣻� | ��������ư汾�Ĺ����������������֪�˵� | ��桢�Ƽ����� |
| ��־�ϴ����񱣻� | ����������־�ϴ����������񣬱���������־���� | ��ϱ����ϴ� |
| �豸��ʶ��λ�� | �����豸/SIM/��վ��Ϣ����λ���� SystemHelper ע����� | ������λ����Ӫ������ |
| ¼��������ʹ�ü�¼ | ����ԭ��Ŀ���¼��������ʹ�ü�¼�����ϰ��б�ö�� | �����Ƶ��ͳ�ƹ��� |
| Ӧ���б����� | �ɼ���Ŀ�귵�ؿ��б���������Ŀ�������ϵͳӦ�� | Ӧ��ɨ�� |
| ��װ��Ԫ��Ϣ | ���ư�װ��Ԫ��Ϣ����װ��Դ��ѯ | ��װ��Դʶ�� |
| �豸״̬����ֵ | ����Ŀ������ڵľɰ��豸״̬αװ | ���ּ���߼� |
| ��֪�ϱ��˵㱣�� | �� Java DNS / URL ����ǰ������֪�˵� | ��Ӧ�������� |

���򲻻������ʵ����״̬��Java �������ز���������ԭ�����磬Ҳ����֤��ֹ��Ȩ�����������з����ϴ��߼���������֤����Ӧ�����ѷ��͵���������

### ��װ������

1. ׼���Ѱ�װ���� LSPosed ���豸���Լ�**ʹ����ȷǩ���� APK**��ģ����� Android �汾Ϊ 10��API 29����ʹ�þ��� Xposed Java API 82��
2. ��װ APK���� LSPosed ������ģ�飬ѡ����Ҫ�������Ƽ�������
3. �����ص�������Ϊ **�ǻ۽���** `com.oplus.pantanal.ums`��**�ǻ��Ƽ�** `com.opos.ads`��**��־����** `com.oplus.olc`���ѽ��õ� RegService �������ֽ��á�
4. �״ΰ�װ���޸����ع��������Ŀ����̻��ֻ������ر仯ͬ����Ŀ������´μ���ʱ��Ч��
5. ��ģ�鲢�����Լ졣��ȡϵͳ״̬��ģ���¼���Լ���Ҫ����ģ�� root ��Ȩ��

1.1.3�C1.1.5 �������Լ�����ʾ������ 1.1.2 �����ع���汾���Ѽ���ͬһ����汾�Ľ��̣�����Ϊ�˽�������ٴ���������

<details>
<summary>ȫ�� 17 ���Ƽ������� / All 17 recommended scope packages</summary>

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

### �Լ�״̬

| ״̬ | ���� |
| --- | --- |
| **���** | ���ع����Ѱ�װ����������ʵ�����м�¼��������ѽ��á��Ƿ񴥷����ϱ������д�������˵���� |
| **���ʧ�ܣ���ɫ���壩** | Hook ��װ����ʼ����������ؼ����ع���ȱʧ��ͬʱ��ʾ����ԭ�򡣲��ֹ�����������ʱҲ��ע���� |
| **δ���** | ģ��δ���á�������δ��ѡ��������Ч����֤�ݻ�װ״̬δ֪�ȡ�û�����м�¼���ᵥ����ɴ�״̬�� |
| **δ����** | ���ο�����δ���ָ���������н��̣�������������֤�� |

�Լ�˶Ե�ǰ PID��������ʶ�����ݹ���汾���ֱ�����ʼ����������м�¼������̸��ǲ��������ӿ�������Ĭ�����õ����������ʾ������ϡ�����������ͨ·�������ǣ�Ҳ��֤���������Ƿ��յ����ݡ�������־�� 1��64��128����������ʾ�����Ѽ�¼�������ޡ�

### �Զ�����������

���͵� `main`���ύ PR ���ֶ����� **Actions �� Build**������ Linux �� Windows �ϱ��벢���� 50 ���������ϼ�顣�ɹ������� `realme-privacy-guard-unsigned-*` ��������ں� APK��У����Ϣ�Ͳ��Խ����

**CI ������δǩ������֤ APK������ֱ�Ӱ�װ��Ҳ����ֱ�Ӹ����ֻ��ϵ�ǩ���汾��** ��ǰ���̲�ʹ�û��ϴ�ǩ��˽Կ�����Զ����� Release����Ҫ�ɰ�װ�汾ʱ��ʹ��ԭǩ���ڱ��ع�������Ҫж��ԭ�����ƹ�ǩ����ƥ�䣬����ᶪʧԭ���á�

### ���ع���

��Ҫ Python 3.10+��JDK 17���Լ� Android SDK `platforms;android-34`��`build-tools;34.0.0`������ `JAVA_HOME` �� `ANDROID_SDK_ROOT`���� `ANDROID_HOME`����Windows �� Linux ʹ��ͬһ���

```sh
python build.py --unsigned
```

�������� Xposed API 82 ��� `dependencies.json` ָ���ľ������ز�У��̶� SHA-256�������ڱ��룬������� APK�����λ�� `build/����ʱ��/`������Ҫ Gradle �� Android Studio��

����ǩ��ʱ�������� JSON ������**�ֿ�֮��**�����ö�Ӧ���뻷��������ִ�У�

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

������ `expected_certificate_sha256` �ֶΣ�Ҫ��ǩ��֤����ָ��ָ��һ�¡���Կ�������ǩ�����ò�Ӧ�ύ���ֿ⡣

### �����������

���ñ�����Ӧ��˽�е��豸���ܴ洢��ͨ������ֻ�� ContentProvider ��Ŀ�� UID �ṩ�Ÿ��������ء�����ͬһϵͳ UID ��������ø÷��ʱ߽硣ģ�鲻��������Ȩ�ޣ�����ʹ�������õ� New XSharedPreferences ��չ���ⲻ������Ǩ�Ƶ� Modern Xposed API��

������ New XSharedPreferences �汾��������Ҫ��Ǩ�ƾ����ã�Դ��֧�ֶ�ȡ��ģ��˽��Ŀ¼�е� `rpg_settings_migration.json`���� 1.1.1 ���Ժ��˽�����ð汾���Ǹ��»ᱣ�����ء�

SystemHelper ���ǻ��Ƽ��Ļ�����ڴ����������� APK ��С�ļ����Լ�飻��С���ǿ��Ŷ�֤����ϵͳ������º�Ӧ���º˶ԡ��ƶ˲��Բ���������֤���豸��־����ȡ��ϵͳ APK��ǩ����Կ������ֿⷢ����

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

Versions 1.1.3�C1.1.5 update diagnostics and presentation while retaining hook revision 1.1.2. Processes already running that hook revision do not need another soft reboot solely for the display update.

### Self-check states

| App label | Meaning |
| --- | --- |
| **��� �� Blocking** | Blocking hooks are installed and ready, actual hits have been recorded, or the component is disabled. Hit counts separately show whether a rule has been triggered. |
| **���ʧ�� �� Blocking failed** | Hook installation/initialization failed or required rules are missing. The status line and nonzero failure total are shown in **red and bold**, with a reason. Working rules in other processes remain documented. |
| **δ��� �� Not blocking** | The module/scope is disabled, valid loading evidence is missing, or installation state is unknown. Zero hits alone does not cause this state. |
| **δ���� �� Not running** | No running process for the component was found in the current snapshot. Check again after it starts. |

Diagnostics match the current PID, process start identity, and compatible hook revision. Initialization records and recent hits are retained separately. Partial process coverage, skipped interfaces, and default-settings fallbacks remain visible. ��Blocking�� is not a claim that every data path is covered or that a server received nothing. Hit logs are sampled at 1, 64, 128, ��; displayed totals are observed lower bounds.

### CI builds

Pushes to `main`, pull requests, and **Actions �� Build �� Run workflow** compile the project on Linux and Windows and run 50 policy/diagnostic checks. Download a `realme-privacy-guard-unsigned-*` artifact from a successful run for the APK, build hashes, and test results.

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

## References / �ο�

- [LSPosed API and preference-sharing documentation](https://github.com/LSPosed/LSPosed/wiki/Develop-Xposed-Modules-Using-Modern-Xposed-API)
- [Using the Xposed Framework API](https://github.com/rovo89/XposedBridge/wiki/Using-the-Xposed-Framework-API)
- [Xposed API reference](https://api.xposed.info/)
- [Android Direct Boot](https://developer.android.com/privacy-and-security/direct-boot)
- [Third-party notices](THIRD_PARTY_NOTICES.md)
