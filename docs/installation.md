---
title: Installation
description: Get Crawler into your FTC project in a few minutes
---

# Installation

*Getting Crawler into your Android Studio project*

## What you need

- **Android Studio** Ladybug (2024.2) or newer
- **Java** — you've written at least one OpMode before
- A **Control Hub** or phone-based robot controller to deploy to

## Crawler is source, not a dependency

Crawler ships as **Java source inside the FTC SDK repo** — there is no JitPack artifact to add. The `Crawler` package lives at:

```
TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Crawler/
```

Because it's source, you can read every file, step through it in the debugger, and modify it — that's the whole point of a learning library.

## Step 1: Get the repo

```bash
git clone https://github.com/CreepyCrawlies16771/Crawler.git
cd Crawler
```

If you don't use git, click **Download ZIP** on the repo page and unzip it.

## Step 2: Open it in Android Studio

1. **File → Open** and select the `Crawler` folder
2. Let Gradle sync finish (it downloads the FTC SDK, FTCLib, and FTC Dashboard — a few minutes the first time)
3. You should see two modules: `FtcRobotController` and `TeamCode`

> ⚠️ **First sync is slow — that's normal.** It can take 10–20+ minutes on a slow connection or a machine that has never built an FTC project before (no cached dependencies). It's downloading the SDK, not a stuck build.

> 💡 **Already have your own FTC SDK project?** Copy the `Crawler` folder into your `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/` directory, and add these to `TeamCode/build.gradle` if they aren't there already:

```gradle
dependencies {
    implementation project(':FtcRobotController')
    implementation 'org.ftclib.ftclib:core:2.1.1'
    implementation 'com.acmerobotics.dashboard:dashboard:0.5.1'
}
```

## Step 3: Connect and deploy

1. Connect the robot controller to your computer over USB
2. Open the example file `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/TeamscodeNotLibrary/ExampleAuto.java` — that `TeamscodeNotLibrary/` folder is *your* code, deliberately kept separate from the `Crawler/` library package, so you can edit it freely
3. Press the green ▶ next to the class, pick your device, and deploy
4. On the robot, open the FTC app → select **Example Auto** → **Play**

Your robot should drive a small path. If motors spin the wrong way, that's what [Setup](setup.md) fixes.

## Step 4: Make it yours

1. Edit the device-name constants at the top of `TeamscodeNotLibrary/MyRobot.java` so they match your Driver Hub configuration
2. Run the [Crawler Tuner](tuning.md) once before your first real autonomous

## The docs site (for maintainers)

The `docs/` markdown files compile into a static site in `docs-html/`. `node_modules/` and `docs-html/` are gitignored — the site is a build artifact.

### Preview locally (with live reload)

```bash
npm install          # once
npm run serve        # build + serve at http://localhost:3000
PORT=8080 npm run serve   # custom port
```

Every time you save a markdown file the site rebuilds and every open browser tab reloads automatically. `npm start` / `npm run dev` are the same thing.

### Build only

```bash
npm run build        # regenerate docs-html/
npm run watch        # rebuild on every markdown save (no server)
```

## Hosting on GitHub Pages

The repo includes a workflow (`.github/workflows/pages.yml`) that builds `docs-html/` and deploys it to GitHub Pages automatically whenever you push:

1. Push this repo to GitHub
2. In the repo **Settings → Pages**, set **Source** to **GitHub Actions**
3. Push to `main` (or `master`/`dev`) — the workflow builds and deploys; your site appears at `https://<user>.github.io/<repo>/`
4. Re-trigger any time from the **Actions** tab → *Deploy docs to GitHub Pages* → **Run workflow**

No base-path configuration needed — every link in the generated site is relative, so it works on the project subpath.

> 💡 **Prefer a branch instead?** You can skip the workflow and push the built site to a `gh-pages` branch. Because `docs-html/` is gitignored, `git subtree push --prefix docs-html` won't work — commit the built folder explicitly instead:
> ```bash
> npm run build
> git checkout --orphan gh-pages       # fresh branch with no history
> git add -f docs-html                 # -f overrides the gitignore
> git commit -m "Build docs site"
> git push --force origin gh-pages
> git checkout dev                     # back to your working branch
> ```
> Then set **Settings → Pages → Source** to **Deploy from a branch** → `gh-pages`. Your `dev`/`main` history stays clean — the docs live only on the `gh-pages` branch.
>
> ⚠️ **Commit `package-lock.json`.** The Pages workflow runs `npm ci`, which requires the lockfile — it's not gitignored, so `git add package-lock.json` along with your normal changes.

---

## Next Steps

**[Setup →](setup.md)** Create `MyRobot.java`, pick a localizer, and fix motor directions
