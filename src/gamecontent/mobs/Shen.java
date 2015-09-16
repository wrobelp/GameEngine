/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamecontent.mobs;

import collision.OpticProperties;
import collision.Rectangle;
import engine.utilities.*;
import game.gameobject.GameObject;
import game.gameobject.entities.ActionState;
import game.gameobject.entities.Mob;
import game.gameobject.interactive.CircleInteractiveCollision;
import game.gameobject.interactive.Interactive;
import game.gameobject.interactive.InteractiveActivatorFrames;
import game.gameobject.stats.MobStats;
import game.logic.navmeshpathfinding.PathFindingModule;
import game.place.Place;
import net.jodk.lang.FastMath;
import org.newdawn.slick.Color;
import sprites.Animation;
import sprites.SpriteSheet;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author przemek
 */
public class Shen extends Mob {

    private final Animation animation;
    int seconds = 0, max = 5;
    private Color skinColor;
    private Delay attack_delay = new Delay(1000);           //TODO - te wartości losowe i zależne od poziomu trudności
    private Delay rest = new Delay(1000);            //TODO - te wartości losowe i zależne od poziomu trudności
    private ActionState idle, run_away, hide, attack, wander, follow;

    {
        idle = new ActionState() {
            @Override
            public void update() {
                lookForCloseEntities(place.players, map.getArea(area).getNearSolidMobs());
                brake(2);

                if (rest.isOver()) {
                    calculateDestinationsForEscape();
                    GameObject closerEnemy = getEnemyCloser();
                    if (closerEnemy != null) {
                        state = hide;
                        destination.set(-1, -1);
                        stats.setProtectionState(true);
                    } else if (destination.getX() > 0) {
                        state = run_away;
                        destination.set(-1, -1);
                        setPathStrategy(PathFindingModule.GET_CLOSE, 250);
                    } else {
                        calculateDestinationsForCloseFriends();
                        if (alpha) {
                            state = wander;
                            maxSpeed = 0.8;
                            destination.set(getX(), getY());
                            seconds = 0;
                            pathData.setAvoidMobile(true);
                            setPathStrategy(PathFindingModule.GET_CLOSE, 90);
                        } else if (secondaryDestination.getX() > 0) {
                            state = follow;
                            pathData.setAvoidMobile(false);
                            setPathStrategy(PathFindingModule.GET_TO, collision.getWidth() * 2);
                        }
                    }
                }
            }
        };
        run_away = new ActionState() {
            @Override
            public void update() {
                if (destination.getX() > 0)
                    secondaryDestination.set(destination.getX(), destination.getY());
                lookForCloseEntities(place.players, map.getArea(area).getNearSolidMobs());
                calculateDestinationsForEscape();
                goTo(destination.getX() > 0 ? destination : secondaryDestination);
                GameObject closerEnemy = getEnemyCloser();
                if (closerEnemy != null) {
                    state = hide;
                    destination.set(-1, -1);
                    stats.setProtectionState(true);
                } else if (destination.getX() < 0 && Methods.pointDistanceSimple2(getX(), getY(), secondaryDestination.getX(), secondaryDestination.getY()) < 4 * hearRange2 / 9) {
                    state = idle;
                }
            }
        };
        hide = new ActionState() {
            @Override
            public void update() {
                brake(2);
                if (appearance.getCurrentFrameIndex() % animation.getFramesPerDirection() == 12) {
                    lookForCloseEntities(place.players, map.getArea(area).getNearSolidMobs());
                    GameObject closerEnemy = getEnemyCloser();

                    if (closerEnemy == null) {
                        state = idle;
                        stats.setProtectionState(false);
                    } else if (rest.isOver() && target == null && (stats.getHealth() < stats.getMaxHealth() || isAnyFriendHurt()) && isInHalfHearingRange(closerEnemy)) {
                        state = attack;
                        target = closerEnemy;
                        maxSpeed = 8;
                        setPathStrategy(PathFindingModule.GET_TO, 0);
                        attack_delay.start();
                    }
                }
            }
        };
        attack = new ActionState() {
            @Override
            public void update() {
                if (xSpeed == 0 && ySpeed == 0)
                    charge();
                if (attack_delay.isOver() || isOutOfRange(target) || target.getMap() != map) {
                    state = idle;
                    target = null;
                    brake(2);
                    rest.start();
                    stats.setProtectionState(false);
                    maxSpeed = 1;
                    setPathStrategy(PathFindingModule.GET_CLOSE, 250);
                }
            }
        };
        follow = new ActionState() {
            @Override
            public void update() {
                lookForCloseEntities(place.players, map.getArea(area).getNearSolidMobs());
                calculateDestinationsForCloseFriends();
                xSpeed = ySpeed = 0;
                int distance = Methods.pointDistanceSimple2(getX(), getY(), secondaryDestination.getX(), secondaryDestination.getY());
                if (distance > collision.getWidth() * collision.getWidth() * 4) {
                    goTo(secondaryDestination);
                }
                repulsion();
                if (xSpeed == 0 && ySpeed == 0)
                    alignment();
                GameObject closerEnemy = getEnemyCloser();
                if (closerEnemy != null) {
                    state = idle;
                    pathData.setAvoidMobile(true);
                    secondaryDestination.set(-1, -1);
                }
                if (isDistance2OutOfRange(distance)) {
                    state = idle;
                    pathData.setAvoidMobile(true);
                    secondaryDestination.set(-1, -1);
                }
            }
        };
        wander = new ActionState() {
            @Override
            public void update() {
                if (rest.isOver()) {

                    RandomGenerator random = RandomGenerator.create((int) System.currentTimeMillis());
                    if (Methods.pointDistanceSimple2(getX(), getY(), destination.getX(), destination.getY()) <= 10000) {
                        int sign = random.next(10) > 512 ? 1 : -1;
                        int shift = (hearRange + random.next(10)) * sign;
                        destination.setX(getX() + shift);
                        sign = random.next(10) > 512 ? 1 : -1;
                        shift = (hearRange + random.next(10)) * sign;
                        destination.setY(getY() + shift);
                        if (destination.getX() < sightRange / 2) {
                            destination.setX(sightRange / 2);
                        }
                        if (destination.getX() > map.getWidth()) {
                            destination.setX(map.getWidth() - sightRange / 2);
                        }
                        if (destination.getY() < collision.getHeight()) {
                            destination.setY(sightRange / 2);
                        }
                        if (destination.getY() > map.getHeight()) {
                            destination.setY(map.getHeight() - sightRange / 2);
                        }
                    }
                    seconds++;
                    if (seconds > max) {
                        seconds = 0;
                        max = random.next(7);
                    }
                    rest.start();
                }
                lookForCloseEntities(place.players, map.getArea(area).getNearSolidMobs());
                if (!closeEnemies.isEmpty()) {
                    state = idle;
                    destination.set(-1, -1);
                    maxSpeed = 1;
                }
                goTo(destination);
            }
        };
    }

    public Shen(int x, int y, Place place, short ID) {
        super(x, y, 1, 500, "Shen", place, "shen", true, ID);
        setCollision(Rectangle.create(48, 34, OpticProperties.NO_SHADOW, this));
        animation = Animation.createDirectionalAnimation((SpriteSheet) appearance, 0, 15);
        appearance = animation;
        //RandomGenerator r = RandomGenerator.create();
        //skinColor = Color.getHSBColor(r.nextFloat(), 1, 1);
        collision.setMobile(true);
        setPathStrategy(PathFindingModule.GET_CLOSE, 250);
        stats = new MobStats(this);
        stats.setStrength(1);
        stats.setDefence(3);
        attack_delay.start();
        rest.start();
        state = idle;
        int[] frames = new int[8];
        for (int i = 0; i < 8; i++) {
            frames[i] = 13 + i * animation.getFramesPerDirection();
        }
        addInteractive(new Interactive(this, new InteractiveActivatorFrames(frames), new CircleInteractiveCollision(0, 64, -24, 32), Interactive.HURT, 0.5f));
    }

    private void repulsion() {
        for (Mob mob : closeFriends) {
            int distance = Methods.pointDistanceSimple2(getX(), getY(), mob.getX(), mob.getY());
            if (distance < collision.getWidth() * collision.getWidth() * 4f) {
                double scale = 1 - (FastMath.sqrt(distance) / (collision.getWidth() * 2f));
                int x = getX() - mob.getX();
                int y = getY() - mob.getY();
                float ratio = Math.abs(y / (float) x);
                x = (int) (Math.signum(x) * hearRange / 2);
                y = (int) (Math.signum(y) * (ratio * Math.abs(x)));
                x += getX();
                y += getY();
                double angle = Methods.pointAngleClockwise(getX(), getY(), x, y);
                xSpeed += scale * Methods.xRadius(angle, maxSpeed);
                ySpeed += scale * Methods.yRadius(angle, maxSpeed);
            }
        }
    }

    private void alignment() {
        if (!closeFriends.isEmpty()) {
            closeFriends.stream().filter(mob -> mob.isAlpha()).forEach(mob -> {
                xSpeed += mob.getXSpeed() / 2;
                ySpeed += mob.getYSpeed() / 2;
            });
        }
    }

    private boolean isAnyFriendHurt() {
        if (!closeFriends.isEmpty()) {
            for (Mob mob : closeFriends) {
                if (mob.getStats().getHealth() < mob.getStats().getMaxHealth())
                    return true;
            }
        }
        return false;
    }

    @Override
    public void update() {
        state.update();
        normalizeSpeed();
        updateAnimation();
        moveWithSliding(xEnvironmentalSpeed + xSpeed, yEnvironmentalSpeed + ySpeed);
        brakeOthers();
    }


    private GameObject getEnemyCloser() {
        for (GameObject object : closeEnemies) {
            if (isInHalfHearingRange(object)) {
                return object;
            }
        }
        return null;
    }

    private void updateAnimation() {
        if (Math.abs(xSpeed) >= 0.1 || Math.abs(ySpeed) >= 0.1) {
            pastDirections[currentPastDirection++] = Methods.pointAngle8Directions(0, 0, xSpeed, ySpeed);
            if (currentPastDirection > 1)
                currentPastDirection = 0;
            if (pastDirections[0] == pastDirections[1])
                setDirection(pastDirections[0] * 45);
            if (target == null) {
                animation.setFPS(7);
                animation.animateIntervalInDirection(getDirection8Way(), 0, 5);
            } else {
                animation.setFPS(30);
                animation.animateIntervalInDirection(getDirection8Way(), 12, 14);
            }
        } else {
            if (stats.isProtectionState()) {
                animation.setFPS(15);
                animation.animateIntervalInDirection(getDirection8Way(), 7, 12);
                animation.setStopAtEnd(true);
//                collision.setWidthAndHeight(32, 23);
            } else {
                animation.animateSingleInDirection(getDirection8Way(), 0);
            }
        }
    }


    @Override
    public void render(int xEffect, int yEffect) {
        if (appearance != null) {
            glPushMatrix();
            glTranslatef(xEffect, yEffect, 0);
            glScaled(Place.getCurrentScale(), Place.getCurrentScale(), 1);
            glTranslatef(getX(), getY(), 0);
            //Drawer.setColor(skinColor);
            animation.updateFrame();
            appearance.render();
            glScaled(1 / Place.getCurrentScale(), 1 / Place.getCurrentScale(), 1);
            if (map != null) {
                Drawer.renderString(name, 0, (int) -((animation.getHeight() * Place.getCurrentScale()) / 2), place.standardFont, map.getLightColor());
            }
            glPopMatrix();

//            renderPathPoints(xEffect, yEffect);


        }
    }

    private void renderPathPoints(int xEffect, int yEffect) {
        PointContainer path = pathData.getPath();
        int current = pathData.getCurrentPointIndex();
        Drawer.setColor(new Color(0.5f, 0.1f, 0.1f));

        glPushMatrix();
        glTranslatef(xEffect, yEffect, 0);
        glScaled(Place.getCurrentScale(), Place.getCurrentScale(), 1);
        if (path != null) {
            for (int i = current; i < path.size(); i++) {
                Drawer.drawRectangle(path.get(i).getX(), path.get(i).getY(), 10, 10);
            }

        }
        if (destination.getX() > 0) {
            Drawer.drawRectangle(destination.getX(), destination.getY(), 10, 10);
        }
        Drawer.refreshColor();
        glPopMatrix();
    }
}