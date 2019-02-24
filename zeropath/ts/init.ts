import './ui/components/app-bar';
import './ui/components/waypoints-list';

import './ui/upload';
import './ui/waypoints';

import './view';
import './java-process';

import * as state from './state';
state.emit('waypointsUpdated', 'added');
