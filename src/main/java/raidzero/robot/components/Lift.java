package raidzero.robot.components;

import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import com.revrobotics.CANDigitalInput.LimitSwitchPolarity;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

public class Lift {

    private static final double KF = 0.0;
    private static final double KP = 0.0;
    private static final double KI = 0.0;
    private static final double KD = 0.0;
    private static final double I_Zone = 0.0;
    private static final double RAMP_Rate = 0.5;
    private static final int pidSlot = 0;

    private SparkMaxPrime leader;
    private CANSparkMax follower;

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

        // Set Brake Mode
        leader.setIdleMode(IdleMode.kBrake);
        follower.setIdleMode(IdleMode.kBrake);
        
        // Set Inverted
        leader.setInverted(false);
        follower.setInverted(false);
        
        // Set Ramp Rate
        leader.setRampRate(RAMP_Rate);
        follower.setRampRate(RAMP_Rate);

        follower.follow(leader);
        leader.setPID(KF, KP, KI, KD, I_Zone, pidSlot);
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
