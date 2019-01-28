package raidzero.robot.components;

import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

public class Lift {
    private final static double[] FPID = {0, 0 , 0 , 0, 0};
    private final static double rampRate = 0.5;
    private final static int PIDSlot = 0;

    private CANSparkMax leader;
    private SparkSensors sensors;

    /**
     * Constucts the Lift object and sets up the motors.
     * 
     * @param leaderID the leader ID
     * @param followerID the follower ID
     * @param inverted the boolean to invert
     */
    public Lift(int leaderID, int followerID, boolean inverted) {
        leader = init(leaderID, followerID, inverted);
        sensors = new SparkSensors(leader);
        sensors.setPID(FPID, PIDSlot);
    }

    /**
     * Inits the motors and configs the settings.
     * 
     * @param leaderID the leader ID
     * @param followerID the follower ID
     * @param inverted inversion of the motor direction
     * @return the leader talon
     */
    private CANSparkMax init(int leaderID, int followerID, boolean inverted) {
        CANSparkMax leader = new CANSparkMax(leaderID, MotorType.kBrushless);
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
        if (sensors.getLimitSwitch().get()) {
            sensors.setEncoder(0);
        }
    }

    /**
     * Moves the lift by percent output.
     * 
     * @param percentV The percentage voltage from -1.0 to 1.0 to run the motors
     */
    public void movePercent(double percentV) {
        sensors.getPIDController().setReference(percentV, ControlType.kDutyCycle);
    }

    /**
     * Moves the lift to a certain encoder position.
     * 
     * @param pos the encoder position to move to
     */
    public void movePosition(int pos) {
        sensors.getPIDController().setReference(pos, ControlType.kPosition, PIDSlot);
    }
}