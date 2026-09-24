# Pocket Cosmos development handoff

Build: JDK 21; `./gradlew testDebugUnitTest assembleDebug assembleRelease`. SDK 6.1.9; SpatialUI/PicoTheme only; retain static Noto Sans SC 500/600.

This repository builds only GameKind.B, package `com.kejing.spatialarcade.orbit`. Preserve simulation behavior. Keep all changing HUD text in one parent depth plane; no per-text alpha/depth animations. Keep debug receivers in src/debug only. Never commit signing keys, passwords, local.properties or logs containing device identifiers.

Detailed internal history is maintained separately in the private context repository.
