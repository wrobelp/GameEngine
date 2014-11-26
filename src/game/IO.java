/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import engine.Main;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author przemek
 */
public class IO {

    public static void ReadFile(File f, Settings settings, boolean isSettings) {
        try (BufferedReader r = new BufferedReader(new FileReader(f));) {
            String s;
            while ((s = r.readLine()) != null) {
                if (isSettings) {
                    AnalizerSettings.AnalizeSetting(s, settings);
                } else {
                    AnalizerInput.AnalizeInput(s, settings);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private IO() {
    }

}
