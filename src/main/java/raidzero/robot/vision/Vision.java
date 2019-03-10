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

import java.util.Optional;
import raidzero.pathgen.Point;

/**
 * Finding position of vision targets and generating waypoints to them.
 */
public class Vision {

	/**
     * The ways of targetting tape targets
     */
	private enum TapeTargetMethod {
		Crude, PNPGyro, PurePNP;
	}

	private static NetworkTableEntry tx, tv, thor, pipeline, tcornx, tcorny, camtran;
	private static double xpos, ypos, absoluteAng, pipedex, ang;
	private static boolean targPres;

	private static MatOfPoint3f mObjectPoints;
    private static Mat mCameraMatrix;
	private static MatOfDouble mDistortionCoefficients;

	/**
     * The tape-targetting method to actually use.
     */
	private static final TapeTargetMethod TAPE_TARGET_METHOD = TapeTargetMethod.PNPGyro;

	/**
     * How far limelight should be behind the tape target in inches, adjustable.
     */
	private static final double Y_OFFSET = 30;

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
	public static void setup() {
		NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight-kaluza");

		// Initialize the NetworkTable entries from the Limelight
		tx = table.getEntry("tx");
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
                new Point3(-1.377 - target_gap/2, -5.325, 0.0), // left bottom-right
                new Point3(1.9363 + target_gap/2, 0.5008, 0.0), // right top-right
				new Point3(0.0 + target_gap/2, 0.0, 0.0), // right top-left
				new Point3(3.3133 + target_gap/2, -4.8242, 0.0), // right bottom-right
                new Point3(1.377 + target_gap/2, -5.325, 0.0) // right bottom-left
        );

        mCameraMatrix = Mat.eye(3, 3, CvType.CV_64F);
        mCameraMatrix.put(0, 0, 2.5751292067328632e+02);
        mCameraMatrix.put(0, 2, 1.5971077914723165e+02);
        mCameraMatrix.put(1, 1, 2.5635071715912881e+02);
		mCameraMatrix.put(1, 2, 1.1971433393615548e+02);

		mDistortionCoefficients = new MatOfDouble(2.9684613693070039e-01, -1.4380252254747885e+00,
			-2.2098421479494509e-03, -3.3894563533907176e-03, 2.5344430354806740e+00);
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
     * Generates waypoints for splining to vision target.
	 *
	 * <p>If called without seeing target, will return empty optional.
     */
	public static Optional<Point[]> pathToTarg() {
		if (targPres) {
			Point startPoint = new Point(0, 0, absoluteAng);
			Point endPoint;
			if (pipedex == 0) {
				// Must be pointing directly at target for tape, while offsets tunable.
				endPoint = new Point(xpos, ypos - Y_OFFSET, 90);
			} else {
				// Approach at an angle slightly steeper than angle at which target is seen.
				// This will make the spline "smoother" in a sense.
				endPoint = new Point(xpos, ypos, 1.5 * ang + absoluteAng);
			}
			return Optional.of(new Point[] { startPoint, endPoint });
		}
		return Optional.empty();
	}

	/**
     * Calculates and stores target position.
	 *
	 * <p>Call periodically when using vision.
     *
     * @param pipelineIndex the pipeline to use (0=tape, 1=ball)
     * @param absAng the gyroscope angle
     */
	public static void calculateTargPos(int pipelineIndex, double absAng) {
		pipedex = pipelineIndex;
		pipeline.setNumber(pipedex);

		absoluteAng = absAng;

		// Calculate position of respective target, or none
		if (tv.getDouble(0) == 1.0) {
			targPres = true;
			if (pipedex == 0) {
				switch(TAPE_TARGET_METHOD) {
					case Crude:
						calculateTapePosCrude();
						break;
					case PNPGyro:
						calculateTapePosPNPGyro();
						break;
					case PurePNP:
						calculateTapePosPurePNP();
						break;
				}
			} else if (pipedex == 1) {
				calculateBallPos();
			}
		} else {
			targPres = false;
		}
	}

	private static void calculateTapePosPurePNP() {
		double[] camdata = camtran.getDoubleArray(new double[] {});

		// limelight's camtran array solves everything for us, with a sign change
		xpos = -camdata[0];
		ypos = -camdata[2];
		ang = camdata[4];
	}

	private static void calculateTapePosPNPGyro() {
		double[] xcorners = tcornx.getDoubleArray(new double[] {});
		double[] ycorners = tcorny.getDoubleArray(new double[] {});
		if (xcorners.length != 8 || ycorners.length != 8) return;

		// if there are indeed 8 points total, then use helper class to sort the points
		PointSorter pointsorter = new PointSorter(xcorners, ycorners);
		MatOfPoint2f imagePoints = pointsorter.sortPoints();

		// solvePNP gets the object "pose," providing translation and rotation
		Mat translationVector = new Mat();
		Calib3d.solvePnP(mObjectPoints, imagePoints, mCameraMatrix, mDistortionCoefficients,
			new Mat(), translationVector);

		// the x and z are turned to polar coordinates
		double xInInches = translationVector.get(0, 0)[0];
		double zInInches = translationVector.get(2, 0)[0];
		double distance = Math.hypot(xInInches, zInInches);
		double angle = Math.atan2(xInInches, zInInches);

		// the polar coordinates are rotated by the gyro angle and turned back to Cartesian
		xpos = distance * Math.cos(angle + Math.toRadians(absoluteAng));
		ypos = distance * Math.sin(angle + Math.toRadians(absoluteAng));
		ang = Math.toDegrees(angle);
	}

	private static void calculateTapePosCrude() {
		// Read x position and width from NetworkTable Entries
		double ax = tx.getDouble(0);
		double pwidth = thor.getDouble(0);

		// Convert angle of box center to pixel
		double px = 160*Math.tan(Math.toRadians(ax)) / Math.tan(Math.toRadians(27)) + 160;

		// Get angle for left and right bounds of box
		double ax1 = Math.atan2(Math.tan(Math.toRadians(27)) / 160 * (px + pwidth / 2.0 - 160), 1);
		double ax2 = Math.atan2(Math.tan(Math.toRadians(27)) / 160 * (px - pwidth / 2.0 - 160), 1);

		// Calculate position to ball using trigonometry with gyroscope angle
		xpos = TAPE_WIDTH * Math.tan(Math.toRadians(absoluteAng) - ax1) / (Math.tan(Math.toRadians(
			absoluteAng) - ax2) - Math.tan(Math.toRadians(absoluteAng) - ax1));
		ypos = xpos * Math.tan(Math.toRadians(absoluteAng) - ax2);
		xpos += TAPE_WIDTH/2;
		ang = ax;
	}

	private static void calculateBallPos() {
		// Read x position and width from NetworkTable Entries
		double ax = tx.getDouble(0);
		double pwidth = thor.getDouble(0);

		// Convert angle of box center to pixel
		double px = 160*Math.tan(Math.toRadians(ax)) / Math.tan(Math.toRadians(27)) + 160;

		// Get angle for left and right bounds of box
		double ax1 = Math.atan2(Math.tan(Math.toRadians(27)) / 160 * (px + pwidth / 2.0 - 160), 1);
		double ax2 = Math.atan2(Math.tan(Math.toRadians(27)) / 160 * (px - pwidth / 2.0 - 160), 1);

		// Calculate distance to ball using left and right bounding angles and use
		// polar to Cartesian formula to get position.
		double ballDist = BALL_WIDTH/2/Math.sin(-(ax2 - ax1)/2) - 44;
		xpos = ballDist*Math.cos(-(ax1 + ax2) / 2 + Math.toRadians(absoluteAng));
		ypos = ballDist*Math.sin(-(ax1 + ax2) / 2 + Math.toRadians(absoluteAng));
		ang = -ax;
	}
}