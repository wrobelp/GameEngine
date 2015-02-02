/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamecontent;

import collision.Figure;
import collision.OpticProperties;
import collision.Rectangle;
import game.gameobject.Player;
import game.place.cameras.Camera;
import game.place.Place;
import game.place.Light;
import sprites.Animation;
import engine.Drawer;
import engine.Methods;
import engine.Time;
import game.gameobject.inputs.InputKeyBoard;
import game.place.Map;
import game.place.WarpPoint;
import net.jodk.lang.FastMath;
import net.packets.MPlayerUpdate;
import net.packets.Update;
import org.lwjgl.input.Keyboard;
import static org.lwjgl.opengl.GL11.*;
import org.newdawn.slick.Color;
import sprites.SpriteSheet;

/**
 *
 * @author przemek
 */
public class MyPlayer extends Player {

    private int tempXSpeed, tempYSpeed;
    private float jumpDelta = 22.6f;  //TYLKO TYMCZASOWE!

    public MyPlayer(boolean first, String name) {
        super(name);
        this.first = first;
        if (first) {
            initializeControllerForFirst();
        } else {
            initializeController();
        }
    }

    private void initializeControllerForFirst() {
        controler = new MyController(this);
        controler.inputs[0] = new InputKeyBoard(Keyboard.KEY_UP);
        controler.inputs[1] = new InputKeyBoard(Keyboard.KEY_DOWN);
        controler.inputs[2] = new InputKeyBoard(Keyboard.KEY_RETURN);
        controler.inputs[3] = new InputKeyBoard(Keyboard.KEY_ESCAPE);
        controler.initialize();
    }

    private void initializeController() {
        controler = new MyController(this);
        controler.initialize();
    }

    @Override
    public void initialize(int startX, int startY, int width, int height, Place place, int x, int y) {
        scale = place.settings.SCALE;
        this.online = place.game.online;
        this.width = Methods.roundHalfUp(scale * width);
        this.height = Methods.roundHalfUp(scale * height);
        this.xStart = Methods.roundHalfUp(scale * startX);
        this.yStart = Methods.roundHalfUp(scale * startY);
        this.setResistance(2);
        this.emitter = true;
        initialize(name, Methods.roundHalfUp(scale * x), Methods.roundHalfUp(scale * y), place);
        this.sprite = place.getSpriteSheet("apple");
        this.light = new Light("light", 0.85f, 0.85f, 0.85f, Methods.roundHalfUp(scale * 1024), Methods.roundHalfUp(scale * 1024), place); // 0.85f - 0.75f daje fajne cienie 1.0f usuwa cały cień
        this.animation = new Animation((SpriteSheet) sprite, 200);
        emits = false;
        setCollision(Rectangle.create(this.width, this.height / 2, OpticProperties.NO_SHADOW, this));
    }

    @Override
    public void initialize(int startX, int startY, int width, int height, Place place) {
        this.online = place.game.online;
        scale = place.settings.SCALE;
        this.width = Methods.roundHalfUp(scale * width);
        this.height = Methods.roundHalfUp(scale * height);
        this.xStart = Methods.roundHalfUp(scale * startX);
        this.yStart = Methods.roundHalfUp(scale * startY);
        this.setResistance(2);
        this.emitter = true;
        this.place = place;
        this.sprite = place.getSpriteSheet("apple");
        this.light = new Light("light", 0.85f, 0.85f, 0.85f, Methods.roundHalfUp(scale * 1024), Methods.roundHalfUp(scale * 1024), place); // 0.85f - 0.75f daje fajne cienie 1.0f usuwa cały cień
        this.animation = new Animation((SpriteSheet) sprite, 200);
        emits = false;
        setCollision(Rectangle.create(this.width, this.height / 2, OpticProperties.NO_SHADOW, this));
    }

    @Override
    protected boolean isColided(int magX, int magY) {
        if (place != null) {
            return collision.isCollideSolid(getX() + magX, getY() + magY, map);
        }
        return false;
    }

    @Override
    protected void move(int xPos, int yPos) {
        setX(x + xPos);
        setY(y + yPos);
        if (camera != null) {
            camera.update();
        }
    }

    @Override
    protected void setPosition(int xPos, int yPos) {
        setX(xPos);
        setY(yPos);
        if (camera != null) {
            camera.update();
        }
    }

    @Override
    public void renderName(Place place, Camera cam) {
        place.renderMessage(0, cam.getXOffset() + getX(), (int) (cam.getYOffset() + getY() + sprite.getSy() + collision.getHeight() / 2 - jumpHeight),
                name, new Color(place.red, place.green, place.blue));
    }

    @Override
    public void render(int xEffect, int yEffect) {
        if (sprite != null) {
            glPushMatrix();
            glTranslatef(getX() + xEffect, getY() + yEffect, 0);
            Drawer.setColor(JUMP_SHADOW_COLOR);
            Drawer.drawElipse(0, 0, Methods.roundHalfUp((float) collision.getWidth() / 2), Methods.roundHalfUp((float) collision.getHeight() / 2), 15);
            Drawer.refreshColor();
            glTranslatef(0, (int) -jumpHeight, 0);
            getAnimation().render();
            glPopMatrix();
        }
    }

    @Override
    public void update() {
        if (jumping) {
            hop = false;
            jumpHeight = FastMath.abs(Methods.xRadius(jumpDelta * 4, 70));
            jumpDelta += Time.getDelta();
            if ((int) jumpDelta >= 68) {
                jumping = false;
                jumpDelta = 22.6f;
            }
        }
        tempXSpeed = (int) (xEnvironmentalSpeed + super.xSpeed);
        tempYSpeed = (int) (yEnvironmentalSpeed + super.ySpeed);
        moveIfPossible(tempXSpeed, tempYSpeed);
        for (WarpPoint warp : map.getWarps()) {
            if (warp.getCollision() != null && warp.getCollision().isCollideSingle(warp.getX(), warp.getY(), collision)) {
                warp.Warp(this);
                break;
            }
        }
        brakeOthers();
    }

    @Override
    public synchronized void sendUpdate(Place place) {
        if (jumping) {
            jumpHeight = FastMath.abs(Methods.xRadius(jumpDelta * 4, 70));
            jumpDelta += Time.getDelta();
            if ((int) jumpDelta >= 68) {
                jumping = false;
                jumpDelta = 22.5f;
            }

        }
        tempXSpeed = (int) (xEnvironmentalSpeed + super.xSpeed);
        tempYSpeed = (int) (yEnvironmentalSpeed + super.ySpeed);
        moveIfPossible(tempXSpeed, tempYSpeed);
        for (WarpPoint warp : map.getWarps()) {
            if (warp.getCollision() != null && warp.getCollision().isCollideSingle(warp.getX(), warp.getY(), collision)) {
                warp.Warp(this);
                break;
            }
        }
        brakeOthers();
        if (online.server != null) {
            online.server.sendUpdate(map.getId(), getX(), getY(), isEmits(), isHop());
        } else if (online.client != null) {
            online.client.sendPlayerUpdate(map.getId(), ID, getX(), getY(), isEmits(), isHop());
            online.pastPositions[online.pastPositionsNumber++].set(getX(), getY());
            if (online.pastPositionsNumber >= online.pastPositions.length) {
                online.pastPositionsNumber = 0;
            }
        } else {
            online.g.endGame();
        }
        hop = false;
    }

    @Override
    public synchronized void updateRest(Update up) {
        try {
            Map map = place.getMapById(((MPlayerUpdate) up).getMapId());
            if (map != null) {
                changeMap(map);
            }
            if (((MPlayerUpdate) up).isHop()) {
                setJumping(true);
            }
            setEmits(((MPlayerUpdate) up).isEmits());
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    @Override
    public synchronized void updateOnline() {
        try {
            if (jumping) {

                jumpHeight = FastMath.abs(Methods.xRadius(jumpDelta * 4, 70));
                jumpDelta += Time.getDelta();
                if ((int) jumpDelta == 68) {
                    jumping = false;
                    jumpDelta = 22.5f;
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    @Override
    public void renderShadowLit(int xEffect, int yEffect, float color, Figure f) {
        if (sprite != null) {
            glPushMatrix();
            glTranslatef((int) x + xEffect, (int) y + yEffect + (int) -jumpHeight, 0);
            Drawer.drawShapeInShade(animation, color);
            glPopMatrix();
        }
    }

    @Override
    public void renderShadow(int xEffect, int yEffect, Figure f) {
        if (sprite != null) {
            glPushMatrix();
            glTranslatef((int) x + xEffect, (int) y + yEffect + (int) -jumpHeight, 0);
            Drawer.drawShapeInBlack(animation);
            glPopMatrix();
        }
    }

    @Override
    public void renderShadowLit(int xEffect, int yEffect, float color, Figure f, int xStart, int xEnd) {
        if (sprite != null) {
            glPushMatrix();
            glTranslatef((int) x + xEffect, (int) y + yEffect + (int) -jumpHeight, 0);
            Drawer.drawShapePartInShade(animation, color, xStart, xEnd);
            glPopMatrix();
        }
    }

    @Override
    public void renderShadow(int xEffect, int yEffect, Figure f, int xStart, int xEnd) {
        if (sprite != null) {
            glPushMatrix();
            glTranslatef((int) x + xEffect, (int) y + yEffect + (int) -jumpHeight, 0);
            Drawer.drawShapePartInBlack(animation, xStart, xEnd);
            glPopMatrix();
        }
    }
}
