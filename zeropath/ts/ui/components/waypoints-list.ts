import { MDCTextField } from '@material/textfield';
import { MDCList } from '@material/list';

import { Point } from '../../types';

const rowClass = 'list-row';

export const list =
    document.querySelector('#waypoints-list') as HTMLUListElement;

const mdcList = new MDCList(list);
mdcList.singleSelection = true;

export function getSelectedIndex() {
    return mdcList.selectedIndex as number;
}

export function setSelectedIndex(index: number) {
    mdcList.selectedIndex = index;
}

export interface FieldData {
    name: string;
    value?: number;
    onChange: (value: number | undefined) => void;
}

export function add(...fields: FieldData[]) {
    const item = document.createElement('li');
    item.setAttribute('role', 'option');
    item.classList.add('mdc-list-item');
    const row = document.createElement('div');
    row.classList.add(rowClass);
    item.appendChild(row);
    list.appendChild(item);
    for (const field of fields) {
        addNumberField(row, field);
    }
}

let nextInputId = 0;

function addNumberField(parent: Node, fieldData: FieldData) {
    const inputId = "waypoints-list-" + nextInputId++;
    const textField = document.createElement('div');
    textField.classList.add('mdc-text-field', 'mdc-text-field--outlined');
    const input = document.createElement('input');
    input.type = 'number';
    input.step = 'any';
    input.classList.add('mdc-text-field__input');
    input.id = inputId;
    input.dataset.name = fieldData.name;
    if (fieldData.value !== undefined) {
        input.value = fieldData.value.toString();
    }
    input.addEventListener('input', () => {
        const value = parseFloat(input.value);
        fieldData.onChange(Number.isNaN(value) ? undefined : value);
    });
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
    label.textContent = fieldData.name;
    outlineNotch.appendChild(label);
    outline.appendChild(outlineNotch);
    const outlineTrailing = document.createElement('div');
    outlineTrailing.classList.add('mdc-notched-outline__trailing');
    outline.appendChild(outlineTrailing);
    textField.appendChild(outline);
    parent.appendChild(textField);
    new MDCTextField(textField);
}

export function modifyIndex(index: number, point: Point) {
    const item = list.children[index];
    setInput(item, 'x', point.x);
    setInput(item, 'y', point.y);
}

function setInput(item: Element, name: string, value: number) {
    const input =
        item.querySelector(`input[data-name='${name}']`) as HTMLInputElement;
    if (parseFloat(input.value) !== value) {
        input.value = value.toString();
    }
}
