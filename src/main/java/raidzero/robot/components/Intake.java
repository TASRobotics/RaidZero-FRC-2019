package raidzero.robot.components;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import edu.wpi.first.wpilibj.DoubleSolenoid;

/**
 * The intake (hook and wheels).
 */
public class Intake {

    private static DoubleSolenoid.Value OPEN_POSITION = DoubleSolenoid.Value.kForward;
    private static DoubleSolenoid.Value CLOSED_POSITION = DoubleSolenoid.Value.kReverse;

    private TalonSRX intakeMotor;
    private DoubleSolenoid hook;

    /**
     * Constructs an intake object.
     *
     * @param intakeId the ID for the intake motor
     * @param forwardChannel the pneumatic channel for grabbing hook
     * @param backwardChannel the pneumatic channel for releasing hook
     */
    public Intake(int intakeId, int forwardChannel, int backwardChannel) {
        intakeMotor = new TalonSRX(intakeId);
        hook = new DoubleSolenoid(forwardChannel, backwardChannel);

        intakeMotor.setNeutralMode(NeutralMode.Brake);
    }

    public TalonSRX getIntakeMotor() {
        return intakeMotor;
    }

    /**
     * Grabs using the hook.
     */
    public void grab() {
        hook.set(CLOSED_POSITION);
    }

    /**
     * Releases the hook.
     */
    public void release() {
        hook.set(OPEN_POSITION);
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
