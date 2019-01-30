package raidzero.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Joystick;

import com.ctre.phoenix.motorcontrol.ControlMode;
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

    public void setMotor(TalonSRX target, int axis) {
        double axisValue = stick.getRawAxis(axis);
    	if (axisValue > 0.2 || axisValue < -0.2) {
            target.set(ControlMode.PercentOutput, axisValue);
        }
    }

    @Override
    public void testInit() {
    }

    @Override
    public void testPeriodic() {
    }

}
