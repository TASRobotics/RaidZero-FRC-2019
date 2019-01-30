package raidzero.robot.components;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.sensors.PigeonIMU;
import edu.wpi.first.wpilibj.DoubleSolenoid;

public class Base {

    private TalonSRX leftMotor;
    private TalonSRX rightMotor;

    private DoubleSolenoid gearShift;

    private PigeonIMU pigeon;

    /**
     * Constructs a Drive object and sets up the motors and gear shift.
     * 
     * @param lLeaderID the ID of the left leader motor
     * @param rLeaderID the ID of the right leader motor
     * @param forwardChannel the forward channel for the gear shift
     * @param reverseChannel the reverse channel for the gear shift
     */
    public Base(int rLeaderId, int lLeaderId, int forwardChannel, int reverseChannel) {
        rightMotor = initSide(rLeaderId, false);
        leftMotor = initSide(lLeaderId, true);
        gearShift = new DoubleSolenoid(forwardChannel, reverseChannel);
        pigeon = new PigeonIMU(0);
    }

    /**
     * Constructs and configures the motors for one side of the robot (i.e. one leader and two
     * followers), and returns the leader motor object.
     * 
     * @param leaderID the ID of the leader motor
     * @param invert whether to invert the leader or not
     * @return the newly constructed leader motor object
     */
    private TalonSRX initSide(int leaderId, boolean invert) {
        TalonSRX leader = new TalonSRX(leaderId);
        TalonSRX follower = new TalonSRX(leaderId + 2);
        TalonSRX follower1 = new TalonSRX(leaderId + 4);

        leader.configFactoryDefault();
        leader.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder);

        leader.configNeutralDeadband(0.001);

        leader.setNeutralMode(NeutralMode.Brake);
        follower.setNeutralMode(NeutralMode.Brake);
        follower1.setNeutralMode(NeutralMode.Brake);

        follower.follow(leader);
        follower1.follow(leader);

        leader.setInverted(invert);
        follower.setInverted(!invert);
        follower1.setInverted(!invert);

        return leader;
    }

    /**
     * Returns the right leader motor.
     * 
     * <p>Anything done to this motor will also be followed by the other right motor.
     * 
     * @return the right leader motor
     */
    public TalonSRX getRightMotor() {
        return rightMotor;
    }

    /**
     * Returns the left leader motor.
     * 
     * <p>Anything done to this motor will also be followed by the other left motor.
     * 
     * @return the left leader motor
     */
    public TalonSRX getLeftMotor() {
        return leftMotor;
    }
    
    /**
     * Sets the gear shift to low gear.
     */
    public void setLowGear() {
        gearShift.set(DoubleSolenoid.Value.kReverse);
    }

    /**
     * Sets the gear shift to high gear.
     */
    public void setHighGear() {
        gearShift.set(DoubleSolenoid.Value.kForward);
    }

    /**
     * Returns the pigeon.
     * 
     * @return the pigeon
     */
    public PigeonIMU getPigeon() {
        return pigeon;
    }

}
