package raidzero.robot.auto;

import raidzero.robot.components.Components;
import raidzero.robot.teleop.Teleop;
import raidzero.robot.vision.Vision;

import java.util.ArrayList;
import java.util.List;

import com.ctre.phoenix.motion.SetValueMotionProfile;

import raidzero.pathgen.Point;

public class Auto {

    private static final double CRUISE_VELOCITY = 10;
    private static final double TARGET_ACCELERATION = 20;

    private static MotionProfile profile;
    private static List<Point[]> pathWayPoints;
    private static int stage;
    private static boolean usingVisionSpline;

    private static Point[] level1Left = {
        new Point(66, 213, 0),
        new Point(124, 213, 0),
        new Point(275, 272, 90),
        new Point(256, 306, 150),
    };

    private static Point[] level2Left = {
        new Point(22, 213, 0),
        new Point(124, 213, 0),
        new Point(275, 272, 90),
        new Point(256, 306, 150),
    };

    private static Point[] level1Right = {
        new Point(66, 111, 0),
        new Point(124, 111, 0),
        new Point(275, 52, -90),
        new Point(256, 18, -150),
    };

    private static Point[] level2Right = {
        new Point(22, 111, 0),
        new Point(124, 111, 0),
        new Point(275, 52, -90),
        new Point(256, 18, -150),
    };

    /**
     * Initialize the auto-specific components.
     *
     * <p>Should be called when the robot starts up.
     */
    public static void initialize() {
        profile = new MotionProfile(Components.getBase().getRightMotor(),
            Components.getBase().getLeftMotor(), Components.getBase().getPigeon());
    }

    /**
     * Configures the components for use in autonomous mode.
     *
     * <p>This should be called once every time the robot is switched to autonomous mode, before
     * calling {@link #run()}.
     */
    public static void setup() {
        stage = 0;
        pathWayPoints = new ArrayList<Point[]>();
        usingVisionSpline = false;

        // Reset encoders and motion profile
        Components.getBase().getLeftMotor().setSelectedSensorPosition(0);
        Components.getBase().getRightMotor().getSensorCollection().setQuadraturePosition(0, 10);
        Components.getBase().getPigeon().setYaw(0);
        profile.reset();

        // Read waypoints
        // Not done yet

        // Code below is temporary
        // Create empty paths
        Point[] path0 = level2Left;
        pathWayPoints.add(path0);
        profile.start(pathWayPoints.get(0), CRUISE_VELOCITY, TARGET_ACCELERATION);
    }

    /**
     * Runs the autonomous code.
     *
     * <p>This should be called repeatedly during autonomous mode.
     */
    public static void run() {
        if (stage < pathWayPoints.size()) {
            profile.controlMP();
            profile.move();
            if (profile.getSetValue() == SetValueMotionProfile.Hold) {
                stage++;
                usingVisionSpline = false;
                if (stage < pathWayPoints.size()) {
                    profile.start(pathWayPoints.get(stage), CRUISE_VELOCITY, TARGET_ACCELERATION);
                } else {
                    Teleop.setup();
                }
            }
            if (!usingVisionSpline && profile.getProgress() > 0.95) {
                Vision.pathToTarg(
                    Components.getBase().getPigeon().getFusedHeading(),
                    profile.getTargetPoint().angle
                ).ifPresent(waypoints -> {
                    usingVisionSpline = true;
                    profile.start(waypoints, CRUISE_VELOCITY, TARGET_ACCELERATION);
                });
            }
        } else {
            Teleop.run();
        }

    }
}
