# Third-party dependencies / 第三方依赖

## Xposed Framework API 82

- Upstream / 上游: https://github.com/rovo89/XposedBridge
- API documentation / API 文档: https://api.xposed.info/
- Coordinates / 坐标: `de.robv.android.xposed:api:82`
- SHA-256: `f48c635f1c7469fdec0e00ad2ea0b7a6b2f5b55065784a35b7ca3a84615e8e25`
- Retrieved from the Maven mirror recorded in `dependencies.json`. The historical Bintray distribution is no longer used.
- Compile-only: the API implementation and its JAR are not redistributed in the module APK or repository. The installed hooking framework supplies the runtime API.

该依赖通过 `dependencies.json` 中的 Maven 镜像获取，并在编译前核对固定哈希。API JAR 不随源码或 APK 分发，运行时 API 由设备上的 Hook 框架提供。

## Build tools / 构建工具

Android SDK tools, Eclipse Temurin/OpenJDK, Python, and GitHub Actions retain their respective licenses. They are installed in the build environment and are not bundled in this repository.

Android SDK、JDK、Python 和 GitHub Actions 均遵循各自许可证，本仓库不分发这些工具本体。
