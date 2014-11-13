/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package engine;

import game.place.Place;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_COMBINE;
import static org.lwjgl.opengl.GL13.GL_COMBINE_RGB;
import static org.lwjgl.opengl.GL13.GL_OPERAND0_RGB;
import static org.lwjgl.opengl.GL13.GL_OPERAND1_RGB;
import static org.lwjgl.opengl.GL13.GL_PREVIOUS;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_SRC0_RGB;
import static org.lwjgl.opengl.GL15.GL_SRC1_RGB;
import org.newdawn.slick.Color;
import sprites.Sprite;

/**
 *
 * @author Wojtek
 */
public class Drawer {

    private static final int white = glGenTextures();

    public static void drawRectangle(int xs, int ys, int w, int h) {
        glTranslatef(xs, ys, 0);
        glDisable(GL_TEXTURE_2D);
        glBegin(GL_QUADS);
        glVertex2f(0, 0);
        glVertex2f(0, h);
        glVertex2f(w, h);
        glVertex2f(w, 0);
        glEnd();
        glEnable(GL_TEXTURE_2D);
    }

    public static void drawCircle(int xs, int ys, int r, int steps) {   //dla małych ilości kroków wychodzą figury foremne (trójkąt, czworokąt, itp.)
        glTranslatef(xs, ys, 0);
        glDisable(GL_TEXTURE_2D);
        glBegin(GL_TRIANGLE_FAN);
        int step = 360 / steps;
        glVertex2f(0, 0);
        for (int i = 0; i <= 360; i += step) {
            glVertex2f((float) Methods.xRadius(i, r), (float) Methods.yRadius(i, r));
        }
        glVertex2f(r, 0);
        glEnd();
        glEnable(GL_TEXTURE_2D);
    }

    public static void drawElipse(int xs, int ys, int rx, int ry, int steps) {   //dla małych ilości kroków wychodzą figury foremne (trójkąt, czworokąt, itp.)
        glTranslatef(xs, ys, 0);
        glDisable(GL_TEXTURE_2D);
        glBegin(GL_TRIANGLE_FAN);
        int step = 360 / steps;
        glVertex2f(0, 0);
        for (int i = 0; i <= 360; i += step) {
            glVertex2f((float) Methods.xRadius(i, rx), (float) Methods.yRadius(i, ry));
        }
        glVertex2f(rx, 0);
        glEnd();
        glEnable(GL_TEXTURE_2D);
    }

    public static void setColor(Color c) {
        glColor4f(c.r, c.g, c.b, c.a);
    }

    public static void refreshColor(Place p) {
        glColor4f(p.r, p.g, p.b, 1.0f);
    }

    public static void refreshBlending() {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public static void refresh(Place p) {
        refreshColor(p);
        refreshBlending();
    }

    public static void drawShapeInColor(Sprite sprite, float r, float g, float b, float alpha) {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glColor4f(r, g, b, alpha);
        glActiveTexture(white);
        sprite.getTex().bind();
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE);
        glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_RGB, GL_REPLACE);
        glTexEnvi(GL_TEXTURE_ENV, GL_SRC0_RGB, GL_PREVIOUS);
        glTexEnvi(GL_TEXTURE_ENV, GL_SRC1_RGB, GL_TEXTURE);
        glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND0_RGB, GL_SRC_COLOR);
        glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND1_RGB, GL_SRC_COLOR);
        sprite.renderNotBind();
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
        glColor4f(1, 1, 1, 1);
    }

    public static void drawShapeInColor(Animation anim, float r, float g, float b, float alpha) {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glColor4f(r, g, b, alpha);
        glActiveTexture(white);
        anim.getSprite().getTex().bind();
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE);
        glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_RGB, GL_REPLACE);
        glTexEnvi(GL_TEXTURE_ENV, GL_SRC0_RGB, GL_PREVIOUS);
        glTexEnvi(GL_TEXTURE_ENV, GL_SRC1_RGB, GL_TEXTURE);
        glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND0_RGB, GL_SRC_COLOR);
        glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND1_RGB, GL_SRC_COLOR);
        anim.renderNotBind(anim.getOwner().isAnimate());
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
        glColor4f(1, 1, 1, 1);
    }

    public static void drawShapeInBlack(Sprite sprite) {
        sprite.getTex().bind();
        glBlendFunc(GL_ZERO, GL_ONE_MINUS_SRC_ALPHA);
        sprite.renderNotBind();
    }

    public static void drawShapeInBlack(Animation anim) {
        anim.getSprite().getTex().bind();
        glBlendFunc(GL_ZERO, GL_ONE_MINUS_SRC_ALPHA);
        anim.render(anim.getOwner().isAnimate());
    }
}
