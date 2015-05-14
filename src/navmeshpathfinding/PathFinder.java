/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package navmeshpathfinding;

import collision.Figure;
import engine.BlueArray;
import engine.Point;
import game.place.Place;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import static navmeshpathfinding.NavigationMeshGenerator.getIndexForShifts;

/**
 *
 * @author WROBELP1
 */
public class PathFinder {

    public static final int TO_LEFT_TOP = 1, TO_LEFT_BOTTOM = 4, TO_RIGHT_BOTTOM = 8, TO_RIGHT_TOP = 2, TO_TOP = 3, TO_LEFT = 5, TO_BOTTOM = 12, TO_RIGHT = 10;
    private static final Set<Node> closedList = new HashSet<>();
    private static final PriorityQueue<Node> openList = new PriorityQueue<>(24, (Node n1, Node n2) -> n1.getFCost() - n2.getFCost());
    private static Point startPoint = new Point(), destinationPoint = new Point();
    private static int width, height;
    private static Triangle startTriangle, endTriangle;
    private static Node destination, beginning;
    private static List<Point> shifted = new BlueArray<>(), result = new BlueArray<>();
    private static PathBase pathBase;

    public static Point[] findPath(NavigationMesh mesh, int xStart, int yStart, int xDestination, int yDestination, Figure collision) {
        if (mesh != null) {
            destinationPoint.set(xStart, yStart);
            startPoint.set(xDestination, yDestination);
            width = collision.getWidth() + 2;
            height = collision.getHeight() + 2;
            pathBase = mesh.getPathBase(startPoint, destinationPoint, collision.getWidth() + 2, collision.getHeight() + 2);
            startTriangle = pathBase.startTriangle;
            endTriangle = pathBase.endTriangle;
            return findSolution(mesh);
        }
        return null;
    }

    private static Point[] findSolution(NavigationMesh mesh) {
        destination = null;
        if (startTriangle != null && endTriangle != null) {
            if (startTriangle == endTriangle) {
                destination = inOneTriangle();
            } else {
                destination = aStar(mesh);
            }
        }
        return produceResult(destination, mesh);
    }

    private static Node inOneTriangle() {
        Node beginning = new Node(startPoint);
        destination = new Node(destinationPoint);
        destination.setParentMakeChild(beginning);
        return destination;
    }

    private static Node aStar(NavigationMesh mesh) {
        Node currentNode;
        readyVariables(mesh);
        createBeginningAndAdjacent();
        while (!openList.isEmpty()) {
            currentNode = openList.poll();
            if (isFittingPoint(currentNode)) {
                closedList.add(currentNode);
                if (isInEndTriangle(currentNode)) {
                    //optimize(mesh);
                    break;
                }
                keepLooking(currentNode);
            }

        }
        return destination;
    }

    private static void readyVariables(NavigationMesh mesh) {
        mesh.reset();
        closedList.clear();
        openList.clear();
        destination = null;
    }

    private static void createBeginningAndAdjacent() {
        beginning = new Node(startPoint);
        beginning.setGHCosts(0, countH(startPoint, destinationPoint));
        closedList.add(beginning);
        for (int i = 0; i < 3; i++) {
            Node node = startTriangle.getNode(i);
            calculateAndAddToOpenList(node, beginning);
        }
    }

    private static void calculateAndAddToOpenList(Node node, Node parent) {
        node.setParentMakeChild(parent);
        node.setGHCosts(countG(node.getPoint(), parent.getPoint()), countH(node.getPoint(), destinationPoint));
        openList.add(node);
    }

    private static boolean isFittingPoint(Node currentNode) {

        return true;
    }

    private static boolean isInEndTriangle(Node currentNode) {
        boolean isFound = false;
        for (int i = 0; i < 3; i++) {
            Node node = endTriangle.getNode(i);
            if (currentNode.getPoint().equals(node.getPoint())) {
                destination = new Node(destinationPoint);
                calculateAndAddToOpenList(destination, currentNode);
                isFound = true;
            }
        }
        return isFound;
    }

    private static void keepLooking(Node currentNode) {
        currentNode.getNeightbours().stream().filter((node) -> (!closedList.contains(node))).forEach((node) -> {
            if (openList.contains(node)) {
                changeIfBetterPath(node, currentNode);
            } else {
                calculateAndAddToOpenList(node, currentNode);
            }
        });
    }

    private static void changeIfBetterPath(Node node, Node currentNode) {
        int temp = countG(node.getPoint(), currentNode.getPoint());
        if (temp + currentNode.getGCost() < node.getGCost()) {
            node.setParentMakeChild(currentNode);
            node.setGCost(temp);
        }
    }

    private static void optimize(NavigationMesh mesh) {
        Node previous, current = destination;
        while (current.getParent() != null && current.getParent().getParent() != null) {
            previous = current.getParent().getParent();
            while (previous != null) {
                if (canBeSkipped(current, previous, mesh)) {
                    current.setParentMakeChild(previous);
                }
                previous = previous.getParent();
            }
            current = current.getParent();
        }
    }

    private static boolean canBeSkipped(Node startNode, Node endNode, NavigationMesh mesh) {
        return !mesh.lineIntersectsMeshBounds(startNode.getPoint(), endNode.getPoint());
    }

    private static int countG(Point point, Point parentPoint) {
//        forbitten.setPoints(point, parentPoint);
//        if (pathBase.forbittenConnections.contains(forbitten)) {
//            return Integer.MAX_VALUE;
//        }
        int x = parentPoint.getX() - point.getX();
        int y = parentPoint.getY() - point.getY();
        return (int) ((x * x + y * y));
    }

    private static int countH(Point point, Point endPoint) {
        int x = endPoint.getX() - point.getX();
        int y = endPoint.getY() - point.getY();
        return (int) ((x * x + y * y));
    }

    private static Point[] produceResult(Node destiation, NavigationMesh mesh) {
        if (destiation != null) {
            return printSolution(destiation, mesh);
        } else {
            System.out.println("Nie znaleziono rozwiązania!");
        }
        return null;
    }

    private static Point[] printSolution(Node destination, NavigationMesh mesh) {
        shifted.clear();
        result.clear();
        Point point;
        Node currentNode = destination;
        while (currentNode != null) {
            point = currentNode.getPoint();
            currentNode = currentNode.getParent();
            result.add(point);
            if (currentNode != null) {
                shifted.add(getNewShiftedPoint(point, mesh, width, height));
            } else {
                shifted.add(point);
            }
        }
        optimizeShifted(mesh);
        return shifted.toArray(new Point[shifted.size()]);
    }

    private static Point getNewShiftedPoint(Point point, NavigationMesh mesh, int width, int height) {     // można zoptymalizować, żeby ustawiał PointContener dla tego, co pyta o ścieżkę.
        //width = height = Math.max(width, height);
        switch (mesh.getShiftDirections()[getIndexForShifts(point.getX() / Place.tileSize, point.getY() / Place.tileSize)]) {
            case TO_LEFT_TOP:
                return new Point(point.getX() - width, point.getY() - height);
            case TO_LEFT_BOTTOM:
                return new Point(point.getX() - width, point.getY() + height);
            case TO_RIGHT_BOTTOM:
                return new Point(point.getX() + width, point.getY() + height);
            case TO_RIGHT_TOP:
                return new Point(point.getX() + width, point.getY() - height);
            case TO_TOP:
                return new Point(point.getX(), point.getY() - height);
            case TO_LEFT:
                return new Point(point.getX() - width, point.getY());
            case TO_BOTTOM:
                return new Point(point.getX(), point.getY() + height);
            case TO_RIGHT:
                return new Point(point.getX() + width, point.getY());
        }
        return new Point(point.getX(), point.getY());
    }

    private static void optimizeShifted(NavigationMesh mesh) {
        Point next, previous;
        for (int i = 1; i < shifted.size() - 1; i++) {
            previous = shifted.get(i - 1);
            for (int j = shifted.size() - 1; j > i; j--) {
                next = shifted.get(j);
                if (canBeSkipped(previous, next, mesh) && canBeSkipped(result.get(i - 1), result.get(j), mesh)) {
                    shifted.remove(i);
                    result.remove(i);
                    i--;
                    break;
                }
            }
        }
    }

    private static boolean canBeSkipped(Point previous, Point next, NavigationMesh mesh) {
        return !mesh.lineIntersectsMeshBounds(previous, next);
    }
}
