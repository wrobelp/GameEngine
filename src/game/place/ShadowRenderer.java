/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.place;

import collision.Area;
import collision.Figure;
import engine.Methods;
import engine.Point;
import game.gameobject.GameObject;
import game.place.cameras.Camera;
import java.util.Arrays;
import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL11.GL_DST_COLOR;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_SRC_COLOR;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_ZERO;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex2f;

/**
 *
 * @author przemek
 */
public class ShadowRenderer {

    private static final int w = Display.getWidth(), h = Display.getHeight();
    private static final Figure[] shades = new Figure[4096];
    private static GameObject emitter;
    private static final Point center = new Point(0, 0);
    private static final Point[] tempPoints = new Point[32], points = new Point[4], leftWallPoints = new Point[4], rightWallPoints = new Point[4];
    private static boolean isLeftWall, isRightWall, leftWallColor, rightWallColor;
    private static Figure shade, tmp, other, left, right;
    private static int nrShades, shDif, nrPoints, lightX, lightY, shP1, shP2, shX, shY, XL1, XL2, XR1, XR2, YL, YR;
    private static double angle, temp, al1, bl1, al2, bl2, XOL, XOR, YOL, YOL2, YOR, YOR2;
    private static float shadeColor;

    public static void preRendLight(Place place, int l) {
        emitter = place.visibleLights[l];
        findShades(emitter, place);
        emitter.getLight().fbo.activate();
        clearFBO(1);
        glBlendFunc(GL_SRC_COLOR, GL_ONE_MINUS_SRC_ALPHA);
        for (int f = 0; f < nrShades; f++) {    //iteracja po Shades - tych co dają cień
            shade = shades[f];
            if (shade != emitter.getCollision()) {
                if (shade.canGiveShadow()) {
                    calculateShadow(emitter, shade);
                    drawShadow(emitter);
                    calculateWalls(shade, emitter);
                    drawWalls(emitter);
                }
                shadeColor = (emitter.getY() - shade.getEndY()) / (float) ((emitter.getHeight()) / 2);
                shade.getOwner().renderShadow(emitter.getLight().getSX() / 2 - (emitter.getX()),
                        emitter.getLight().getSY() / 2 - (emitter.getY()) + h - emitter.getLight().getSY(), shade.canBeLit() && emitter.getY() >= shade.getEndY(), shadeColor, shade);
            } else {
                emitter.renderShadow(-emitter.getX() + emitter.getLight().getSX() / 2, -emitter.getY() + emitter.getLight().getSY() / 2 + h - emitter.getLight().getSY(), true, 1, shade);
            }
        }
        glColor3f(1f, 1f, 1f);
        glBlendFunc(GL_DST_COLOR, GL_ONE_MINUS_SRC_ALPHA);
        emitter.getLight().render(h - emitter.getLight().getSY());
        emitter.getLight().fbo.deactivate();
    }

    private static void findShades(GameObject src, Place place) {
        // Powinno sortować według wysoskości - najpierw te, które są najwyżej na planszy, a później coraz niższe,
        // obiekty tej samej wysokości powinny być renderowane w kolejności od najdalszych od źródła, do najbliższych.
        nrShades = 0;
        for (Area a : place.areas) { //iteracja po Shades - tych co dają cień
            if (!a.isBorder()) {
                if (a.isWhole) {
                    tmp = a.getCollision();
                    if (tmp != null && (Math.abs(tmp.getCentralY() - src.getY()) <= (src.getLight().getSY() >> 1) + (tmp.getHeight() >> 1))
                            && (Math.abs(tmp.getCentralX() - src.getX()) <= (src.getLight().getSX() >> 1) + (tmp.getWidth() >> 1))) {
                        shades[nrShades++] = tmp;
                        tmp.setDistFromLight((src.getCollision() == tmp) ? -1 : Math.abs(src.getX() - tmp.getCentralX()));
                    }
                    for (Figure tmp : a.getParts()) {
                        if (tmp != null && !tmp.canBeLit() && (Math.abs(tmp.getCentralY() - src.getY()) <= (src.getLight().getSY() >> 1) + (tmp.getHeight() >> 1))
                                && (Math.abs(tmp.getCentralX() - src.getX()) <= (src.getLight().getSX() >> 1) + (tmp.getWidth() >> 1))) {
                            shades[nrShades++] = tmp;
                            tmp.setDistFromLight((src.getCollision() == tmp) ? -1 : Math.abs(src.getX() - tmp.getCentralX()));
                        }
                    }
                } else {
                    for (Figure tmp : a.getParts()) {
                        if (tmp != null && (Math.abs(tmp.getCentralY() - src.getY()) <= (src.getLight().getSY() >> 1) + (tmp.getHeight() >> 1))
                                && (Math.abs(tmp.getCentralX() - src.getX()) <= (src.getLight().getSX() >> 1) + (tmp.getWidth() >> 1))) {
                            shades[nrShades++] = tmp;
                            tmp.setDistFromLight((src.getCollision() == tmp) ? -1 : Math.abs(src.getX() - tmp.getCentralX()));
                        }
                    }
                }
            }
        }

//        for (GameObject tile : place.foregroundTiles) {   // FGTiles muszą mieć Collision
//            tmp = tile.getCollision();
//            if ((Math.abs(tmp.getOwner().getY() - src.getY()) <= (src.getLight().getSY() >> 1) + (tmp.getOwner().getHeight() >> 1))
//                    && (Math.abs(tmp.getOwner().getX() - src.getX()) <= (src.getLight().getSX() >> 1) + (tmp.getOwner().getWidth() >> 1))) {
//                shades[nrShades++] = tmp;
//                tmp.setDistFromLight((src.getCollision() == tmp) ? -1 : Math.abs(src.getX() - tmp.getCentralX()));
//            }
//        }
        for (GameObject go : place.depthObj) {   // FGTiles muszą mieć Collision
            tmp = go.getCollision();
            if ((Math.abs(tmp.getOwner().getY() - src.getY()) <= (src.getLight().getSY() >> 1) + (tmp.getOwner().getHeight() >> 1))
                    && (Math.abs(tmp.getOwner().getX() - src.getX()) <= (src.getLight().getSX() >> 1) + (tmp.getOwner().getWidth() >> 1))) {
                shades[nrShades++] = tmp;
                tmp.setDistFromLight((src.getCollision() == tmp) ? -1 : Math.abs(src.getX() - tmp.getCentralX()));
            }
        }
        Arrays.sort(shades, 0, nrShades);
//        System.out.println("Posortowana: ");
//        for (int i = 0; i < nrShades; i++) {
//            System.out.println("Y: " + shades[i].getEndY() + " X: " + shades[i].getDistFromLight() + " - Klasa:" + shades[i].getOwner().getClass());
//        }
//        System.out.print("\n");
    }

    private static void calculateShadow(GameObject src, Figure thisShade) {
        findPoints(src, thisShade);
        findLeftSideOfShadow();
        findRightSideOfShadow();
    }

    private static void findPoints(GameObject src, Figure thisShade) {
        center.set(src.getX(), src.getY());
        nrPoints = thisShade.listPoints().length;
        System.arraycopy(thisShade.listPoints(), 0, tempPoints, 0, nrPoints);
        angle = 0;
        for (int p = 0; p < nrPoints; p++) {
            for (int s = p + 1; s < nrPoints; s++) {
                temp = Methods.ThreePointAngle(tempPoints[p].getX(), tempPoints[p].getY(), tempPoints[s].getX(), tempPoints[s].getY(), center.getX(), center.getY());
                if (temp > angle) {
                    angle = temp;
                    shP1 = p;
                    shP2 = s;
                }
            }
        }
        points[0] = tempPoints[shP1];
        points[1] = tempPoints[shP2];
        shDif = (src.getLight().getSX() + src.getLight().getSY()) << 2;
    }

    private static void findLeftSideOfShadow() {
        if (points[0].getX() == center.getX()) {
            points[2].set(points[0].getX(), points[0].getY() + (points[0].getY() > center.getY() ? shDif : -shDif));
        } else if (points[0].getY() == center.getY()) {
            points[2].set(points[0].getX() + (points[0].getX() > center.getX() ? shDif : -shDif), points[0].getY());
        } else {
            al1 = (center.getY() - points[0].getY()) / (double) (center.getX() - points[0].getX());
            bl1 = points[0].getY() - al1 * points[0].getX();
            if (al1 > 0) {
                shX = points[0].getX() + (points[0].getY() > center.getY() ? shDif : -shDif);
                shY = (int) (al1 * shX + bl1);
            } else if (al1 < 0) {
                shX = points[0].getX() + (points[0].getY() > center.getY() ? -shDif : shDif);
                shY = (int) (al1 * shX + bl1);
            } else {
                shX = points[0].getX();
                shY = points[0].getY() + (points[0].getY() > center.getY() ? shDif : -shDif);
            }
            points[2].set(shX, shY);
        }
    }

    private static void findRightSideOfShadow() {
        if (points[1].getX() == center.getX()) {
            points[3].set(points[1].getX(), points[1].getY() + (points[1].getY() > center.getY() ? shDif : -shDif));
        } else if (points[1].getY() == center.getY()) {
            points[3].set(points[1].getX() + (points[1].getX() > center.getX() ? shDif : -shDif), points[1].getY());
        } else {
            al2 = (center.getY() - points[1].getY()) / (double) (center.getX() - points[1].getX());
            bl2 = points[1].getY() - al2 * points[1].getX();
            if (al2 > 0) {
                shX = points[1].getX() + (points[1].getY() > center.getY() ? shDif : -shDif);
                shY = (int) (al2 * shX + bl2);
            } else if (al2 < 0) {
                shX = points[1].getX() + (points[1].getY() > center.getY() ? -shDif : shDif);
                shY = (int) (al2 * shX + bl2);
            } else {
                shX = points[1].getX();
                shY = points[1].getY() + (points[1].getY() > center.getY() ? shDif : -shDif);
            }
            points[3].set(shX, shY);
        }
    }

    private static void calculateWalls(Figure f, GameObject src) {
        findWalls(f, src);
        calculateLeftWall(f, src);
        calculateRightWall(f, src);
    }

    private static void findWalls(Figure f, GameObject src) {
        left = right = null;
        for (int i = 0; i < nrShades; i++) {
            other = shades[i];
            if (f.getY() < src.getY() && other.canGiveShadow() && other != f && other.getEndY() < f.getEndY() && other.getEndY() < src.getY()) {
                XOL = ((other.getEndY() - bl1) / al1);
                XOR = ((other.getEndY() - bl2) / al2);

                YOL = (al1 * other.getX() + bl1);
                YOL2 = (al1 * other.getEndX() + bl1);
                YOR = (al2 * other.getX() + bl2);
                YOR2 = (al2 * other.getEndX() + bl2);

                if (points[0].getY() > points[2].getY() && points[0].getY() > other.getEndY() && ((XOL >= other.getX() && XOL <= other.getEndX()) || (YOL >= other.getY() && YOL <= other.getEndY()) || (YOL2 >= other.getY() && YOL2 <= other.getEndY()))) {
                    left = (left != null) ? (Math.abs(f.getY() - other.getY()) < Math.abs(f.getY() - left.getY())) ? other : left : other;
                }
                if (points[1].getY() > points[3].getY() && points[1].getY() > other.getEndY() && ((XOR >= other.getX() && XOR <= other.getEndX()) || (YOR >= other.getY() && YOR <= other.getEndY()) || (YOR2 >= other.getY() && YOR2 <= other.getEndY()))) {
                    right = (right != null) ? (Math.abs(f.getY() - other.getY()) < Math.abs(f.getY() - right.getY())) ? other : right : other;
                }
            }
        }
    }

    private static void calculateLeftWall(Figure f, GameObject src) {
        if (left != null) {    //czy lewy koniec pada na ścianę?
            YL = left.getEndY();
            XL1 = Methods.RoundHU((YL - bl1) / al1);
            XR1 = Methods.RoundHU((YL - bl2) / al2);
            if (Math.abs(al1) > 0 && XL1 >= left.getX() && XL1 <= left.getEndX() && XL1 < f.getX()) { //dodaj światło
                XL2 = al1 > 0 ? left.getX() : left.getEndX();
                leftWallPoints[0].set(XL1, YL - left.getHeight());
                leftWallPoints[1].set(XL1, YL);
                leftWallPoints[2].set(XL2, YL);
                leftWallPoints[3].set(XL2, YL - left.getHeight());
                leftWallColor = isLeftWall = true;
//                System.out.println("Left Light");
            } else if (XL1 != f.getX() && XL1 >= left.getX() && XL1 <= left.getEndX()) { //dodaj cień
                XL2 = al1 > 0 ? left.getX() : left.getEndX();
                leftWallPoints[0].set(XL1, YL);
                leftWallPoints[1].set(XL1, YL - left.getHeight());
                leftWallPoints[2].set(XL2, YL - left.getHeight());
                leftWallPoints[3].set(XL2, YL);
                leftWallColor = false;
                isLeftWall = true;
//                System.out.println("Left Shade");
            } else {    // rysuj zaciemniony
                YOL = Methods.RoundHU(al1 * left.getX() + bl1);
                YOL2 = Methods.RoundHU(al1 * left.getEndX() + bl1);
                if ((XL1 != f.getX() && XL1 != left.getX() && XL1 != left.getEndX() && src.getX() != f.getEndX() && src.getX() != f.getX()) || (YOL > left.getY() && YOL < left.getEndY()) || (YOL2 > left.getY() && YOL2 < left.getEndY())) {
                    left.getOwner().renderShadow(src.getLight().getSX() / 2 - (src.getX()),
                            src.getLight().getSY() / 2 - (src.getY()) + h - src.getLight().getSY(), Math.abs(al1) >= 0 && XL1 < f.getX(), 1, left);
//                    System.out.println("Left Dark");
                }
            }
        }
    }

    private static void calculateRightWall(Figure f, GameObject src) {
        if (right != null) {     //czy prawy koniec pada na ścianę?
            YR = right.getEndY();
            XR1 = Methods.RoundHU((YR - bl2) / al2);
            XL1 = Methods.RoundHU((YR - bl1) / al1);
            if (Math.abs(al2) > 0 && XR1 >= right.getX() && XR1 <= right.getEndX() && XR1 > f.getEndX()) {     // dodaj światło
                XR2 = al2 > 0 ? right.getX() : right.getEndX();
                rightWallPoints[0].set(XR1, YR - right.getHeight());
                rightWallPoints[1].set(XR1, YR);
                rightWallPoints[2].set(XR2, YR);
                rightWallPoints[3].set(XR2, YR - right.getHeight());
                isRightWall = rightWallColor = true;
//                System.out.println("Right Light");
            } else if (XR1 != f.getEndX() && XR1 >= right.getX() && XR1 <= right.getEndX()) { //dodaj cień
                XR2 = al2 > 0 ? right.getX() : right.getEndX();
                rightWallPoints[0].set(XR1, YR);
                rightWallPoints[1].set(XR1, YR - right.getHeight());
                rightWallPoints[2].set(XR2, YR - right.getHeight());
                rightWallPoints[3].set(XR2, YR);
                rightWallColor = false;
                isRightWall = true;
//                System.out.println("Right Shade");
            } else {   // rysuj zaciemniony
                YOR = Methods.RoundHU(al2 * right.getX() + bl2);
                YOR2 = Methods.RoundHU(al2 * right.getEndX() + bl2);
                if ((XR1 != f.getEndX() && XR1 != right.getX() && XR1 != right.getEndX() && src.getX() != f.getEndX() && src.getX() != f.getX()) || (YOR > right.getY() && YOR < right.getEndY()) || (YOR2 > right.getY() && YOR2 < right.getEndY())) {
                    right.getOwner().renderShadow(src.getLight().getSX() / 2 - (src.getX()),
                            src.getLight().getSY() / 2 - (src.getY()) + h - src.getLight().getSY(), Math.abs(al2) >= 0 && XR1 > f.getEndX(), 1, right);
//                    System.out.println("Right Dark");
                }
            }
        }
    }

    private static void drawWalls(GameObject emitter) {
        int lX = emitter.getLight().getSX();
        int lY = emitter.getLight().getSY();
        glDisable(GL_TEXTURE_2D);
        if (isRightWall) {
            if (rightWallColor) {
                glColor3f(1, 1, 1);
            } else {
                glColor3f(0, 0, 0);
            }
            glPushMatrix();
            glTranslatef(lX / 2 - emitter.getX(), lY / 2 - emitter.getY() + h - lY, 0);
            glBegin(GL_QUADS);
            glVertex2f(rightWallPoints[0].getX(), rightWallPoints[0].getY());
            glVertex2f(rightWallPoints[1].getX(), rightWallPoints[1].getY());
            glVertex2f(rightWallPoints[2].getX(), rightWallPoints[2].getY());
            glVertex2f(rightWallPoints[3].getX(), rightWallPoints[3].getY());
            glEnd();
            glPopMatrix();
            isRightWall = false;
        }
        if (isLeftWall) {
            if (leftWallColor) {
                glColor3f(1, 1, 1);
            } else {
                glColor3f(0, 0, 0);
            }
            glPushMatrix();
            glTranslatef(lX / 2 - emitter.getX(), lY / 2 - emitter.getY() + h - lY, 0);
            glBegin(GL_QUADS);
            glVertex2f(leftWallPoints[0].getX(), leftWallPoints[0].getY());
            glVertex2f(leftWallPoints[1].getX(), leftWallPoints[1].getY());
            glVertex2f(leftWallPoints[2].getX(), leftWallPoints[2].getY());
            glVertex2f(leftWallPoints[3].getX(), leftWallPoints[3].getY());
            glEnd();
            glPopMatrix();
            isLeftWall = false;
        }
        glEnable(GL_TEXTURE_2D);
    }

    private static void drawShadow(GameObject emitter) {
        lightY = emitter.getLight().getSY();
        glDisable(GL_TEXTURE_2D);
        glColor3f(0, 0, 0);
        glPushMatrix();
        glTranslatef(emitter.getLight().getSX() / 2 - emitter.getX(), lightY / 2 - emitter.getY() + h - lightY, 0);
        glBegin(GL_QUADS);
        glVertex2f(points[0].getX(), points[0].getY());
        glVertex2f(points[2].getX(), points[2].getY());
        glVertex2f(points[3].getX(), points[3].getY());
        glVertex2f(points[1].getX(), points[1].getY());
        glEnd();
        glPopMatrix();
        glEnable(GL_TEXTURE_2D);
    }

    public static void clearFBO(float color) {
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_ONE, GL_ZERO);
        glColor3f(color, color, color);
        glBegin(GL_QUADS);
        glVertex2f(0, 0);
        glVertex2f(0, h);
        glVertex2f(w, h);
        glVertex2f(w, 0);
        glEnd();
        glEnable(GL_TEXTURE_2D);
    }

    public static void drawLight(int textureHandle, GameObject emitter, Camera cam) {
        lightX = emitter.getLight().getSX();
        lightY = emitter.getLight().getSY();
        glPushMatrix();
        glTranslatef(emitter.getX() - emitter.getLight().getSX() / 2 + cam.getXOffEffect(), emitter.getY() - emitter.getLight().getSY() / 2 + cam.getYOffEffect(), 0);
        glBindTexture(GL_TEXTURE_2D, textureHandle);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 1);
        glVertex2f(0, 0);
        glTexCoord2f(1, 1);
        glVertex2f(lightX, 0);
        glTexCoord2f(1, 0);
        glVertex2f(lightX, lightY);
        glTexCoord2f(0, 0);
        glVertex2f(0, lightY);
        glEnd();
        glPopMatrix();
    }

    public static void initVariables(Place place) {
        for (int i = 0; i < 4; i++) {
            points[i] = new Point(0, 0);
            tempPoints[i] = new Point(0, 0);
            leftWallPoints[i] = new Point(0, 0);
            rightWallPoints[i] = new Point(0, 0);
        }
    }

    private ShadowRenderer() {
    }
}
