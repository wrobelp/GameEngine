/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamecontent;

import engine.Delay;
import game.gameobject.*;
import sprites.Animation;

/**
 * @author przemek
 */
public class MyController extends PlayerController {

    public static final byte MENU_UP = 0, MENU_DOWN = 1, MENU_ACTION = 2, MENU_BACK = 3, MENU_LEFT = 4, MENU_RIGHT = 5, UP = 6, DOWN = 7, LEFT = 8, RIGHT = 9, ATTACK = 10, RUN = 11, LIGHT = 12, ZOOM = 13, NEXT = 14, PREVIOUS = 15, BLOCK = 16, DODGE = 17, REVERSE = 18, SNEAK = 19,
            ACTION_9 = 20, ACTION_8 = 21, ACTION_7 = 22, ACTION_6 = 23, ACTION_5 = 24, ACTION_4 = 25, ACTION_3 = 26, ACTION_2 = 27, ACTION_1 = 28;
    public static final byte MENU_ACTIONS_COUNT = 6, ACTIONS_COUNT = 29, ATTACK_COUNT = 5;

    public static final byte ATTACK_SLASH = 0, ATTACK_THRUST = 1, ATTACK_UPPER_SLASH = 2, ATTACK_WEAK_PUNCH = 3, ATTACK_STRONG_PUNCH = 4;
    private final int[] attackFrames;
    private final Delay sideDelay;
    private int direction, lagDuration, attackType, sideDirection;
    private boolean running, diagonal, inputLag;
    private Animation playerAnimation;
    private MyGUI gui;

    public MyController(Entity inControl, MyGUI playersGUI) {
        super(inControl);
        gui = playersGUI;
        inputs = new AnyInput[ACTIONS_COUNT];
        actions = new Action[ACTIONS_COUNT];
        sideDelay = new Delay(25);
        attackType = 0;
        attackFrames = new int[]{22, 27, 31, 38, 40};
    }

    @Override
    public void initialize() {
        for (int i = 0; i < ACTIONS_COUNT; i++) {
            actions[i] = new Action(inputs[i]);
        }
    }

    @Override
    public void getInput() {
        if (gui != null) {
            updateActionsIfNoLag();
            //ANIMACJA//
            direction = inControl.getDirection();
            running = !actions[RUN].isKeyPressed();

            playerAnimation = (Animation) inControl.getAppearance();
            playerAnimation.setAnimate(true);
            diagonal = true;

            if (inControl.isAbleToMove()) {
                if (actions[ATTACK].isKeyPressed()) {
                    updateAttack();
                } else {
                    updateMovement();
                }
                updateRest();
            } else {
                playerAnimation.animateSingleInDirection(direction / 45, 0);
            }
            if (!actions[ATTACK].isKeyPressed()) {
                if (running) {
                    playerAnimation.setFPS((int) (inControl.getSpeed() * 4));
                } else {
                    playerAnimation.setFPS((int) (inControl.getSpeed() * 3));
                }
            } else {
                playerAnimation.setFPS(60);
            }
        }
    }

    private void updateActionsIfNoLag() {
        if (inputLag) {
            lagDuration--;
            if (lagDuration < 0) {
                inputLag = false;
            }
            for (int i = MENU_ACTIONS_COUNT; i < ACTIONS_COUNT; i++) {
                actions[i].updatePassiveState();
            }
        } else {
            for (int i = MENU_ACTIONS_COUNT; i < ACTIONS_COUNT; i++) {
                actions[i].updateActiveState();
            }
        }
    }

    private void updateAttack() {
        if (actions[ATTACK].isKeyClicked()) {
            setInputLag(30);
        }
        playerAnimation.setStopAtEnd(true);
        switch (attackType) {
            case ATTACK_SLASH:
                playerAnimation.animateIntervalInDirection(direction / 45, 21, 25);
                break;
            case ATTACK_THRUST:
                playerAnimation.animateIntervalInDirection(direction / 45, 26, 28);
                break;
            case ATTACK_UPPER_SLASH:
                playerAnimation.animateIntervalInDirection(direction / 45, 29, 36);
                break;
            case ATTACK_WEAK_PUNCH:
                playerAnimation.animateIntervalInDirection(direction / 45, 37, 39);
                break;
            case ATTACK_STRONG_PUNCH:
                playerAnimation.animateIntervalInDirection(direction / 45, 40, 41);
                break;
        }
        inControl.brakeWithModifier(2, 2);
    }

    private void updateMovement() {
        playerAnimation.setStopAtEnd(false);
        if (actions[UP].isKeyPressed()) {
            if (actions[LEFT].isKeyPressed()) {
                animateMoving(135);
                inControl.addSpeed(-4, -4);
            } else if (actions[RIGHT].isKeyPressed()) {
                animateMoving(45);
                inControl.addSpeed(4, -4);
            } else {
                if (actions[LEFT].isKeyReleased()) {
                    sideDirection = 135;
                    sideDelay.start();
                }
                if (actions[RIGHT].isKeyReleased()) {
                    sideDirection = 45;
                    sideDelay.start();
                }
                animateMoving(90);
                inControl.addSpeed(0, -4);
            }
        } else if (actions[DOWN].isKeyPressed()) {
            if (actions[LEFT].isKeyPressed()) {
                animateMoving(225);
                inControl.addSpeed(-4, 4);
            } else if (actions[RIGHT].isKeyPressed()) {
                animateMoving(315);
                inControl.addSpeed(4, 4);
            } else {
                if (actions[LEFT].isKeyReleased()) {
                    sideDirection = 225;
                    sideDelay.start();
                }
                if (actions[RIGHT].isKeyReleased()) {
                    sideDirection = 315;
                    sideDelay.start();
                }
                animateMoving(270);
                inControl.addSpeed(0, 4);
            }
        } else {
            if (actions[RIGHT].isKeyPressed()) {
                if (actions[UP].isKeyReleased()) {
                    sideDirection = 45;
                    sideDelay.start();
                }
                if (actions[DOWN].isKeyReleased()) {
                    sideDirection = 315;
                    sideDelay.start();
                }
                animateMoving(0);
                inControl.addSpeed(4, 0);
            } else if (actions[LEFT].isKeyPressed()) {
                if (actions[UP].isKeyReleased()) {
                    sideDirection = 135;
                    sideDelay.start();
                }
                if (actions[DOWN].isKeyReleased()) {
                    sideDirection = 225;
                    sideDelay.start();
                }
                animateMoving(180);
                inControl.addSpeed(-4, 0);
            } else {
                playerAnimation.animateSingleInDirection(direction / 45, 0);
            }
        }
        if (!actions[UP].isKeyPressed() && !actions[DOWN].isKeyPressed()) {
            diagonal = false;
            inControl.brake(1);
        }
        if (!actions[LEFT].isKeyPressed() && !actions[RIGHT].isKeyPressed()) {
            diagonal = false;
            inControl.brake(0);
        }
        if (sideDelay.isActive() && sideDelay.isOver()) {
            if (!actions[UP].isKeyPressed() && !actions[DOWN].isKeyPressed()
                    && !actions[LEFT].isKeyPressed() && !actions[RIGHT].isKeyPressed()) {
                inControl.setDirection(sideDirection);
            }
            sideDelay.stop();
        }
    }

    private void updateRest() {
        if (actions[NEXT].isKeyClicked()) {
            attackType += 1;
            if (attackType > ATTACK_COUNT - 1) {
                attackType = 0;
            }
            gui.changeAttackIcon(attackType);
        } else if (actions[PREVIOUS].isKeyClicked()) {
            attackType -= 1;
            if (attackType < 0) {
                attackType = ATTACK_COUNT - 1;
            }
            gui.changeAttackIcon(attackType);
        }
        if (!running) {
            inControl.setMaxSpeed(diagonal ? 1.5 : 2);
        } else {
            inControl.setMaxSpeed(diagonal ? 6 : 8);
        }
        if (actions[LIGHT].isKeyClicked()) {
            inControl.setEmits(!inControl.isEmits());
        }
        if (actions[ZOOM].isKeyClicked()) {
            if (inControl instanceof Player) {
                ((Player) inControl).getCamera().switchZoom();
            }
        }
    }


    private void animateMoving(int direction) {
        if (running) {
            playerAnimation.animateIntervalInDirection(direction / 45, 7, 18);
        } else {
            playerAnimation.animateIntervalInDirection(direction / 45, 1, 6);
        }
        inControl.setDirection(direction);
    }

    @Override
    public boolean isMenuOn() {
        actions[MENU_BACK].updateActiveState();
        return actions[MENU_BACK].isKeyClicked();
    }

    @Override
    public void getMenuInput() {
        for (int i = 0; i < MENU_ACTIONS_COUNT; i++) {
            actions[i].updateActiveState();
        }
        actions[RIGHT].updateActiveState();
        actions[LEFT].updateActiveState();
        if (actions[MENU_UP].isKeyClicked()) {
            ((Player) inControl).getMenu().setChosen(-1);
        } else if (actions[MENU_DOWN].isKeyClicked()) {
            ((Player) inControl).getMenu().setChosen(1);
        }
        if (actions[MENU_ACTION].isKeyClicked()) {
            ((Player) inControl).getMenu().choice(0);
        } else if (actions[MENU_RIGHT].isKeyClicked() || actions[RIGHT].isKeyClicked()) {
            ((Player) inControl).getMenu().choice(1);
        } else if (actions[MENU_LEFT].isKeyClicked() || actions[LEFT].isKeyClicked()) {
            ((Player) inControl).getMenu().choice(2);
        } else if (actions[MENU_BACK].isKeyClicked()) {
            ((Player) inControl).getMenu().back();
        }
    }

    @Override
    public int getActionsCount() {
        return ACTIONS_COUNT;
    }

    public int[] getAttackFrames() {
        return attackFrames;
    }

    public int getAttackType() {
        return attackType;
    }

    private void setInputLag(int time) {
        if (!inputLag) {
            inputLag = true;
            lagDuration = time;
        }
    }

    public void setPlayersGUI(MyGUI gui) {
        this.gui = gui;
    }
}
