package raidzero.robot.components;

import com.revrobotics.CANDigitalInput;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import com.revrobotics.CANDigitalInput.LimitSwitchPolarity;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

public class Lift {

    // Preset arm positions
    public static final double SECOND_ROCKET = 22.2;
    public static final double THIRD_ROCKET = 42.5;

    /* Don't ask why... Calculated from experimental data
     * 3 -> 10.4
     * 4 -> 13.9
     * 5 -> 17.6
     * 6 -> 21.09
     */
    private static final double POSITION_CONV_FACTOR = 3.58;

    private static final double KF = 0.4663; // Max speed (up) is 2194 rpm
    private static final double KP = 0.18;
    private static final double KI = 0.00;
    private static final double KD = 15.2;
    private static final double I_ZONE = 0.0;
    private static final double RAMP_RATE = 0.0; // Acceleration

    private static final int MANUAL_SLOT = 0;
    private static final int PID_SLOT = 1;

    private SparkMaxPrime leader;
    private CANSparkMax follower;
    private CANDigitalInput limitSwitch;

    /**
     * Constucts the Lift object and sets up the motors.
     *
     * @param leaderID the leader ID
     * @param followerID the follower ID
     * @param inverted the boolean to invert
     */
    public Lift(int leaderID, int followerID) {
        leader = new SparkMaxPrime(leaderID, MotorType.kBrushless);
        follower = new CANSparkMax(followerID, MotorType.kBrushless);

        // Restore to factory settings
        leader.restoreFactoryDefaults();
        follower.restoreFactoryDefaults();

        // Set brake mode
        leader.setIdleMode(IdleMode.kBrake);
        follower.setIdleMode(IdleMode.kBrake);

        // Set limit switch
        limitSwitch = leader.getForwardLimitSwitch(LimitSwitchPolarity.kNormallyClosed);
        limitSwitch.enableLimitSwitch(false);

        // Set motor inversion
        leader.setInverted(true);
        follower.setInverted(true);

        // Set ramp rate
        leader.setOpenLoopRampRate(RAMP_RATE);
        leader.setClosedLoopRampRate(RAMP_RATE);
        follower.setOpenLoopRampRate(RAMP_RATE);
        follower.setClosedLoopRampRate(RAMP_RATE);

        // Make follower follow the leader motor
        follower.follow(leader);

        // Configure PID values
        leader.setPID(KF, KP, KI, KD, I_ZONE, -0.7, 0.7, PID_SLOT);
    }

    /**
     * Resets the position of the encoder to 0.
     *
     * @return the encoder position
     */
    public void resetEncoderPos() {
        leader.setPosition(0);
    }

    /**
     * Returns the position of the encoder.
     *
     * @return the encoder position in rotations
     */
    public double getEncoderPos() {
        return leader.getPosition();
    }

    /**
     * Resets the encoder if the limit switch detects the lift.
     *
     * <p>Should be periodically called.
     */
    public void limitReset() {
        if (limitSwitch.get()) {
            resetEncoderPos();
        }
    }

    /**
     * Moves the lift by percent output.
     *
     * @param percentV The percentage voltage from -1.0 to 1.0 to run the motors
     */
    public void movePercent(double percentV) {
        leader.set(percentV, ControlType.kDutyCycle, MANUAL_SLOT);
    }

    /**
     * Runs the lift to a certain encoder rotation (Position PID).
     *
     * @param pos the target encoder rotation
     */
    public void movePosition(double pos) {
        leader.set(pos / POSITION_CONV_FACTOR, ControlType.kPosition, PID_SLOT);
    }

}
