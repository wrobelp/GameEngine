/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.packets;

import java.io.Serializable;

/**
 *
 * @author przemek
 */
public class PacketMPlayerUpdate implements Serializable {

    private MPlayerUpdate mpu;

    public PacketMPlayerUpdate() {
    }

    public PacketMPlayerUpdate(short mapId, byte id, int x, int y, boolean isEmits, boolean isHop) {
        mpu = new MPlayerUpdate(mapId, id, x, y, isEmits, isHop);
        mpu.Trim();
    }

    public synchronized void update(short mapId, byte id, int x, int y, boolean isEmits, boolean isHop, float SCALE) {
        if (mpu == null || mpu.getMapId() != mapId) {
            mpu = new MPlayerUpdate(mapId, id, (int) (((float) x) / SCALE), (int) (((float) y) / SCALE), isEmits, isHop);
        } else {
            mpu.Update((int) (((float) x) / SCALE), (int) (((float) y) / SCALE));
        }
        mpu.Trim();
    }

    public synchronized void reset() {
        mpu = null;
    }

    public synchronized MPlayerUpdate up() {
        return mpu;
    }
}
