import { MDCTextField } from '@material/textfield';
import { MDCList } from '@material/list';

import { Point } from '../../types';

const rowClass = 'list-row';

const list = document.querySelector('#waypoints-list') as HTMLUListElement;

const mdcList = new MDCList(list);
mdcList.singleSelection = true;

export function add(point: Point) {
    const item = document.createElement('li');
    item.setAttribute('role', 'option');
    item.classList.add('mdc-list-item');
    const row = document.createElement('div');
    row.classList.add(rowClass);
    item.appendChild(row);
    list.appendChild(item);
    addNumberField(row, 'x', point.x);
    addNumberField(row, 'y', point.y);
    addNumberField(row, 'angle', point.angle);
}

let nextInputId = 0;

function addNumberField(parent: Node, name: string, value?: number) {
    const inputId = "waypoints-list-" + nextInputId++;
    const textField = document.createElement('div');
    textField.classList.add('mdc-text-field', 'mdc-text-field--outlined');
    const input = document.createElement('input');
    input.type = 'number';
    input.step = 'any';
    input.classList.add('mdc-text-field__input');
    input.id = inputId;
    if (value !== undefined) {
        input.value = value.toString();
    }
    textField.appendChild(input);
    const outline = document.createElement('div');
    outline.classList.add('mdc-notched-outline');
    const outlineLeading = document.createElement('div');
    outlineLeading.classList.add('mdc-notched-outline__leading');
    outline.appendChild(outlineLeading);
    const outlineNotch = document.createElement('div');
    outlineNotch.classList.add('mdc-notched-outline__notch');
    const label = document.createElement('label');
    label.classList.add('mdc-floating-label');
    label.htmlFor = inputId;
    label.textContent = name;
    outlineNotch.appendChild(label);
    outline.appendChild(outlineNotch);
    const outlineTrailing = document.createElement('div');
    outlineTrailing.classList.add('mdc-notched-outline__trailing');
    outline.appendChild(outlineTrailing);
    textField.appendChild(outline);
    parent.appendChild(textField);
    new MDCTextField(textField);
}
