package raidzero.robot.vision;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

import java.util.Optional;

import raidzero.robot.pathgen.*;

/**
 * Finding position of vision targets and generating waypoints to them.
 */
public class Vision {

	private static NetworkTableEntry tx, tv, thor, pipeline;
	private static double xpos, ypos, absoluteAng, pipedex, ang;
	private static boolean targPres;

	/**
     * Width of tape target
     */
	public static final double tapeWidth = 11;

	/**
     * Width of tape target
     */
	public static final double ballWidth = 12;

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

		// Set the pipeline index to whatever the limelight is currently at
		pipedex = pipeline.getDouble(0.0);
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
				endPoint = new Point(xpos, ypos - 40, 90);
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
				calculateTapePos();
			} else if (pipedex == 1) {
				calculateBallPos();
			}
		} else {
			targPres = false;
		}
	}

	private static void calculateTapePos() {
		// Read x position and width from NetworkTable Entries
		double ax = tx.getDouble(0);
		double pwidth = thor.getDouble(0);

		// Convert angle of box center to pixel
		double px = 160*Math.tan(Math.toRadians(ax)) / Math.tan(Math.toRadians(27)) + 160;

		// Get angle for left and right bounds of box
		double ax1 = Math.atan2(Math.tan(Math.toRadians(27)) / 160 * (px + pwidth / 2.0 - 160), 1);
		double ax2 = Math.atan2(Math.tan(Math.toRadians(27)) / 160 * (px - pwidth / 2.0 - 160), 1);

		// Calculate position to ball using trigonometry with gyroscope angle
		xpos = tapeWidth * Math.tan(Math.toRadians(absoluteAng) - ax1) / (Math.tan(Math.toRadians(
			absoluteAng) - ax2) - Math.tan(Math.toRadians(absoluteAng) - ax1));
		ypos = xpos * Math.tan(Math.toRadians(absoluteAng) - ax2);
		xpos += tapeWidth/2;
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
		double ballDist = ballWidth/2/Math.sin(-(ax2 - ax1)/2) - 44;
		xpos = ballDist*Math.cos(-(ax1 + ax2) / 2 + Math.toRadians(absoluteAng));
		ypos = ballDist*Math.sin(-(ax1 + ax2) / 2 + Math.toRadians(absoluteAng));
		ang = -ax;
	}
}