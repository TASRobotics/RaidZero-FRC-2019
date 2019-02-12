package raidzero.robot.components;

import com.revrobotics.CANDigitalInput;
import com.revrobotics.CANDigitalInput.LimitSwitch;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import com.revrobotics.CANDigitalInput.LimitSwitchPolarity;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

public class Lift {

    private static final double KF = 0.0;
    private static final double KP = 0.18;
    private static final double KI = 0.00;
    private static final double KD = 15.2;
    private static final double I_ZONE = 0.0;
    private static final double RAMP_RATE = 0.5;

    private static final double MAX_VELOCITY = 2000.0;
    private static final double MIN_VELOCITY = 0.0;
    private static final double MAX_ACCELERATION = 1500.0;

    private static final double ALLOWED_ERROR = 0.1;

    private static final int PID_SLOT = 0;
    private static final int SMART_MOTION_SLOT = 0;

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

        // Reset to factory defaults
        leader.restoreFactoryDefaults();

        // Set brake mode
        leader.setIdleMode(IdleMode.kBrake);
        follower.setIdleMode(IdleMode.kBrake);

        // Set limit switch
        limitSwitch = leader.getReverseLimitSwitch(LimitSwitchPolarity.kNormallyClosed);

        // Set motor inversion
        leader.setInverted(false);
        follower.setInverted(false);

        // Set ramp rate
        leader.setOpenLoopRampRate(RAMP_RATE);
        leader.setClosedLoopRampRate(RAMP_RATE);
        follower.setOpenLoopRampRate(RAMP_RATE);
        follower.setClosedLoopRampRate(RAMP_RATE);

        // Make follower follow the leader motor
        follower.follow(leader);

        // Configure PID values + SmartMotion
        leader.setPID(KF, KP, KI, KD, I_ZONE, PID_SLOT);
        leader.configureSmartMotion(MIN_VELOCITY, MAX_VELOCITY, MAX_ACCELERATION,
            ALLOWED_ERROR, SMART_MOTION_SLOT);
    }

    /**
     * Returns the position of the encoder.
     *
     * @return the encoder position
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
            leader.setPosition(0);
        }
    }

    /**
     * Moves the lift by percent output.
     *
     * @param percentV The percentage voltage from -1.0 to 1.0 to run the motors
     */
    public void movePercent(double percentV) {
        leader.set(percentV, ControlType.kDutyCycle, PID_SLOT);
    }

    /**
     * Runs the lift to a certain encoder position (SmartMotion).
     *
     * @param pos the encoder position to move to
     */
    public void movePosition(double pos) {
        leader.set(pos, ControlType.kSmartMotion, PID_SLOT);
    }

}
