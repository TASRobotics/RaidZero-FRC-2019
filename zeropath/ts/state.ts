import { EventEmitter } from 'events';

interface StateEvent {
    waypointsUpdated: {
        type: 'added'
    } | {
        type: 'modified',
        index: number
    } | {
        type: 'selected'
    };
    pathUpdated: null;
}

const emitter = new EventEmitter();

export function emit<T extends keyof StateEvent>
(eventType: T, eventInfo: StateEvent[T]) {
    emitter.emit(eventType, eventInfo);
}

export function on<T extends keyof StateEvent>
(eventType: T, listener: (eventInfo: StateEvent[T]) => void) {
    emitter.on(eventType, listener);
}

export function once<T extends keyof StateEvent>
(eventType: T, listener: (eventInfo: StateEvent[T]) => void) {
    emitter.once(eventType, listener);
}
