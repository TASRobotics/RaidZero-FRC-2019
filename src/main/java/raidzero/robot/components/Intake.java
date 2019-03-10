package raidzero.robot.components;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.LimitSwitchNormal;
import com.ctre.phoenix.motorcontrol.LimitSwitchSource;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

/**
 * The intake (hook and wheels).
 */
public class Intake {

    private static final double HOOK_POWER = 1.0;
    private TalonSRX intakeMotor;
    private TalonSRX hookMotor;

    /**
     * Constructs an intake object.
     *
     * @param wheelId the ID for the intake motor
     * @param hookID the ID for the hook motor
     */
    public Intake(int wheelId, int hookID) {
        intakeMotor = new TalonSRX(wheelId);
        hookMotor = new TalonSRX(hookID);

        hookMotor.configForwardLimitSwitchSource(LimitSwitchSource.FeedbackConnector,
            LimitSwitchNormal.NormallyOpen);
        hookMotor.configReverseLimitSwitchSource(LimitSwitchSource.FeedbackConnector,
            LimitSwitchNormal.NormallyOpen);

        hookMotor.setNeutralMode(NeutralMode.Brake);
        intakeMotor.setNeutralMode(NeutralMode.Brake);

        hookMotor.setInverted(true);
    }

    /**
     * Returns the hook motor object.
     *
     * @return the hook motor object
     */
    public TalonSRX getHookMotor() {
        return hookMotor;
    }

    /**
     * Returns the intake motor object.
     *
     * @return the intake motor object
     */
    public TalonSRX getIntakeMotor() {
        return intakeMotor;
    }

    /**
     * Grabs using the hook.
     */
    public void grab() {
        hookMotor.set(ControlMode.PercentOutput, HOOK_POWER);
    }

    /**
     * Releases the hook.
     */
    public void release() {
        hookMotor.set(ControlMode.PercentOutput, -HOOK_POWER);
    }

    /**
     * Stops the hook.
     */
    public void stopHook() {
        hookMotor.set(ControlMode.PercentOutput, 0);
    }

    /**
     * Runs the wheels in.
     */
    public void runWheelsIn(double power) {
        intakeMotor.set(ControlMode.PercentOutput, power);
    }

    /**
     * Runs the wheels out.
     */
    public void runWheelsOut(double power) {
        intakeMotor.set(ControlMode.PercentOutput, -power);
    }

    /**
     * Stops the wheels.
     */
    public void stopWheels() {
        intakeMotor.set(ControlMode.PercentOutput, 0);
    }

}
