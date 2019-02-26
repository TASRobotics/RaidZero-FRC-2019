# zeropath

A tool for editing, previewing, and uploading paths.

## Setup

### Development build

#### Prerequisites

Install [node.js](https://nodejs.org). If you already have it installed, make sure it is a fairly recent version.

You should also have the FRC JDK which should have been installed as part of WPILib.

#### Terminal

Assuming you have this repository open in VS Code, you can press `ctrl` + `` ` `` to open the integrated terminal.

The current working directory after you open a new terminal should be `RaidZero-FRC-2019`. Run `cd zeropath` to change to the `zeropath` directory. **All subsequent commands should be run with `zeropath` as the current working directory.**

#### Initial setup

You only have to do this once.

Run `npm install -g typescript` to install the TypeScript compiler. Requires internet connection.

#### Install npm dependencies

Run `npm install` to install npm dependencies. This needs to be run every time any dependencies are updated, i.e. when the `dependencies` section in `package.json` is changed. Requires internet connection.

Warning: this may take a while the first time you run it.

#### Compile TypeScript code

Run `tsc -p .` to compile the TypeScript source files. This needs to be run every time any TypeScript files are changed (i.e. files in the `ts` directory).

If you are working on the code and don't want to re-compile every time, you can run the TypeScript compiler in watch mode by pressing `ctrl` + `shift` + `B` in VS Code. Now any modified files will be automatically recompiled.

#### Compile and install Java code

Run `"../gradlew" installDist -Dorg.gradle.java.home=C:\Users\Public\frc2019\jdk\` to build and install the Java process. This needs to be run every time any Java code within this sub-project or any dependency of this sub-project (i.e. `pathgen`) is modified.

Note: This is assuming the FRC 2019 JDK is installed in `C:\Users\Public\frc2019\jdk\` on your computer.

#### Run

To run the development build, run `npm start`.

Since this is the development build, there will be a few tools for debugging. The developer tools window will open automatically, and the menu bar will be visible. You can press `F5` to reload the page in the renderer process.

### Production build

To build the production version of the app, you should first complete all the steps above.

#### Initial setup

You only have to do this once.

Run `npm install -g electron-packager`. Requires internet connection.

#### Package app

Run `electron-packager .`. Requires internet connection the first time you run it.

Warning: this may take a while.

#### Run

The executable should be located in `zeropath/zeropath-win32-x64` and named `zeropath.exe`.

To make it more convenient to run, you can right-click on the executable in File Explorer and choose pin to start or pin to taskbar.

You have to re-package the app any time it has been updated.
