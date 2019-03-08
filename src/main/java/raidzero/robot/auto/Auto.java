package raidzero.robot.auto;

import raidzero.robot.components.Components;
import raidzero.robot.teleop.Teleop;

import java.util.ArrayList;
import java.util.List;

import com.ctre.phoenix.motion.SetValueMotionProfile;

import raidzero.pathgen.Point;

public class Auto {

    private static MotionProfile profile;
    private static List<Point[]> pathWayPoints;
    private static int stage;

    // points is left here for now for single path testing in the future
    private static Point[] points = {
        new Point(66, 213, 0),
        new Point(124, 213, 0),
        new Point(275, 272, 90),
        new Point(256, 310, 150),
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

        // Reset encoders and motion profile
        Components.getBase().getLeftMotor().setSelectedSensorPosition(0);
        Components.getBase().getRightMotor().getSensorCollection().setQuadraturePosition(0, 10);
        Components.getBase().getPigeon().setYaw(0);
        profile.reset();

        // Read waypoints
        // Not done yet

        // Code below is temporary
        // Create empty paths
        Point[] path0 = points;
        pathWayPoints.add(path0);
        profile.start(pathWayPoints.get(0), 10, 20);
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
                profile.start(pathWayPoints.get(stage), 10, 20);
            }
        } else {
            Teleop.run();
        }

    }
}
