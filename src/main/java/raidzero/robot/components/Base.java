package raidzero.robot.components;

import edu.wpi.first.wpilibj.TimedRobot;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.sensors.PigeonIMU;
import edu.wpi.first.wpilibj.DoubleSolenoid;

public class Base {

    private TalonSRX leftMotor;
    private TalonSRX rightMotor;

    private DoubleSolenoid gearShift;

    private static final double[] motProfFPID = { 0, 0, 0, 0, 0, 1.0};
    private static final double[] turnFPID = { 0, 0, 0, 0, 0, 1.0};

    private PigeonIMU pigeon;

    public Base() {
        rightMotor = initSide(0, false);
        leftMotor = initSide(1, true);

        //gearShift = 
    }

    public Base(int rLeaderid, int lLeaderid, int forwardChannel, int reverseChannel) {
        rightMotor = initSide(rLeaderid, false);
        leftMotor = initSide(lLeaderid, true);
        gearShift = new DoubleSolenoid(forwardChannel, reverseChannel);
        pigeon = new PigeonIMU(0);
    }

    public TalonSRX initSide(int leaderID, boolean invert) {
        TalonSRX leader = new TalonSRX(leaderID);
        TalonSRX follower = new TalonSRX(leaderID + 2);
        TalonSRX follower1 = new TalonSRX(leaderID + 4);

        leader.configFactoryDefault();
        leader.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder);
        leader.configNeutralDeadband(0.001);
        leader.configMotionProfileTrajectoryPeriod(1);
        //leader.setStatusFramePeriod ??

        leader.setNeutralMode(NeutralMode.Brake);
        follower.setNeutralMode(NeutralMode.Brake);
        follower1.setNeutralMode(NeutralMode.Brake);

        follower.follow(new TalonSRX(leaderID));
        follower1.follow(new TalonSRX(leaderID));

        leader.setInverted(invert);
        follower.setInverted(!invert);
        follower1.setInverted(!invert);

        return leader;
    }

    public void setPID(int PID_SLOT, double[] FPID, TalonSRX temp) {
        temp.config_kF(PID_SLOT, FPID[0]);
        temp.config_kP(PID_SLOT, FPID[1]);
        temp.config_kI(PID_SLOT, FPID[2]);
        temp.config_kD(PID_SLOT, FPID[3]);
        temp.config_IntegralZone(PID_SLOT, (int) FPID[4]);
    }

    public TalonSRX getRightMotor() {
        return rightMotor;
    }

    public TalonSRX getLeftMotor() {
        return leftMotor;
    }

    public void setLowGear() {
        gearShift.set(DoubleSolenoid.Value.kReverse);
    }

    public void setHighGear() {
        gearShift.set(DoubleSolenoid.Value.kForward);
    }

    public PigeonIMU getPigeon() {
        return pigeon;
    }

}
