/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package navmeshpathfinding;

import collision.Figure;
import collision.Rectangle;
import engine.*;
import engine.Point;
import game.gameobject.Entity;

import java.awt.*;
import java.util.BitSet;
import java.util.List;

/**
 * @author przemek
 */
public class PathData {

    protected final static int PATH_RETURNED = 7;
    final static int PASSING = 0;
    final static int PASSED = 1;
    final static int CHOICE = 2;
    final static int BLOCKED = 3;
    final static int STUCK = 4;
    final static int OBSTACLE_BETWEEN = 5;
    final static int PATH_REQUESTED = 6;
    protected final Point preyPoint = new Point();
    final int width;
    final int height;
    final int widthHalf;
    final int heightHalf;
    final int xCorrection;
    final int yCorrection;
    final Point finalDestination = new Point();
    final Point pastPosition = new Point();
    final Point correction = new Point();
    final Point last1CorrectionPoint = new Point();
    final Point last2CorrectionPoint = new Point();
    final Point tempCorrection = new Point();
    final Rectangle testing;

    final BitSet flags = new BitSet();
    final Point[] castingPoints = {new Point(), new Point()};
    final Point[] castingDestination = {new Point(), new Point()};
    final PointContainer path = new PointContainer(16);
    final PointedValueContainer correctionPoints = new PointedValueContainer(4);
    final List<Figure> close = new BlueArray<>();
    final Polygon poly = new Polygon();
    final Delay delay = new Delay(250);
    int x;
    int y;
    int xS;
    int xE;
    int yS;
    int yE;
    int xRef;
    int yRef;
    int passedCount;
    int alternateCount;
    int min;
    int temp;
    int xDS;
    int xDE;
    int yDS;
    int yDE;
    int xPass;
    int yPass;
    int xInAWay;
    int yInAWay;
    int currentPoint;
    int xDistance;
    int yDistance;
    int scope;
    int lastCorner = -1;
    double xSpeed;
    double ySpeed;
    double pastXSpeed;
    double pastYSpeed;
    Point destination;
    Point desired;
    PointedValue closest;
    PointContainer newPath;
    Figure inAWay;
    Figure lastInAWay;
    Figure collided;
    private int stuckCount;

    public PathData(Entity owner, int scope) {
        this.scope = scope;
        width = owner.getCollision().getWidth();
        widthHalf = width / 2;
        height = owner.getCollision().getHeight();
        heightHalf = height / 2;
        xCorrection = widthHalf + 1;
        yCorrection = heightHalf + 1;
        testing = Rectangle.createTileRectangle(width, height);
        delay.start();
    }

    public void update(Entity owner, int xDest, int yDest) {
        x = owner.getX();
        y = owner.getY();
        xS = owner.getCollision().getX();
        xE = owner.getCollision().getXEnd();
        yS = owner.getCollision().getY();
        yE = owner.getCollision().getYEnd();
        finalDestination.set(xDest, yDest);
        if (owner.getArea() != -1) {
            xRef = owner.getMap().areas[owner.getArea()].getXInPixels();
            yRef = owner.getMap().areas[owner.getArea()].getYInPixels();
        }
        Figure.updateWhatClose(owner, x, y, ((int) (owner.getRange()) >> 2), x, y, owner.getMap(), close);
        close.sort((Figure f1, Figure f2) -> f1.getLightDistance() - f2.getLightDistance());
        flags.set(OBSTACLE_BETWEEN, isObstacleBetween());
        updateStuck();
    }

    private void updateStuck() {
        if (x == pastPosition.getX() && y == pastPosition.getY()) {
            stuckCount++;
            if (stuckCount >= 2) {
                flags.set(STUCK);
                return;
            }
        } else {
            stuckCount = 0;
        }
        flags.clear(STUCK);
    }

    private boolean isObstacleBetween() {
        if (close.isEmpty()) {
            return false;
        }
        PathStrategyCore.setPolygonForTesting(this, finalDestination);
        return PathStrategyCore.anyFigureInAWay(poly, close) != null;
    }

    public void calculateSpeed(double maxSpeed) {
        if (!flags.get(PASSED)) {
            double angle = Methods.pointAngleClockwise(x, y, destination.getX(), destination.getY());
            xDistance = Math.abs(x - destination.getX());
            yDistance = Math.abs(y - destination.getY());
            xSpeed = Methods.xRadius(angle, Math.min(maxSpeed, xDistance));
            ySpeed = Methods.yRadius(angle, Math.min(maxSpeed, yDistance));
        }
    }

    public void rememberPast() {
        pastXSpeed = xSpeed;
        pastYSpeed = ySpeed;
        pastPosition.set(x, y);
    }

    public Point getCurrentPoint() {
        return path.get(currentPoint);
    }

    public Point getLastPoint() {
        return path.get(path.size() - 1);
    }

    public void clearPath() {
        path.clear();
    }

    public double getXSpeed() {
        return xSpeed;
    }

    public double getYSpeed() {
        return ySpeed;
    }

    public int getScope() {
        return scope;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

}
