/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamecontent.choices;

import game.AnalizerSettings;
import game.Settings;
import game.gameobject.menu.MenuChoice;
import game.place.Menu;

/**
 *
 * @author przemek
 */
public class SmoothShadowsChoice extends MenuChoice {

    public SmoothShadowsChoice(String label, Menu menu, Settings settings) {
        super(label, menu, settings);
    }

    @Override
    public void action() {
        if (settings.isSupfboMS) {
            settings.nrSamples *= 2;
            if (settings.nrSamples == 0) {
                settings.nrSamples = 2;
            }
            if (settings.nrSamples > settings.maxSamples) {
                settings.nrSamples = 0;
            }
            AnalizerSettings.update(settings);
        } else {
            settings.nrSamples = 0;
            AnalizerSettings.update(settings);
        }
    }

    @Override
    public String getLabel() {
        if (!settings.isSupfboMS) {
            return label + settings.language.m.Off + " (" + settings.language.m.Unsupported + ")";
        } else if (settings.nrSamples == 0) {
            return label + settings.language.m.Off;
        }
        return label + settings.nrSamples + "x";

    }
}