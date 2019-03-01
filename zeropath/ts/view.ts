import * as d3axis from 'd3-axis';
import * as d3drag from 'd3-drag';
import * as d3scale from 'd3-scale';
import * as d3selection from 'd3-selection';
import * as d3shape from 'd3-shape';
import * as d3zoom from 'd3-zoom';

import data from './data';
import * as fieldMeasurements from './field';
import * as state from './state';
import * as theme from './theme';
import { Point, PathPoint } from './types';

const fieldImagePath = 'res/2019-field.jpg';

const circleRadius = 10;
const pathWidth = 2;

type GetElement<S> = S extends d3selection.Selection<infer E, any, any, any> ?
    E : never;
type GetDatum<S> = S extends d3selection.Selection<any, infer D, any, any> ?
    D : never;

type BasicSelection<E extends d3selection.BaseType> =
    d3selection.Selection<E, unknown, d3selection.BaseType, unknown>;

type ZoomBehavior<E, D> = E extends d3zoom.ZoomedElementBaseType ?
    d3zoom.ZoomBehavior<E, D> : never;
type ZoomBehaviorOn<S> = ZoomBehavior<GetElement<S>, GetDatum<S>>;
type ZoomEvent<E, D> = E extends d3zoom.ZoomedElementBaseType ?
    d3zoom.D3ZoomEvent<E, D> : never;
type ZoomEventOn<S> = ZoomEvent<GetElement<S>, GetDatum<S>>;

type DragBehavior<E, D, Subject> = E extends d3drag.DraggedElementBaseType ?
    d3drag.DragBehavior<E, D, Subject> : never;
type DragBehaviorOn<Selection, Subject> =
    DragBehavior<GetElement<Selection>, GetDatum<Selection>, Subject>;
type DragEvent<E, D, Subject> = E extends d3drag.DraggedElementBaseType ?
    d3drag.D3DragEvent<E, D, Subject> : never;
type DragEventOn<Selection, Subject> =
    DragEvent<GetElement<Selection>, GetDatum<Selection>, Subject>;

interface CircleDragSubject extends d3drag.SubjectPosition {
    point: Point
}

const svg = d3selection.select('#view') as BasicSelection<SVGSVGElement>;
const svgNode = svg.node() as SVGSVGElement;
const { width: svgWidth, height: svgHeight } = svgNode.getBoundingClientRect();

function getZoomTransform() {
    return d3zoom.zoomTransform(svgNode);
}

const xScale = d3scale.scaleLinear()
    .domain([0, fieldMeasurements.width * (svgWidth / svgHeight)])
    .range([0, svgWidth]);
const xAxis = d3axis.axisBottom(xScale);

const yScale = d3scale.scaleLinear()
    .domain([0, fieldMeasurements.width])
    .range([0, svgHeight]);
const yAxis = d3axis.axisRight(yScale);

const field = svg.append('g');

field.append('svg:image')
    .attr('x', xScale(0))
    .attr('y', yScale(0))
    .attr('height', yScale(fieldMeasurements.width))
    .attr('xlink:href', fieldImagePath);

const xAxisGroup = svg.append('g').call(xAxis).style('color', 'white');
const yAxisGroup = svg.append('g').call(yAxis).style('color', 'white');

const path = field.append('path')
    .style('fill', 'none')
    .style('stroke', 'white');

function scalePathWidth(transform: d3zoom.ZoomTransform) {
    path.style('stroke-width', pathWidth / transform.k + "px")
}

scalePathWidth(getZoomTransform());

function updatePath() {
    path.datum(data.path).attr('d', d3shape.line<PathPoint>()
        .x(point => xScale(point.x))
        .y(point => yScale(point.y)));
}

state.on('pathUpdated', updatePath);

updatePath();

function selectCircles(): BasicSelection<SVGCircleElement> {
    return field.selectAll('circle');
}

const zoomBehavior = (d3zoom.zoom() as ZoomBehaviorOn<typeof svg>)
    .translateExtent([[0, 0],
        [xScale(fieldMeasurements.length), yScale(fieldMeasurements.width)]])
    .on('zoom', () => {
        const zoomEvent = d3selection.event as ZoomEventOn<typeof svg>;
        const transform = zoomEvent.transform;
        field.attr('transform', transform.toString());
        xAxisGroup.call(xAxis.scale(transform.rescaleX(xScale)));
        yAxisGroup.call(yAxis.scale(transform.rescaleY(yScale)));
        selectCircles().attr('r', circleRadius / transform.k);
        scalePathWidth(transform);
    });

svg.call(zoomBehavior);
zoomBehavior.scaleBy(svg, 1); // Enforce initial translate extent

function updateCircles() {
    const transform = getZoomTransform();
    const circles = selectCircles().data(data.waypoints);
    type CircleDragBehavior = DragBehaviorOn<typeof circles, CircleDragSubject>;
    type CircleDragEvent = DragEventOn<typeof circles, CircleDragSubject>;
    circles
        .enter().append('circle')
            .attr('r', circleRadius / transform.k)
            .call((d3drag.drag() as CircleDragBehavior)
                .subject(point => ({
                    x: transform.rescaleX(xScale)(point.x),
                    y: transform.rescaleY(yScale)(point.y),
                    point
                }))
                .on('start', (_point, i) => {
                    data.selectedWaypointIndex = i;
                    state.emit('waypointsUpdated', {
                        type: 'selected'
                    });
                })
                .on('drag', (_point, i) => {
                    const dragEvent = d3selection.event as CircleDragEvent;
                    dragEvent.subject.point.x =
                        transform.rescaleX(xScale).invert(dragEvent.x);
                    dragEvent.subject.point.y =
                        transform.rescaleY(yScale).invert(dragEvent.y);
                    state.emit('waypointsUpdated', {
                        type: 'modified',
                        index: i
                    });
                }))
        .merge(circles)
            .attr('cx', d => xScale(d.x))
            .attr('cy', d => yScale(d.y))
            .style('fill', (_point, i) => i === data.selectedWaypointIndex ?
                theme.primaryColorLight : 'white');
}

state.on('waypointsUpdated', updateCircles);
