package raidzero.robot.auto;

import raidzero.robot.pathgen.Point;
import java.lang.Math;

public class AutoPoints {
    // Starting positions for the robot on upper half of the field
    public static final Point topHabitat = new Point(67, 205, 0);
    public static final Point topLevel1 = new Point(24, 205, 0);
    public static final Point topLevel2 = new Point(24, 205, 0);

    // Starting positions for the robot on bottom half of the field
    public static final Point botHabitat = new Point(67, 119, 0);
    public static final Point botLevel1 = new Point(24, 119, 0);
    public static final Point botLevel2 = new Point(24, 162, 0);



    // fFront cargo positions
    public static final Point topFrontCargo = new Point(201.8, 172.3, 0);
    public static final Point botFrontCargo = new Point(201.8, 150.4, 0);

    // Cargo positions on upper half of the field
    public static final Point topCargo1 = new Point(260.6, 208, Math.PI / 2);
    public static final Point topCargo2 = new Point(282.5, 208, Math.PI / 2);
    public static final Point topCargo3 = new Point(304, 208, Math.PI / 2);

    // Rocket positions on upper half of the field where 1 is the closer rocket station.
    public static final Point topRocket1 = new Point(260.6, 208, 3 * Math.PI / 16);
    public static final Point topRocket2 = new Point(282.5, 208, Math.PI / 2);


    // Cargo positions on bottom half of the field
    public static final Point botCargo1 = new Point(260.6, 116, Math.PI / 2);
    public static final Point botCargo2 = new Point(282.5, 116, Math.PI / 2);
    public static final Point botCargo3 = new Point(304, 116, Math.PI / 2);

    // Rocket positions on bottom half of the field where 1 is the closer rocket station.
    public static final Point botRocket1 = new Point(198, 30, -3 * Math.PI / 16);
    public static final Point botRocket2 = new Point(229, 48, -Math.PI / 2);
}