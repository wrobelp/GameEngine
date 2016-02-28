/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package engine.utilities;

import game.ScreenPlace;
import game.text.FontHandler;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;
import sprites.Appearance;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.GL_SRC0_RGB;
import static org.lwjgl.opengl.GL15.GL_SRC1_RGB;

/**
 * @author Wojtek
 */
public class Drawer {

    private static final Texture font = loadFontTexture();
    public static int displayWidth, displayHeight;
    private static float xCurrent, yCurrent;
    private static Color currentColor = Color.white;

    private static Texture loadFontTexture() {
        try {
            InputStream stream = ResourceLoader.getResourceAsStream("/res/textures/white.png");
            Texture t = TextureLoader.getTexture("png", stream, GL_LINEAR);
            stream.close();
            return t;
        } catch (IOException exception) {
            Logger.getLogger(ScreenPlace.class.getName()).log(Level.SEVERE, null, exception);
            ErrorHandler.javaError(exception.getMessage());
        }
        return null;
    }

    public static void setUpDisplay() {
        displayWidth = Display.getWidth();
        displayHeight = Display.getHeight();
    }

    public static void bindFontTexture() {
        font.bind();
    }

    public static void clearScreen(float color) {
        glClearColor(color, color, color, 0f);
        glClear(GL_COLOR_BUFFER_BIT);
    }

    public static void refreshForRegularDrawing() {
        refreshColor();
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        if (Display.isFullscreen()) {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        } else {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        }
    }

    public static void refreshColor() {
        glColor4f(currentColor.r, currentColor.g, currentColor.b, 1.0f);
    }

    public static void setColorAlpha(float alpha) {
        glColor4f(currentColor.r, currentColor.g, currentColor.b, alpha);
    }

    public static Color getCurrentColor() {
        return currentColor;
    }

    public static void setCurrentColor(Color color) {
        Drawer.currentColor = color;
    }

    public static Color setPercentToRGBColor(int percent, Color color) {
        if (percent == 100) {
            percent = 99;
        }
        float r, g;
        if (percent < 50) {
            r = (percent / 50f);
            g = 1f;
        } else {
            r = 1f;
            g = ((50f - percent % 50f) / 50f);
        }
        color.r = r;
        color.g = g;
        color.b = 0f;
        return color;
    }

    public static void setCentralPoint() {  //Miejsce do którego można wrócić
        xCurrent = 0;
        yCurrent = 0;
    }

    public static void translate(float x, float y) {
        xCurrent += x;
        yCurrent += y;
        glTranslatef((int) x, (int) y, 0f);
    }

    public static void returnToCentralPoint() {
        glTranslatef(-xCurrent, -yCurrent, 0f);
        setCentralPoint();
    }

    public static void drawRectangleInShade(int xStart, int yStart, int width, int height, float color) {
        glColor3f(color, color, color);
        drawRectangle(xStart, yStart, width, height);
        glColor3f(1f, 1f, 1f);
    }

    public static void drawRectangleInBlack(int xStart, int yStart, int width, int height) {
        glColor3f(0f, 0f, 0f);
        drawRectangle(xStart, yStart, width, height);
        glColor3f(1f, 1f, 1f);
    }

    public static void drawTextureTriangle(int xA, int yA, int xB, int yB, int xC, int yC) {
        glBegin(GL_TRIANGLES);
        glTexCoord2f(1, 1);
        glVertex2f(xA, yA);
        glTexCoord2f(0, 1);
        glVertex2f(xB, yB);
        glTexCoord2f(1, 0);
        glVertex2f(xC, yC);
        glEnd();
    }

    public static void drawTriangle(int xA, int yA, int xB, int yB, int xC, int yC) {
        glDisable(GL_TEXTURE_2D);
        glBegin(GL_TRIANGLES);
        glVertex2f(xA, yA);
        glVertex2f(xB, yB);
        glVertex2f(xC, yC);
        glEnd();
        glEnable(GL_TEXTURE_2D);
    }

    public static void drawRectangle(int xStart, int yStart, int width, int height) {
        translate(xStart, yStart);
        glDisable(GL_TEXTURE_2D);
        glBegin(GL_QUADS);
        glVertex2f(0, 0);
        glVertex2f(0, height);
        glVertex2f(width, height);
        glVertex2f(width, 0);
        glEnd();
        glEnable(GL_TEXTURE_2D);
    }

    public static void drawRectangleBorder(int xStart, int yStart, int width, int height) {
        translate(xStart, yStart);
        glDisable(GL_TEXTURE_2D);
        glBegin(GL_LINE_LOOP);
        glVertex2f(0, 0);
        glVertex2f(0, height);
        glVertex2f(width, height);
        glVertex2f(width, 0);
        glEnd();
        glEnable(GL_TEXTURE_2D);
    }

    public static void drawTextureQuad(int xA, int yA, int xB, int yB, int xC, int yC, int xD, int yD) {
        glBegin(GL_QUADS);
        glTexCoord2f(1, 0);
        glVertex2f(xA, yA);
        glTexCoord2f(0, 0);
        glVertex2f(xB, yB);
        glTexCoord2f(0, 1);
        glVertex2f(xC, yC);
        glTexCoord2f(1, 1);
        glVertex2f(xD, yD);
        glEnd();
    }

    public static void drawQuad(int xA, int yA, int xB, int yB, int xC, int yC, int xD, int yD) {
        glDisable(GL_TEXTURE_2D);
        glBegin(GL_QUADS);
        glVertex2f(xA, yA);
        glVertex2f(xB, yB);
        glVertex2f(xC, yC);
        glVertex2f(xD, yD);
        glEnd();
        glEnable(GL_TEXTURE_2D);
    }

    public static void drawCircle(int xStart, int yStart, int radius, int precision) {
        drawEllipse(xStart, yStart, radius, radius, precision);
    }

    public static void drawEllipse(int xStart, int yStart, int xRadius, int yRadius, int precision) {  //Zbyt mała precyzja tworzy figury foremne
        translate(xStart, yStart);
        glDisable(GL_TEXTURE_2D);
        glBegin(GL_TRIANGLE_FAN);
        glVertex2f(0, 0);
        int step = 360 / precision;
        for (int i = 0; i <= 360; i += step) {
            glVertex2f((float) Methods.xRadius(i, xRadius), (float) Methods.yRadius(i, yRadius));
        }
        glVertex2f(xRadius, 0);
        glEnd();
        glEnable(GL_TEXTURE_2D);
    }

    public static void drawCircleSector(int xStart, int yStart, int radius, int startAngle, int endAngle, int precision) {
        drawEllipseSector(xStart, yStart, radius, radius, startAngle, endAngle, precision);
    }

    public static void drawEllipseSector(int xStart, int yStart, int xRadius, int yRadius, int startAngle, int endAngle, int precision) {
        // Zbyt mała precyzja tworzy figury foremne
        if (endAngle < startAngle) {
            endAngle += 360;
        }
        translate(xStart, yStart);
        glDisable(GL_TEXTURE_2D);
        glBegin(GL_TRIANGLE_FAN);
        glVertex2f(0, 0);
        int step = (endAngle - startAngle) / precision;
        for (int i = startAngle; i <= endAngle; i += step) {
            glVertex2f((float) Methods.xRadius(i, xRadius), (float) Methods.yRadius(i, yRadius));
        }
        glEnd();
        glEnable(GL_TEXTURE_2D);
    }

    public static void drawEllipseBow(int xStart, int yStart, int xRadius, int yRadius, int width, int startAngle, int endAngle, int precision) {
        if (startAngle > endAngle) {
            int tmp = startAngle;
            startAngle = endAngle;
            endAngle = tmp;
        }
        translate(xStart, yStart);
        glDisable(GL_TEXTURE_2D);
        glBegin(GL_QUAD_STRIP);
        int step = (endAngle - startAngle) / precision;
        if (step <= 0) {
            step = 1;
        }
        for (int i = startAngle; i < endAngle; i += step) {
            glVertex2f((float) Methods.xRadius(i, xRadius), (float) Methods.yRadius(i, yRadius));
            glVertex2f((float) Methods.xRadius(i, xRadius - width), (float) Methods.yRadius(i, yRadius - width));
        }
        glVertex2f((float) Methods.xRadius(endAngle, xRadius), (float) Methods.yRadius(endAngle, yRadius));
        glVertex2f((float) Methods.xRadius(endAngle, xRadius - width), (float) Methods.yRadius(endAngle, yRadius - width));
        glEnd();
        glEnable(GL_TEXTURE_2D);
    }

    public static void drawBow(int xStart, int yStart, int radius, int width, int startAngle, int endAngle, int precision) {
        if (startAngle > endAngle) {
            int tmp = startAngle;
            startAngle = endAngle;
            endAngle = tmp;
        }
        translate(xStart, yStart);
        glDisable(GL_TEXTURE_2D);
        glBegin(GL_QUAD_STRIP);
        int step = (endAngle - startAngle) / precision;
        for (int i = startAngle; i < endAngle; i += step) {
            glVertex2f((float) Methods.xRadius(i, radius), (float) Methods.yRadius(i, radius));
            glVertex2f((float) Methods.xRadius(i, radius - width), (float) Methods.yRadius(i, radius - width));
        }
        glVertex2f((float) Methods.xRadius(endAngle, radius), (float) Methods.yRadius(endAngle, radius));
        glVertex2f((float) Methods.xRadius(endAngle, radius - width), (float) Methods.yRadius(endAngle, radius - width));
        glEnd();
        glEnable(GL_TEXTURE_2D);
    }

    public static void drawRing(int xStart, int yStart, int radius, int width, int precision) {
        translate(xStart, yStart);
        glDisable(GL_TEXTURE_2D);
        glBegin(GL_QUAD_STRIP);
        int step = 360 / precision;
        for (int i = 0; i < 360; i += step) {
            glVertex2f((float) Methods.xRadius(i, radius), (float) Methods.yRadius(i, radius));
            glVertex2f((float) Methods.xRadius(i, radius - width), (float) Methods.yRadius(i, radius - width));
        }
        glVertex2f((float) Methods.xRadius(360, radius), (float) Methods.yRadius(360, radius));
        glVertex2f((float) Methods.xRadius(360, radius - width), (float) Methods.yRadius(360, radius - width));
        glEnd();
        glEnable(GL_TEXTURE_2D);
    }

    public static void drawLineWidth(int xStart, int yStart, int xDelta, int yDelta, int width) {
        translate(xStart, yStart);
        glDisable(GL_TEXTURE_2D);
        glBegin(GL_QUADS);
        int angle = (int) Methods.pointAngleClockwise(xStart, yStart, xStart + xDelta, yStart + yDelta) + 90;
        int xWidth = (int) Methods.xRadius(angle, width / 2);
        int yWidth = (int) Methods.yRadius(angle, width / 2);
        glVertex2f(xWidth, yWidth);
        glVertex2f(-xWidth, -yWidth);
        glVertex2f(xDelta - xWidth, yDelta - yWidth);
        glVertex2f(xDelta + xWidth, yDelta + yWidth);
        glEnd();
        glEnable(GL_TEXTURE_2D);
    }

    public static void drawLine(int xStart, int yStart, int xDelta, int yDelta) {
        drawLineWidth(xStart, yStart, xDelta, yDelta, 1);
    }

    public static void drawShapeInShade(Appearance appearance, float color) {
        prepareDrawingShape(color);
        appearance.bindCheck();
        changeShapeToColor();
        appearance.render();
        cleanAfterDrawingShape();
    }

    public static void drawShapePartInShade(Appearance appearance, float color, int partXStart, int partXEnd) {
        prepareDrawingShape(color);
        appearance.bindCheck();
        changeShapeToColor();
        appearance.renderPart(partXStart, partXEnd);
        cleanAfterDrawingShape();
    }

    public static void drawShapeInBlack(Appearance appearance) {
        appearance.bindCheck();
        glBlendFunc(GL_ZERO, GL_ONE_MINUS_SRC_ALPHA);
        appearance.render();
        setBlendAttributesForShadows();
    }

    public static void drawShapePartInBlack(Appearance appearance, int partXStart, int partXEnd) {
        appearance.bindCheck();
        glBlendFunc(GL_ZERO, GL_ONE_MINUS_SRC_ALPHA);
        appearance.renderPart(partXStart, partXEnd);
        setBlendAttributesForShadows();
    }

    private static void prepareDrawingShape(float color) {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glColor3f(color * color, color * color, color * color);
        glBindTexture(GL_TEXTURE_2D, font.getTextureID());
    }

    private static void cleanAfterDrawingShape() {
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
        setBlendAttributesForShadows();
        glColor3f(1, 1, 1);
    }

    private static void changeShapeToColor() {
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE);
        glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_RGB, GL_REPLACE);
        glTexEnvi(GL_TEXTURE_ENV, GL_SRC0_RGB, GL_PREVIOUS);
        glTexEnvi(GL_TEXTURE_ENV, GL_SRC1_RGB, GL_TEXTURE);
        glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND0_RGB, GL_SRC_COLOR);
        glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND1_RGB, GL_SRC_COLOR);
    }

    private static void setBlendAttributesForShadows() {
        glBlendFunc(GL_SRC_COLOR, GL_ONE_MINUS_SRC_ALPHA);
    }

    public static void setColorStatic(Color color) {
        glColor4f(color.r, color.g, color.b, color.a);
    }

    public static void setColorBlended(Color color) {
        glColor4f(color.r * currentColor.r, color.g * currentColor.g, color.b * currentColor.b, color.a);
    }

    public static void renderStringCentered(String message, double x, double y, FontHandler font, Color color) {
        Drawer.bindFontTexture();
        font.drawLine(message, (float) (x - font.getWidth(message) / 2),
                (float) (y - (4 * font.getHeight()) / 3), color);
    }

    public static void renderString(String message, double x, double y, FontHandler font, Color color) {
        Drawer.bindFontTexture();
        font.drawLine(message, (int) x, (int) y, color);
    }
}
