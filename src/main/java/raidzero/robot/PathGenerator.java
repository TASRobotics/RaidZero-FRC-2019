package raidzero.robot;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;

public class PathGenerator {

    private static final double cruiseVelocity = 10;
    private static final double targetAcceleration = 20;
    private static final double updateTime = 0.1;
    private static final double deltaD = updateTime * cruiseVelocity;

    private static final double samplingInterval = 1;

    public static void generatePath(Point[] waypoints) {
        var cumulativeDistances = new double[waypoints.length];
        for (var i = 1; i < waypoints.length; i++) {
            cumulativeDistances[i] = cumulativeDistances[i - 1]
                + Math.hypot(waypoints[i].x - waypoints[i - 1].x,
                    waypoints[i].y - waypoints[i - 1].y);
        }
        var totalDistance = cumulativeDistances[cumulativeDistances.length - 1];
        var xValues = new double[waypoints.length];
        var yValues = new double[waypoints.length];
        for (var i = 0; i < waypoints.length; i++) {
            xValues[i] = waypoints[i].x;
            yValues[i] = waypoints[i].y;
        }
        var splineInterpolator = new SplineInterpolator();
        var xSpline = splineInterpolator.interpolate(cumulativeDistances, xValues);
        var ySpline = splineInterpolator.interpolate(cumulativeDistances, yValues);
        var angles = new double[(int) (totalDistance / samplingInterval)];
        for (var i = 1; i < waypoints.length; i++) {
            angles[i] = Math.toDegrees(
                Math.atan2(yValues[i] - yValues[i - 1], xValues[i] - xValues[i - 1]));
            if (i > 1) {
                if (angles[i] - angles[i - 1] > 180) {
                    angles[i] -= 360;
                } else if (angles[i] - angles[i - 1] < -180) {
                    angles[i] += 360;
                }
            }
        }
        angles[0] = angles[1];
    }

}
