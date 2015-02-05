/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.place;

import collision.Area;
import engine.Drawer;
import game.gameobject.GameObject;
import game.gameobject.Mob;
import game.gameobject.Player;
import game.place.cameras.Camera;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Collection;

/**
 *
 * @author Wojtek
 */
public class Map {

    public ArrayList<GameObject> visibleLights = new ArrayList<>(128);
    public final Place place;

    protected final Tile[] tiles;
    protected final ArrayList<Area> areas = new ArrayList<>();
    protected final String name;
    protected final int width, height, tileSize;
    protected final int widthInTiles, heightInTiles;

    protected final short mapID;
    protected short mobID = 0;
    protected final ArrayList<Mob> solidMobs = new ArrayList<>();
    protected final ArrayList<Mob> flatMobs = new ArrayList<>();
    protected final ArrayList<GameObject> solidObjects = new ArrayList<>();
    protected final ArrayList<GameObject> flatObjects = new ArrayList<>();
    protected final ArrayList<GameObject> emitters = new ArrayList<>();
    protected final ArrayList<WarpPoint> warps = new ArrayList<>();

    protected final ArrayList<GameObject> foregroundTiles = new ArrayList<>();
    protected final ArrayList<GameObject> objectsOnTop = new ArrayList<>();
    protected final ArrayList<GameObject> depthObjects = new ArrayList<>();
    protected final Comparator<GameObject> depthComparator = (GameObject firstObject, GameObject secondObject)
            -> firstObject.getDepth() - secondObject.getDepth();

    public Map(short mapID, String name, Place place, int width, int height, int tileSize) {
        this.place = place;
        this.name = name;
        this.mapID = mapID;
        this.width = width;
        this.height = height;
        this.tileSize = tileSize;
        widthInTiles = width / tileSize;
        heightInTiles = height / tileSize;
        tiles = new Tile[widthInTiles * heightInTiles];
    }

    public void addForegroundTileAndReplace(GameObject tile, int x, int y, int depth) {
        tiles[x / tileSize + y / tileSize * heightInTiles] = null;
        foregroundTiles.stream().filter((object) -> (object.getX() == x && object.getY() == y)).forEach((object) -> {
            foregroundTiles.remove(object);
        });
        addForegroundTile(tile, x, y, depth);
    }

    public void addForegroundTile(GameObject tile, int x, int y, int depth) {
        tile.setX(x);
        tile.setY(y);
        tile.setDepth(depth);
        addForegroundTile(tile);
    }

    public void addForegroundTile(GameObject tile) {
        foregroundTiles.add(tile);
        sortObjectsByDepth(foregroundTiles);
    }

    public void deleteForegroundTile(GameObject tile) {
        foregroundTiles.remove(tile);
        sortObjectsByDepth(foregroundTiles);
    }

    public void deleteForegroundTile(int x, int y) {
        foregroundTiles.stream().filter((foregroundTile) -> (foregroundTile.getX() == x && foregroundTile.getY() == y)).forEach((foregroundTile) -> {
            foregroundTiles.remove(foregroundTile);
        });
        sortObjectsByDepth(foregroundTiles);
    }

    public void addArea(Area area) {
        areas.add(area);
    }
    
    public void deleteArea(Area area) {
        System.out.println(areas.contains(area));
        areas.remove(area);
        System.out.println(areas.contains(area));
    }
    
    public void addObject(GameObject object) {
        object.setMapNotChange(this);
        if (object.isOnTop()) {
            objectsOnTop.add(object);
        } else {
            depthObjects.add(object);
        }
        if (!(object instanceof Player)) {
            addNotPlayerObject(object);
        }
    }

    private void addNotPlayerObject(GameObject object) {
        if (object.isEmitter()) {
            emitters.add(object);
        }
        if (object instanceof WarpPoint) {
            addWarpPoint((WarpPoint) object);
        } else if (object instanceof Mob) {
            addMob((Mob) object);
        } else {
            if (object.isSolid()) {
                solidObjects.add(object);
            } else {
                flatObjects.add(object);
            }
        }
    }

    private void addWarpPoint(WarpPoint warp) {
        warps.add(warp);
        warp.setPlace(place);
    }

    private void addMob(Mob mob) {
        if (mob.isSolid()) {
            solidMobs.add(mob);
        } else {
            flatMobs.add(mob);
        }
    }

    public void deleteForegroundTile(GameObject tile) {
        foregroundTiles.remove(tile);
        sortObjectsByDepth(foregroundTiles);
    }

    public void deleteForegroundTile(int x, int y) {
        foregroundTiles.stream().filter((foregroundTile)
                -> (foregroundTile.getX() == x && foregroundTile.getY() == y)).forEach((foregroundTile) -> {
                    foregroundTiles.remove(foregroundTile);
                });
        sortObjectsByDepth(foregroundTiles);
    }

    public void deleteObject(GameObject object) {
        object.setMapNotChange(null);
        if (!(object instanceof Player)) {
            deleteNotPlayerObject(object);
        }
        if (object.isOnTop()) {
            objectsOnTop.remove(object);
        } else {
            depthObjects.remove(object);
        }
    }

    private void deleteNotPlayerObject(GameObject object) {
        if (object.isEmitter()) {
            emitters.remove(object);
        }
        if (object instanceof WarpPoint) {
            warps.remove((WarpPoint) object);
        } else if (object instanceof Mob) {
            deleteMob((Mob) object);
        } else {
            if (object.isSolid()) {
                solidObjects.remove(object);
            } else {
                flatObjects.remove(object);
            }
        }
    }

    private void deleteMob(Mob mob) {
        if (mob.isSolid()) {
            solidMobs.remove(mob);
        } else {
            flatMobs.remove(mob);
        }
    }

    public void renderBackground(Camera camera) {
        Drawer.refreshForRegularDrawing();
        for (int y = 0; y < heightInTiles; y++) {
            if (camera.getYStart() < (y + 1) * tileSize && camera.getYEnd() > y * tileSize) {
                for (int x = 0; x < width / tileSize; x++) {
                    if (camera.getXStart() < (x + 1) * tileSize && camera.getXEnd() > x * tileSize) {
                        Tile tile = tiles[x + y * heightInTiles];
                        if (tile != null) {
                            tile.renderSpecific(camera.getXOffsetEffect() + x * tileSize, camera.getYOffsetEffect() + y * tileSize);
                        }
                    }
                }
            }
        }
    }

    public void renderObjects(Camera camera) {
        Drawer.refreshForRegularDrawing();
        renderBottom(camera);
        renderTop(camera);
    }

    public void renderBottom(Camera camera) {
        sortObjectsByDepth(depthObjects);
        int y = 0;
        for (GameObject object : depthObjects) {
            for (; y < foregroundTiles.size() && foregroundTiles.get(y).getDepth() < object.getDepth(); y++) {
                if (isObjectInSight(camera, foregroundTiles.get(y))) {
                    foregroundTiles.get(y).render(camera.getXOffsetEffect(), camera.getYOffsetEffect());
                }
            }
            if (object.isVisible() && isObjectInSight(camera, object)) {
                object.render(camera.getXOffsetEffect(), camera.getYOffsetEffect());
            }
        }
        for (int i = y; i < foregroundTiles.size(); i++) {
            foregroundTiles.get(i).render(camera.getXOffsetEffect(), camera.getYOffsetEffect());
        }
    }

    public void renderTop(Camera camera) {
        sortObjectsByDepth(objectsOnTop);
        for (GameObject object : objectsOnTop) {
            if (object.isVisible()
                    && isObjectInSight(camera, object)) {
                object.render(camera.getXOffsetEffect(), camera.getYOffsetEffect());
            }
        }
    }

    public void sortObjectsByDepth(ArrayList<GameObject> objects) {
        Collections.sort(objects, depthComparator);
    }

    protected void renderText(Camera camera) {
        renderPlayersNames(camera);
        renderMobsNames(camera);
    }

    private void renderPlayersNames(Camera camera) {
        for (int i = 0; i < place.playersCount; i++) {
            if (place.players[i].getMap().equals(this) && isObjectInSight(camera, place.players[i])) {
                ((Player) place.players[i]).renderName(camera);
            }
        }
    }

    private void renderMobsNames(Camera camera) {
        solidMobs.stream().filter((mob) -> (isObjectInSight(camera, mob))).forEach((mob) -> {
            mob.renderName(camera);
        });
    }

    private boolean isObjectInSight(Camera camera, GameObject object) {
        return camera.getYStart() <= object.getY() + (object.getHeight())
                && camera.getYEnd() >= object.getY() - (object.getHeight())
                && camera.getXStart() <= object.getX() + (object.getWidth())
                && camera.getXEnd() >= object.getX() - (object.getWidth());
    }

    public WarpPoint findWarp(String name) {
        for (WarpPoint warp : warps) {
            if (warp.getName().equals(name)) {
                return warp;
            }
        }
        return null;
    }

    public void clear() {
        solidMobs.clear();
        flatMobs.clear();
        solidObjects.clear();
        flatObjects.clear();
        emitters.clear();
        visibleLights.clear();
        areas.clear();
        depthObjects.clear();
        foregroundTiles.clear();
        objectsOnTop.clear();
    }

    public int getTileWidth() {
        return widthInTiles;
    }

    public int getTileHeight() {
        return widthInTiles;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getTileSize() {
        return tileSize;
    }

    public Tile getTile(int x, int y) {
        return tiles[x + y * heightInTiles];
    }

    public Tile getTile(int index) {
        return tiles[index];
    }

    public String getName() {
        return name;
    }

    public short getID() {
        return mapID;
    }

    public Collection<Mob> getSolidMobs() {
        return Collections.unmodifiableList(solidMobs);
    }

    public Collection<Mob> getFlatMobs() {
        return Collections.unmodifiableList(flatMobs);
    }

    public Collection<Area> getAreas() {
        return Collections.unmodifiableList(areas);
    }

    public Collection<GameObject> getSolidObjects() {
        return Collections.unmodifiableList(solidObjects);
    }

    public Collection<GameObject> getFlatObjects() {
        return Collections.unmodifiableList(flatObjects);
    }

    public Collection<GameObject> getEmitters() {
        return Collections.unmodifiableList(emitters);
    }

    public Collection<GameObject> getDepthObjects() {
        return Collections.unmodifiableList(depthObjects);
    }

    public Collection<GameObject> getObjectsOnTop() {
        return Collections.unmodifiableList(objectsOnTop);
    }

    public Collection<WarpPoint> getWarps() {
        return Collections.unmodifiableList(warps);
    }

    public Collection<GameObject> getForegroundTiles() {
        return Collections.unmodifiableList(foregroundTiles);
    }

    public void setTile(int x, int y, Tile tile) {
        tiles[x + y * heightInTiles] = tile;
    }

    public void setTile(int index, Tile tile) {
        tiles[index] = tile;
    }
}
