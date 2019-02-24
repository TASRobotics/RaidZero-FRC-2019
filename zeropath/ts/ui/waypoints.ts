import * as waypointsList from './components/waypoints-list';

import data from '../data';
import * as state from '../state';

state.on('waypointsUpdated', info => {
    switch (info) {
        case 'added':
            for (const waypoint of data.waypoints) {
                waypointsList.add(waypoint);
            }
            break;
    }
});
