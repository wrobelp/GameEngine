/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamedesigner;

import engine.systemcommunication.IO;
import engine.utilities.Drawer;
import engine.utilities.Methods;
import engine.utilities.SimpleKeyboard;
import game.gameobject.GUIObject;
import game.place.Place;
import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Wojtek
 */
public class GUIHandler extends GUIObject {

    private final int tile, xStart, yStart;
    private final ObjectPlace objPlace;
    private final SimpleKeyboard key;
    private final int DO_NOTHING = -1, NAMING = 0, CHOOSING = 1, HELPING = 2, QUESTIONING = 3, VIEWING = 4;
    private final Comparator<File> nameComparator = (File o1, File o2) -> {
        if ((o1.isDirectory() && o2.isDirectory()) || (!o1.isDirectory() && !o2.isDirectory())) {
            return o1.getName().compareTo(o2.getName());
        } else if (o1.isDirectory()) {
            return -1;
        } else {
            return 1;
        }
    };
    private final String[] help = new String[]{
            "H:", "Help",
            "1 ... 4:", "Change mode",
            "S:", "Save as",
            "CTRL + S:", "QuickSave",
            "L:", "Load object",
            "CTRL + BACKSPACE:", "Clear map",
            "",
            "CTRL + ARROWS:", "Change selection",
            "CTRL + Z:", "Reset selection",
            "A:", "Run mode",
            "BACKSPACE:", "Cancel",
            "U:", "Undo",
            "",
            "SPACE:", "Create",
            "DELETE:", "Delete",
            "ALT:", "Create altered",
            "",
            "+:", "Zoom in/out",
            "V:", "Visibility options",
            "B:", "Lock Block",
            "M:", "Move Blocks",
            "HOME:", "Set starting point",
            "PAGE UP/DOWN:", "Raise/Lower elevation",
            "",
            "//TILE MODE (1)",
            "",
            "SHIFT + ARROWS:", "Change tile",
            "T:", "Load spriteSheet",
            "Q:", "Switch to light-based mode",
            "",
            "//BLOCK MODE (2)",
            "",
            "SHIFT + ARROWS:", "Change block height",
            "R:", "Rounded blocks mode",
            "C:", "Place rounded block with last settings",
            "",
            "//OBJECT MODE (4)",
            "",
            "SHIFT + ARROWS:", "Change link radius"};
    private int mode, selected;
    private ArrayList<File> list;
    private String text = "", extension;
    private boolean firstLoop;
    private boolean[] options;
    private String[] prettyOptions;
    private File previous;

    private int helpLength;

    public GUIHandler(Place place) {
        super("gui", place);
        tile = Place.tileSize;
        objPlace = (ObjectPlace) place;
        mode = DO_NOTHING;
        key = new SimpleKeyboard();
        visible = false;
        xStart = (int) (tile * 0.1);
        yStart = (int) (tile * 2.5);

        for (String s : help) {
            if (!s.equals("") && s.charAt(s.length() - 1) == ':') {
                helpLength = Math.max(helpLength, place.standardFont.getWidth(s));
            }
        }
        helpLength *= 1.1;
    }

    public void changeToNamingConsole() {
        mode = NAMING;
        visible = true;
        firstLoop = true;
    }

    public void changeToChooser(ArrayList<File> list, String extension) {
        mode = CHOOSING;
        this.list = list;
        this.extension = extension;
        previous = list.get(0).getParentFile();
        Collections.sort(list, nameComparator);
        selected = 0;
        visible = true;
    }

    public void changeToHelpingScreen() {
        mode = HELPING;
        visible = true;
    }

    public void changeToViewingOptions(boolean[] options, String[] prettyOptions) {
        mode = VIEWING;
        this.options = options;
        selected = 0;
        this.prettyOptions = new String[options.length * 2];
        for (int i = 0; i < options.length; i++) {
            this.prettyOptions[2 * i] = prettyOptions[i] + ": ";
            this.prettyOptions[2 * i + 1] = options[i] ? "ON" : "OFF";
        }
        visible = true;
    }

    public boolean isWorking() {
        return mode != DO_NOTHING;
    }

    private void stop() {
        mode = DO_NOTHING;
        visible = false;
    }

    private void renderNamingConsole() {
        key.keyboardStart();

        if (firstLoop) {
            while (Keyboard.next()) {
                Keyboard.getEventKey();
            }
            firstLoop = false;
            return;
        }
        text = Methods.editWithKeyboard(text);
        Drawer.renderString("Write filename: " + text, (int) (xStart * Place.getCurrentScale()), (int) (yStart * Place.getCurrentScale()),
                place.standardFont, new Color(1f, 1f, 1f));

        if (key.keyPressed(Keyboard.KEY_RETURN)) {
            if (text.length() > 0) {
                try {
                    FileReader fl = new FileReader("res/objects/" + text + ".puz");
                    BufferedReader load = new BufferedReader(fl);
                    mode = QUESTIONING;
                    load.close();
                    fl.close();
                    return;
                } catch (IOException e) {
                }
                objPlace.saveObject(text);
            }
            stop();
        }

        key.keyboardEnd();
    }

    private void renderChoosingFile() {
        key.keyboardStart();

        Drawer.renderString(">", (int) (xStart * Place.getCurrentScale()), (int) (yStart * Place.getCurrentScale()),
                place.standardFont, new Color(1f, 1f, 1f));

        int delta;
        String name;
        File tmp;
        for (int i = 0; i < list.size(); i++) {
            tmp = list.get(i);
            delta = (int) ((i - selected) * tile * 0.5);
            if (tmp == previous) {
                name = "../";
            } else if (tmp.isDirectory()) {
                name = "<" + tmp.getName() + ">";
            } else {
                name = tmp.getName();
            }
            Drawer.renderString(name, (int) ((xStart + tile * 0.2) * Place.getCurrentScale()), (int) ((yStart + delta) * Place.getCurrentScale()),
                    place.standardFont, new Color(1f, 1f, 1f));
        }

        if (key.keyPressed(Keyboard.KEY_UP)) {
            selected--;
            if (selected < 0) {
                selected = list.size() - 1;
            }
        }
        if (key.keyPressed(Keyboard.KEY_DOWN)) {
            selected++;
            if (selected > list.size() - 1) {
                selected = 0;
            }
        }
        if (key.keyPressed(Keyboard.KEY_RETURN)) {
            if (list.get(selected).isDirectory()) {
                previous = list.get(selected).getParentFile();
                list = IO.getSpecificFilesList(list.get(selected), extension);
                if (!previous.getName().equals("res")) {
                    list.add(previous);
                }
                Collections.sort(list, nameComparator);
                selected = 0;
            } else {
                objPlace.getFile(list.get(selected));
                stop();
            }
        }
        if (key.keyPressed(Keyboard.KEY_BACK)) {
            stop();
        }
        key.keyboardEnd();
    }

    private void renderHelp() {
        key.keyboardStart();

        Drawer.renderString(">", (int) (xStart * Place.getCurrentScale()), (int) (yStart * Place.getCurrentScale()),
                place.standardFont, new Color(1f, 1f, 1f));

        int delta;
        int index = 0;
        for (int i = 0; i < help.length; i++, index++) {
            delta = (int) ((index - selected) * tile * 0.5);
            Drawer.renderString(help[i], (int) ((xStart + tile * 0.2) * Place.getCurrentScale()), (int) ((yStart + delta) * Place.getCurrentScale()),
                    place.standardFont, new Color(1f, 1f, 1f));
            if (!help[i].equals("") && help[i].charAt(help[i].length() - 1) == ':') {
                Drawer.renderString(help[++i], helpLength + (int) ((xStart + tile * 0.2) * Place.getCurrentScale()), (int) ((yStart + delta) * Place
                                .getCurrentScale()),
                        place.standardFont, new Color(1f, 1f, 1f));
            }
        }
        if (key.keyPressed(Keyboard.KEY_UP)) {
            selected--;
            if (selected < 0) {
                selected = help.length - 1;
            }
        }
        if (key.keyPressed(Keyboard.KEY_DOWN)) {
            selected++;
            if (selected > help.length - 1) {
                selected = 0;
            }
        }
        if (key.keyPressed(Keyboard.KEY_RETURN) || key.keyPressed(Keyboard.KEY_BACK)) {
            stop();
        }
        key.keyboardEnd();
    }

    private void renderViewingOptions() {
        key.keyboardStart();

        Drawer.renderString(">", (int) (xStart * Place.getCurrentScale()), (int) (yStart * Place.getCurrentScale()),
                place.standardFont, new Color(1f, 1f, 1f));

        int delta;
        for (int i = 0; i < options.length; i++) {
            delta = (int) ((i - selected) * tile * 0.5);
            Drawer.renderString(prettyOptions[2 * i] + prettyOptions[2 * i + 1], (int) ((xStart + tile * 0.2) * Place.getCurrentScale()), (int) ((yStart +
                            delta) * Place.getCurrentScale()),
                    place.standardFont, new Color(1f, 1f, 1f));
        }

        if (key.keyPressed(Keyboard.KEY_UP)) {
            selected--;
            if (selected < 0) {
                selected = options.length - 1;
            }
        }
        if (key.keyPressed(Keyboard.KEY_DOWN)) {
            selected++;
            if (selected > options.length - 1) {
                selected = 0;
            }
        }
        if (key.keyPressed(Keyboard.KEY_RETURN)) {
            options[selected] = !options[selected];
            prettyOptions[2 * selected + 1] = options[selected] ? "ON" : "OFF";
            objPlace.setViewingOption(selected);
        }
        if (key.keyPressed(Keyboard.KEY_BACK)) {
            stop();
        }
        key.keyboardEnd();
    }

    private void renderQuestion() {
        key.keyboardStart();
        Drawer.renderString("File with that name already exist.", (int) (xStart * Place.getCurrentScale()), (int) (yStart * Place.getCurrentScale()),
                place.standardFont, new Color(1f, 1f, 1f));
        Drawer.renderString("Replace?", (int) (xStart * Place.getCurrentScale()), (int) ((yStart + tile * 0.5) * Place.getCurrentScale()),
                place.standardFont, new Color(1f, 1f, 1f));
        Drawer.renderString("YES[Enter] / NO[Backspace]", (int) (xStart * Place.getCurrentScale()), (int) ((yStart + tile) * Place.getCurrentScale()),
                place.standardFont, new Color(1f, 1f, 1f));

        if (key.keyPressed(Keyboard.KEY_RETURN)) {
            objPlace.saveObject(text);
            stop();
        }
        if (key.keyPressed(Keyboard.KEY_BACK)) {
            stop();
        }
        key.keyboardEnd();
    }

    @Override
    public void render(int xEffect, int yEffect) {
        if (player != null) {
            glPushMatrix();
            glScaled(Place.getCurrentScale(), Place.getCurrentScale(), 1);
            //glTranslatef(xEffect, yEffect, 0);
            glScaled(1 / Place.getCurrentScale(), 1 / Place.getCurrentScale(), 1);
            switch (mode) {
                case QUESTIONING:
                    renderQuestion();
                    break;
                case NAMING:
                    renderNamingConsole();
                    break;
                case CHOOSING:
                    renderChoosingFile();
                    break;
                case HELPING:
                    renderHelp();
                    break;
                case VIEWING:
                    renderViewingOptions();
                    break;
            }
            Drawer.refreshForRegularDrawing();
            glPopMatrix();
        }
    }

}
