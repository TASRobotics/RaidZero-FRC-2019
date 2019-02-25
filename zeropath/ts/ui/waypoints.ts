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
        case 'selected':
            waypointsList.setSelectedIndex(data.selectedWaypointIndex);
            break;
    }
});

waypointsList.list.addEventListener('click', () => {
    data.selectedWaypointIndex = waypointsList.getSelectedIndex();
    state.emit('waypointsUpdated', 'selected');
});
