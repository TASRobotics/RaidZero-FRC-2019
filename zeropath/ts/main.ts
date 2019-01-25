import { app, BrowserWindow } from 'electron';

require('electron-debug')({
    devToolsMode: 'right'
});

let win: BrowserWindow | null = null;

app.on('ready', () => {

    win = new BrowserWindow({
        width: 1000,
        height: 600,
        webPreferences: {
            nodeIntegration: true
        }
    });

    win.loadFile('index.html');

    win.on('closed', () => {
        win = null;
    });

});

app.on('window-all-closed', () => {
    app.quit();
});
