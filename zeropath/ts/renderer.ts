import { axisBottom as d3AxisBottom, axisRight as d3AxisRight } from 'd3-axis';
import { D3DragEvent, drag as d3drag } from 'd3-drag';
import { scaleLinear as d3scaleLinear } from 'd3-scale';
import { BaseType, event as d3event, select as d3select, Selection }
    from 'd3-selection';
import { D3ZoomEvent, zoom as d3zoom } from 'd3-zoom';

import * as fieldMeasurements from './field';
import { Point, stateEmitter, StateEvent, waypoints } from './state';

type FieldZoomEvent = D3ZoomEvent<Element, {}>;
type CircleDragEvent = D3DragEvent<Element, Point, Point>;

const svg = d3select('svg')
    .call(d3zoom()
        .on('zoom', () => {
            const zoomEvent = d3event as FieldZoomEvent;
            field.attr('transform', d3event.transform);
            xAxisGroup.call(xAxis.scale(zoomEvent.transform.rescaleX(xScale)));
            yAxisGroup.call(yAxis.scale(zoomEvent.transform.rescaleY(yScale)));
        }));
// const { width: svgWidth, height: svgHeight } =
//     (svg.node() as SVGSVGElement).getBBox();
const svgWidth = parseInt(svg.attr('width'));
const svgHeight = parseInt(svg.attr('height'));

const xScale = d3scaleLinear()
    // .domain([0, fieldMeasurements.length])
    .domain([-1, svgWidth + 1])
    .range([-1, svgWidth + 1]);
const xAxis = d3AxisBottom(xScale);
const xAxisGroup = svg.append('g').call(xAxis);

const yScale = d3scaleLinear()
    // .domain([0, fieldMeasurements.width])
    .domain([-1, svgHeight + 1])
    .range([-1, svgHeight + 1]);
const yAxis = d3AxisRight(yScale);
const yAxisGroup = svg.append('g').call(yAxis);

const field = svg.append('g');

function updateCircles() {
    const circles = field.selectAll('circle') as
        Selection<SVGCircleElement, Point, BaseType, {}>;
    circles
        .data(waypoints)
        .enter().append('circle')
            .attr('r', 20)
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

stateEmitter.on(StateEvent.WaypointsUpdated, updateCircles);

updateCircles();
