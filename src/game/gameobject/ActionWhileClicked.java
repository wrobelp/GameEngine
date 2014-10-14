/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.gameobject;

/**
 *
 * @author przemek
 */
public abstract class ActionWhileClicked extends Action {

    public ActionWhileClicked(AnyInput in, Entity inControl) {
        super(in, inControl);
    }

    @Override
    public void Do() {
        if (in != null && in.isPut()) {
            Act();
        } else {
            noAct();
        }
    }

    @Override
    public abstract void Act();
    
    public abstract void noAct();

}