package raidzero.robot.components;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.LimitSwitchNormal;
import com.ctre.phoenix.motorcontrol.LimitSwitchSource;

public class Arm {

    private TalonSRX arm;
    private TalonSRX armFollower;

    private static final int FORWARD = 0;
    private static final int MIDDLE = 1;
    private static final int BACKWARD = 1;

    private static final int PID_X = 0;

    private static final int TARGET_VEL = 1;
    private static final int TARGET_ACCEL = 1;

    private static final double P_VALUE = 1.0;
    private static final double I_VALUE = 0.0;
    private static final double D_VALUE = 0.0;
    private static final double F_VALUE = 0.0;
    private static final int IZ_VALUE = 0;

    private static final int VEL_TOLERANCE = 1;
    private static final int POS_TOLERANCE = 1;

    /**
     * This enum contains the possible positions to go to
     */
    public enum Position {
        Front, Middle, Back
    }

    /**
    * Constructs the Arm object and configures the arm motor
    * 
    * @param armId the ID of the talon controlling the arm 
    * @param followerId the ID of the talon controlling the second arm motor
    */
    public Arm(int armId, int followerId) {
        arm = new TalonSRX(armId);
        armFollower = new TalonSRX(followerId);

        arm.setNeutralMode(NeutralMode.Brake);
        armFollower.setNeutralMode(NeutralMode.Brake);

        armFollower.follow(arm);

        //the tachs are daisy chained together
        //which solder pad is soldered will decide which one is forward and reverse
        arm.configForwardLimitSwitchSource(LimitSwitchSource.FeedbackConnector, 
            LimitSwitchNormal.NormallyClosed);
        arm.configReverseLimitSwitchSource(LimitSwitchSource.FeedbackConnector,
            LimitSwitchNormal.NormallyClosed);

        arm.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder);
        
        arm.configMotionCruiseVelocity(TARGET_VEL);
        arm.configMotionAcceleration(TARGET_ACCEL);

        arm.setSelectedSensorPosition(0);
        arm.setInverted(false);
        armFollower.setInverted(true);

        arm.config_kP(PID_X, P_VALUE);
        arm.config_kI(PID_X, I_VALUE);
        arm.config_kD(PID_X, D_VALUE);
        arm.config_kF(PID_X, F_VALUE);
        arm.config_IntegralZone(PID_X, IZ_VALUE);
    }

    /**
     * Resets encoder to a certain position
     * 
     * @param resetPos the position to reset to
     * resetPos may be changed into an enum, but for now a variable is fine
     */
    public void setEncoder(int resetPos) {
        arm.setSelectedSensorPosition(resetPos);
    }

    /**
     * Gets the encoder position
     * 
     * @return encoder position
     */
    public int getEncoder() {
        return arm.getSelectedSensorPosition(PID_X);
    }

    /** 
     * Returns the value of the reverse limit switch
     * 
     * @return Whether the reverse limit switch has been reached
     */
    private boolean getReverseLimit() {
        return arm.getSensorCollection().isRevLimitSwitchClosed();
    }

    /** 
     * Returns value of the forward limit switch
     * 
     * @return Whether the forward limit switch has been reached
     */
    private boolean getForwardLimit() {
        return arm.getSensorCollection().isRevLimitSwitchClosed();
    }

    /**
     * Check if the hard limit has been reached, 
     * and reset the encoder if so
     */
    public void checkAndResetAtHardLimit() {
        if (getForwardLimit()) {
            setEncoder(FORWARD);
        } else if (getReverseLimit()) {
            setEncoder(BACKWARD);
        }
    }

    /**
     * 
     * @param targetPos the target position
     * @return whether the target has been reached or not
     */
    public boolean isFinished(double targetPos) {
        int currentVel = arm.getSelectedSensorVelocity(PID_X);
        int currentPos = arm.getSelectedSensorPosition(PID_X);
        return Math.abs(currentVel) <= VEL_TOLERANCE 
            && Math.abs(targetPos - currentPos) <= POS_TOLERANCE;
    }

    /**
     * Moves the arm by percent output
     * @param speed the speed
     */
    public void movePercentOutput(double speed) {
        arm.set(ControlMode.PercentOutput, speed);
    }

    /**
     * Moves the arm to one of three positions
     * 
     * @param destination the position to move to
     */
    public void move(Position destination) {
        switch (destination) {
            case Front:
                arm.set(ControlMode.MotionMagic, FORWARD);
                break;
            case Middle:
                arm.set(ControlMode.MotionMagic, MIDDLE);
                break;
            case Back:
                arm.set(ControlMode.MotionMagic, BACKWARD);
                break;
        }
    }
}
