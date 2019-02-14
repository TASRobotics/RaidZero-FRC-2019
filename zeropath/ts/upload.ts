import { Client } from 'ssh2';

import { waypoints } from './state';

export default function () {
    const conn = new Client();
    conn.on('ready', () => {

    });
}
