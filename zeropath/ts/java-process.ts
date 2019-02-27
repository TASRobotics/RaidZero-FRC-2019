import { spawn } from 'child_process';
import { EOL } from 'os';
import * as path from 'path';
import * as readline from 'readline';

import data from './data';
import * as state from './state';
import { Point, PathPoint } from './types';

interface Request {
    waypoints: Point[];
    cruiseVelocity: number;
    targetAcceleration: number;
}

interface Response {
    path: PathPoint[];
}

const scriptPath = path.join(__dirname, '..',
    'build', 'install', 'zeropath', 'bin', 'zeropath.bat');

const proc = spawn('"' + scriptPath + '"', {
    shell: true,
    env: {
        JAVA_HOME: 'C:\\Users\\Public\\frc2019\\jdk\\'
    }
});

proc.stderr.on('data', chunk => {
    console.error(chunk.toString());
});

const rl = readline.createInterface({
    input: proc.stdout,
    terminal: false,
    crlfDelay: Infinity
});

function send(req: Request, cb: (res: Response) => void) {
    proc.stdin.write(JSON.stringify(req) + EOL);
    rl.once('line', line => {
        cb(JSON.parse(line));
    });
}

state.on('waypointsUpdated', info => {
    switch (info.type) {
        case 'added':
        case 'modified':
            send({
                waypoints: data.waypoints,
                cruiseVelocity: 10,
                targetAcceleration: 20
            }, ({ path }) => {
                data.path = path;
                state.emit('pathUpdated', null);
            });
            break;
    }
});
