package raidzero.robot;

/**
 * Simple tuple-like data class with x and y coordinates.
 */
public class Point {

    /**
     * The x-coordinate.
     */
    public final double x;

    /**
     * The y-coordinate.
     */
    public final double y;

    /**
     * Constructs a Point object.
     * 
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

}
