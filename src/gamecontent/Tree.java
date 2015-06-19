/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamecontent;

import collision.OpticProperties;
import collision.Rectangle;
import game.gameobject.Mob;
import game.place.Place;

/**
 *
 * @author przemek
 */
public class Tree extends Mob {
    
    public Tree(int x, int y, int width, int height, double speed, int range, String name, Place place, boolean solid, short ID) {
        super(x, y, speed, range, name, place, "bigtree", solid, ID);
        setCollision(Rectangle.create(width, height, OpticProperties.FULL_SHADOW, this));
        setSimpleLighting(false);
    }
    
    @Override
    public void update() {
//        if (prey != null && ((MyPlayer) prey).isInGame()) {
//            chase(prey);
//            if (Methods.pointDistance(getX(), getY(), prey.getX(), prey.getY()) > range * 1.5 || prey.getMap() != map) {
//                prey = null;
//            }
//        } else {
//            look(place.players);
//            brake(2);
//        }
//        moveIfPossible((int) (xEnvironmentalSpeed + xSpeed), (int) (yEnvironmentalSpeed + ySpeed));
//        brakeOthers();
    }
}
