package raidzero.robot.vision;

import org.opencv.core.Point;
import org.opencv.core.MatOfPoint2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

/**
 * Helper class to sort points of tape targets.
 */
public class PointSorter {

    private double[] xcorns, ycorns;

    /**
     * Initialize object with the limelight-generated x and y corner arrays.
     *
     * @param xcorns double array of corner x-values
     * @param ycorns double array of corner y-values
     */
    public PointSorter(double[] xcorns, double[] ycorns) {
        this.xcorns = xcorns;
        this.ycorns = ycorns;
    }

    /**
     * Returns MatOfPoint2f with all corner points sorted.
     */
    public MatOfPoint2f sortPoints() {
        // Add points to a list and sort by x values
        List<Point> allPoints = new ArrayList<>();
        for(int j = 0; j < 8; j++) {
            allPoints.add(new Point(xcorns[j], ycorns[j]));
        }
        allPoints.sort(Comparator.comparing(point -> (point.x)));

        // Separate list into those on the left tape and those on the right tape
        List<Point> leftPoints = allPoints.subList(0, 4);
        List<Point> rightPoints = allPoints.subList(4, 8);

        // For each piece of tape, sort points vertically
        leftPoints.sort(Comparator.comparing(point -> (point.x + point.y * 1000)));
        rightPoints.sort(Comparator.comparing(point -> (point.x + point.y * 1000)));

        return new MatOfPoint2f(
                leftPoints.get(0),
                leftPoints.get(1),
                leftPoints.get(2),
                // leftPoints.get(3),
                rightPoints.get(0),
                rightPoints.get(1),
                rightPoints.get(2)
                // rightPoints.get(3)
        );
    }
}