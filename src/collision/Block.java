/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package collision;

import engine.Drawer;
import engine.Main;
import engine.Point;
import game.gameobject.GameObject;
import game.place.ForegroundTile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author Wojtek
 */
public class Block extends GameObject {

    private final ArrayList<Figure> top = new ArrayList<>(1);
    private final ArrayList<ForegroundTile> topForegroundTiles = new ArrayList<>();
    private final ArrayList<ForegroundTile> wallForegroundTiles = new ArrayList<>();

    public static Block create(int x, int y, int width, int height, int shadowHeight) {
        return new Block(x, y, width, height, shadowHeight, false);
    }

    public static Block createRound(int x, int y, int width, int height, int shadowHeight) {
        return new Block(x, y, width, height, shadowHeight, true);
    }

    private Block(int x, int y, int width, int height, int shadowHeight, boolean round) {  //Point (x, y) should be in left top corner of Area
        this.x = x;
        this.y = y;
        name = "area";
        solid = true;
        simpleLighting = !round;
        if (round) {
            setCollision(RoundRectangle.createShadowHeight(0, 0, width, height, OpticProperties.FULL_SHADOW, shadowHeight, this));
        } else {
            setCollision(Rectangle.createShadowHeight(0, 0, width, height, OpticProperties.FULL_SHADOW, shadowHeight, this));
        }
    }

    public void setTop(Figure top) {
        this.top.clear();
        this.top.add(top);
        this.top.trimToSize();
    }

    public void addForegroundTile(ForegroundTile foregroundTile) {
        if (foregroundTile.isWall()) {
            wallForegroundTiles.add(foregroundTile);
        } else {
            topForegroundTiles.add(foregroundTile);
            top.add(foregroundTile.getCollision());
        }
        foregroundTile.setBlockPart(true);
    }

    public void removeForegroundTile(ForegroundTile foregroundTile) {
        wallForegroundTiles.remove(foregroundTile);
        topForegroundTiles.remove(foregroundTile);
        top.remove(foregroundTile.getCollision());
    }

    public void pushCorner(int corner, int tileSize, int xChange, int yChange) {
        if (collision instanceof RoundRectangle) {
            ((RoundRectangle) collision).pushCorner(corner, tileSize, xChange, yChange);
        }
    }

    public boolean isCollide(int x, int y, Figure figure) {
        return figure.isCollideSingle(x, y, collision);
    }

    public Figure whatCollide(int x, int y, Figure figure) {
        if (figure.isCollideSingle(x, y, collision)) {
            return collision;
        }
        return null;
    }

    @Override
    public void renderShadowLit(int xEffect, int yEffect, float color, Figure figure) {
        glPushMatrix();
        if (isSimpleLighting()) {
            Drawer.drawRectangleInShade(figure.getX() + xEffect, figure.getY() - figure.getShadowHeight() + yEffect,
                    figure.width, figure.height + figure.getShadowHeight(), color);
        } else {
            glTranslatef(xEffect + getX(), yEffect + getY(), 0);
            Drawer.setCentralPoint();
            wallForegroundTiles.stream().forEach((wall) -> {
                Figure col = wall.getCollision();
                Drawer.returnToCentralPoint();
                Drawer.translate(col.getX() - getX(), col.getY() - getY() - col.getShadowHeight());
                if (wall.isSimpleLighting()) {
                    Drawer.drawRectangleInShade(0, 0, col.width, col.height + col.getShadowHeight(), color);
                } else {
                    Drawer.drawShapeInShade(wall, color);
                }

            });

        }
        glPopMatrix();
    }

    @Override
    public void renderShadow(int xEffect, int yEffect, Figure figure) {
        glPushMatrix();
        if (isSimpleLighting()) {
            Drawer.drawRectangleInBlack(figure.getX() + xEffect, figure.getY() - figure.getShadowHeight() + yEffect,
                    figure.width, figure.height + figure.getShadowHeight());
        } else {
            glTranslatef(xEffect + getX(), yEffect + getY(), 0);
            Drawer.setCentralPoint();
            wallForegroundTiles.stream().forEach((wall) -> {
                Figure col = wall.getCollision();
                Drawer.returnToCentralPoint();
                Drawer.translate(col.getX() - getX(), col.getY() - getY() - col.getShadowHeight());
                if (wall.isSimpleLighting()) {
                    Drawer.drawRectangleInBlack(0, 0, col.width, col.height + col.getShadowHeight());
                } else {
                    Drawer.drawShapeInBlack(wall);
                }
            });
        }
        glPopMatrix();
    }

    @Override
    public void renderShadowLit(int xEffect, int yEffect, float color, Figure figure, int xStart, int xEnd) {
        glPushMatrix();
        if (isSimpleLighting()) {
            System.out.println("Powinno być nie używane w Block");
            glTranslatef(figure.getX() + xEffect, figure.getY() - figure.getShadowHeight() + yEffect, 0);
            Drawer.drawRectangleInShade(0, 0, figure.width, figure.height + figure.getShadowHeight(), color);
        } else {
            glTranslatef(xEffect + getX(), yEffect + getY(), 0);
            Drawer.setCentralPoint();
            wallForegroundTiles.stream().forEach((wall) -> {
                Figure col = wall.getCollision();
                Drawer.returnToCentralPoint();
                Drawer.translate(col.getX() - getX(), col.getY() - getY() - col.getShadowHeight());
                if (wall.isSimpleLighting()) {
                    Drawer.drawRectangleInShade(xStart, 0, xEnd - xStart, col.height + col.getShadowHeight(), color);
                } else {
                    Drawer.drawShapePartInShade(wall, color, xStart, xEnd);
                }
            });
        }
        glPopMatrix();
    }

    @Override
    public void renderShadow(int xEffect, int yEffect, Figure figure, int xStart, int xEnd) {
        glPushMatrix();
        if (isSimpleLighting()) {
            System.out.println("Powinno być nie używane w Block");
            glTranslatef(figure.getX() + xEffect, figure.getY() - figure.getShadowHeight() + yEffect, 0);
            Drawer.drawRectangleInBlack(0, 0, figure.width, figure.height + figure.getShadowHeight());
        } else {
            glTranslatef(xEffect + getX(), yEffect + getY(), 0);
            Drawer.setCentralPoint();
            wallForegroundTiles.stream().forEach((wall) -> {
                Figure col = wall.getCollision();
                Drawer.returnToCentralPoint();
                Drawer.translate(col.getX() - getX(), col.getY() - getY() - col.getShadowHeight());
                if (wall.isSimpleLighting()) {
                    Drawer.drawRectangleInBlack(xStart, 0, xEnd - xStart, col.height + col.getShadowHeight());
                } else {
                    Drawer.drawShapePartInBlack(wall, xStart, xEnd);
                }
            });
        }
        glPopMatrix();
    }

    @Override
    public void render(int xEffect, int yEffect) {
        if (Main.DEBUG) {
            System.err.println("Empty method - " + Thread.currentThread().getStackTrace()[1].getMethodName() + " - from " + this.getClass());
        }
    }

    //b:x:y:width:height:shadowHeight
    public String saveToString(int xBegin, int yBegin, int tile) {
        return "b:" + ((int) (x - xBegin) / tile) + ":" + ((int) (y - yBegin) / tile) + ":"
                + (collision.width / tile) + ":" + (collision.height / tile) + ":" + (collision.getShadowHeight() / tile);
    }

    public Collection<ForegroundTile> getTopForegroundTiles() {
        return Collections.unmodifiableCollection(topForegroundTiles);
    }

    public Collection<ForegroundTile> getWallForegroundTiles() {
        return Collections.unmodifiableCollection(wallForegroundTiles);
    }

    public Collection<Figure> getTop() {
        return Collections.unmodifiableCollection(top);
    }

    public Collection<Point> getPoints() {
        return collision.getPoints();
    }
}
