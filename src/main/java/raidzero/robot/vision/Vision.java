package raidzero.robot.vision;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

import raidzero.robot.pathgen.*;

public class Vision {

	private static NetworkTableEntry tx, tv, thor, pipeline;

	public static double xpos, ypos, absoluteAng, pipedex, ang;
	private static boolean targPres;

	private static final double tapeWidth = 15.4;
	private static final double ballWidth = 12;//9.2;
	
		
	public static void setup() {
		NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight-kaluza");

		tx = table.getEntry("tx");
		tv = table.getEntry("tv");
		thor = table.getEntry("thor");
		pipeline = table.getEntry("pipeline");

		pipedex = pipeline.getDouble(0.0);
	}
	
	public static void postToSmartDashBoard() {
		SmartDashboard.putBoolean("Target Presence", targPres);
		SmartDashboard.putNumber("XTarget", xpos);
		SmartDashboard.putNumber("YTarget", ypos);
		SmartDashboard.putNumber("AbsAng", absoluteAng);
		SmartDashboard.putNumber("AngTarget", ang);
		SmartDashboard.putNumber("Pipeline", pipedex);
	}

	public static Point[] pathToTarg() {
		if(targPres) {
			if(pipedex == 0) return new Point[] {new Point(0, 0, absoluteAng), new Point(xpos + 4, ypos + 10, 90)};
			else return new Point[] {new Point(0, 0, absoluteAng), new Point(xpos, ypos, ang + absoluteAng)};
		} else {
			return null;
		}
	}
	
	public static void calculateTargPos(int mode, double absAng) {
		pipedex = mode;
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