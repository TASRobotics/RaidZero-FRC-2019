package raidzero.robot.vision;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Point3;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.MatOfDouble;
import org.opencv.core.CvType;
import org.opencv.core.Core;

import java.util.Optional;
import java.util.Arrays;
import raidzero.pathgen.Point;

/**
 * Finding position of vision targets and generating waypoints to them.
 */
public class Vision {

	/**
     * The ways of targetting tape targets
     */
	private enum TapeTargetMethod {
		PNPRIO, PNPLL;
	}

	private static NetworkTableEntry tx, ty, tv, thor, pipeline, tcornx, tcorny, camtran;
	private static double xpos, ypos, absoluteAng, pipedex, ang;
	private static boolean targPres;

	private static MatOfPoint3f mObjectPoints;
    private static Mat mCameraMatrix;
	private static MatOfDouble mDistortionCoefficients;

	/**
     * The tape-targetting method to actually use.
     */
	private static final TapeTargetMethod TAPE_TARGET_METHOD = TapeTargetMethod.PNPLL;

	/**
     * How far limelight should be behind the tape target in inches, adjustable.
     */
	private static final double Y_OFFSET = -32;

	private static final double X_OFFSET = 1; // -1.5;

	/**
     * Width of tape target, only used for crude.
     */
	private static final double TAPE_WIDTH = 11;

	/**
     * Width of ball target, adjustable.
     */
	private static final double BALL_WIDTH = 12;

	/**
     * Initializes limelight network table entries.
     */
	public static void initialize() {
		NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight-kaluza");

		// Initialize the NetworkTable entries from the Limelight
		tx = table.getEntry("tx");
		ty = table.getEntry("ty");
		tv = table.getEntry("tv");
		thor = table.getEntry("thor");
		pipeline = table.getEntry("pipeline");
		tcornx = table.getEntry("tcornx");
		tcorny = table.getEntry("tcorny");
		camtran = table.getEntry("camtran");

		// Set the pipeline index to whatever the limelight is currently at
		pipedex = pipeline.getDouble(0.0);

		// Set camera values
		double target_gap = 8.0;
		mObjectPoints = new MatOfPoint3f(
				new Point3(-1.9363 - target_gap/2, 0.5008, 0.0), // left top-left
				new Point3(0.0 - target_gap/2, 0.0, 0.0), // left top-right
				new Point3(-3.3133 - target_gap/2, -4.8242, 0.0), // left bottom-left
                // new Point3(-1.377 - target_gap/2, -5.325, 0.0), // left bottom-right
                new Point3(1.9363 + target_gap/2, 0.5008, 0.0), // right top-right
				new Point3(0.0 + target_gap/2, 0.0, 0.0), // right top-left
				new Point3(3.3133 + target_gap/2, -4.8242, 0.0) // right bottom-right
                // new Point3(1.377 + target_gap/2, -5.325, 0.0) // right bottom-left
        );

		// setup camera matrix
        mCameraMatrix = Mat.eye(3, 3, CvType.CV_64F);
        mCameraMatrix.put(0, 0, 2.5751292067328632e+02);
        mCameraMatrix.put(0, 2, 1.5971077914723165e+02);
        mCameraMatrix.put(1, 1, 2.5635071715912881e+02);
		mCameraMatrix.put(1, 2, 1.1971433393615548e+02);

		// setup limelight 2 distortion coefficients
		mDistortionCoefficients = new MatOfDouble(2.9684613693070039e-01, -1.4380252254747885e+00,
			-2.2098421479494509e-03, -3.3894563533907176e-03, 2.5344430354806740e+00);
	}

	/**
	 * Turn on limelight led
	 */
	public static void ledOn() {
		NetworkTableInstance.getDefault().getTable("limelight-kaluza").getEntry("ledMode").setNumber(3);
	}

	/**
	 * Turn off limelight led
	 */
	public static void ledOff() {
		NetworkTableInstance.getDefault().getTable("limelight-kaluza").getEntry("ledMode").setNumber(1);
	}

	/**
	 * Setup the cameras for purely driver cam view
	 */
	public static void driverCamSetup() {
		NetworkTableInstance.getDefault().getTable("limelight-kaluza").getEntry("pipeline").setNumber(2);
		NetworkTableInstance.getDefault().getTable("limelight-kaluza").getEntry("camMode").setNumber(0);
		NetworkTableInstance.getDefault().getTable("limelight-kaluza").getEntry("ledMode").setNumber(1);
		// Picture-in-Picture mode
		NetworkTableInstance.getDefault().getTable("limelight-kaluza").getEntry("stream").setNumber(2);
	}

	/**
     * Posts target information to SmartDashboard.
     */
	public static void postToSmartDashBoard() {
		SmartDashboard.putBoolean("Target Presence", targPres);
		SmartDashboard.putNumber("XTarget", xpos);
		SmartDashboard.putNumber("YTarget", ypos);
		SmartDashboard.putNumber("RelAngTarget", ang);
		SmartDashboard.putNumber("Pipeline", pipedex);

		// Gyro angle is not strictly vision, but also post to SmartDashboard
		SmartDashboard.putNumber("AbsAng", absoluteAng);
	}

	/**
     * Outputs double array with left and right motor powers to move toward largest ball.
     */
	public static double[] moveToBall() {
		// set limelight pipeline to target balls
		pipedex = 1;
		pipeline.setNumber(pipedex);

		// tunable constants
		double KpAim = 0.07;
		double KpDistance = 0.11;
		double min_aim_command = 0.1;

		// get vertical and horizontal angle of ball in limelight view
		double ax = tx.getDouble(0.0);
		double ay = ty.getDouble(0.0);

		double heading_error = -ax;
		double distance_error = -ay;
		double steering_adjust = 0;
		steering_adjust = 0;

		// steering adjust proportional with offset
		if (ax > 2.0) {
			steering_adjust += KpAim*heading_error - min_aim_command;
		} else if (ax < -2.0) {
			steering_adjust += KpAim*heading_error + min_aim_command;
		}

		// distance adjust proportional
		double distance_adjust = KpDistance * distance_error;

		// arcade drive with steering and heading adjust, applying a percentage speed limit
		double limitus = 0.75;
		double left_command = distance_adjust + steering_adjust;
		double right_command = distance_adjust - steering_adjust;
		left_command = Math.min(Math.max(left_command*limitus, -limitus), limitus);
		right_command = Math.min(Math.max(right_command*limitus, -limitus), limitus);

		return new double[] { left_command, right_command };
	}

	/**
     * Generates waypoints for splining to vision target, call if target ang unknown (teleop).
	 *
	 * <p>If called without seeing target, will return empty optional.
	 *
	 * @param absAng gyroscope angle
     */
	public static Optional<Point[]> pathToTarg(double absAng) {
		pipedex = 0;
		pipeline.setNumber(pipedex);

		absoluteAng = absAng;

		// Calculate position of respective target, or none
		boolean safe = tv.getDouble(0) == 1.0;
		switch (TAPE_TARGET_METHOD) {
			case PNPLL:
				safe &= calculateTapePosPNPLL();
				break;
			case PNPRIO:
				safe &= calculateTapePosPNPRIO();
				break;
		}

		// if target seen and methods return true, go ahead and make waypoints
		if (safe) {
			Point startPoint = new Point(0, 0, absAng);
			Point endPoint = new Point(xpos, ypos, ang);
			Point straightenPoint = new Point(
				2 * Math.cos(Math.toRadians(ang)) + xpos,
				2 * Math.sin(Math.toRadians(ang)) + ypos, ang);
			// System.out.println("xpos " + xpos + "\typos " + ypos + "\tang " + ang + "\tabsang" + absAng);
			return Optional.of(new Point[] { startPoint, endPoint, straightenPoint });
		}
		return Optional.empty();
	}

	private static void objectToFieldCoords(double[] info) {
		double xtemp = -info[0] + X_OFFSET;
		double ytemp = -info[1] + Y_OFFSET; // offset controls how far back from target to go
		double yawang = info[2];

		ang = absoluteAng - yawang;
		double angus = ang - Math.toDegrees(Math.atan2(xtemp, ytemp));
		double distus = Math.hypot(xtemp, ytemp);

		xpos = distus * Math.cos(Math.toRadians(angus));
		ypos = distus * Math.sin(Math.toRadians(angus));
	}

	/**
	 * @return if PNP worked
	 */
	private static boolean calculateTapePosPNPLL() {
		double[] camdata = camtran.getDoubleArray(new double[] {});
		System.out.println(Arrays.toString(camdata));
		if (Arrays.stream(camdata).allMatch(x -> x == 0)) {
			return false;
		}

		// limelight's camtran array solves everything for us, with a sign change
		objectToFieldCoords(new double[] {camdata[0], camdata[2], camdata[4]});

		return true;
	}

	private static boolean calculateTapePosPNPRIO() {
		double[] xcorners = tcornx.getDoubleArray(new double[] {});
		double[] ycorners = tcorny.getDoubleArray(new double[] {});
		if (xcorners.length != 8 || ycorners.length != 8) {
			return false;
		}

		// if there are indeed 8 points total, then use helper class to sort the points
		PointSorter pointsorter = new PointSorter(xcorners, ycorners);
		MatOfPoint2f imagePoints = pointsorter.sortPoints();

		// solvePNP gets the object "pose," providing translation and rotation
		Mat rotationVector = new Mat();
		Mat translationVector = new Mat();
		Calib3d.solvePnP(mObjectPoints, imagePoints, mCameraMatrix, mDistortionCoefficients,
			rotationVector, translationVector);

		// System.out.println("rotationVector: " + rotationVector.dump());
		// System.out.println("translationVector: " + translationVector.dump());

		Mat rotationMatrix = new Mat();
		Calib3d.Rodrigues(rotationVector, rotationMatrix);
		Mat objectSpaceTranslationVector = new Mat();
		Core.gemm(rotationMatrix, translationVector, -1.0, new Mat(), 0.0, objectSpaceTranslationVector);

		// the x and z are turned to polar coordinates
		double camX = translationVector.get(0, 0)[0];
		double camZ = translationVector.get(2, 0)[0];
		double angle1 = Math.toDegrees(Math.atan2(camX, camZ));

		double objectX = objectSpaceTranslationVector.get(0, 0)[0];
		double objectZ = objectSpaceTranslationVector.get(2, 0)[0];
		double angle2 = Math.toDegrees(Math.atan2(-objectX, objectZ));

		double[] camdata = {objectX, objectZ, angle2 - angle1};
		System.out.println(Arrays.toString(camdata));

		objectToFieldCoords(camdata);

		return true;
	}
}
