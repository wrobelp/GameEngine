/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.myGame;

import game.gameobject.Player;
import game.gameobject.Controler;
import game.gameobject.Entity;
import org.lwjgl.input.Controllers;

/**
 *
 * @author przemek
 */
public class MyPadWS extends Controler {

    private final int padNr;
    private final boolean isPressed[] = new boolean[24];

    public MyPadWS(Entity inControl, int padNr) {
        super(inControl);
        //this.padNr = padNr;
        this.padNr = 4;

    }

    //if (Controllers.getControllerCount() > 0) !!!                <-----
    @Override
    protected void getInput() {
        Controllers.poll();

//        for (int i = 0; i < Controllers.getController(4).getButtonCount(); i++) {
//            Controllers.getController(4).poll();
//            if(Controllers.getController(4).isButtonPressed(i)){
//                System.out.println("Naciśnięto: " + i);
//            }
//        }
//
        if (Controllers.getController(padNr).getXAxisValue() != 0.0f) {

            int xPad = Controllers.getController(padNr).getXAxisValue() > 0.2f ? 1 : Controllers.getController(padNr).getXAxisValue() < -0.2f ? -1 : 0;
            inControl.canMove(xPad, 0);
            if (xPad == 1) {
                ((Player) inControl).getAnim().setFlip(1);
            } else if (xPad == -1) {
                ((Player) inControl).getAnim().setFlip(0);
            }
        }
        if (Controllers.getController(padNr).getYAxisValue() != 0.0) {
            int yPad = Controllers.getController(padNr).getYAxisValue() > 0.1 ? 1 : Controllers.getController(padNr).getYAxisValue() < -0.1 ? -1 : 0;
            if (yPad != 0) {
                inControl.canMove(0, yPad);
            }
        }
        if (Controllers.getController(padNr).isButtonPressed(7)) {
            inControl.setSpeed(16);
        } else {
            inControl.setSpeed(8);
        }

        {
            int key = 3;
            if (Controllers.getController(padNr).isButtonPressed(key)) {
                if (!isPressed[key]) {
                    ((Player) inControl).setEmits(!((Player) inControl).isEmits());
                    isPressed[key] = true;
                }
            } else {
                isPressed[key] = false;
            }
        }
        if (Controllers.getController(padNr).isButtonPressed(6)) {
            ((Player) inControl).getPlace().shakeCam(((Player) inControl).getCam());
        }
    }

    @Override
    protected boolean isMenuOn() {
        Controllers.poll();
        int key = 9;
        if (Controllers.getController(padNr).isButtonPressed(key)) {
            if (!isPressed[key]) {
                isPressed[key] = true;
                return isPressed[key];
            }
        } else {
            isPressed[key] = false;
        }
        return false;
    }

    @Override
    protected void getMenuInput() {
        Controllers.poll();
        {
            int key = 16; //left Stick up
            if (Controllers.getController(padNr).getYAxisValue() < 0.0) {
                if (!isPressed[key]) {
                    ((Player) inControl).menu.setChoosen(-1);
                    isPressed[key] = true;
                }
            } else {
                isPressed[key] = false;
            }
        }
        {
            int key = 17; //left Stick down
            if (Controllers.getController(padNr).getYAxisValue() > 0.0) {
                if (!isPressed[key]) {
                    ((Player) inControl).menu.setChoosen(1);
                    isPressed[key] = true;
                }
            } else {
                isPressed[key] = false;
            }
        }
        {
            int key = 12; //pov down
            if (Controllers.getController(padNr).getPovY() < 0.0) {
                if (!isPressed[key]) {
                    ((Player) inControl).menu.setChoosen(-1);
                    isPressed[key] = true;
                }
            } else {
                isPressed[key] = false;
            }
        }
        {
            int key = 13; //pov up
            if (Controllers.getController(padNr).getPovY() > 0.0) {
                if (!isPressed[key]) {
                    ((Player) inControl).menu.setChoosen(1);
                    isPressed[key] = true;
                }
            } else {
                isPressed[key] = false;
            }
        }
        {
            int key = 2;
            if (Controllers.getController(padNr).isButtonPressed(key)) {
                if (!isPressed[key]) {
                    ((Player) inControl).menu.choice();
                    isPressed[key] = true;
                }
            } else {
                isPressed[key] = false;
            }
        }
    }
}