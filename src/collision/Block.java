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

    private ArrayList<Figure> top = new ArrayList<>(1);
    private ArrayList<ForegroundTile> topForegroundTiles = new ArrayList<>();
    private ArrayList<ForegroundTile> wallForegroundTiles = new ArrayList<>();

    public Block(int x, int y, int width, int height, int shadowHeight) {  //Point (x, y) should be in left top corner of Area
        this.x = x;
        this.y = y;
        name = "area";
        solid = true;
        simpleLighting = true;
        setCollision(Rectangle.createShadowHeight(0, 0, width, height, OpticProperties.FULL_SHADOW, shadowHeight, this));
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
    }

    public void removeForegroundTile(ForegroundTile foregroundTile) {
        wallForegroundTiles.remove(foregroundTile);
        topForegroundTiles.remove(foregroundTile);
        top.remove(foregroundTile.getCollision());
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
        glTranslatef(figure.getX() + xEffect, figure.getY() - figure.getShadowHeight() + yEffect, 0);
        if (figure.getOwner().isSimpleLighting()) {
            Drawer.drawRectangleInShade(0, 0, figure.width, figure.height + figure.getShadowHeight(), color);
        } else if (figure.isGiveShadow()) {
            wallForegroundTiles.stream().forEach((wall) -> {
                Drawer.drawShapeInShade(wall, color);
            });
        } else {
            topForegroundTiles.stream().forEach((top) -> {
                Drawer.drawShapeInShade(top, color);
            });
        }
        glPopMatrix();
    }

    @Override
    public void renderShadow(int xEffect, int yEffect, Figure figure) {
        if (figure.getOwner().isSimpleLighting()) {
            glPushMatrix();
            glTranslatef(figure.getX() + xEffect, figure.getY() - figure.getShadowHeight() + yEffect, 0);
            Drawer.drawRectangleInBlack(0, 0, figure.width, figure.height + figure.getShadowHeight());
            glPopMatrix();
        } else if (figure.isGiveShadow()) {
            wallForegroundTiles.stream().forEach((wall) -> {
                Drawer.drawShapeInBlack(wall);
            });
        } else {
            topForegroundTiles.stream().forEach((top) -> {
                Drawer.drawShapeInBlack(top);
            });
        }
    }

    @Override
    public void renderShadowLit(int xEffect, int yEffect, float color, Figure figure, int xStart, int xEnd) {
        if (figure.getOwner().isSimpleLighting()) {
            glPushMatrix();
            glTranslatef(figure.getX() + xEffect, figure.getY() - figure.getShadowHeight() + yEffect, 0);
            Drawer.drawRectangleInShade(0, 0, figure.width, figure.height + figure.getShadowHeight(), color);
            glPopMatrix();
        } else if (figure.isGiveShadow()) {
            wallForegroundTiles.stream().forEach((wall) -> {
                Drawer.drawShapePartInShade(wall, color, xStart, xEnd);
            });
        } else {
            topForegroundTiles.stream().forEach((top) -> {
                Drawer.drawShapePartInShade(top, color, xStart, xEnd);
            });
        }
    }

    @Override
    public void renderShadow(int xEffect, int yEffect, Figure figure, int xStart, int xEnd) {
        if (figure.getOwner().isSimpleLighting()) {
            glPushMatrix();
            glTranslatef(figure.getX() + xEffect, figure.getY() - figure.getShadowHeight() + yEffect, 0);
            Drawer.drawRectangleInBlack(0, 0, figure.width, figure.height + figure.getShadowHeight());
            glPopMatrix();
        } else if (figure.isGiveShadow()) {
            wallForegroundTiles.stream().forEach((wall) -> {
                Drawer.drawShapePartInBlack(wall, xStart, xEnd);
            });
        } else {
            topForegroundTiles.stream().forEach((top) -> {
                Drawer.drawShapePartInBlack(top, xStart, xEnd);
            });
        }
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
