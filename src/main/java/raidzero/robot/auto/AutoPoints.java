package raidzero.robot.auto;

import raidzero.robot.pathgen.Point;
import java.lang.Math;

public class AutoPoints {
    // Starting positions for the robot on upper half of the field
    public static final Point leftL1 = new Point(67, 205, 0);
    public static final Point leftL2 = new Point(24, 205, 0);

    // Starting positions for the robot on right bottom half of the field
    public static final Point rightL1 = new Point(67, 119, 0);
    public static final Point rightL2 = new Point(24, 119, 0);



    // Front cargo positions
    public static final Point leftFrontCargo = new Point(201.8, 172.3, 0);
    public static final Point rightFrontCargo = new Point(201.8, 150.4, 0);



    // Cargo positions on upper half of the field
    public static final Point leftCargo1 = new Point(260.6, 208, Math.PI / 2);
    public static final Point leftCargo2 = new Point(282.5, 208, Math.PI / 2);
    public static final Point leftCargo3 = new Point(304, 208, Math.PI / 2);

    // Rocket positions on upper half of the field where 1 is the closer rocket station.
    public static final Point leftRocket1 = new Point(260.6, 208, 3 * Math.PI / 16);
    public static final Point leftRocket2 = new Point(282.5, 208, Math.PI / 2);



    // Cargo positions on right bottom half of the field
    public static final Point rightCargo1 = new Point(260.6, 116, Math.PI / 2);
    public static final Point rightCargo2 = new Point(282.5, 116, Math.PI / 2);
    public static final Point rightCargo3 = new Point(304, 116, Math.PI / 2);

    // Rocket positions on right bottom half of the field where 1 is the closer rocket station.
    public static final Point rightRocket1 = new Point(198, 30, -3 * Math.PI / 16);
    public static final Point rightRocket2 = new Point(229, 48, -Math.PI / 2);



    // Loading station positions
    public static final Point leftLS = new Point(20, 296, Math.PI);
    public static final Point rightLS = new Point(20, 26.6, Math.PI / 2);
}