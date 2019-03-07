package raidzero.robot.auto;

import raidzero.robot.components.Components;
import raidzero.pathgen.Point;

public class Auto {

    private static MotionProfile profile;
    private static Point[] points = {
        new Point(66, 213, 0),
        new Point(124, 213, 0),
        new Point(275, 272, 90),
        new Point(256, 310, 150),
        // new Point(252.34, 231.5),
        // new Point(385.74, 210.54)
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
        Components.getBase().getLeftMotor().setSelectedSensorPosition(0);
        Components.getBase().getRightMotor().getSensorCollection().setQuadraturePosition(0, 10);
        Components.getBase().getPigeon().setYaw(0);
        profile.reset();
        profile.start(points, 15, 20);
    }

    /**
     * Runs the autonomous code.
     *
     * <p>This should be called repeatedly during autonomous mode.
     */
    public static void run() {
        profile.move();
        profile.controlMP();
    }
}
