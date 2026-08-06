---
title: Tuning Overview
description: Why tuning matters, what Crawler tunes, and the order it does it in
---

# Tuning Overview

*Why tuning matters, what Crawler tunes, and why the order matters*

## Why tuning matters

Imagine walking toward a target while blindfolded, believing every stride is exactly 30 cm — but your real stride is 25 cm. You'd stop short every single time. A robot is exactly the same, except it can't feel the floor: it only knows encoder ticks and IMU readings.

Tuning teaches Crawler the truth about **your** robot:

- How wide the odometry wheels are apart (**track width**)
- How far the center wheel sits from the robot's center (**center offset**)
- How big the wheels really are and how many ticks a revolution produces (**wheel diameter / ticks per rev**)
- How much power is needed to overcome friction (**min power**)
- How aggressively to correct position and heading errors (**PID gains**)
- How fast to cruise and how far ahead to look (**move speed / follow distance**)

Without these, everything drifts — and the errors grow the farther the robot travels.

## What Crawler tunes

<div class="diagram" role="img" aria-label="Tuning stages: odometry, then PID, then path following">
<svg viewBox="0 0 760 190" xmlns="http://www.w3.org/2000/svg" font-family="'JetBrains Mono', monospace">
  <defs>
    <linearGradient id="sg" x1="0" y1="0" x2="1" y2="0">
      <stop offset="0" stop-color="#4ADE80"/><stop offset="1" stop-color="#22C55E"/>
    </linearGradient>
  </defs>
  <rect x="20"  y="40" width="220" height="110" rx="14" fill="#0f1720" stroke="#4ADE80" stroke-width="2"/>
  <rect x="270" y="40" width="220" height="110" rx="14" fill="#0f1720" stroke="#22C55E" stroke-width="2"/>
  <rect x="520" y="40" width="220" height="110" rx="14" fill="#0f1720" stroke="url(#sg)" stroke-width="3"/>
  <g fill="#E5E7EB" font-size="13" text-anchor="middle">
    <text x="130" y="72">1 · Odometry</text>
    <text x="130" y="94" fill="#9CA3AF" font-size="11">track width · center offset</text>
    <text x="130" y="112" fill="#9CA3AF" font-size="11">wheel diameter · ticks/rev</text>
    <text x="130" y="134" fill="#4ADE80" font-size="11">Steps 1–4</text>
    <text x="380" y="72">2 · Robot-relative PID</text>
    <text x="380" y="94" fill="#9CA3AF" font-size="11">drive/strafe Kp·Ki·Kd</text>
    <text x="380" y="112" fill="#9CA3AF" font-size="11">steer P·I·D · min power</text>
    <text x="380" y="134" fill="#4ADE80" font-size="11">Step 5</text>
    <text x="630" y="72">3 · Path following</text>
    <text x="630" y="94" fill="#9CA3AF" font-size="11">move speed · turn speed</text>
    <text x="630" y="112" fill="#9CA3AF" font-size="11">follow distance · thresholds</text>
    <text x="630" y="134" fill="#4ADE80" font-size="11">Steps 6–7</text>
  </g>
  <g stroke="#4ADE80" stroke-width="3" fill="none">
    <path d="M240 95 H270"/><path d="M490 95 H520"/>
  </g>
</svg>
</div>

| Area | What it controls | Used by |
|---|---|---|
| **Odometry** | How accurately the robot knows where it is | Everything |
| **Robot-relative PID** | How precisely `drive`, `strafe`, and `turn` stop on target | `RobotOrientedDrive`, TeleOp precision |
| **Path following** | How smoothly the robot tracks waypoint paths | `FOFollower` / pure pursuit |

## Why the order matters

You cannot tune PID before odometry is accurate — the PID loop measures its error from odometry, so bad odometry looks like a bad PID controller. And you can't tune path following before the PID loop is stable, because pure pursuit commands drive through the same motors.

The tuner locks the order so the foundation is always solid first:

```
Odometry (steps 1–4)  →  PID (step 5)  →  Path following (steps 6–7)
```

## The tuner workflow

1. **Run the tuner** — a TeleOp named **Crawler Tuner**
2. **Adjust values** — with the gamepad (D-pad + RB) or by typing into **FTC Dashboard** → `Crawler Tuner`
3. **Test** — each step runs a real test and reports the result on the Driver Station
4. **Copy** — press **Square** (or finish Step 7) to print the tuned builder lines
5. **Paste** — into the tuned section of `MyRobot.builder()`, rebuild, redeploy
6. **Verify** — run the **Crawler Smoke Test** and **Crawler System Test**

## Before you start

- ✓ Robot fully wired; all device names in `MyRobot.java` match the Driver Hub configuration
- ✓ Odometry pods mounted, plugged in, spinning freely
- ✓ 3×3 m clear floor space
- ✓ Battery above 80% (voltage affects power — tune at competition conditions)
- ✓ FTC Dashboard open on a laptop on the robot's WiFi

> 💡 **Tune at competition voltage.** A low battery changes how much power "0.5" actually delivers. If your robot feels different on match day, re-check the PID and min power values.

---

## Next Steps

**[Start tuning →](tuning.md)** The 7 steps and gamepad controls
