package raidzero.robot;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.TimedRobot;
import raidzero.robot.auto.Auto;
import raidzero.robot.components.Components;
import raidzero.robot.teleop.Teleop;

import raidzero.robot.vision.Vision;

/**
 * The main robot class.
 */
public class Robot extends TimedRobot {

    /**
     * Initializes everything.
     */
    @Override
    public void robotInit() {
        Components.initialize();
        Auto.initialize();
        Teleop.initialize();
    }

    /**
     * Runs setup code for autonomous mode.
     *
     * <p>This is called once when autonomous mode begins.
     */
    @Override
    public void autonomousInit() {
        Vision.setup();
        Auto.setup();
        // NetworkTableInstance.getDefault().getTable("limelight-kaluza").getEntry("pipeline").setNumber(2);
        NetworkTableInstance.getDefault().getTable("limelight-kaluza").getEntry("camMode").setNumber(0);
        NetworkTableInstance.getDefault().getTable("limelight-kaluza").getEntry("stream").setNumber(1);

    }

    /**
     * Runs periodic code for autonomous mode.
     *
     * <p>This is called repeatedly during autonomous mode.
     */
    @Override
    public void autonomousPeriodic() {
        Auto.run();
    }

    /**
     * Runs setup code for teleop mode.
     *
     * <p>This is called once when teleop mode begins.
     */
    @Override
    public void teleopInit() {
        Teleop.setup();
        // NetworkTableInstance.getDefault().getTable("limelight-kaluza").getEntry("pipeline").setNumber(2);
        NetworkTableInstance.getDefault().getTable("limelight-kaluza").getEntry("camMode").setNumber(0);
        NetworkTableInstance.getDefault().getTable("limelight-kaluza").getEntry("stream").setNumber(1);

    }

    /**
     * Runs periodic code for teleop mode.
     *
     * <p>This is called repeatedly during teleop mode.
     */
    @Override
    public void teleopPeriodic() {
        Teleop.run();
    }

    /**
     * Runs periodic code for disabled mode.
     *
     * <p>This is called repeatedly during disabled mode.
     */
    @Override
    public void disabledPeriodic() {
        Auto.disabled();
        // NetworkTableInstance.getDefault().getTable("limelight-kaluza").getEntry("pipeline").setNumber(2);
        NetworkTableInstance.getDefault().getTable("limelight-kaluza").getEntry("camMode").setNumber(0);
        NetworkTableInstance.getDefault().getTable("limelight-kaluza").getEntry("stream").setNumber(1);
    }

    /**
     * Runs setup code for test mode.
     *
     * <p>This is called once when test mode begins.
     */
    @Override
    public void testInit() {
        Vision.setup();
    }

    /**
     * Runs periodic code for test mode.
     */
    @Override
    public void testPeriodic() {
        var pigeon = Components.getBase().getPigeon();
        pigeon.setYaw(0);
        double[] xs = new double[3];
        pigeon.getYawPitchRoll(xs);
        System.out.println(xs[0]);
        Vision.pathToTarg(xs[0]);
    }

}
