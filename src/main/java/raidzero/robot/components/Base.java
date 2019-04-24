package raidzero.robot.components;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.sensors.PigeonIMU;

public class Base {

    private TalonSRX leftMotor;
    private TalonSRX rightMotor;

    private PigeonIMU pigeon;

    /**
     * Constructs a Drive object and sets up the motors and gear shift.
     *
     * @param lLeaderId the ID of the left leader motor
     * @param lFollower1Id the ID of the first left follower motor
     * @param lFollower2Id the ID of the second left follower motor
     * @param rLeaderId the ID of the right leader motor
     * @param rFollower1Id the ID of the first right follower motor
     * @param rFollower2Id the ID of the second right follower motor
     * @param pigeonId the ID of the pigeon
     */
    public Base(int rLeaderId, int rFollower1Id, int rFollower2Id, int lLeaderId, int lFollower1Id,
    int lFollower2Id, int pigeonId) {
        rightMotor = initSide(rLeaderId, rFollower1Id, rFollower2Id, false);
        leftMotor = initSide(lLeaderId, lFollower1Id, lFollower2Id, true);
        pigeon = new PigeonIMU(pigeonId);
    }

    /**
     * Constructs and configures the motors for one side of the robot (i.e. one leader and two
     * followers), and returns the leader motor object.
     *
     * @param leaderID the ID of the leader motor
     * @param invert whether to invert the leader or not
     * @return the newly constructed leader motor object
     */
    private TalonSRX initSide(int leaderId, int follower1Id, int follower2Id, boolean invert) {
        TalonSRX leader = new TalonSRX(leaderId);
        TalonSRX follower1 = new TalonSRX(follower1Id);
        TalonSRX follower2 = new TalonSRX(follower2Id);

        leader.configFactoryDefault();
        follower1.configFactoryDefault();
        follower2.configFactoryDefault();

        leader.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder);

        leader.configNeutralDeadband(0.001);

        leader.setNeutralMode(NeutralMode.Brake);
        follower1.setNeutralMode(NeutralMode.Brake);
        follower2.setNeutralMode(NeutralMode.Brake);

        follower1.follow(leader);
        follower2.follow(leader);

        leader.setInverted(invert);
        follower1.setInverted(invert);
        follower2.setInverted(invert);

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
     * Returns the pigeon.
     *
     * @return the pigeon
     */
    public PigeonIMU getPigeon() {
        return pigeon;
    }

    /**
     * Get the yaw value of the pigeon
     */
    public double getYaw() {
        double[] angles = new double[3];
        pigeon.getYawPitchRoll(angles);
        return angles[0];
    }

}
