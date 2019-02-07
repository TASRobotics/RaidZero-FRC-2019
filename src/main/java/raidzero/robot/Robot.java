package raidzero.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import raidzero.robot.auto.Auto;
import raidzero.robot.components.Components;
import raidzero.robot.teleop.Teleop;

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
        Auto.setup();
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
     * Runs setup code for test mode.
     * 
     * <p>This is called once when test mode begins.
     */
    @Override
    public void testInit() {
    }

    /**
     * Runs periodic code for test mode.
     */
    @Override
    public void testPeriodic() {
    }

}
