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
    private static final double KP = 0.0;
    private static final double KI = 0.0;
    private static final double KD = 0.0;
    private static final double I_ZONE = 0.0;
    private static final double RAMP_RATE = 0.5;
    private static final int PID_SLOT = 0;

    private SparkMaxPrime leader;
    private CANSparkMax follower;
  //  private CANDigitalInput LS;

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
        
        //Set limit switch
      // LS = new CANDigitalInput(leader, LimitSwitch.kReverse, LimitSwitchPolarity.kNormallyClosed);

        // Set Inverted
        leader.setInverted(false);
        follower.setInverted(false);
        
        // Set Ramp Rate
        leader.setRampRate(RAMP_RATE);
        follower.setRampRate(RAMP_RATE);

        follower.follow(leader);
        leader.setPID(KF, KP, KI, KD, I_ZONE, PID_SLOT);
    }
    
    /**
     * Resets the encoder if the limit switch detects the lift.
     * 
     * <p> Should be periodically called.
     */
    public void limitReset() {
        if (leader.getReverseLimitSwitch(LimitSwitchPolarity.kNormallyClosed).get()) {
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
     * Runs the lift to a certain encoder position.
     * 
     * @param pos the encoder position to move to
     */
    public void movePosition(double pos) {
        leader.set(pos, ControlType.kPosition, PID_SLOT);
    }

}
