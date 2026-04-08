---
title: Installation
description: Add Crawler to your FTC project in 4 simple steps
---

# Installation

*Getting Crawler into your Android Studio project*

## What You Need

Before you start, make sure you have:

- **Android Studio** (installed and working)
- **FTC SDK** (your team's GitHub fork cloned locally)
- **Basic Java** (you've written at least one OpMode before)

If you don't have the FTC SDK yet, ask your coach or check [FTC's official guide](https://github.com/FIRST-Tech-Challenge/FtcRobotController).

## Step 1: Add JitPack to Repositories

Open the `build.gradle` file in your project root (not the one inside `TeamCode`). Find the `repositories` block and add JitPack. It should look like this after:

```gradle
repositories {
    google()
    mavenCentral()
    maven { url "https://jitpack.io" }  // Add this line
}
```

**What this does:** JitPack is a service that hosts open source libraries. This tells Android Studio where to find Crawler when we ask for it.

## Step 2: Add Crawler Dependency

Open `TeamCode/build.gradle`. Find the `dependencies` block and add Crawler:

```gradle
dependencies {
    // ... your other dependencies ...
    implementation 'com.github.Fission310:Crawler:1.0.0'
}
```

**What this does:** This tells Android Studio to download Crawler and make it available to your code.

> 📝 **Note:** Replace `1.0.0` with whatever version of Crawler your team is using. Your coach can tell you which one.

## Step 3: Sync Gradle

Click **File → Sync Now** in Android Studio, or wait for the yellow notification bar to appear and click "Sync Now".

Your computer will download Crawler and all its dependencies. This might take 30-60 seconds the first time.

## Step 4: Verify

If Android Studio shows **no red errors** in your code, you're ready to go. If you see red wavy lines under anything in the editor, let your coach know.

---

## Next Steps

**[Setup →](setup.md)** Write your first three Crawler files: MyRobot, your autonomous, and your TeleOp
