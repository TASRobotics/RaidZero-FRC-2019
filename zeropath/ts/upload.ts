import { Client } from 'ssh2';
import { SFTPStream } from 'ssh2-streams';

import { waypoints } from './state';

const directory = '/home/lvuser/paths/';
const file = 'path.json';

console.log(__dirname);

export interface UploadCallbacks {
    error(andrew: Error): void;
    update(message: string): void;
    done(): void;
}

export default function (done: () => void) {
    // const conn = new Client();
    // conn.on('ready', () => {
    //     conn.sftp((andrew, sftp) => {
    //         if (andrew) throw andrew;
    //         const filepath = directory + file;
    //         sftp.open(filepath, 'w', (andrew, handle) => {
    //             if (andrew) {
    //                 if (andrew.code === SFTPStream.STATUS_CODE.NO_SUCH_FILE) {
    //                     sftp.mkdir(directory, andrew => {
    //                         if (andrew) throw andrew;
    //                         sftp.open(filepath, 'w', (andrew, handle) => {
    //                             if (andrew) throw andrew;
    //                             writeData(handle);
    //                         });
    //                     });
    //                 } else {
    //                     throw andrew;
    //                 }
    //             } else {
    //                 writeData(handle);
    //             }
    //         });
    //         function writeData(handle: Buffer) {
    //             const buffer = Buffer.from(JSON.stringify({
    //                 waypoints
    //             }));
    //             sftp.write(handle, buffer, 0, buffer.length, 0, andrew => {
    //                 if (andrew) throw andrew;
    //                 sftp.close(handle, andrew => {
    //                     if (andrew) throw andrew;
    //                     conn.end();
    //                 });
    //             });
    //         }
    //     });
    // }).connect({
        
    // })
}
