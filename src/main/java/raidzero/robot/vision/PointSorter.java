package raidzero.robot.vision;

import org.opencv.core.Point;
import org.opencv.core.MatOfPoint2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

public class PointSorter {

    private double[] xcorns, ycorns;

    public PointSorter(double[] xcorns, double[] ycorns) {
        this.xcorns = xcorns;
        this.ycorns = ycorns;
    }

    public MatOfPoint2f sortPoints() {
        List<Point> allPoints = new ArrayList<>();
        for(int j = 0; j < 8; j++) {
            allPoints.add(new Point(xcorns[j], ycorns[j]));
        }
        allPoints.sort(Comparator.comparing(point -> (point.x)));

        List<Point> leftPoints = allPoints.subList(0, 4);
        List<Point> rightPoints = allPoints.subList(4, 8);

        leftPoints.sort(Comparator.comparing(point -> (point.x + point.y * 1000)));
        rightPoints.sort(Comparator.comparing(point -> (point.x + point.y * 1000)));

        return new MatOfPoint2f(
                leftPoints.get(0),
                leftPoints.get(1),
                leftPoints.get(2),
                leftPoints.get(3),
                rightPoints.get(0),
                rightPoints.get(1),
                rightPoints.get(2),
                rightPoints.get(3)
        );
    }
}