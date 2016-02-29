/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.place.cameras;

import engine.lights.Light;
import game.gameobject.GUIObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author przemek
 */
public class NearObjects {

    protected final ArrayList<GUIObject> gui = new ArrayList<>();
    private final ArrayList<Light> visibleLights = new ArrayList<>();


    public void addVisibleLight(Light light) {
        visibleLights.add(light);
    }

    public List<Light> getVisibleLights() {
        return visibleLights;
    }

    public void clearVisibleLights() {
        visibleLights.clear();
    }

}
