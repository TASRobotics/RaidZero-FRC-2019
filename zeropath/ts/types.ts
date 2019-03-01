export interface Point {
    x: number;
    y: number;
    angle?: number;
}

export interface PathPoint {
    x: number;
    y: number;
    position: number;
    velocity: number;
    time: number;
    angle: number;
}
