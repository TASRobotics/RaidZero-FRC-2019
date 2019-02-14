import * as d3axis from 'd3-axis';
import * as d3drag from 'd3-drag';
import * as d3scale from 'd3-scale';
import * as d3selection from 'd3-selection';
import * as d3zoom from 'd3-zoom';

import * as fieldMeasurements from './field';
import { Point, stateEmitter, StateEvent, waypoints } from './state';

const circleRadius = 10;

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

const svg = d3selection.select('svg') as BasicSelection<SVGSVGElement>;
const svgNode = svg.node() as SVGSVGElement;
const { width: svgWidth, height: svgHeight } = svgNode.getBoundingClientRect();

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

function selectCircles(): BasicSelection<SVGCircleElement> {
    return field.selectAll('circle');
}

function scaleCircleRadius(transform: d3zoom.ZoomTransform) {
    return circleRadius / transform.k;
}

svg.call((d3zoom.zoom() as ZoomBehaviorOn<typeof svg>)
    .on('zoom', () => {
        const zoomEvent = d3selection.event as ZoomEventOn<typeof svg>;
        field.attr('transform', d3selection.event.transform);
        xAxisGroup.call(xAxis.scale(zoomEvent.transform.rescaleX(xScale)));
        yAxisGroup.call(yAxis.scale(zoomEvent.transform.rescaleY(yScale)));
        selectCircles().attr('r', scaleCircleRadius(zoomEvent.transform));
    }));

function updateCircles() {
    const transform = d3zoom.zoomTransform(svgNode);
    const circles = selectCircles().data(waypoints);
    type CircleDragBehavior = DragBehaviorOn<typeof circles, CircleDragSubject>;
    type CircleDragEvent = DragEventOn<typeof circles, CircleDragSubject>;
    circles
        .enter().append('circle')
            .attr('r', scaleCircleRadius(transform))
            .call((d3drag.drag() as CircleDragBehavior)
                .subject(point => ({
                    x: transform.rescaleX(xScale)(point.x),
                    y: transform.rescaleY(yScale)(point.y),
                    point
                }))
                .on('drag', () => {
                    const dragEvent = d3selection.event as CircleDragEvent;
                    dragEvent.subject.point.x =
                        transform.rescaleX(xScale).invert(dragEvent.x);
                    dragEvent.subject.point.y =
                        transform.rescaleY(yScale).invert(dragEvent.y);
                    stateEmitter.emit(StateEvent.WaypointsUpdated);
                }))
        .merge(circles)
            .attr('cx', d => xScale(d.x))
            .attr('cy', d => yScale(d.y));
}

stateEmitter.on(StateEvent.WaypointsUpdated, updateCircles);

updateCircles();
