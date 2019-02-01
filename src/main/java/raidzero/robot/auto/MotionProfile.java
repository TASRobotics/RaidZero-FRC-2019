package raidzero.robot.auto;

import com.ctre.phoenix.motion.MotionProfileStatus;
import com.ctre.phoenix.motion.SetValueMotionProfile;
import com.ctre.phoenix.motion.TrajectoryPoint;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.Notifier;
import raidzero.robot.pathgen.PathGenerator;
import raidzero.robot.pathgen.PathPoint;
import raidzero.robot.pathgen.Point;

public class MotionProfile {

    private static final int MAIN_PID_SLOT = 0;
    private static final int AUX_PID_SLOT = 1;
    private static final int BASE_TRAJ_PERIOD_MS = 0;
    private static final double SENSOR_UNITS_PER_INCH = 81.9;
    private static final int MIN_POINTS_IN_TALON = 20;

    private boolean start;
    private int state;
    private MotionProfileStatus status;
    private SetValueMotionProfile setValue;
    private TalonSRX talon;

    class PeriodicRunnable implements java.lang.Runnable {
	    public void run() {
            talon.processMotionProfileBuffer();    
        }
	}
    Notifier notifer = new Notifier(new PeriodicRunnable());

    public MotionProfile(TalonSRX tal) {
        talon = tal;
        setValue = SetValueMotionProfile.Disable;
        status = new MotionProfileStatus();
        notifer.startPeriodic(0.005);
        state = 0;
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
        start = true;
    }

    /**
     * Call periodically to control states of the motion profile.
     */
    public void control() {
        /*
		 * 0 state is fill the points
         * 1 state is wait for enough points
         * 2 state is run the points
		 */
		switch (state) {
            case 0:
                if (start) {
                    start = false;
                    setValue = SetValueMotionProfile.Disable;
                    state = 1;
                }
				break;
			case 1: 
				talon.getMotionProfileStatus(status);
                if (status.btmBufferCnt > MIN_POINTS_IN_TALON) {
                    setValue = SetValueMotionProfile.Enable;    
                    state = 2;
				}
				break;
			case 2:
				talon.getMotionProfileStatus(status);
				if (status.activePointValid && status.isLast) {
                    setValue = SetValueMotionProfile.Hold;
					state = 0;
				}
                break;
            }
    }
 
    /**
     * Starts filling the buffer with trajectory points.
     * 
     * @param waypoints the array of points created by the path generator
     * @param talon the talon to push the trajectory points
     */
    private void startFilling(PathPoint[] waypoints) {

	    // Clear under run error
        if (status.hasUnderrun) {
			talon.clearMotionProfileHasUnderrun();
        }
        
        // Clear the buffer just in case the robot is still running some points
        talon.clearMotionProfileTrajectories();
        // Set the base period of the trajectory points
        talon.configMotionProfileTrajectoryPeriod(BASE_TRAJ_PERIOD_MS);

        for (int i = 0; i < waypoints.length; i++) {
            TrajectoryPoint tp = new TrajectoryPoint();
            tp.position = waypoints[i].position * SENSOR_UNITS_PER_INCH;
            tp.velocity = waypoints[i].velocity * SENSOR_UNITS_PER_INCH;
            tp.timeDur = (int) waypoints[i].time;
            tp.auxiliaryPos = waypoints[i].angle;          
            tp.profileSlotSelect0 = MAIN_PID_SLOT;
            tp.profileSlotSelect1 = AUX_PID_SLOT;
            tp.zeroPos = false;

            if (i == 0) {
                tp.zeroPos = true;
            }

            if (i == waypoints.length - 1) {
                tp.isLastPoint = true;
            }

            talon.pushMotionProfileTrajectory(tp);

        }

    }

    /**
     * Gets the routine of the motion profile
     * @return the value of the routine. 0 for disable motion profile. 1 for enable motion profile.
     *         2 for holding the current trajectory point.
     */
    public SetValueMotionProfile getSetValue() {
		return setValue;
	}

}
