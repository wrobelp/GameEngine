/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamedesigner;

import engine.systemcommunication.IO;
import engine.utilities.Point;
import engine.utilities.SimpleKeyboard;
import game.Game;
import game.Settings;
import game.gameobject.inputs.Action;
import game.gameobject.inputs.InputKeyBoard;
import game.place.Place;
import game.place.map.Area;
import game.place.map.Map;
import game.text.FontBase;
import org.lwjgl.input.Keyboard;
import org.newdawn.slick.openal.SoundStore;
import sprites.SpriteBase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static game.place.map.Area.X_IN_TILES;
import static game.place.map.Area.Y_IN_TILES;

/**
 * @author przemek
 */
public class ObjectPlace extends Place {

    public static final int MODE_TILE = 0, MODE_BLOCK = 1, MODE_VIEWING = 2, MODE_OBJECT = 3;

    private final Action changeSplitScreenMode;
    private final Action changeSplitScreenJoin;
    private final updater[] updates = new updater[2];
    private final SimpleKeyboard key;
    private final String[] prettyOptions;
    private final boolean[] viewingOptions;
    private ObjectUI ui;
    private int mode;
    private String lastName;
    private GUIHandler guiHandler;
    private ObjectPlayer editor;
    private boolean altMode, noBlocks, grid;
    private ObjectMap objMap;
    private UndoControl undo;
    private short xWorkingAreaInTiles = 160;
    private short yWorkingAreaInTiles = 160;

    public ObjectPlace(Game game, int tileSize) {
        super(game, tileSize);
        Area.X_IN_TILES = xWorkingAreaInTiles;
        Area.Y_IN_TILES = yWorkingAreaInTiles;
        Place.xAreaInPixels = X_IN_TILES * tileSize;
        Place.yAreaInPixels = Y_IN_TILES * tileSize;
        dayCycle.setTime(18, 0);
        lastName = "";
        changeSplitScreenMode = new Action(new InputKeyBoard(Keyboard.KEY_INSERT));
        changeSplitScreenJoin = new Action(new InputKeyBoard(Keyboard.KEY_END));

        prettyOptions = new String[]{"Tiles: ", "Background: ", "Blocks: ", "Block Outlines: ", "FGTiles: "};
        viewingOptions = new boolean[prettyOptions.length];
        for (int i = 0; i < viewingOptions.length; i++) {
            viewingOptions[i] = true;
        }
        key = new SimpleKeyboard();
    }

    @Override
    public void generateAsGuest() {
        objMap = new ObjectMap(mapIDCounter++, this, xWorkingAreaInTiles * tileSize, yWorkingAreaInTiles * tileSize, Place.tileSize);
        ui = new ObjectUI(Place.tileSize, sprites.getSpriteSheet("tlo", "backgrounds"), this);
        guiHandler = new GUIHandler(this);
        maps.add(objMap);
        editor = ((ObjectPlayer) players[0]);
        editor.addGui(ui);
        editor.addGui(guiHandler);
        undo = new UndoControl(objMap, 20);
        //sounds.init("res");
        fonts = new FontBase(20);
        fonts.add("Amble-Regular", (int) (Settings.nativeScale * 24));
        standardFont = fonts.getFont(0);
        SoundStore.get().poll(0);
        initializeMethods();
    }

    @Override
    public void generateAsHost() {
        generateAsGuest();
    }

    @Override
    public void update() {
        updates[game.mode].update();
    }

    private void initializeMethods() {
        updates[0] = () -> {
            if (areKeysUsable()) {
                keyboardHandling();
            }
            if (playersCount > 1) {
                changeSplitScreenJoin.updateActiveState();
                changeSplitScreenMode.updateActiveState();
                if (changeSplitScreenJoin.isKeyClicked()) {
                    Settings.joinSplitScreen = !Settings.joinSplitScreen;
                }
                if (changeSplitScreenMode.isKeyClicked()) {
                    changeSSMode = true;
                }
                cameras[playersCount - 2].updateStatic();
            }
            updateAreas();
            for (int i = 0; i < playersCount; i++) {
                players[i].update();
            }
        };
        updates[1] = () -> System.err.println("ONLINE?..... pfft....");
    }

    private void updateAreas() {
        tempMaps.clear();
        for (int i = 0; i < playersCount; i++) {
            Map map = players[i].getMap();
            if (!tempMaps.contains(map)) {
                map.clearAreasToUpdate();
                tempMaps.add(map);
            }
            map.addAreasToUpdate(map.getNearAreas(players[i].getArea()));
        }
        tempMaps.stream().forEach((mapToUpdate) -> mapToUpdate.updateAreasToUpdate());
    }

    private void keyboardHandling() {
        key.keyboardStart();

        if (key.key(Keyboard.KEY_H)) {
            guiHandler.changeToHelpingScreen();
        }
        if (key.key(Keyboard.KEY_T)) {
            guiHandler.changeToChooser(IO.getSpecificFilesList("res/textures", "spr"));
        }
        if (key.key(Keyboard.KEY_L)) {
            guiHandler.changeToChooser(IO.getSpecificFilesList("res/objects", "puz"));
        }
        if (key.key(Keyboard.KEY_V)) {
            guiHandler.changeToViewingOptions(viewingOptions, prettyOptions);
        }
        if (key.key(Keyboard.KEY_G)) {
            grid = !grid;
        }
        if (key.keyPressed(Keyboard.KEY_U)) {
            undo.undo();
            objMap.deleteObject(editor);
            objMap.addObject(editor);
        }
        if (key.key(Keyboard.KEY_BACK) && key.keyPressed(Keyboard.KEY_LCONTROL)) {
            objMap.clear();
            objMap.addObject(editor);
            lastName = "";
            printMessage("MAP CLEARED");
        }
        altMode = key.key(Keyboard.KEY_LMENU);

        if (key.keyPressed(Keyboard.KEY_1)) {
            if (mode == MODE_TILE) {
                noBlocks = !noBlocks;
                printMessage("BLOCKS ARE " + (noBlocks ? "INVISIBLE" : "VISIBLE") + " NOW");
            } else {
                printMessage("TILE MODE");
            }
            setMode(MODE_TILE);
        }
        if (key.keyPressed(Keyboard.KEY_2)) {
            setMode(MODE_BLOCK);
            printMessage("BLOCK MODE");
        }
        if (key.keyPressed(Keyboard.KEY_3)) {
            setMode(MODE_VIEWING);
            printMessage("VIEWING MODE");
        }
        if (key.keyPressed(Keyboard.KEY_4)) {
            setMode(MODE_OBJECT);
            printMessage("OBJECT MODE");
        }

        if (key.keyPressed(Keyboard.KEY_S)) {
            if (key.key(Keyboard.KEY_LCONTROL) && !"".equals(lastName)) {
                saveObject(lastName);
            } else {
                guiHandler.changeToNamingConsole();
            }
        }
        key.keyboardEnd();
    }

    public void setViewingOption(int index) {
        switch (index) {
            case 0:
                objMap.setTilesVisibility(viewingOptions[index]);
                break;
            case 1:
                objMap.switchBackground();
                break;
            case 2:
                objMap.setBlocksVisibility(viewingOptions[index]);
                break;
            case 3:
                noBlocks = !viewingOptions[index];
                break;
            case 4:
                objMap.setFGTVisibility(viewingOptions[index]);
                break;
        }
    }

    public UndoControl getUndoControl() {
        return undo;
    }

    public boolean isAltMode() {
        return altMode;
    }

    public boolean isBlocksMode() {
        return !noBlocks;
    }

    public boolean isGridEnabled() {
        return grid;
    }

    public void setCentralPoint(int x, int y) {
        objMap.setCentralPoint(x, y);
    }

    public int getMode() {
        return mode;
    }

    private void setMode(int mode) {
        this.mode = mode;
        editor.setMode(mode);
        ui.setMode(mode);
    }

    public boolean areKeysUsable() {
        return !guiHandler.isWorking();
    }

    public void saveObject(String name) {
        lastName = name;
        ArrayList<String> content = objMap.saveMap();

        try (PrintWriter save = new PrintWriter("res/objects/" + name + ".puz")) {
            content.stream().forEach(save::println);
            printMessage("Object \"" + name + ".puz\" was saved.");
            save.close();
        } catch (FileNotFoundException e) {
            printMessage("A file cannot be created!");
        }
    }

    @Override
    public int getPlayersCount() {
        if (game.mode == 0) {
            return playersCount;
        } else {
            return 1;
        }
    }

    public void clearMap() {
        objMap.clear();
        printMessage("Map cleaned");
    }

    public void getFile(File f) {
        String name = f.getName();
        String[] file = name.split("\\.");
        if (file[1].equals("spr")) {
            try {
                System.out.println(f.getCanonicalPath() + " " + file[0]);
                ui.setSpriteSheet(sprites.getSpriteSheet(file[0], SpriteBase.getSpritePath(f)));
                printMessage("SpriteSheet \"" + name + "\" was loaded");
            } catch (java.lang.ClassCastException e) {
                printMessage("\"" + name + "\" is not a SpriteSheet!");
            } catch (IOException ex) {
                Logger.getLogger(ObjectPlace.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            ObjectPO loaded = new ObjectPO(file[0], this);
            //PuzzleObject loaded = new PuzzleObject(file[0], this);
            Point p = loaded.getStartingPoint();
            objMap.clear();
            loaded.placePuzzle(p.getX(), p.getY(), objMap);
            objMap.deleteObject(editor);
            objMap.addObject(editor);
            printMessage("Object \"" + name + "\" was loaded");
            undo.removeMoves();
            lastName = file[0];
            editor.changeMap(objMap, p.getX(), p.getY());
        }
    }

    private interface updater {

        void update();
    }
}
