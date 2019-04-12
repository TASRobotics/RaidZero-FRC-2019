package raidzero.robot.auto;

import com.ctre.phoenix.motion.MotionProfileStatus;
import com.ctre.phoenix.motion.SetValueMotionProfile;
import com.ctre.phoenix.motion.TrajectoryPoint;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.FollowerType;
import com.ctre.phoenix.motorcontrol.RemoteSensorSource;
import com.ctre.phoenix.motorcontrol.SensorTerm;
import com.ctre.phoenix.motorcontrol.StatusFrame;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.sensors.PigeonIMU;

import edu.wpi.first.wpilibj.Notifier;
import raidzero.pathgen.PathGenerator;
import raidzero.pathgen.PathPoint;
import raidzero.pathgen.Point;
import raidzero.robot.Settings;

public class MotionProfile {

    /**
     * The states of controlling the motion profile arc
     */
    private enum State {
        FillPoints, WaitPoints, Run;
    };

    private static final int REMOTE_0 = 0;
    private static final int REMOTE_1 = 1;
    private static final int PID_PRIMARY_SLOT = 0;
    private static final int PID_AUX_SLOT = 1;
    private static final int TIMEOUT_MS = 10;
    private static final double PIGEON_SCALE = 3600.0 / 8192.0;

    private static final double PRIMARY_F = 0.51;
    private static final double PRIMARY_P = 1.4;
    private static final double PRIMARY_I = 0.0001;
    private static final double PRIMARY_D = 30;
    private static final int PRIMARY_INT_ZONE = 50;

    private static final double AUX_F = 0.269; // Max was 380 dps
    private static final double AUX_P = 2.9;
    private static final double AUX_I = 0.0001;
    private static final double AUX_D = 150;
    private static final int AUX_INT_ZONE = 20;

    private static final int BASE_TRAJ_PERIOD_MS = 0;
    private static final double SENSOR_UNITS_PER_INCH = 110;
    private static final int MIN_POINTS_IN_TALON = 10;
    private static final int CLOSED_LOOP_TIME_MS = 1;

    private boolean reversed;
    private boolean initRun;
    private State state;
    private MotionProfileStatus status;
    private SetValueMotionProfile setValue;
    private TalonSRX rightTal;
    private TalonSRX leftTal;
    private PigeonIMU pidgey;

    Notifier notifer = new Notifier(() -> {
        rightTal.processMotionProfileBuffer();
    });

    /**
     * Creates the motion profile object and sets up the motors
     *
     * @param rightMaster the right master of the base
     * @param leftMaster the left master of the base
     * @param pidgey the pigeon object to use
     */
    public MotionProfile(TalonSRX rightMaster, TalonSRX leftMaster, PigeonIMU pidgey) {
        rightTal = rightMaster;
        leftTal = leftMaster;
        this.pidgey = pidgey;

        setValue = SetValueMotionProfile.Disable;
        status = new MotionProfileStatus();
        rightTal.changeMotionControlFramePeriod(3);
        notifer.startPeriodic(0.003);
        state = State.FillPoints;
        reversed = false;
        setup();
    }

    /**
     * Sets up the sensors and configurations for MP
     */
    private void setup() {
        leftTal.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder);
        rightTal.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder);

        switch(Settings.VERSION) {
            case PRAC:
                rightTal.setSensorPhase(false);
                leftTal.setSensorPhase(false);
                break;
            case COMP:
                rightTal.setSensorPhase(true);
                leftTal.setSensorPhase(true);
                break;
        }

        // Configure the left side encoder as a remote sensor for the right Talon
        rightTal.configRemoteFeedbackFilter(leftTal.getDeviceID(),
            RemoteSensorSource.TalonSRX_SelectedSensor,	REMOTE_0);

        // Configure the Pigeon as the other Remote Slot on the Right Talon
        rightTal.configRemoteFeedbackFilter(pidgey.getDeviceID(), RemoteSensorSource.Pigeon_Yaw,
            REMOTE_1);

        // Setup Sum signal to be used for distance
        rightTal.configSensorTerm(SensorTerm.Sum0, FeedbackDevice.RemoteSensor0);
        rightTal.configSensorTerm(SensorTerm.Sum1, FeedbackDevice.QuadEncoder);

        // Configure Sum [Sum of both QuadEncoders] to be used for Primary PID Index
        rightTal.configSelectedFeedbackSensor(FeedbackDevice.SensorSum, PID_PRIMARY_SLOT,
            TIMEOUT_MS);

        // Scale Feedback by 0.5 to half the sum of Distance
        rightTal.configSelectedFeedbackCoefficient(0.5, PID_PRIMARY_SLOT, TIMEOUT_MS);

        // Configure Pigeon's Yaw to be used for Auxiliary PID Index
        rightTal.configSelectedFeedbackSensor(FeedbackDevice.RemoteSensor1, PID_AUX_SLOT,
            TIMEOUT_MS);

        // Scale the Feedback Sensor using a coefficient (Configured for 360 units of resolution)
        rightTal.configSelectedFeedbackCoefficient(PIGEON_SCALE, PID_AUX_SLOT, TIMEOUT_MS);

        // Set status frame periods to ensure we don't have stale data
        rightTal.setStatusFramePeriod(StatusFrame.Status_12_Feedback1, 20);
        rightTal.setStatusFramePeriod(StatusFrame.Status_13_Base_PIDF0, 20);
        rightTal.setStatusFramePeriod(StatusFrame.Status_14_Turn_PIDF1, 20);
        rightTal.setStatusFramePeriod(StatusFrame.Status_10_Targets, 20);
        leftTal.setStatusFramePeriod(StatusFrame.Status_2_Feedback0, 5);

        // FPID Gains for the distance part
        rightTal.config_kP(PID_PRIMARY_SLOT, PRIMARY_P);
        rightTal.config_kI(PID_PRIMARY_SLOT, PRIMARY_I);
        rightTal.config_kD(PID_PRIMARY_SLOT, PRIMARY_D);
        rightTal.config_kF(PID_PRIMARY_SLOT, PRIMARY_F);
        rightTal.config_IntegralZone(PID_PRIMARY_SLOT, PRIMARY_INT_ZONE);

        // FPID Gains for turning part
        rightTal.config_kP(PID_AUX_SLOT, AUX_P);
        rightTal.config_kI(PID_AUX_SLOT, AUX_I);
        rightTal.config_kD(PID_AUX_SLOT, AUX_D);
        rightTal.config_kF(PID_AUX_SLOT, AUX_F);
        rightTal.config_IntegralZone(PID_AUX_SLOT, AUX_INT_ZONE);

        // Set the period of the closed loops to be 1 ms
        rightTal.configClosedLoopPeriod(PID_PRIMARY_SLOT, CLOSED_LOOP_TIME_MS);
        rightTal.configClosedLoopPeriod(PID_AUX_SLOT, CLOSED_LOOP_TIME_MS);
        rightTal.configAuxPIDPolarity(false);
    }

    /**
     * Starts the motion profile by filling the points.
     *
     * @param points the points to put in the path generator
     * @param tarVel the target velocity desired in in/100ms
     * @param tarAccel the target acceleration desired in in/100ms/s
     */
    public void start(Point[] points, double tarVel, double tarAccel) {
        startFilling(PathGenerator.generatePath(points, tarVel, tarAccel));
        initRun = true;
    }

    /**
     * Call periodically to control states of the motion profile.
     */
    public void controlMP() {
        switch (state) {
            case FillPoints:
                if (initRun) {
                    initRun = false;
                    setValue = SetValueMotionProfile.Disable;
                    state = State.WaitPoints;
                }
                break;
            case WaitPoints:
                rightTal.getMotionProfileStatus(status);
                if (status.btmBufferCnt > MIN_POINTS_IN_TALON) {
                    setValue = SetValueMotionProfile.Enable;
                    state = State.Run;
                }
                break;
            case Run:
                rightTal.getMotionProfileStatus(status);
                if (status.activePointValid && status.isLast) {
                    setValue = SetValueMotionProfile.Hold;
                    state = State.FillPoints;
                }
                break;
        }
    }


    /**
     * Moves the base in motion profile arc mode.
     *
     * <p>This should be periodically called.
     */
    public void move() {
        rightTal.set(ControlMode.MotionProfileArc, setValue.value);
        leftTal.follow(rightTal, FollowerType.AuxOutput1);
    }

    /**
     * Returns the current set value of the motors
     *
     * @return the set value of the motors
     */
    public SetValueMotionProfile getSetValue() {
        return setValue;
    }

    /**
     * Set the path to go reversed or not
     *
     * @param reversed true if want bot to go backwards, else false
     */
    public void setReverse(boolean reversed) {
        this.reversed = reversed;
    }

    /**
     * Clears the Motion profile buffer and resets state info
     */
    public void reset() {
        rightTal.clearMotionProfileTrajectories();
        setValue = SetValueMotionProfile.Disable;
        state = State.FillPoints;
        initRun = false;
    }

    /**
     * Starts filling the buffer with trajectory points.
     *
     * @param waypoints the array of points created by the path generator
     */
    private void startFilling(PathPoint[] waypoints) {
        int reverse = reversed ? -1 : 1;
        // Clear under run error
        if (status.hasUnderrun) {
            rightTal.clearMotionProfileHasUnderrun();
        }

        // Clear the buffer just in case the robot is still running some points
        rightTal.clearMotionProfileTrajectories();
        // Set the base period of the trajectory points
        rightTal.configMotionProfileTrajectoryPeriod(BASE_TRAJ_PERIOD_MS);

        for (int i = 0; i < waypoints.length; i++) {
            TrajectoryPoint tp = new TrajectoryPoint();
            tp.position = waypoints[i].position * reverse * SENSOR_UNITS_PER_INCH;
            tp.velocity = waypoints[i].velocity * reverse * SENSOR_UNITS_PER_INCH;
            tp.timeDur = (int) waypoints[i].time;
            tp.auxiliaryPos = waypoints[i].angle * 10;
            tp.useAuxPID = true;
            tp.profileSlotSelect0 = PID_PRIMARY_SLOT;
            tp.profileSlotSelect1 = PID_AUX_SLOT;
            tp.zeroPos = false;

            if (i == 0) {
                tp.zeroPos = true;
            }

            if (i == waypoints.length - 1) {
                tp.isLastPoint = true;
            }

            rightTal.pushMotionProfileTrajectory(tp);

        }

    }

}
