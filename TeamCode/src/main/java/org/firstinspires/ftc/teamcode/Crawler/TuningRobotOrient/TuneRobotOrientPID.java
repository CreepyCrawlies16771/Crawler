package org.firstinspires.ftc.teamcode.Crawler.TuningRobotOrient;

import androidx.annotation.NonNull;

import org.firstinspires.ftc.teamcode.Crawler.core.RobotConfig;
import org.firstinspires.ftc.teamcode.Crawler.RobotOrient.ROMovementEngine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * TuneRobotOrientPID
 *
 * Tuning OpMode for Crawler RO PID constants.
 * Cycles through forward, strafe, and turn tuning.
 * Persists tuned values to /sdcard/Crawler/tuned_pid.txt on the Control Hub.
 *
 * Controls:
 *   X            — cycle tuning mode
 *   Triangle     — exit OpMode
 *   Right Bumper — start selected tune
 *   D-pad Up     — overshoot (decrease Kp)
 *   D-pad Down   — undershoot (increase Kp)
 *   Circle       — accept and save current values
 *   D-pad Left   — re-run current tune
 */
public class TuneRobotOrientPID extends ROMovementEngine {

    // How much each button press adjusts the constant.
    // Separate step sizes per axis since their scales differ.
    private static final double DRIVE_STEP   = 0.01;
    private static final double STRAFE_STEP  = 0.01;
    private static final double TURN_STEP    = 0.005;

    // Where tuned values are saved on the Control Hub filesystem.
    private static final String SAVE_DIR  = "/sdcard/Crawler/";
    private static final String SAVE_FILE = SAVE_DIR + "tuned_pid.txt";

    // -----------------------------------------------------------------------
    // Entry point
    // -----------------------------------------------------------------------

    @Override
    public void runPath() throws InterruptedException {
        TuneState currentMode = TuneState.forwardTune;
        boolean endOpMode = false;

        telemetry.addLine("=== Crawler PID Tuner ===");
        telemetry.addLine("X: cycle mode  |  Triangle: exit  |  Right Bumper: start tune");
        telemetry.update();

        while (!endOpMode && opModeIsActive()) {

            if (gamepad1.triangle) {
                endOpMode = true;
                continue;
            }

            // Cycle mode — wait for release to avoid multiple triggers per press.
            if (gamepad1.x) {
                currentMode = nextMode(currentMode);
                waitForRelease(() -> gamepad1.x);
            }

            telemetry.addData("Mode", currentMode.label);
            telemetry.addLine("Press Right Bumper to start tune");
            telemetry.update();

            // Start the selected tune only on a fresh press.
            if (gamepad1.right_bumper) {
                waitForRelease(() -> gamepad1.right_bumper);
                switch (currentMode) {
                    case forwardTune:
                        runForwardTune();
                        break;
                    case sideTune:
                        runSideTune();
                        break;
                    case turnTune:
                        runTurnTune();
                        break;
                }
            }
        }

        telemetry.addLine("Tuning complete. Values saved to " + SAVE_FILE);
        telemetry.update();
        sleep(2000);
    }

    // -----------------------------------------------------------------------
    // Tune routines
    // -----------------------------------------------------------------------

    private void runForwardTune() throws InterruptedException {
        boolean tuning = true;
        while (tuning && opModeIsActive()) {
            telemetry.addLine("=== Forward Tune ===");
            telemetry.addData("Current drive Kp", RobotConfig.RobotOriented.Kp);
            telemetry.addLine("RB: drive 1 m  |  Up: overshoot  |  Down: undershoot");
            telemetry.addLine("Circle: accept & save  |  Triangle: back");
            telemetry.update();

            if (gamepad1.triangle) break;

            if (gamepad1.right_bumper) {
                waitForRelease(() -> gamepad1.right_bumper);
                drivePID(1, 0);
                sleep(1000);
                tuning = awaitFeedback(
                        () -> RobotConfig.RobotOriented.Kp -= DRIVE_STEP,
                        () -> RobotConfig.RobotOriented.Kp += DRIVE_STEP
                );
                if (!tuning) persistValues();
            }
        }
    }

    private void runSideTune() throws InterruptedException {
        boolean tuning = true;
        while (tuning && opModeIsActive()) {
            telemetry.addLine("=== Strafe Tune ===");
            telemetry.addData("Current strafe Kp", RobotConfig.RobotOriented.strafe_Kp);
            telemetry.addLine("RB: strafe 1 m  |  Up: overshoot  |  Down: undershoot");
            telemetry.addLine("Circle: accept & save  |  Triangle: back");
            telemetry.update();

            if (gamepad1.triangle) break;

            if (gamepad1.right_bumper) {
                waitForRelease(() -> gamepad1.right_bumper);
                strafePID(1, 0);
                sleep(1000);
                tuning = awaitFeedback(
                        () -> RobotConfig.RobotOriented.strafe_Kp -= STRAFE_STEP,
                        () -> RobotConfig.RobotOriented.strafe_Kp += STRAFE_STEP
                );
                if (!tuning) persistValues();
            }
        }
    }

    private void runTurnTune() throws InterruptedException {
        boolean tuning = true;
        while (tuning && opModeIsActive()) {
            telemetry.addLine("=== Turn Tune ===");
            telemetry.addData("Current STEER_P", RobotConfig.RobotOriented.STEER_P);
            telemetry.addLine("RB: turn 180 deg  |  Up: overshoot  |  Down: undershoot");
            telemetry.addLine("Circle: accept & save  |  Triangle: back");
            telemetry.update();

            if (gamepad1.triangle) break;

            if (gamepad1.right_bumper) {
                waitForRelease(() -> gamepad1.right_bumper);
                turnPID(180);
                sleep(1000);
                tuning = awaitFeedback(
                        () -> RobotConfig.RobotOriented.STEER_P -= TURN_STEP,
                        () -> RobotConfig.RobotOriented.STEER_P += TURN_STEP
                );
                if (!tuning) persistValues();
            }
        }
    }

    // -----------------------------------------------------------------------
    // Feedback loop
    // Returns true  → keep tuning (adjust was made, re-run)
    // Returns false → accepted (circle pressed, save and exit loop)
    // -----------------------------------------------------------------------

    /**
     * Blocks until the team indicates overshoot, undershoot, or acceptance.
     *
     * @param onOvershoot  runnable that decreases the constant
     * @param onUndershoot runnable that increases the constant
     * @return true if tuning should continue, false if accepted
     */
    private boolean awaitFeedback(Runnable onOvershoot, Runnable onUndershoot)
            throws InterruptedException {

        telemetry.addLine("Reset robot to start. Then:");
        telemetry.addLine("Up: overshoot  |  Down: undershoot  |  Circle: accept");
        telemetry.update();

        while (opModeIsActive()) {
            if (gamepad1.dpad_up) {
                onOvershoot.run();
                waitForRelease(() -> gamepad1.dpad_up);
                return true;  // re-run tune
            }
            if (gamepad1.dpad_down) {
                onUndershoot.run();
                waitForRelease(() -> gamepad1.dpad_down);
                return true;  // re-run tune
            }
            if (gamepad1.circle) {
                waitForRelease(() -> gamepad1.circle);
                return false; // accepted — exit loop
            }
        }
        return false;
    }

    // -----------------------------------------------------------------------
    // Persistence
    // -----------------------------------------------------------------------

    /**
     * Writes current RobotConfig PID values to the Control Hub filesystem.
     * File is human-readable so teams can copy values into RobotConfig manually
     * or Crawler can read them back on next boot (future hot-reload hook).
     */
    private void persistValues() {
        try {
            File dir = new File(SAVE_DIR);
            if (!dir.exists()) dir.mkdirs();

            FileWriter writer = getFileWriter();
            writer.close();

            telemetry.addLine("Saved to " + SAVE_FILE);
            telemetry.update();
            sleep(1500);

        } catch (IOException e) {
            telemetry.addData("Save failed", e.getMessage());
            telemetry.update();
            sleep(2000);
        }
    }

    @NonNull
    private static FileWriter getFileWriter() throws IOException {
        FileWriter writer = new FileWriter(SAVE_FILE, false); // overwrite
        writer.write("# Crawler tuned PID values\n");
        writer.write("# Copy these into RobotConfig, (or let HotReload pick them up) curretnly not avaliable.\n\n");
        writer.write("drive.Kp="    + RobotConfig.RobotOriented.Kp        + "\n");
        writer.write("strafe.Kp="   + RobotConfig.RobotOriented.strafe_Kp + "\n");
        writer.write("turn.STEER_P=" + RobotConfig.RobotOriented.STEER_P  + "\n");
        return writer;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Cycles to the next TuneState, wrapping around. */
    private TuneState nextMode(TuneState current) {
        TuneState[] values = TuneState.values();
        return values[(current.ordinal() + 1) % values.length];
    }

    /**
     * Spins until the provided condition is false.
     * Prevents a single button press from triggering multiple actions.
     */
    private void waitForRelease(BooleanSupplier condition) throws InterruptedException {
        while (condition.getAsBoolean() && opModeIsActive()) {
            sleep(20);
        }
        sleep(50); // debounce
    }

    //functional interface, avoids needing java.util.function on older SDK versions.
    @FunctionalInterface
    private interface BooleanSupplier {
        boolean getAsBoolean();
    }


    private enum TuneState {
        forwardTune("Forward (drive Kp)"),
        sideTune("Strafe (strafe Kp)"),
        turnTune("Turn (STEER_P)");

        final String label;
        TuneState(String label) { this.label = label; }
    }
}