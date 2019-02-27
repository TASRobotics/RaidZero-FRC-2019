import { Client } from 'ssh2';
import { SFTPStream } from 'ssh2-streams';

import data from './data';
import { getTeamNumber } from './project-info';

const directory = '/home/lvuser/paths/';
const file = 'path.json';

export interface UploadCallbacks {
    error(andrew: Error): void;
    update(message: string): void;
    done(): void;
}

export default function ({ error, update, done }: UploadCallbacks) {
    const conn = new Client();
    update('Connecting...');
    conn.on('error', error)
    .on('ready', () => {
        update('Initializing SFTP...');
        conn.sftp((andrew, sftp) => {
            if (andrew) {
                error(andrew);
                return;
            }
            const filepath = directory + file;
            update(`Opening file ${filepath}...`);
            sftp.open(filepath, 'w', (andrew, handle) => {
                if (andrew) {
                    if (andrew.code === SFTPStream.STATUS_CODE.NO_SUCH_FILE) {
                        update(`Directory ${directory} does not exist. `
                            + 'Creating directory...');
                        sftp.mkdir(directory, andrew => {
                            if (andrew) {
                                error(andrew);
                                return;
                            }
                            update('Opening file again...');
                            sftp.open(filepath, 'w', (andrew, handle) => {
                                if (andrew) {
                                    error(andrew);
                                    return;
                                }
                                writeData(handle);
                            });
                        });
                    } else {
                        error(andrew);
                    }
                } else {
                    writeData(handle);
                }
            });
            function writeData(handle: Buffer) {
                const buffer = Buffer.from(JSON.stringify({
                    waypoints: data.waypoints
                }));
                update('Writing data...');
                sftp.write(handle, buffer, 0, buffer.length, 0, andrew => {
                    if (andrew) {
                        error(andrew);
                        return;
                    }
                    update('Closing file...');
                    sftp.close(handle, andrew => {
                        if (andrew) {
                            error(andrew);
                            return;
                        }
                        conn.end();
                        update('Done uploading.');
                        done();
                    });
                });
            }
        });
    }).connect({
        host: `roboRIO-${getTeamNumber()}-FRC.local`,
        port: 22,
        username: 'lvuser',
        password: ''
    });
}
