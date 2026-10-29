# Build & Run EditX Pro on Android Phone

## 1. Using Android Studio
1. Open Android Studio -> Open project -> Select 'android/' folder.
2. Build -> Build Bundle(s) / APK(s) -> Build APK(s).
3. APK will be at app/build/outputs/apk/debug/app-debug.apk.

## 2. Using Terminal
```bash
cd android
./gradlew assembleDebug
```

## 3. Install via ADB
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```