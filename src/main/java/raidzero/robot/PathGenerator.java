package raidzero.robot;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;

public class PathGenerator {

    private static final double cruiseVelocity = 10;
    private static final double targetAcceleration = 20;

    private static final double queryInterval = 1;

    private static SplineInterpolator splineInterpolator = new SplineInterpolator();

    public static void generatePath(Point[] waypoints) {

        var waypointXValues = new double[waypoints.length];
        var waypointYValues = new double[waypoints.length];
        for (var i = 0; i < waypoints.length; i++) {
            waypointXValues[i] = waypoints[i].x;
            waypointYValues[i] = waypoints[i].y;
        }

        var cumulativeWaypointDistances = new double[waypoints.length];
        for (var i = 1; i < waypoints.length; i++) {
            cumulativeWaypointDistances[i] = cumulativeWaypointDistances[i - 1]
                + Math.hypot(waypointXValues[i] - waypointXValues[i - 1],
                    waypointYValues[i] - waypointYValues[i - 1]);
        }
        var totalWaypointDistance =
            cumulativeWaypointDistances[cumulativeWaypointDistances.length - 1];

        var xSpline = splineInterpolator.interpolate(cumulativeWaypointDistances, waypointXValues);
        var ySpline = splineInterpolator.interpolate(cumulativeWaypointDistances, waypointYValues);

        var queryCount = (int) Math.ceil(totalWaypointDistance / queryInterval) + 1;
        var xQueries = new double[queryCount];
        var yQueries = new double[queryCount];
        for (var i = 0; i < queryCount - 1; i++) {
            xQueries[i] = xSpline.value(i * queryInterval);
            yQueries[i] = ySpline.value(i * queryInterval);
        }
        xQueries[queryCount - 1] = waypointXValues[waypointXValues.length - 1];
        yQueries[queryCount - 1] = waypointYValues[waypointYValues.length - 1];

        var angles = new double[queryCount];
        for (var i = 1; i < angles.length; i++) {
            angles[i] = Math.toDegrees(Math.atan2(
                yQueries[i] - yQueries[i - 1], xQueries[i] - xQueries[i - 1]));
            if (i > 1) {
                if (angles[i] - angles[i - 1] > 180) {
                    angles[i] -= 360;
                } else if (angles[i] - angles[i - 1] < -180) {
                    angles[i] += 360;
                }
            }
        }
        angles[0] = angles[1];

        var positions = new double[queryCount];
        for (var i = 1; i < positions.length; i++) {
            positions[i] = positions[i - 1]
                + Math.hypot(xQueries[i] - xQueries[i - 1], yQueries[i] - yQueries[i - 1]);
        }

    }

}
