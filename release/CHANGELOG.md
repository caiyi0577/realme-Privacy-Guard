## 中文

- 支持九类采集和上报保护开关。
- 自检区分阻断、阻断失败、未阻断、未运行；失败用红色粗体标注。
- 已就绪的 Hook 计为阻断，实际命中次数单独显示。
- 改进当前进程日志读取，保留初始化与近期命中记录。

**独立上架包 `io.github.caiyi0577.rpg`，不能覆盖旧包 `com.caiyi.rpg`。原版配置和作用域不会自动继承。请先停用旧模块，再启用本模块，避免双重 Hook。手机现有模块未被本次发布操作修改。新包已通过构建与规则测试，尚未在手机启用验证。**

## English

- Nine configurable collection/reporting protections.
- Four diagnostic states, with blocking failures highlighted in red and bold.
- Ready blocking hooks count as protection; actual hits remain separately visible.
- Improved current-process log selection and initialization/hit record retention.

**This is the separate `io.github.caiyi0577.rpg` distribution. It cannot update `com.caiyi.rpg` in place or automatically inherit its settings/scope. Disable the legacy module before enabling this one. Publishing does not modify the existing phone installation. This package passed build/policy checks but has not yet been activated on a device.**

Package: `io.github.caiyi0577.rpg`

APK SHA-256: `0721d27cbad3ddd32db447a07f42cd853a627d6d6f24349285ffd51c7880688a`
