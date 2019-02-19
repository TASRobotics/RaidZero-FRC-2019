import { spawn } from 'child_process';
import { EOL } from 'os';
import * as path from 'path';
import * as readline from 'readline';

import state, { Point, ZeroPathPoint, stateEmitter, StateEvent } from './state';

interface Request {
    waypoints: Point[];
    cruiseVelocity: number;
    targetAcceleration: number;
}

interface Response {
    path: ZeroPathPoint[];
}

const scriptPath = path.join(__dirname, '..', 'run-java-process.cmd');

const proc = spawn(scriptPath, {
    shell: true
});

const rl = readline.createInterface({
    input: proc.stdout,
    terminal: false,
    crlfDelay: Infinity
});

export function send(req: Request, cb: (res: Response) => void) {
    proc.stdin.write(JSON.stringify(req) + EOL);
    rl.once('line', line => {
        cb(JSON.parse(line));
    });
}

stateEmitter.on(StateEvent.WaypointsUpdated, () => {
    send({
        waypoints: state.waypoints,
        cruiseVelocity: 10,
        targetAcceleration: 20
    }, ({ path }) => {
        state.path = path;
        stateEmitter.emit(StateEvent.PathUpdated);
        console.log('Emitted');
    });
});

setTimeout(() => {
    console.log('Emitting');
    stateEmitter.emit(StateEvent.WaypointsUpdated);
}, 1000);
