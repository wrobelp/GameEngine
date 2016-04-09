/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import game.gameobject.GameObject;

/**
 * @author przemek
 */
public abstract class ScreenPlace {

    public final Game game;
    public GameObject[] players;

    public ScreenPlace(Game game) {
        this.game = game;
    }

    public abstract void update();

    public abstract void render();
}
