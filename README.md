# Snake Classic

A simple classic Snake Android game for sideload testing first, then possible Google Play publishing.

## Features

- Main menu and options menu
- Swipe, on-screen button, or tap-turn controls
- Easy, normal, and hard difficulty/speed
- Neon, classic, and ocean themes
- Compact or tall board layout
- Sound effects toggle
- Ad placeholder toggle; real ads require Google AdMob IDs later
- Friends screen with generated 6-character share code
- Android share sheet invite text with future deep-link format
- Join-code placeholder for later backend/online leaderboard
- In-game pause button with resume/settings/menu
- Changing difficulty from a paused game resets the current score/game with warning
- Other settings can be changed from pause without resetting current game
- Friends screen controls shifted lower to reduce overlap
- Wall mode option: Solid walls or Wrap through walls
- Separate high scores per difficulty + wall mode combination
- Fixed compact layout board-boundary issue by resetting board-affecting settings and guarding food bounds

## Build debug APK

```bash
export JAVA_HOME=/home/chris/.local/opt/jdk-17
export ANDROID_HOME=/home/chris/.local/opt/android-sdk
export ANDROID_SDK_ROOT=$ANDROID_HOME
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:/home/chris/.local/opt/gradle-8.9/bin:$PATH
gradle assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`
