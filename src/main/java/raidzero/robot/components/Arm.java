package raidzero.robot.components;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import raidzero.robot.Settings;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.LimitSwitchNormal;
import com.ctre.phoenix.motorcontrol.LimitSwitchSource;

public class Arm {

    private TalonSRX arm;
    private TalonSRX armFollower;
    private int onTheLeft;

    // Preset arm positions
    public static final int BALL_INTAKE = -350;//-450;
    public static final int ROCKET_BALL = 850;
    public static final int STARTING_POS = 3310;
    public static final int CARGO = 5562;

    private static final int PID_X = 0;

    private static final int TARGET_VEL = 500;
    private static final int TARGET_ACCEL = 800;

    private static final double P_VALUE = 7.0;
    private static final double I_VALUE = 0;
    private static final double D_VALUE = 50.0;
    private static final double F_VALUE = 1.86 / 2;
    private static final int IZ_VALUE = 50;

    private static final int VEL_TOLERANCE = 1;
    private static final int POS_TOLERANCE = 1;

    /**
     * This enum contains the possible positions to go to
     */
    public enum Position {
        Front, Starting, Back
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

        // The tachs are daisy chained together
        // Which solder pad is soldered will decide which one is forward and reverse
        arm.configReverseLimitSwitchSource(LimitSwitchSource.FeedbackConnector,
            LimitSwitchNormal.Disabled);
        arm.configForwardLimitSwitchSource(LimitSwitchSource.FeedbackConnector,
            LimitSwitchNormal.Disabled);

        arm.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder);

        arm.configMotionCruiseVelocity(TARGET_VEL);
        arm.configMotionAcceleration(TARGET_ACCEL);

        arm.setSensorPhase(false);

        switch(Settings.VERSION) {
            case PRAC:
                onTheLeft = 1;
                arm.setInverted(false);
                armFollower.setInverted(true);
                break;
            case COMP:
                onTheLeft = -1;
                arm.setInverted(true);
                armFollower.setInverted(false);
                break;
        }

        // Set the starting position as the current one
        arm.setSelectedSensorPosition(onTheLeft * STARTING_POS, PID_X, 100);

        arm.config_kP(PID_X, P_VALUE);
        arm.config_kI(PID_X, I_VALUE);
        arm.config_kD(PID_X, D_VALUE);
        arm.config_kF(PID_X, F_VALUE);
        arm.config_IntegralZone(PID_X, IZ_VALUE);
    }

    /**
     * Sets encoder to a certain position
     *
     * @param pos the position to set the encoder to
     */
    public void setEncoderPos(int pos) {
        arm.setSelectedSensorPosition(pos, PID_X, 0);
    }

    /**
     * Returns the encoder position
     *
     * @return the encoder position of the arm
     */
    public int getEncoderPos() {
        return arm.getSelectedSensorPosition(PID_X);
    }

    /**
     * Returns the encoder velocity
     *
     * @return the encoder velocity
     */
    public int getEncoderVel() {
        return arm.getSelectedSensorVelocity(PID_X);
    }

    /**
     * Returns whether the reverse limit switch has been reached
     *
     * @return whether the reverse limit switch has been reached
     */
    public boolean getReverseLimit() {
        return arm.getSensorCollection().isRevLimitSwitchClosed();
    }

    /**
     * Check if the hard limit has been reached and reset the encoder if so
     */
    public void checkAndResetAtHardLimit() {
        if (getReverseLimit()) {
            setEncoderPos(STARTING_POS);
        }
    }

    /**
     * Checks if the arm has reached its target.
     *
     * @param targetPos the target position
     * @return whether the target has been reached or not
     */
    public boolean isFinished(double targetPos) {
        return Math.abs(getEncoderVel()) <= VEL_TOLERANCE
            && Math.abs(targetPos - getEncoderPos()) <= POS_TOLERANCE;
    }

    /**
     * Moves the arm by percent output (motor power)
     *
     * @param power the motor power (between -1 and 1)
     */
    public void movePercentOutput(double power) {
        arm.set(ControlMode.PercentOutput, power);
    }

    /**
     * Move the arm to a specific encoder position
     *
     * @param position the position in encoder ticks
     */
    public void move(int position) {
        arm.set(ControlMode.MotionMagic, position);
    }

}
