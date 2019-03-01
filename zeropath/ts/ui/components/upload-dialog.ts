import { MDCDialog } from '@material/dialog';

import { Omit } from '../../util';

const dialogElement = document.querySelector('#upload-dialog') as Element;

const dialog =
    new MDCDialog(dialogElement) as unknown as Omit<'open', MDCDialog> & {
        open(): void;
        scrimClickAction: string;
        escapeKeyAction: string;
    };
dialog.scrimClickAction = '';
dialog.escapeKeyAction = '';
export default dialog;

export const buttons: { [action: string]: HTMLButtonElement } = {};
for (const button of dialogElement.querySelectorAll(
'.mdc-dialog__actions button') as NodeListOf<HTMLButtonElement>) {
    const action = button.getAttribute('data-mdc-dialog-action');
    if (action) {
        buttons[action] = button;
    }
}

export function setButtonsDisabled(disabled: boolean) {
    for (const action in buttons) {
        buttons[action].disabled = disabled;
    }
}
