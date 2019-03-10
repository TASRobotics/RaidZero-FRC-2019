import { Point, PathPoint } from './types';

const waypoints = [{
    x: 0,
    y: 0
}, {
    x: 100,
    y: 100
}, {
    x: 200,
    y: 300
}, {
    x: 300,
    y: 300
}] as Point[];

export default {
    waypoints,
    selectedWaypointIndex: 0,
    path: [] as PathPoint[]
};
