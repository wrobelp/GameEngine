package game.gameobject.temporalmodifiers;

import game.gameobject.entities.Entity;

/**
 * Created by przemek on 02.02.16.
 */
public class LockChanger extends TemporalChanger {

    private Entity entity;

    public LockChanger(int time) {
        super();
        this.time = time;
    }

    @Override
    public void onStop() {
        if (entity != null) {
            entity.setUnableToMove(false);
        }
    }

    @Override
    public void modifyEffect(Entity entity) {
        this.entity = entity;
    }
}
