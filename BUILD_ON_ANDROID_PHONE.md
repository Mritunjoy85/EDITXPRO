# Build APK Using ONLY an Android Mobile Phone (No PC Needed)

## Method 1: 1-Click Free Cloud Build with GitHub Actions (Easiest)
1. Download the Project ZIP on your phone.
2. Go to github.com on your phone browser, create a repository named 'editx-pro'.
3. Upload the files including '.github/workflows/build_apk.yml'.
4. Tap 'Actions' -> 'Build EditX Pro APK'.
5. Once built (approx 2 mins), tap 'Artifacts' -> 'EditXPro-Debug-APK'.
6. Direct download of the installable 'app-debug.apk' right onto your phone!

## Method 2: Offline Build on Android with Termux
1. Install Termux from F-Droid (free).
2. Run:
   pkg update -y
   pkg install openjdk-17 gradle -y
   termux-setup-storage
3. Extract project into ~/storage/downloads/editx/android
4. Run:
   gradle assembleDebug
5. The compiled APK will be at app/build/outputs/apk/debug/app-debug.apk!