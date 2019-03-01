import * as waypointsList from './components/waypoints-list';

import data from '../data';
import * as state from '../state';

state.on('waypointsUpdated', info => {
    switch (info.type) {
        case 'added':
            for (const [index, waypoint] of data.waypoints.entries()) {
                waypointsList.add({
                    name: 'x',
                    value: waypoint.x,
                    onChange: emitAnd(x => {
                        if (x !== undefined) {
                            waypoint.x = x;
                        }
                    })
                }, {
                    name: 'y',
                    value: waypoint.y,
                    onChange: emitAnd(y => {
                        if (y !== undefined) {
                            waypoint.y = y;
                        }
                    })
                }, {
                    name: 'angle',
                    value: waypoint.angle,
                    onChange: emitAnd(angle => {
                        waypoint.angle = angle;
                    })
                });
                function emitAnd(f: (value: number | undefined) => void) {
                    return (value: number | undefined) => {
                        f(value);
                        state.emit('waypointsUpdated', {
                            type: 'modified',
                            index
                        });
                    };
                }
            }
            break;
        case 'modified':
            waypointsList.modifyIndex(info.index, data.waypoints[info.index]);
            break;
        case 'selected':
            waypointsList.setSelectedIndex(data.selectedWaypointIndex);
            break;
    }
});

waypointsList.list.addEventListener('click', () => {
    data.selectedWaypointIndex = waypointsList.getSelectedIndex();
    state.emit('waypointsUpdated', {
        type: 'selected'
    });
});
