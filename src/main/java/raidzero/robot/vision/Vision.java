package raidzero.robot.vision;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

import raidzero.robot.pathgen.*;

/**
 * Finding position of vision targets and generating waypoints to them.
 */
public class Vision {

	/**
     * NetworkTable Entries from the limelight about target contour
	 * 
     * <p>includes x position, y position, bounding box width, and pipeline
     */
	private static NetworkTableEntry tx, tv, thor, pipeline;

	public static double xpos, ypos, absoluteAng, pipedex, ang;
	private static boolean targPres;

	/**
     * Width of tape target
     */
	public static final double tapeWidth = 15.4;

	/**
     * Width of tape target
     */
	public static final double ballWidth = 12;//9.2;
	
	/**
     * Initializes limelight network table entries.
     */
	public static void setup() {
		NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight-kaluza");

		tx = table.getEntry("tx");
		tv = table.getEntry("tv");
		thor = table.getEntry("thor");
		pipeline = table.getEntry("pipeline");

		pipedex = pipeline.getDouble(0.0);
	}
	
	/**
     * Posts target information to SmartDashboard.
     */
	public static void postToSmartDashBoard() {
		SmartDashboard.putBoolean("Target Presence", targPres);
		SmartDashboard.putNumber("XTarget", xpos);
		SmartDashboard.putNumber("YTarget", ypos);
		SmartDashboard.putNumber("AbsAng", absoluteAng);
		SmartDashboard.putNumber("AngTarget", ang);
		SmartDashboard.putNumber("Pipeline", pipedex);
	}

	/**
     * Generates waypoints for splining to vision target.
     */
	public static Point[] pathToTarg() {
		if(targPres) {
			if(pipedex == 0) return new Point[] {new Point(0, 0, absoluteAng), new Point(xpos + 4, ypos + 10, 90)};
			else return new Point[] {new Point(0, 0, absoluteAng), new Point(xpos, ypos, ang + absoluteAng)};
		} else {
			return null;
		}
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

		if(tv.getDouble(0) == 1.0) {
			targPres = true;            
			if(pipedex == 0) {
				calculateTapePos();
			} else {
				calculateBallPos();
			}
		} else {
			targPres = false;
		}
	}

	/**
     * Calculates position of tape relative to robot.
     */
	public static void calculateTapePos() {
		double radAng = Math.toRadians(absoluteAng);

		//read values
		double ax = tx.getDouble(0) - 8.78;
		//double ay = ty.getDouble(0);
		double pwidth = thor.getDouble(0);

		//convert ang to pixel
		double px = 160*Math.tan(Math.toRadians(ax))/Math.tan(Math.toRadians(27)) + 160;

		//get back to angle for left and right bounds of target
		double ax1 = Math.atan2(Math.tan(Math.toRadians(27))/160*(px + pwidth/2.0 - 160), 1);
		double ax2 = Math.atan2(Math.tan(Math.toRadians(27))/160*(px - pwidth/2.0 - 160), 1);

		xpos = tapeWidth*Math.tan(radAng - ax1)/(Math.tan(radAng - ax2) - Math.tan(radAng - ax1));
		ypos = xpos*Math.tan(radAng - ax2);
		xpos += tapeWidth/2;
		ang = ax;
	}

	/**
     * Calculates position of ball relative to robot.
     */
	public static void calculateBallPos() {
		//read values
		double ax = tx.getDouble(0);
		//double ay = ty.getDouble(0);
		double pwidth = thor.getDouble(0);

		//convert ang to pixel
		double px = 160*Math.tan(Math.toRadians(ax))/Math.tan(Math.toRadians(27)) + 160;

		//get back to angle for left and right bounds of target
		double ax1 = -Math.atan2(Math.tan(Math.toRadians(27))/160*(px + pwidth/2.0 - 160), 1);
		double ax2 = -Math.atan2(Math.tan(Math.toRadians(27))/160*(px - pwidth/2.0 - 160), 1);

		// System.out.println(ax +  " " + ax1 + " " + ax2);

		double ballDist = ballWidth/2/Math.sin((ax2 - ax1)/2) - 44;
		xpos = ballDist*Math.cos((ax1 + ax2)/2 + Math.toRadians(absoluteAng));
		ypos = ballDist*Math.sin((ax1 + ax2)/2 + Math.toRadians(absoluteAng));
		// xpos = ballWidth*Math.tan(ax2)/(Math.tan(ax1) - Math.tan(ax2));
		// ypos = xpos/Math.tan(ax2);
		// xpos += ballWidth/2;
		ang = Math.toDegrees((ax1 + ax2)/2);
	}
}