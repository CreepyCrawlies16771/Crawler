---
title: Installation
description: Get Crawler into your FTC project in a few minutes
---

# Installation

*Getting Crawler into your Android Studio project*

## What you need

- **Android Studio** Ladybug (2024.2) or newer
- **Java** — you've written at least one OpMode before
- A **Rev Control Hub** or legal robot controller to deploy to

## Crawler is source, not a dependency

Crawler ships as **Java source inside the FTC SDK repo** — there is no JitPack artifact to add. The `Crawler` package lives at:

```
TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Crawler/
```

Because it's source, you can read every file, step through it in the debugger, and modify it — that's the whole point of a learning library.

> **We're keeping Crawler source-only for now!** - Being able to read, step through, and modify every line is the while point of a learning library. A dependency-based install (Jitpack) may come later as an alternative for teams that just want the pathing math without the source, but source-copy will stay the default way to get started

## Step 1: Get the code

There are two ways to get Crawler:

### Option A — Starter project (easiest)

The [`starter` branch](https://github.com/CreepyCrawlies16771/Crawler/tree/starter) is a clean, ready-to-build FTC project with Crawler already installed — you only write your robot:

```bash
git clone -b starter https://github.com/CreepyCrawlies16771/Crawler.git
cd Crawler
```

### Option B — The library repo

Clone the full repo and copy the `Crawler` package into your own FTC project:

```bash
git clone https://github.com/CreepyCrawlies16771/Crawler.git
cd Crawler
```

If you don't use git, click **Download ZIP** on the repo page and unzip it.

## Step 2: Open it in Android Studio

1. **File → Open** and select the `Crawler` folder
2. Let Gradle sync finish (it downloads the FTC SDK, FTCLib, and FTC Dashboard — a few minutes the first time)
3. You should see two modules: `FtcRobotController` and `TeamCode`

> ⚠️ **First sync is slow — that's normal.** It can take 10–20+ minutes on a slow connection or a machine that has never built an FTC project before (no cached dependencies). It's downloading the SDK, not a error or a problem. This is excluded form the startup time, since it is out side of our control!.

> 💡 **Chose Option B?** Copy the `Crawler` folder into your `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/` directory, and add these to `TeamCode/build.gradle` if they aren't there already:

```gradle
dependencies {
    implementation project(':FtcRobotController')
    implementation 'org.ftclib.ftclib:core:2.1.1'
    implementation 'com.acmerobotics.dashboard:dashboard:0.5.1'
}
```

## Step 3: Connect and deploy

1. Connect the robot controller to your computer over USB
2. Open the example file `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Teamcode/Examples/ExampleAuto.java` — that `Teamcode/` folder is *your* code, deliberately kept separate from the `Crawler/` library package, so you can edit it freely
3. Press the green ▶ next to the class, pick your device, and deploy
4. On the robot, open the FTC app → select **Example Auto** → **Play**

Your robot should drive a small path. If motors spin the wrong way, that's what [Setup](setup.md) fixes.

## Step 4: Make it yours

1. Edit the device-name constants at the top of `Teamcode/Examples/MyRobot.java` so they match your Driver Hub configuration
2. Run the [Crawler Tuner](tuning.md) once before your first real autonomous

---

## Next Steps

**[Setup →](setup.md)** Create `MyRobot.java`, pick a localizer, and fix motor directions
