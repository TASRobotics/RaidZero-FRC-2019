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
                while (angles[i] - angles[i - 1] > 180) {
                    angles[i] -= 360;
                }
                while (angles[i] - angles[i - 1] < -180) {
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
        var totalDistance = positions[positions.length - 1];

        var velocities = new double[queryCount];
        var times = new double[queryCount];
        {
            boolean reachesCruiseVelocity = false;
            int i;
            for (i = 0; positions[i] <= totalDistance / 2; i++) {
                var velocity = Math.sqrt(2 * targetAcceleration * positions[i]);
                if (velocity > cruiseVelocity) {
                    reachesCruiseVelocity = true;
                    break;
                }
                velocities[i] = velocity;
                times[i] = Math.sqrt(2 * positions[i] / targetAcceleration);
            }
            int j;
            for (j = velocities.length - 1; j >= i; j--) {
                var velocity = Math.sqrt(2 * targetAcceleration * (totalDistance - positions[j]));
                if (velocity > cruiseVelocity) {
                    break;
                }
                velocities[j] = velocity;
            }
            var cruiseStartTime = cruiseVelocity / targetAcceleration;
            var cruiseStartPosition = cruiseVelocity * cruiseVelocity / (2 * targetAcceleration);
            int k;
            for (k = i; k <= j; k++) {
                velocities[k] = cruiseVelocity;
                times[k] = cruiseStartTime + (positions[k] - cruiseStartPosition) / cruiseVelocity;
            }
            var decelerateStartPosition = reachesCruiseVelocity
                ? totalDistance - cruiseStartPosition
                : totalDistance / 2;
            var decelerateStartTime = reachesCruiseVelocity
                ? cruiseStartTime + (decelerateStartPosition - cruiseStartPosition) / cruiseVelocity
                : Math.sqrt(totalDistance / targetAcceleration);
            var decelerateInitialVelocity = reachesCruiseVelocity
                ? cruiseVelocity
                : Math.sqrt(targetAcceleration * totalDistance);
            for (; k < times.length; k++) {
                times[k] = decelerateStartTime
                    + (-decelerateInitialVelocity
                        + Math.sqrt(decelerateInitialVelocity * decelerateInitialVelocity
                            - 2 * -targetAcceleration * (decelerateStartPosition - positions[k])))
                    / -targetAcceleration;
            }
        }

    }

}
