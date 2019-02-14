import { MDCDialog } from '@material/dialog';
import { MDCTopAppBar } from '@material/top-app-bar';

import upload from './upload';
import { Omit } from './util';

new MDCTopAppBar(document.querySelector('.mdc-top-app-bar') as Element);

const uploadDialog = new MDCDialog(document.querySelector('#upload-dialog') as
    Element) as unknown as Omit<'open', MDCDialog> & {
        open(): void;
        scrimClickAction: string;
        escapeKeyAction: string;
    };
uploadDialog.scrimClickAction = '';
uploadDialog.escapeKeyAction = '';

const uploadButton = document.querySelector('#upload-button') as HTMLElement;
uploadButton.addEventListener('click', () => {
    uploadDialog.open();
    upload();
});
