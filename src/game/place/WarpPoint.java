/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.place;

import collision.Figure;
import engine.Main;
import game.gameobject.GameObject;
import game.gameobject.Player;

/**
 *
 * @author Wojtek
 */
public class WarpPoint extends GameObject {

    private final boolean isWarp;   //Czy teleportuje
    private final boolean isStatic;   //Czy teleportuje na wskazany punkt, czy na obiekt
    private int toX;
    private int toY;
    private Map destination;
    private String stringDestination = null;

    public WarpPoint(String name, int x, int y, int toX, int toY, Map map) {
        this.name = name;
        this.x = x;
        this.y = y;
        isWarp = true;
        isStatic = true;
        this.toX = toX;
        this.toY = toY;
        destination = map;
    }

    public WarpPoint(String name, int x, int y, int toX, int toY, String map) {
        this.name = name;
        this.x = x;
        this.y = y;
        isWarp = true;
        isStatic = true;
        this.toX = toX;
        this.toY = toY;
        stringDestination = map;
    }

    public WarpPoint(String name, int x, int y, Map map) {
        this.name = name;
        this.x = x;
        this.y = y;
        isWarp = true;
        isStatic = false;
        destination = map;
    }

    public WarpPoint(String name, int x, int y, String map) {
        this.name = name;
        this.x = x;
        this.y = y;
        isWarp = true;
        isStatic = false;
        stringDestination = map;
    }

    public WarpPoint(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
        isWarp = false;
        isStatic = false;
    }

    public void Warp(GameObject object) {
        if (isWarp) {
            if (isStatic) {
                if (destination != null) {
                    object.changeMap(destination);
                } else if (stringDestination != null) {
                    object.changeMap(map.place.getMapByName(stringDestination));
                }
                object.setX(toX);
                object.setY(toY);
            } else {
                WarpPoint warp;
                if (destination != null) {
                    object.changeMap(destination);
                    warp = destination.findWarp(name);
                } else {
                    Map m = map.place.getMapByName(stringDestination);
                    object.changeMap(m);
                    warp = m.findWarp(name);
                }
                o.setX(w.x);
                o.setY(w.y);
                if (o instanceof Player && ((Player) o).getCamera() != null) {
                    ((Player) o).getCamera().update();
                }
            }
        }
    }

    @Override
    public void render(int xEffect, int yEffect) {
        if (Main.DEBUG) {
            System.err.println("Empty method - " + Thread.currentThread().getStackTrace()[1].getMethodName() + " - from " + this.getClass());
        }
    }

    @Override
    public void renderShadowLit(int xEffect, int yEffect, float color, Figure f) {
        if (Main.DEBUG) {
            System.err.println("Empty method - " + Thread.currentThread().getStackTrace()[1].getMethodName() + " - from " + this.getClass());
        }
    }

    @Override
    public void renderShadowLit(int xEffect, int yEffect, float color, Figure f, int xs, int xe) {
        if (Main.DEBUG) {
            System.err.println("Empty method - " + Thread.currentThread().getStackTrace()[1].getMethodName() + " - from " + this.getClass());
        }
    }

    @Override
    public void renderShadow(int xEffect, int yEffect, Figure f) {
        if (Main.DEBUG) {
            System.err.println("Empty method - " + Thread.currentThread().getStackTrace()[1].getMethodName() + " - from " + this.getClass());
        }
    }

    @Override
    public void renderShadow(int xEffect, int yEffect, Figure f, int xs, int xe) {
        if (Main.DEBUG) {
            System.err.println("Empty method - " + Thread.currentThread().getStackTrace()[1].getMethodName() + " - from " + this.getClass());
        }
    }
}
