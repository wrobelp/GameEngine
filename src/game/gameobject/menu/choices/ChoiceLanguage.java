/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.gameobject.menu.choices;

import game.AnalizerSettings;
import game.Settings;
import game.gameobject.menu.MenuChoice;
import game.myGame.MyMenu;

/**
 *
 * @author przemek
 */
public class ChoiceLanguage extends MenuChoice {

    private int i;

    public ChoiceLanguage(String label, MyMenu menu, Settings settings) {
        super(label, menu, settings);
    }

    @Override
    public void action() {
        for (i = 0; i < settings.languages.size(); i++) {
            if (settings.languages.get(i).Lang.equals(settings.lang)) {
                i++;
                break;
            }
        }
        if (i >= settings.languages.size()) {
            i = 0;
        }
        settings.lang = settings.languages.get(i).Lang;
        AnalizerSettings.Update(settings);
    }

    @Override
    public String getLabel() {
        for (i = 0; i < settings.languages.size(); i++) {
            if (settings.languages.get(i).Lang.equals(settings.lang)) {
                i++;
                break;
            }
        }
        i--;
        if (i >= settings.languages.size()) {
            i = 0;
        }
        return label + settings.lang + " [" + (i + 1) + "/" + settings.languages.size() + "]";
    }
}
