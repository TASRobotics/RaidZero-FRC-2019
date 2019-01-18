function motionProfile = RobotPath()
    % Units are in inches
    CruiseVel = 10; % Units: Inches per 100 ms
    TargetAcc = 20; % Units: Inches per 100 ms per ms 
    Updatetime = 0.1; % Default update time: 0.1 DON'T CHANGE!
    DeltaD = Updatetime*CruiseVel; % Self Explanatory
    
    Waypoints = [65.8 203.2; 78.3 267.7; 134.2 293.1; 128.5 211.8; 192.6 179.4]; % Input the waypoints here! Units: Inches
    
    numPoints = length(Waypoints(:,1)); % Number of waypoints
    interpoints = 0:.01:1; % Create points from 0 -> 1 incrementing 0.01
    
    Direction = Waypoints(1:numPoints-1,:) - Waypoints(2:numPoints,:);
    Dist = sqrt(diag(Direction * Direction'));
    % finds distance between each point
    param = [0; cumsum(Dist)/sum(Dist)];
    % divides distance traveled by total distance
    
    Fit = spline(param, Waypoints', interpoints);
    % finds points in an arc based on points given by waypoint seperated by
    % values from param and limited (min and max value) by interpoints
    % scatter(Fit(1,:), Fit(2,:)); 
    plot(Fit(1,:),Fit(2,:));
    hold on;
    axis equal;
    xlim([0, 648]);
    ylim([0, 324]);
    angles = makeAngles(Fit');
    % finds the angle in between each point from one point to next
    travelled = zeros(length(param),1);
    % initialize distance travelled matrix
    
    for i = 2:length(Fit)
        travelled(i) = arclength(Fit(1,1:i), Fit(2,1:i));
    end
    % travelled gives you total distance travelled after going pass each
    % point of arclength
    TotalDist = travelled(length(travelled))
    % total distance travelled
    
    Nacc = ceil(CruiseVel^2 / (TargetAcc * DeltaD)); % ??
    Nvel = ceil(TotalDist/DeltaD - Nacc); % ??
    
    distmarks = zeros(2*Nacc+Nvel,1);
    velmarks = zeros(length(distmarks),1);
    for i=1:Nacc
        distmarks(i) = 1/2*TargetAcc*(i*DeltaD/CruiseVel)^2;
        distmarks(length(distmarks)+1-i) = TotalDist - distmarks(i);
        velmarks(i) = i*TargetAcc*DeltaD/CruiseVel;
        velmarks(length(velmarks)+1-i) = velmarks(i);
    end
    for i=1:Nvel
        distmarks(Nacc+i) = distmarks(Nacc)+i*DeltaD;
        velmarks(Nacc+i) = CruiseVel;
    end
    times = 0:Updatetime:Updatetime*(length(distmarks)-1);
    dist2Param = spline(travelled,interpoints',distmarks);
    anglemarks = spline(interpoints,angles,dist2Param)-90;
    quiver(spline(interpoints,Fit(1,:),dist2Param),spline(interpoints,Fit(2,:),dist2Param),cos(pi/180*spline(interpoints,angles,dist2Param)),sin(pi/180*spline(interpoints,angles,dist2Param)));
    motionProfileRaw = [distmarks velmarks times' anglemarks];
    
    % Convert to Java Format
    motionProfile = strings([length(motionProfileRaw), 1]); % Create empty list of strings
    for i = 1:length(motionProfileRaw)
        allOne = sprintf('%.4f,' , motionProfileRaw(i,:)); % Combine the columns for that row
        motionProfile(i) = allOne(1:end-1); % Remove extra comma
        if i == length(motionProfileRaw)
            motionProfile(i) = "{" + motionProfile(i) + "}";
        else
            motionProfile(i) = "{" + motionProfile(i) + "},";
        end
    end
    fprintf('%s\n', motionProfile{:});
    
    motionProfile = length(motionProfile); % Returns # of points
    hold off;
    
    function angles = makeAngles(trajectory)
        numpoints = length(trajectory(:,1));
        % finds number of points on trajectory
        comparison = [trajectory(1,1) trajectory(1,2);
            trajectory;
            trajectory(numpoints,1) trajectory(numpoints,1)];
        comparison(2,:) = [(comparison(1,1)+comparison(3,1))/2, (comparison(1,2)+comparison(3,2))/2]; % This is here to fix the second row which messes up the initial angles. It removes the first row.
        angles = unwrap(atan2(comparison(2:numpoints+1,2)-comparison(1:numpoints,2),(comparison(2:numpoints+1,1)-comparison(1:numpoints,1))));
        angles = 360/(2*pi)*angles;
    end
end