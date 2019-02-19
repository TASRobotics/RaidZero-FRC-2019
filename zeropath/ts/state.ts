import { EventEmitter } from 'events';

import * as field from './field';

export interface Point {
    x: number;
    y: number;
}

export interface ZeroPathPoint {
    x: number;
    y: number;
    position: number;
    velocity: number;
    time: number;
    angle: number;
}

export const stateEmitter = new EventEmitter();
export const enum StateEvent {
    WaypointsUpdated = 'waypointsUpdated',
    PathUpdated = 'pathUpdated'
}

export default {
    waypoints: [{
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
    }] as Point[],
    path: [] as ZeroPathPoint[]
};
