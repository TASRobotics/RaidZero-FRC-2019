package raidzero.robot.vision;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

import raidzero.robot.pathgen.*;

public class Vision {

    private static NetworkTableEntry tx, ty, tv, thor, pipeline;

    private static double xpos, ypos, absoluteAng, pipedex, ang;

	private static final double tapeWidth = 14.0;
	private static final double ballWidth = 12;//9.2;
    
        
    public static void initVision() {
		NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight-kaluza");

		tx = table.getEntry("tx");
		tv = table.getEntry("tv");
		thor = table.getEntry("thor");
		pipeline = table.getEntry("pipeline");

		pipedex = pipeline.getDouble(0.0);
    }
    
    public static Point[] getTargetPos(double absAng) {
        if(tv.getDouble(0) == 1.0) {
            absoluteAng = absAng;
			if(pipedex == 0) {
                calculateTapePos();
                return new Point[] {new Point(0, 0, absoluteAng), new Point(xpos + 3, ypos - 35, 90)};
            } else {
                calculateBallPos();
                return new Point[] {new Point(0, 0, absoluteAng), new Point(xpos, ypos, ang + absoluteAng)};
            }
        }
        return null;
    }

	public static void calculateTapePos() {
		double radAng = Math.toRadians(absoluteAng);

		//read values
		double ax = tx.getDouble(0);
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
        
        return;
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

		// System.out.println(ang +  " " + ax1 + " " + ax2);
	}
}