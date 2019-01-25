import { event as d3event, select as d3select, Selection, BaseType } from 'd3-selection';
import { drag as d3drag, D3DragEvent } from 'd3-drag';

import { Point, stateEmitter, StateEvent, waypoints } from './state';

type CircleDragEvent = D3DragEvent<Element, Point, Point>;

function update() {
    const circles = d3select('svg').selectAll('circle').data(waypoints) as
        Selection<SVGCircleElement, Point, BaseType, {}>;
    circles
        .enter()
            .append('circle')
            .attr('r', 25)
            .call(d3drag()
                .on('start', () => {
                    console.log('dragStart');
                }).on('end', () => {
                    console.log('dragEnd');
                }).on('drag', () => {
                    const dragEvent = d3event as CircleDragEvent;
                    dragEvent.subject.x = dragEvent.x;
                    dragEvent.subject.y = dragEvent.y;
                    console.log(dragEvent.subject);
                    stateEmitter.emit(StateEvent.WaypointsUpdated);
                }))
        .merge(circles)
            .attr('cx', d => d.x)
            .attr('cy', d => d.y);
}

stateEmitter.on(StateEvent.WaypointsUpdated, update);

update();
