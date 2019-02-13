import { EventEmitter } from 'events';

import * as field from './field';

export interface Point {
    x: number;
    y: number;
}

export const stateEmitter = new EventEmitter();
export const enum StateEvent {
    WaypointsUpdated = 'waypointsUpdated'
}

export const waypoints: Point[] = [{
    x: 0,
    y: 0
}, {
    x: field.length,
    y: 0
}, {
    x: 0,
    y: field.width
}, {
    x: field.length,
    y: field.width
}];
