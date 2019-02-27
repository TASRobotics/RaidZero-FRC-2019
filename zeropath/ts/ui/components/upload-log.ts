const uploadLog = document.querySelector('#upload-log') as Element;

export function addMessage(message: string, class_?: string) {
    const elem = document.createElement('p');
    elem.textContent = message;
    if (class_) {
        elem.classList.add(class_);
    }
    uploadLog.appendChild(elem);
}

export function clear() {
    while (uploadLog.firstChild) {
        uploadLog.removeChild(uploadLog.firstChild);
    }
}
