/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.gameobject;

/**
 *
 * @author przemek
 */

import collision.Figure;
import engine.Light;
import game.place.Map;
import sprites.Animation;
import sprites.Sprite;

import java.util.ArrayList;
import java.util.List;

public abstract class GameObject {

    protected double x, y;
    protected int depth;
    protected boolean solid, emitter, emits, onTop, simpleLighting, visible, wall, mobile;
    protected Sprite sprite;
    protected ArrayList<Light> lights = new ArrayList<>(1);
    protected ArrayList<Interactive> interactives = new ArrayList<>(1);
    protected String name;
    protected Map map;
    protected int area = -1, prevArea = -1;
    protected Figure collision;
    protected Animation animation;

    public abstract void render(int xEffect, int yEffect);

    public abstract void renderShadowLit(int xEffect, int yEffect, Figure figure);

    public abstract void renderShadowLit(int xEffect, int yEffect, Figure figure, int xStart, int xEnd);

    public abstract void renderShadow(int xEffect, int yEffect, Figure figure);

    public abstract void renderShadow(int xEffect, int yEffect, Figure figure, int xStart, int xEnd);

    protected void initialize(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
        depth = 0;
        visible = true;
    }

    public void changeMap(Map map) {
        if (this.map != null && this.map != map) {
            this.map.deleteObject(this);
        }
        this.map = map;
        this.map.addObject(this);
    }

    public void updateAreaPlacement() {
        if (map != null) {
            if (area != -1 && prevArea != -1) {
                prevArea = area;
                area = map.getAreaIndex(getX(), getY());
                map.changeAreaIfNeeded(area, prevArea, this);
            } else {
                area = map.getAreaIndex(getX(), getY());
                prevArea = area;
            }
        }
    }

    public void addLight(Light light) {
        lights.add(light);
    }

    public void addInteractive(Interactive interactive) {
        interactives.add(interactive);
    }

    public boolean isSolid() {
        return solid;
    }

    public void setSolid(boolean solid) {
        this.solid = solid;
    }

    public boolean isOnTop() {
        return onTop;
    }

    public void setOnTop(boolean onTop) {
        this.onTop = onTop;
    }

    public boolean isEmitter() {
        return emitter;
    }

    public boolean isEmits() {
        return emits;
    }

    public void setEmits(boolean emits) {
        this.emits = emits;
    }

    public boolean isSimpleLighting() {
        return simpleLighting;
    }

    public void setSimpleLighting(boolean simpleLighting) {
        this.simpleLighting = simpleLighting;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean vis) {
        this.visible = vis;
    }

    public boolean isMobile() {
        return mobile;
    }

    public void setMobile(boolean mobile) {
        this.mobile = mobile;
    }

    public boolean isInteractive() {
        return !interactives.isEmpty();
    }

    public int getX() {
        return (int) x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public int getY() {
        return (int) y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getXInDouble() {
        return x;
    }

    public double getYInDouble() {
        return y;
    }

    public int getDepth() {
        return (int) (depth + y);
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getPureDepth() {
        return depth;
    }

    public int getXEnd() {
        return (int) x + collision.getWidth();
    }

    public int getYEnd() {
        return (int) y + collision.getHeight();
    }

    public int getEndOfX() {
        return (int) x + collision.getWidth() / 2;
    }

    public int getEndOfY() {
        return (int) y + collision.getHeight() / 2;
    }

    public int getXSpriteTextureCorner() {
        if (sprite != null) {
            return (int) x + sprite.getXOffset();
        } else {
            return (int) x;
        }
    }

    public int getYSpriteTextureCorner() {
        if (sprite != null) {
            return (int) y + sprite.getYOffset();
        } else {
            return (int) y;
        }
    }

    public int getXSpriteBegin() {
        if (sprite != null) {
            return (int) x + sprite.getXOffset() + sprite.getXStart();
        } else {
            return (int) x;
        }
    }

    public int getYSpriteBegin() {
        if (sprite != null) {
            return (int) y + sprite.getYOffset() + sprite.getYStart();
        } else {
            return (int) y;
        }
    }

    public int getXSpriteEnd() {
        if (sprite != null) {
            return (int) x + sprite.getXOffset() + sprite.getXStart() + sprite.getActualWidth();
        } else {
            return (int) x;
        }
    }

    public int getYSpriteEnd() {
        if (sprite != null) {
            return (int) y + sprite.getYOffset() + sprite.getYStart() + sprite.getActualHeight();
        } else {
            return (int) y;
        }
    }

    public int getXSpriteOffset() {
        return sprite.getXOffset();
    }

    public int getYSpriteOffset() {
        return sprite.getYOffset();
    }

    public int getXSpriteOffsetWidth() {
        return sprite.getXOffset() + sprite.getWidth();
    }

    public Figure getCollision() {
        return collision;
    }

    public void setCollision(Figure figure) {
        collision = figure;
    }

    public Map getMap() {
        return map;
    }

    public int getArea() {
        return area;
    }

    public int getPrevArea() {
        return prevArea;
    }

    public int getCollisionWidth() {
        return collision != null ? collision.getWidth() : sprite.getActualWidth();
    }

    public int getCollisionHeight() {
        return collision != null ? collision.getHeight() : sprite.getActualHeight();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Light> getLights() {
        return lights;
    }

    public List<Interactive> getInteractives() {
        return interactives;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public Animation getAnimation() {
        return animation;
    }

    public void setPosition(double x, double y) {
        setX(x);
        setY(y);
        updateAreaPlacement();
    }

    public void setMapNotChange(Map map) {
        this.map = map;
    }
}
