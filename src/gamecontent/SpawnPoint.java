package gamecontent;

import collision.Figure;
import collision.OpticProperties;
import collision.Rectangle;
import engine.utilities.Delay;
import engine.utilities.Drawer;
import game.gameobject.GameObject;
import game.gameobject.entities.Mob;
import game.place.Place;
import game.place.cameras.Camera;
import game.place.map.Area;
import game.place.map.Map;
import sprites.Appearance;

/**
 * Created by przemek on 16.11.15.
 */
public class SpawnPoint extends GameObject {

    Delay delay;
    Class<? extends Mob> mob;
    int maxMobs, width, height, currentMaxMobs;

    private SpawnPoint(int x, int y, int width, int height, String name, Class<? extends Mob> mob, int seconds, int maxMobs, Appearance appearance) {
        initialize(name, x, y);
        setCollision(Rectangle.create(width, height, OpticProperties.NO_SHADOW, this));
        setSolid(appearance != null);
        this.width = width;
        this.height = height;
        this.mob = mob;
        this.appearance = appearance;
        this.currentMaxMobs = this.maxMobs = maxMobs;
        this.setToUpdate(true);
        delay = Delay.createInSeconds(seconds);
        delay.start();
    }

    public static SpawnPoint createVisible(int x, int y, int width, int height, String name, Class<? extends Mob> mob, int seconds, int maxMobs, Appearance
            appearance) {
        return new SpawnPoint(x, y, width, height, name, mob, seconds, maxMobs, appearance);
    }

    public static SpawnPoint createInVisible(int x, int y, int width, int height, String name, Class<? extends Mob> mob, int seconds, int maxMobs) {
        return new SpawnPoint(x, y, width, height, name, mob, seconds, maxMobs, null);
    }

    public void lowerSpawning() {
        if (currentMaxMobs > 0) {
            currentMaxMobs--;
        }
    }

    @Override
    public void update() {
        if (delay.isOver()) {
            delay.start();
            if (appearance != null || cantBeSeen()) {
                int mobs = 0;
                Area area = map.getArea(getX(), getY());
                for (Mob m : area.getNearSolidMobs()) {
                    if (m.getClass().getName().equals(mob.getName()) && m.getSpawner() == this) {
                        mobs++;
                    }
                }
                if (mobs < currentMaxMobs) {
                    if (area.getNearSolidMobs().stream().anyMatch((object) -> (object.getClass().getName().equals(mob.getName())
                            && collision.checkCollision(getX(), getY(), object)))) {
                        return;
                    }
                    try {
                        Mob m = mob.newInstance();
                        m.initialize(getX(), getY(), map.place);
                        m.setSpawner(this);
                        map.addObject(m);
                        System.out.println("Adding " + m.getName());
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private boolean cantBeSeen() {
        Camera cam;
        Map map;
        for (int player = 0; player < this.map.place.getPlayersCount(); player++) {
            map = this.map.place.players[player].getMap();
            if (map == this.map) {
                cam = (this.map.place.players[player].getCamera());
                if ((cam.getYStart() - height - Place.tileSize + floatHeight < y && cam.getYEnd() + height + Place.tileSize > y)
                        && (cam.getXStart() - width - Place.tileSize < x && cam.getXEnd() + width + Place.tileSize > x)) {
                    return false;
                }
            }
        }
        return true;
    }

    public Class getType() {
        return mob;
    }

    @Override
    public void render() {
        if (appearance != null) {
            Drawer.regularShader.translate(getX(), (int) (getY() - floatHeight));
            appearance.render();
            Drawer.refreshColor();
        }
    }

    @Override
    public void renderShadowLit(Figure figure) {
        if (appearance != null) {
            Drawer.drawShapeLit(appearance, getX(), getY() - (int) floatHeight);
        }
    }

    @Override
    public void renderShadow(Figure figure) {
        if (appearance != null) {
            Drawer.drawShapeBlack(appearance, collision.getDarkValue(), getX(), getY() - (int) floatHeight);
        }
    }

    @Override
    public void renderShadowLit(int xStart, int xEnd) {
        if (appearance != null) {
            Drawer.drawShapePartLit(appearance, getX(), getY() - (int) floatHeight, xStart, xEnd);
        }
    }

    @Override
    public void renderShadow(int xStart, int xEnd) {
        if (appearance != null) {
            Drawer.drawShapePartBlack(appearance, collision.getDarkValue(), getX(), getY() - (int) floatHeight, xStart, xEnd);
        }
    }

}
