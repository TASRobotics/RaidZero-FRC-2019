import uploadButton from './components/upload-button';
import uploadDialog, { buttons, setButtonsDisabled }
    from './components/upload-dialog';
import * as uploadLog from './components/upload-log';

import upload from '../upload';

uploadButton.addEventListener('click', runUpload);
buttons.reupload.addEventListener('click', () => {
    // hack
    setTimeout(() => {
        runUpload();
    }, 200);
});

function runUpload() {
    uploadLog.clear();
    setButtonsDisabled(true);
    uploadDialog.open();
    upload({
        error: andrew => {
            uploadLog.addMessage(andrew.message, 'error');
            setButtonsDisabled(false);
        },
        update: message => {
            uploadLog.addMessage(message);
        },
        done: () => {
            setButtonsDisabled(false);
        }
    });
}
