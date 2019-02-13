import * as d3axis from 'd3-axis';
import * as d3drag from 'd3-drag';
import * as d3scale from 'd3-scale';
import * as d3selection from 'd3-selection';
import * as d3zoom from 'd3-zoom';

import * as fieldMeasurements from './field';
import { Point, stateEmitter, StateEvent, waypoints } from './state';

type FieldZoomEvent = d3zoom.D3ZoomEvent<Element, {}>;
type CircleDragEvent = d3drag.D3DragEvent<SVGCircleElement, Point, CircleDragSubject>;

interface CircleDragSubject extends d3drag.SubjectPosition {
    point: Point
}

const svg = d3selection.select('svg')
    .call(d3zoom.zoom()
        .on('zoom', () => {
            const zoomEvent = d3selection.event as FieldZoomEvent;
            field.attr('transform', d3selection.event.transform);
            xAxisGroup.call(xAxis.scale(zoomEvent.transform.rescaleX(xScale)));
            yAxisGroup.call(yAxis.scale(zoomEvent.transform.rescaleY(yScale)));
            const circles = field.selectAll('circle') as
                d3selection.Selection<SVGCircleElement, Point, d3selection.BaseType, {}>;
            circles
                .attr('r', () => 10 / zoomEvent.transform.k)
        }));
const svgNode = svg.node() as SVGSVGElement;
// const { width: svgWidth, height: svgHeight } =
//     (svg.node() as SVGSVGElement).getBBox();
const svgWidth = parseInt(svg.attr('width'));
const svgHeight = parseInt(svg.attr('height'));

const xScale = d3scale.scaleLinear()
    .domain([0, fieldMeasurements.length])
    .range([0, svgWidth]);
const xAxis = d3axis.axisBottom(xScale);
const xAxisGroup = svg.append('g').call(xAxis);

const yScale = d3scale.scaleLinear()
    .domain([0, fieldMeasurements.length])
    .range([0, svgHeight]);
const yAxis = d3axis.axisRight(yScale);
const yAxisGroup = svg.append('g').call(yAxis);

const field = svg.append('g');

function updateCircles() {
    const transform = d3zoom.zoomTransform(svgNode);
    const circles = field.selectAll('circle') as
        d3selection.Selection<SVGCircleElement, Point, d3selection.BaseType, {}>;
    circles
        .data(waypoints)
        .enter().append('circle')
            .attr('r', () => 10 / transform.k)
            .call((d3drag.drag() as d3drag.DragBehavior<SVGCircleElement, Point, CircleDragSubject>)
                .subject(point => ({
                    x: transform.rescaleX(xScale)(point.x),
                    y: transform.rescaleY(yScale)(point.y),
                    point
                }))
                .on('drag', () => {
                    const dragEvent = d3selection.event as CircleDragEvent;
                    dragEvent.subject.point.x = transform.rescaleX(xScale).invert(dragEvent.x);
                    dragEvent.subject.point.y = transform.rescaleY(yScale).invert(dragEvent.y);
                    stateEmitter.emit(StateEvent.WaypointsUpdated);
                }))
        .merge(circles)
            .attr('cx', d => xScale(d.x))
            .attr('cy', d => yScale(d.y));
}

stateEmitter.on(StateEvent.WaypointsUpdated, updateCircles);

updateCircles();
