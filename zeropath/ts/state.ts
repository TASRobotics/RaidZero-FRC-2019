import { EventEmitter } from 'events';

export interface Point {
    x: number;
    y: number;
}

export const stateEmitter = new EventEmitter();
export const enum StateEvent {
    WaypointsUpdated = 'waypointsUpdated'
}

export const waypoints = [{
    x: 100,
    y: 200
}, {
    x: 200,
    y: 400
}, {
    x: 300,
    y: 400
}];
