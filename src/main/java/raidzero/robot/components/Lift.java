package raidzero.robot.components;

import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import com.revrobotics.CANDigitalInput.LimitSwitchPolarity;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

public class Lift {

    private final static double kF = 0.0;
    private final static double kP = 0.0;
    private final static double kI = 0.0;
    private final static double kD = 0.0;
    private final static double iZone = 0.0;
    private final static double rampRate = 0.5;
    private final static int pidSlot = 0;

    private SparkMaxPrime leader;

    /**
     * Constucts the Lift object and sets up the motors.
     * 
     * @param leaderID the leader ID
     * @param followerID the follower ID
     * @param inverted the boolean to invert
     */
    public Lift(int leaderID, int followerID, boolean inverted) {
        leader = init(leaderID, followerID, inverted);
        leader.setPID(kF, kP, kI, kD, iZone, pidSlot);
    }

    /**
     * Inits the motors and configs the settings.
     * 
     * @param leaderID the leader ID
     * @param followerID the follower ID
     * @param inverted inversion of the motor direction
     * @return the leader talon
     */
    private SparkMaxPrime init(int leaderID, int followerID, boolean inverted) {
        SparkMaxPrime leader = new SparkMaxPrime(leaderID, MotorType.kBrushless);
        CANSparkMax follower = new CANSparkMax(followerID, MotorType.kBrushless);

        // Set Brake Mode
        leader.setIdleMode(IdleMode.kBrake);
        follower.setIdleMode(IdleMode.kBrake);
        
        // Set Inverted
        leader.setInverted(inverted);
        follower.setInverted(inverted);
        
        // Set Ramp Rate
        leader.setRampRate(rampRate);
        follower.setRampRate(rampRate);

        follower.follow(leader);
        return leader;
    }
    
    /**
     * Resets the encoder if the limit switch detects the lift.
     * 
     * <p> Should be periodically called.
     */
    public void limitReset() {
        if (leader.getForwardLimitSwitch(LimitSwitchPolarity.kNormallyClosed).get()) {
            leader.setPosition(0);
        }
    }

    /**
     * Moves the lift by percent output.
     * 
     * @param percentV The percentage voltage from -1.0 to 1.0 to run the motors
     */
    public void movePercent(double percentV) {
        leader.set(percentV, ControlType.kDutyCycle, pidSlot);
    }

    /**
     * Runs the lift to a certain encoder position.
     * 
     * @param pos the encoder position to move to
     */
    public void movePosition(double pos) {
        leader.set(pos, ControlType.kPosition, pidSlot);
    }

}