package raidzero.robot.pathgen;

import org.apache.commons.math3.analysis.interpolation.HermiteInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;

/**
 * Calculations for generating path from waypoints.
 */
public class PathGenerator {

    /**
     * Number of query points per inch of approximate distance.
     *
     * <p>Increasing this number results in more query points and greater
     * accuracy for path generation.
     */
    private static final double QUERY_INTERVAL = 1;

    private static SplineInterpolator splineInterpolator = new SplineInterpolator();

    /**
     * Generates a path that passes through the given waypoints on the field.
     *
     * <p>The returned path contains data which can be passed to motion profile to execute the path.
     * The robot will accelerate at a constant rate, then reach a constant cruise velocity, then
     * decelerate at the same rate. In other words, the velocity vs time graph shows an isosceles
     * trapezoid.
     *
     * @param waypoints points that the path should pass through
     * @param cruiseVelocity the target constant cruise velocity of the robot in in/100ms
     * @param targetAcceleration the target constant acceleration (and deceleration) of the robot in
     * in/100ms/s
     * @return an array of points on the path
     */
    public static PathPoint[] generatePath(Point[] waypoints,
    double cruiseVelocity, double targetAcceleration) {

        // Convert the target acceleration to in/(100ms)^2 for consistent unit of time
        targetAcceleration /= 10;

        // Separate the waypoints array into two arrays of x and y values respectively
        var waypointXValues = new double[waypoints.length];
        var waypointYValues = new double[waypoints.length];
        for (var i = 0; i < waypoints.length; i++) {
            waypointXValues[i] = waypoints[i].x;
            waypointYValues[i] = waypoints[i].y;
        }

        // Calculate the running total of straight-line distances between waypoints
        var cumulativeWaypointDistances = new double[waypoints.length];
        for (var i = 1; i < waypoints.length; i++) {
            cumulativeWaypointDistances[i] = cumulativeWaypointDistances[i - 1]
                + Math.hypot(waypointXValues[i] - waypointXValues[i - 1],
                    waypointYValues[i] - waypointYValues[i - 1]);
        }
        var totalWaypointDistance =
            cumulativeWaypointDistances[cumulativeWaypointDistances.length - 1];

        // Use the straight-line distance between waypoints as a monotonically increasing
        // approximation for the time/distance parameter (since we don't know the time or distance
        // yet) to calculate hermite spline interpolations for the x values and y values separately.
        // This means that the resulting query points for the spline will NOT be evenly spaced with
        // respect to time or distance.
        HermiteInterpolator hermiteInterpolatorX = new HermiteInterpolator();
        HermiteInterpolator hermiteInterpolatorY = new HermiteInterpolator();
        for (var i = 0; i < waypoints.length; i++) {
            if (waypoints[i].a.isPresent()) {
                hermiteInterpolatorX.addSamplePoint(cumulativeWaypointDistances[i],
                    new double[] {waypoints[i].x}, new double[] {Math.cos(waypoints[i].a.getAsDouble())});
                hermiteInterpolatorY.addSamplePoint(cumulativeWaypointDistances[i],
                    new double[] {waypoints[i].y}, new double[] {Math.sin(waypoints[i].a.getAsDouble())});
            } else {
                hermiteInterpolatorX.addSamplePoint(cumulativeWaypointDistances[i],
                    new double[] {waypoints[i].x});
                hermiteInterpolatorY.addSamplePoint(cumulativeWaypointDistances[i],
                    new double[] {waypoints[i].y});
            }
        }
        var xSpline = hermiteInterpolatorX.getPolynomials()[0];
        var ySpline = hermiteInterpolatorY.getPolynomials()[0];
        var dxSpline = hermiteInterpolatorX.getPolynomials()[0].derivative();
        var dySpline = hermiteInterpolatorY.getPolynomials()[0].derivative();

        // We want queries to be evenly spaced (with respect to straight-line distance between
        // waypoints) starting from the first waypoint, then have a query at the last waypoint no
        // matter what. This may result in the spacing between the second-to-last waypoint and the
        // last waypoint to be different than the standard query interval.
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

        // The angle for each point is calculated from the arctan of the ratio of the y and x derivatives.
        // Since atan2 wraps the angle to be within -180 to 180 (because it has no way of knowing
        // the actual angle of the robot), we estimate the real unwrapped angle by looking at if the
        // current angle would be closer to the previous angle after adding or subtracting 360. This
        // is fine because the robot can't turn more than 180 degrees in between two data points on
        // the path. The angle for the first point should be given. The angle of the last points
        // needs to be inputted directly from last waypoint because derivative function breaks there.
        path[0].angle = Math.toDegrees(waypoints[0].a.getAsDouble());
        for (var i = 1; i < path.length; i++) {
            path[i].angle = Math.toDegrees(Math.atan2(dyQueries[i], dxQueries[i]));
            if (i == path.length - 1) {
                path[i].angle = Math.toDegrees(waypoints[waypoints.length - 1].a.getAsDouble());
            }
            if (i > 1) {
                while (path[i].angle - path[i - 1].angle > 180) {
                    path[i].angle -= 360;
                }
                while (path[i].angle - path[i - 1].angle < -180) {
                    path[i].angle += 360;
                }
            }
        }

        // Position is calculated with a running total of arc lengths with Riemann sum.
        var cumPos = 0;
        for (var i = 1; i < path.length; i++) {
            cumPos += 0.5*Math.hypot(dxQueries[i - 1], dyQueries[i - 1])*queryInterval
                + 0.5*Math.hypot(dxQueries[i], dyQueries[i])*queryInterval;
            path[i].position = cumPos;
        }
        var totalDistance = path[path.length - 1].position;

        // Calculations for velocity and time are in three separate stages for the acceleration,
        // constant velocity, and deceleration parts respectively.
        {
            var reachesCruiseVelocity = false;
            // First stage: we find velocity and time for data points assuming constant acceleration
            // up until either (1) we reach the cruise velocity, or (2) we have not reached cruise
            // velocity and we are halfway through the path already, in which case we must start
            // decelerating.
            int i;
            for (i = 0; path[i].position <= totalDistance / 2; i++) {
                // v^2 = 2 a x
                // v = sqrt(2 a x)
                var velocity = Math.sqrt(2 * targetAcceleration * path[i].position);
                if (velocity > cruiseVelocity) {
                    reachesCruiseVelocity = true;
                    break;
                }
                path[i].velocity = velocity;
                // x = (1/2) a t^2
                // t^2 = 2 x / a
                // t = sqrt(2 x / a)
                path[i].time = Math.sqrt(2 * path[i].position / targetAcceleration);
            }
            // Third stage for velocity: we do the same thing as the first stage, but working
            // backwards instead. We stop when either (1) we reach the cruise velocity, or (2) we
            // reach where stage 1 stopped, in which case there is no constant velocity part.
            int j;
            for (j = path.length - 1; j >= i; j--) {
                var velocity = Math.sqrt(2 * targetAcceleration
                    * (totalDistance - path[j].position));
                if (velocity > cruiseVelocity) {
                    break;
                }
                path[j].velocity = velocity;
            }
            // v = a t
            // t = v / a
            var cruiseStartTime = cruiseVelocity / targetAcceleration;
            // v^2 = 2 a x
            // x = v^2 / (2 a)
            var cruiseStartPosition = cruiseVelocity * cruiseVelocity / (2 * targetAcceleration);
            int k;
            // Second stage: we calculate velocity and time in between the end of first stage and
            // start of third stage. If we never reach the cruise velocity, this loop never runs.
            for (k = i; k <= j; k++) {
                path[k].velocity = cruiseVelocity;
                // t = t0 + delta_t
                // v = delta_x / delta_t
                // delta_t = delta_x / v
                // delta_x = x - x0
                // t = t0 + (x - x0) / v
                path[k].time = cruiseStartTime
                    + (path[k].position - cruiseStartPosition) / cruiseVelocity;
            }
            var decelerateStartPosition = reachesCruiseVelocity
                // It takes the same distance to accelerate and decelerate to/from the same velocity
                ? totalDistance - cruiseStartPosition
                : totalDistance / 2;
            var decelerateStartTime = reachesCruiseVelocity
                ? cruiseStartTime + (decelerateStartPosition - cruiseStartPosition) / cruiseVelocity
                : Math.sqrt(totalDistance / targetAcceleration);
            var decelerateInitialVelocity = reachesCruiseVelocity
                ? cruiseVelocity
                // v^2 = 2 a x_mid
                // v = sqrt(2 a x_mid)
                // x_mid = x_total / 2
                // v = sqrt(a x_total)
                : Math.sqrt(targetAcceleration * totalDistance);
            // Third stage for time: calculate from end of stage 2 to end of path
            for (; k < path.length; k++) {
                // x = x0 + v0 t + (1/2) (-a) t^2
                // (1/2) (-a) t^2 + v0 t + (x0 - x) = 0
                // t = (-v0 +- sqrt(v0^2 - 4 ((1/2) (-a)) (x0 - x))) / (2 ((1/2) (-a)))
                // t = (-v0 +- sqrt(v0^2 - 2 (-a) (x0 - x))) / -a
                path[k].time = decelerateStartTime
                    + (-decelerateInitialVelocity
                        + Math.sqrt(decelerateInitialVelocity * decelerateInitialVelocity
                            - 2 * -targetAcceleration
                                * (decelerateStartPosition - path[k].position)))
                    / -targetAcceleration;
            }
        }

        // Calculate the time differences (i.e. make the times not cumulative)
        for (var i = path.length - 1; i > 0; i--) {
            path[i].time -= path[i - 1].time;
        }

        return path;

    }

}
