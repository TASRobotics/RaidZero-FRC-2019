package raidzero.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

/**
 * The main robot class.
 */
public class Robot extends TimedRobot {

    Joystick stick;
    @Override
    public void robotInit() {
        stick = new Joystick(0);
    }

    @Override
    public void autonomousInit() {
    }

    @Override
    public void autonomousPeriodic() {
    }

    @Override
    public void teleopInit() {
    }

    @Override
    public void teleopPeriodic() {
        
    }

    public void setMotor(TalonSRX a, int b) {
    	if (stick.getRawAxis(b) > 0.2 || stick.getRawAxis(b) < -0.2)
    	a.set(stick.getRawAxis(b));
    }

    @Override
    public void testInit() {
    }

    @Override
    public void testPeriodic() {
    }

}
