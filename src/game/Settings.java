/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import engine.Methods;
import engine.SoundBase;
import game.gameobject.Player;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Controller;
import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.ARBTextureMultisample;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.glGetInteger;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GLContext;

/**
 *
 * @author przemek
 */
public class Settings {

    public DisplayMode[] tempModes;
    public DisplayMode[] modes;
    public int modesNumber;
    public DisplayMode display = Display.getDesktopDisplayMode();
    public int curentMode;
    public boolean fullScreen;
    public boolean hSplitScreen;
    public boolean joinSS;
    public int nrPlayers = 1;
    public float volume = 0.5f;
    public SoundBase sounds;
    public int resWidth;
    public int resHeight;
    public float SCALE;
    public int freq;
    public int depth = display.getBitsPerPixel();
    public boolean vSync;
    public int nrSamples = 0;
    public String lang;
    public ArrayList<Language> languages = new ArrayList<>();
    public Language language;
    public int actionsNr;
    public Player[] players;
    public Controller[] controllers;
    public int worldSeed;
    public int maxSamples;
    public int isSupfboVer3;
    public boolean isSupfboMS;
    public boolean shadowOff;
    public String serverIP = "127.0.0.1";

    public Settings() {
        int minW = 1024;
        int minH = 768;
        int maxW = 1920;
        int maxH = 1200;
        try {
            tempModes = Display.getAvailableDisplayModes();
        } catch (LWJGLException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
        }
        DisplayMode temp;
        if (tempModes[0].getWidth() >= minW && tempModes[0].getWidth() <= maxW
                && tempModes[0].getHeight() >= minH && tempModes[0].getHeight() <= maxH && tempModes[0].getBitsPerPixel() == depth) {
            modesNumber++;
        }
        int i, j;
        for (i = 1; i < tempModes.length; i++) {
            if (tempModes[i].getWidth() >= minW && tempModes[i].getWidth() <= maxW && tempModes[i].getHeight() >= minH && tempModes[i].getHeight() <= maxH && tempModes[i].getBitsPerPixel() == depth) {
                modesNumber++;
            }
            temp = tempModes[i];
            for (j = i; j > 0 && isBigger(tempModes[j - 1], temp); j--) {
                tempModes[j] = tempModes[j - 1];
            }
            tempModes[j] = temp;
        }
        modes = new DisplayMode[modesNumber];
        i = 0;
        for (DisplayMode mode : tempModes) {
            if (mode.getWidth() >= minW && mode.getWidth() <= maxW && mode.getHeight() >= minH && mode.getHeight() <= maxH && mode.getBitsPerPixel() == depth) {
                modes[i++] = mode;
            }
        }
        resWidth = modes[0].getWidth();
        resHeight = modes[0].getHeight();
        freq = modes[0].getFrequency();
        languages.add(new LangPL());
        languages.add(new LangENG());
        language = languages.get(0);
        lang = language.lang;
    }

    private boolean isBigger(DisplayMode checked, DisplayMode temp) {
        if (checked.getBitsPerPixel() > temp.getBitsPerPixel()) {
            return true;
        } else if (checked.getWidth() > temp.getWidth()) {
            return true;
        } else if (checked.getWidth() == temp.getWidth() && checked.getHeight() > temp.getHeight()) {
            return true;
        } else {
            return checked.getWidth() == temp.getWidth() && checked.getHeight() == temp.getHeight() && checked.getFrequency() > temp.getFrequency();
        }
    }

    public void update(int nr, Player[] players, Controller[] controllers) {
        actionsNr = nr;
        this.players = players;
        this.controllers = controllers;
        this.SCALE = ((int) ((resHeight / 1024f / 0.25f)) * 0.25f) >= 1 ? 1 : (int) ((resHeight / 1024f / 0.25f)) * 0.25f;
        try {
            GL30.glGenFramebuffers();
            GL32.glTexImage2DMultisample(GL32.GL_TEXTURE_2D_MULTISAMPLE, nrSamples, GL_RGBA8, 10, 10, false);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
            isSupfboVer3 = 0;
            isSupfboMS = true;
            maxSamples = glGetInteger(GL30.GL_MAX_SAMPLES) / 2;
            maxSamples = maxSamples > 8 ? 8 : maxSamples;
            nrSamples = (nrSamples > maxSamples) ? maxSamples : nrSamples;
        } catch (Exception e) {
            if (GLContext.getCapabilities().GL_ARB_framebuffer_object) {
                isSupfboVer3 = 1;
                try {
                    ARBTextureMultisample.glTexImage2DMultisample(ARBTextureMultisample.GL_TEXTURE_2D_MULTISAMPLE, nrSamples, GL_RGBA8, 10, 10, false);
                    ARBFramebufferObject.glBindFramebuffer(ARBFramebufferObject.GL_DRAW_FRAMEBUFFER, 0);
                    isSupfboMS = true;
                    maxSamples = glGetInteger(GL30.GL_MAX_SAMPLES) / 2;
                    maxSamples = maxSamples > 8 ? 8 : maxSamples;
                    nrSamples = (nrSamples > maxSamples) ? maxSamples : nrSamples;
                } catch (Exception ex) {
                    isSupfboMS = false;
                }
            } else if (GLContext.getCapabilities().GL_EXT_framebuffer_object) {
                isSupfboVer3 = 2;
                isSupfboMS = false;
            } else {
                Methods.JavaError(language.m.FBOError);
            }
        }
    }
}
