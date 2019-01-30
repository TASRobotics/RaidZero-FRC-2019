package raidzero.robot;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;

public class PathGenerator {

    private static final double QUERY_INTERVAL = 1;

    private static SplineInterpolator splineInterpolator = new SplineInterpolator();

    public static PathPoint[] generatePath(Point[] waypoints,
    double cruiseVelocity, double targetAcceleration) {

        targetAcceleration /= 10;

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

        var queryCount = (int) Math.ceil(totalWaypointDistance / QUERY_INTERVAL) + 1;
        var xQueries = new double[queryCount];
        var yQueries = new double[queryCount];

        for (var i = 0; i < queryCount - 1; i++) {
            xQueries[i] = xSpline.value(i * QUERY_INTERVAL);
            yQueries[i] = ySpline.value(i * QUERY_INTERVAL);
        }
        xQueries[queryCount - 1] = waypointXValues[waypointXValues.length - 1];
        yQueries[queryCount - 1] = waypointYValues[waypointYValues.length - 1];

        var path = new PathPoint[queryCount];
        for (var i = 0; i < path.length; i++) {
            path[i] = new PathPoint();
        }

        for (var i = 1; i < path.length; i++) {
            path[i].angle = Math.toDegrees(Math.atan2(
                yQueries[i] - yQueries[i - 1], xQueries[i] - xQueries[i - 1]));
            if (i > 1) {
                while (path[i].angle - path[i - 1].angle > 180) {
                    path[i].angle -= 360;
                }
                while (path[i].angle - path[i - 1].angle < -180) {
                    path[i].angle += 360;
                }
            }
        }
        path[0].angle = path[1].angle;

        for (var i = 1; i < path.length; i++) {
            path[i].position = path[i - 1].position
                + Math.hypot(xQueries[i] - xQueries[i - 1], yQueries[i] - yQueries[i - 1]);
        }
        var totalDistance = path[path.length - 1].position;

        {
            boolean reachesCruiseVelocity = false;
            int i;
            for (i = 0; path[i].position <= totalDistance / 2; i++) {
                var velocity = Math.sqrt(2 * targetAcceleration * path[i].position);
                if (velocity > cruiseVelocity) {
                    reachesCruiseVelocity = true;
                    break;
                }
                path[i].velocity = velocity;
                path[i].time = Math.sqrt(2 * path[i].position / targetAcceleration);
            }
            int j;
            for (j = path.length - 1; j >= i; j--) {
                var velocity = Math.sqrt(2 * targetAcceleration
                    * (totalDistance - path[j].position));
                if (velocity > cruiseVelocity) {
                    break;
                }
                path[j].velocity = velocity;
            }
            var cruiseStartTime = cruiseVelocity / targetAcceleration;
            var cruiseStartPosition = cruiseVelocity * cruiseVelocity / (2 * targetAcceleration);
            int k;
            for (k = i; k <= j; k++) {
                path[k].velocity = cruiseVelocity;
                path[k].time = cruiseStartTime
                    + (path[k].position - cruiseStartPosition) / cruiseVelocity;
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
            for (; k < path.length; k++) {
                path[k].time = decelerateStartTime
                    + (-decelerateInitialVelocity
                        + Math.sqrt(decelerateInitialVelocity * decelerateInitialVelocity
                            - 2 * -targetAcceleration
                                * (decelerateStartPosition - path[k].position)))
                    / -targetAcceleration;
            }
        }

        for (var i = path.length - 1; i > 0; i--) {
            path[i].time -= path[i - 1].time;
        }

        return path;

    }

}
