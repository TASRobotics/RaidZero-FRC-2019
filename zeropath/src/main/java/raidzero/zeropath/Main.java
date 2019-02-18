package raidzero.zeropath;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import com.google.gson.Gson;

import raidzero.pathgen.PathGenerator;

public class Main {

    public static void main(String[] args) throws IOException {
        var reader = new BufferedReader(new InputStreamReader(System.in));
        var gson = new Gson();
        while (true) {
            var req = gson.fromJson(reader.readLine(), Request.class);
            var queryData = PathGenerator.getQueryData(req.waypoints);
            var splines = PathGenerator.calculateSplines(req.waypoints, queryData);
            var xQueries = PathGenerator.query(t -> splines.x.value(t)[0], queryData);
            var yQueries = PathGenerator.query(t -> splines.y.value(t)[0], queryData);
            var res = new Response();
            res.path = new ZeroPathPoint[queryData.queryCount];
            for (var i = 0; i < res.path.length; i++) {
                res.path[i] = new ZeroPathPoint();
                res.path[i].x = xQueries[i];
                res.path[i].y = yQueries[i];
            }
            PathGenerator.calculatePathPoints(
                res.path, req.cruiseVelocity, req.targetAcceleration, splines, queryData);
            System.out.println(gson.toJson(res));
        }
    }

}
