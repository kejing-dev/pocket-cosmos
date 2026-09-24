# Pocket Cosmos | 口袋宇宙弹球

[English](README.md) · [下载 release APK](https://github.com/kejing-dev/pocket-cosmos/releases/latest)

适用于 PICO shared space 的轻量手势游戏。

![原生游戏截图](docs/images/gameplay.png)

## 玩法

- 拉动弹簧火箭活塞发射，进入迷你宇宙。
- 恒星、运动中的卫星、引力弹弓、彗星、虫洞与星云影响球路和计分。
- 按住一个实体踏板产生连续、非实体的引力场。初始 10 格，每秒消耗 1 格。
- 优先消耗能量，耗尽后每 50 分购买一格；松开后每 4 秒充一格，不透支分数。
- 点亮三个信标触发 8 秒 Nova 倍率；底部回收轨道把球送回发射区。
- 一个顶栏合并上次/本次事件、得分、能量和静音。

## 构建

使用 JDK 21、Android SDK platform 35 和仓库中的 Gradle wrapper。项目保持 PICO Spatial SDK / SpatialUI **6.1.9**，使用 Kotlin 与 Noto Sans SC。

```sh
./gradlew testDebugUnitTest assembleDebug
./gradlew assembleRelease
```

Windows 下使用 `gradlew.bat`。通过 `ANDROID_HOME` 或未跟踪的 `local.properties` 配置 Android SDK。
需要签名时在本地设置 `ARCADE_KEYSTORE_PATH`、`ARCADE_KEYSTORE_PASSWORD`、`ARCADE_KEY_ALIAS`、`ARCADE_KEY_PASSWORD`；缺省时生成未签名的 release APK。密钥与密码不提交到仓库。

发布附件是关闭调试标记、使用专用发行密钥签名的 release APK。签名与此前开发版不同，不能直接覆盖旧 debug 安装；替换开发版前请自行备份需要的数据。

## 源码与验证

`app/src/main` 为 Kotlin 应用及当前游戏的运行资源，`models` 为 Blender 源模型。模拟库及回归测试延续四应用原型的共享实现，因此源码保留未启用的游戏分支；本仓库只构建当前应用，不打包其他应用的模型或音乐媒体。

构建、测试、签名和 APK 校验见 [验证记录](docs/VALIDATION.md)。截图来自已验证开发基线的原生运行画面，不代表真实手势验收。真实手部追踪、主观音效、双目舒适度和长时性能仍需设备测试。

## 资源说明

仓库含原创 GLB / Blender 游戏模型；Noto Sans SC 遵循 [SIL OFL](licenses/NotoSansSC-OFL.txt)。PICO SDK 与 Android 依赖分别遵循其许可。签名密钥、设备标识和完整开发对话不放入应用仓库。

[Retained regression score attribution / 回归数据曲谱来源](licenses/music-scores.md)
