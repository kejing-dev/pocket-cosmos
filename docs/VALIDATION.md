# Validation / 验证记录

- Release build: **v0.11.0**, versionCode **11**, arm64-v8a, non-debuggable.
- Tests: **75 passed**, 0 failures, 0 errors.
- APK signature verified with Android apksigner. Public certificate: [signature.txt](signature.txt).
- APK SHA-256: `3530d07ca207ed2a91c606ccfd7c43b1817165471f97b3e6fc83861dfcfeb971`.
- Only this game's runtime media is included; other games' simulation code remains in the shared regression library.
- Native screenshots come from this standalone debug build with v0.11 gameplay. Release compilation, signature, manifest and tests were checked separately. Physical hand tracking, ray dragging and long-session performance are not claimed as release acceptance.

本次生成独立项目、正式签名的 release APK。已核对构建类型、签名、包名、资源范围和 75 项测试。截图来自本次独立项目的 debug 构建，保留 v0.11 玩法；发行包的真实手势仍需设备验收。

See [machine-readable evidence](build-verification.json).

## Native HUD regression

The standalone debug build completed 320 HUD updates at a fixed camera. The checked HUD region had zero differing pixels above the 16/255 threshold. [Evidence](hud-regression.json). The corresponding signed release is bound by hash; this comparison is not a release hand-input test.
