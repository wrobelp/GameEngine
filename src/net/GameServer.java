/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net;

import net.packets.NewMPlayer;
import net.packets.PacketMessage;
import net.packets.PacketJoinResponse;
import net.packets.PacketJoinRequest;
import net.packets.PacketAddMPlayer;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import engine.Delay;
import engine.Methods;
import game.gameobject.Mob;
import game.gameobject.Player;
import java.io.IOException;
import net.packets.MPlayerUpdate;
import net.packets.PacketMPlayerUpdate;
import net.packets.PacketRemoveMPlayer;

/**
 *
 * @author przemek
 */
public class GameServer {

    private final Server server;
    private final Player pl;
    private final GameOnline game;
    private final float SCALE;
    public boolean isRunning;
    private MPlayer[] MPlayers = new MPlayer[4];
    private boolean[] isConnected = new boolean[4];
    private int nrPlayers = 0;
    private byte id = 0;
    private Delay delay;

    public GameServer(final Player pl, final GameOnline game) {
        this.pl = pl;
        this.game = game;
        this.SCALE = game.g.settings.SCALE;
        this.server = new Server();

        delay = new Delay(33);
        delay.terminate();

        try {
            Log.set(Log.LEVEL_DEBUG);
            KryoUtil.registerServerClasses(server);
            server.addListener(new Listener() {
                @Override
                public synchronized void connected(Connection connection) {
                    try {
                        System.out.println("Received a connection from " + connection.getRemoteAddressTCP().getHostString() + " (" + connection.getID() + ")");
                    } catch (Exception e) {
                        cleanUp(e);
                    }
                }

                @Override
                public synchronized void disconnected(Connection connection) {
                    try {
                        int i;
                        String name = "Client";
                        byte id = -1;
                        for (i = 0; i < isConnected.length; i++) {
                            isConnected[i] = false;
                        }
                        for (Connection c : server.getConnections()) {
                            for (i = 1; i < nrPlayers; i++) {
                                if (MPlayers[i].getConnection() == c) {
                                    isConnected[i - 1] = true;
                                }
                            }
                        }
                        for (i = 1; i < nrPlayers; i++) {
                            if (!isConnected[i - 1]) {
                                name = MPlayers[i].getName();
                                id = MPlayers[i].getId();
                                MPlayers[i] = null;
                                if (i < nrPlayers - 1) {
                                    for (int j = i; j < nrPlayers - 1; j++) {
                                        MPlayers[j] = MPlayers[j + 1];
                                    }
                                }
                                nrPlayers--;
                            }
                        }
                        game.removePlayer(id);
                        for (i = 1; i < nrPlayers; i++) {
                            MPlayers[i].getConnection().sendTCP(new PacketRemoveMPlayer(id));
                        }
                        System.out.println(name + " (" + id + ") disconnected!");
                    } catch (Exception e) {
                        cleanUp(e);
                    }
                }

                @Override
                public synchronized void received(Connection connection, Object obj) {
                    try {
                        if (obj instanceof PacketMPlayerUpdate) {
                            PacketMPlayerUpdate pmpu = (PacketMPlayerUpdate) obj;
                            //System.out.println(ObjectSize.sizeInBytes(pmpu) + " BYTES");
                            MPlayer curPl = findPlayer(pmpu.MPU().getId());
                            if (curPl != null) {
                                curPl.Update(pmpu.MPU().getX(), pmpu.MPU().getY(), 1);
                                for (int i = 1; i < nrPlayers; i++) {
                                    if (MPlayers[i].getId() != pmpu.MPU().getId()) {
                                        MPlayers[i].PU().PlayerUpdate(curPl, pmpu.MPU().isEmits(), pmpu.MPU().isHop());
                                    }
                                }
                                game.playerUpdate(pmpu);
                            }
                        } else if (obj instanceof PacketMessage) {
                            connection.sendUDP(new PacketMessage("Hello Client!"));
                        } else if (obj instanceof PacketJoinRequest) {
                            if (nrPlayers < 4) {
                                makeSureIdIsUnique();
                                NewMPlayer nmp = AddNewPlayer(((PacketJoinRequest) obj).getName(), connection);
                                connection.sendTCP(new PacketJoinResponse(id++, MPlayers[nrPlayers].getX(), MPlayers[nrPlayers].getY()));
                                sendToAll(nmp);
                                sendToNew(connection);
                                nrPlayers++;
                                System.out.println(MPlayers[nrPlayers - 1].getName() + " (" + MPlayers[nrPlayers - 1].getId() + ") connected");
                            } else {
                                connection.sendTCP(new PacketJoinResponse((byte) -1));
                            }
                        }
//                        else if (obj instanceof PacketInput) {
//                            MPlayer curPl = findPlayer(((PacketInput) obj).getId());
//                            if (curPl != null) {
//                                curPl.inGame().ctrl.setInput(((PacketInput) obj).inputs());
//                                curPl.Update(curPl.inGame().getX(), curPl.inGame().getY(), SCALE);
//                                PacketMPlayerUpdate mpup = new PacketMPlayerUpdate(curPl);
//                                sendToAllButOwner(mpup, ((PacketInput) obj).getId());
//                            }
//                        }
                    } catch (Exception e) {
                        cleanUp(e);
                    }
                }

            });
            try {
                server.bind(KryoUtil.TCP_PORT, KryoUtil.UDP_PORT);
            } catch (IOException ex) {
                Methods.Error(ex.getMessage() + "!");
                return;
            }

            MPlayers[0] = new MPlayer("Server", id, null);
            MPlayers[0].setPosition(128 + id * 128, 256);
            MPlayers[0].setPlayer(pl);
            pl.setName(MPlayers[0].getName());
            pl.id = id++;
            pl.setX(((float) MPlayers[0].getX()) * SCALE);
            pl.setY(((float) MPlayers[0].getY()) * SCALE);
            nrPlayers++;

            isRunning = true;
            System.out.println("Server started!");

        } catch (Exception e) {
            cleanUp(e);
        }
    }

    public synchronized void Start() {
        server.start();
    }

    public synchronized void Close() {
        server.stop();
        server.close();
    }

    public synchronized void sendUpdate(int x, int y, boolean isEmits, boolean isHop) {
        try {
            MPlayers[0].Update(x, y, SCALE);
            for (int i = 1; i < nrPlayers; i++) {
                MPlayers[i].PU().PlayerUpdate(MPlayers[0], isEmits, isHop);
                for (Mob mob : game.g.getPlace().sMobs) {
                    MPlayers[i].PU().MobUpdate(mob.id, mob.getX(), mob.getY(), SCALE);
                }
            }
            if (delay.isOver()) {
                for (int i = 1; i < nrPlayers; i++) {
                    if (MPlayers[i] != null) {
                        MPlayers[i].getConnection().sendTCP(MPlayers[i].PU());
                    }
                    if (MPlayers[i] != null) {
                        MPlayers[i].resetPU();
                    }
                    delay.restart();
                }
            }
        } catch (Exception e) {
            cleanUp(e);
        }
    }

    public synchronized MPlayer findPlayer(byte id) {
        for (int i = 1; i < nrPlayers; i++) {
            if (MPlayers[i].getId() == id) {
                return MPlayers[i];
            }
        }
        return null;
    }

    private synchronized void cleanUp(Exception e) {
        game.g.endGame();
        Methods.Exception(e);
    }

    private synchronized void makeSureIdIsUnique() {
        for (int j = 0; j < nrPlayers; j++) {
            for (int i = 0; i < nrPlayers; i++) {
                if (id == MPlayers[i].getId()) {
                    id++;
                }
            }
        }
    }

    private synchronized NewMPlayer AddNewPlayer(String name, Connection connection) {
        MPlayers[nrPlayers] = new MPlayer(name, id, connection);
        MPlayers[nrPlayers].setPosition(128 + id * 128, 256);
        NewMPlayer nmp = new NewMPlayer(MPlayers[nrPlayers]);
        game.addPlayer(nmp);
        return nmp;
    }

    private synchronized void sendToAll(NewMPlayer nmp) {
        for (int i = 1; i < nrPlayers; i++) {   // send NewPlayer to All
            MPlayers[i].getConnection().sendTCP(new PacketAddMPlayer(nmp));
        }
    }

    private synchronized void sendToAll(MPlayerUpdate mpup) {
        for (int i = 1; i < nrPlayers; i++) {
            MPlayers[i].getConnection().sendTCP(mpup);
        }
    }

    private synchronized void sendToAllButOwner(MPlayerUpdate mpup, int id) {
        for (int i = 1; i < nrPlayers; i++) {
            if (MPlayers[i].getId() != id) {
                MPlayers[i].getConnection().sendTCP(mpup);
            }
        }
    }

    private synchronized void sendToNew(Connection connection) {
        for (int i = 0; i < nrPlayers; i++) {   // send Players to NewPlayer
            connection.sendTCP(new PacketAddMPlayer(new NewMPlayer(MPlayers[i])));
        }
    }
}