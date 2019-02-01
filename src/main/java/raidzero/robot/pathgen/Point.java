package raidzero.robot.pathgen;

import java.util.*;

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
     * The angle, if provided.
     */
    public final OptionalDouble a;

    /**
     * Constructs a Point object.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
        this.a = OptionalDouble.empty();
    }

    /**
     * Constructs a Point object.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param a the angle
     */
    public Point(double x, double y, double a) {
        this.x = x;
        this.y = y;
        this.a = OptionalDouble.of(Math.toRadians(a));
    }
}
