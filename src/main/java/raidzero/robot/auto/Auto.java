package raidzero.robot.auto;

import raidzero.robot.components.Components;
import raidzero.robot.teleop.Teleop;

import java.util.ArrayList;
import java.util.List;

import com.ctre.phoenix.motion.SetValueMotionProfile;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import raidzero.pathgen.Point;

public class Auto {

    private static SendableChooser<Point[]> choose;
    private static MotionProfile profile;
    private static List<Point[]> pathWayPoints;
    private static int stage;

    // points is left here for now for single path testing in the future
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

    private static Point[] level2LeftFront = {
        new Point(22, 213, 0),
        new Point(72, 213),
        new Point(156, 190),
        new Point(200, 173, 0),
    };

    private static Point[] level2RightFront = {
        new Point(22, 111, 0),
        new Point(72, 111),
        new Point(156, 134),
        new Point(200, 151, 0),
    };

    /**
     * Initialize the auto-specific components.
     *
     * <p>Should be called when the robot starts up.
     */
    public static void initialize() {
        choose = new SendableChooser<>();
        choose.setDefaultOption("Do nothing", null);
        choose.addOption("level 1 left", level1Left);
        choose.addOption("level 2 left", level2Left);
        choose.addOption("level 1 right", level1Right);
        choose.addOption("level 2 right", level2Right);
        choose.addOption("level 2 left front", level2LeftFront);
        choose.addOption("level 2 right front", level2RightFront);
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
        var selected = choose.getSelected();
        if (selected != null) {
            pathWayPoints.add(selected);
            profile.start(pathWayPoints.get(0), 10, 20);
        }
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
                if (stage < pathWayPoints.size()) {
                    profile.start(pathWayPoints.get(stage), 10, 20);
                } else {
                    Teleop.setup();
                }
            }
        } else {
            Teleop.run();
        }

    }
}
