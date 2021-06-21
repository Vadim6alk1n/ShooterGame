package ShooterPack.Server;

import ShooterPack.Game.Collisions;
import ShooterPack.Game.GameObject;
import ShooterPack.Game.Packets.*;
import ShooterPack.Game.Vec2f;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Scanner;

import static ShooterPack.Game.Packets.PacketCInput.*;

public class GameServer extends CancellableRunnable{
    ArrayList<PlayerConnection> players;
    volatile ArrayList<byte[]> msgbuffer;
    volatile ArrayList<byte[]> msgtmpbuffer;

    //ShooterPack.Game data
    ArrayList<GameObject> gameObjects;
    ArrayList<GameObject> updatedObjects;
    ArrayList<GameObject> createdObjects;
    ArrayList<GameObject> killedObjects;
    Collisions collisions;

    //system.in
    public volatile boolean show_packet_size;
    public volatile boolean show_packet_binary;

    GameServer(ArrayList<CancellableRunnable> pool)
    {
        GameServerConsole gsc = new GameServerConsole(this);
        pool.add(gsc);
        gsc.start();
        msgbuffer = new ArrayList<>();
        msgtmpbuffer = new ArrayList<>();
        gameObjects = new ArrayList<>();
        collisions = new Collisions();
        players = new ArrayList<>();
        updatedObjects = new ArrayList<>();
        createdObjects = new ArrayList<>();
        killedObjects = new ArrayList<>();
        show_packet_size = false;
        show_packet_binary = false;
    }

    public synchronized void pushmsg(byte[] msg)
    {
        msgtmpbuffer.add(msg);
    }
    public synchronized void transfermsg()
    {
        for (var i : msgtmpbuffer)
        {
            msgbuffer.add(i);
        }
        msgtmpbuffer.clear();
    }

    @Override
    public void run() {

        //border walls
        gameObjects.add(new GameObject(-200,-200,11*200+400,200,GameObject.gameobjecttype_wall));
        gameObjects.add(new GameObject(-200,0,200,11*200+200,GameObject.gameobjecttype_wall));
        gameObjects.add(new GameObject(0,11*200,11*200,200,GameObject.gameobjecttype_wall));
        gameObjects.add(new GameObject(11*200,0,200,11*200+200,GameObject.gameobjecttype_wall));

        //spawn walls
        gameObjects.add(new GameObject(200,600,600,200,GameObject.gameobjecttype_wall));
        gameObjects.add(new GameObject(600,200,200,400,GameObject.gameobjecttype_wall));

        gameObjects.add(new GameObject(1400,200,200,600,GameObject.gameobjecttype_wall));
        gameObjects.add(new GameObject(1600,600,400,200,GameObject.gameobjecttype_wall));

        gameObjects.add(new GameObject(200,1400,600,200,GameObject.gameobjecttype_wall));
        gameObjects.add(new GameObject(600,1600,200,400,GameObject.gameobjecttype_wall));

        gameObjects.add(new GameObject(1400,1400,600,200,GameObject.gameobjecttype_wall));
        gameObjects.add(new GameObject(1400,1600,200,400,GameObject.gameobjecttype_wall));

        long currenttime = System.currentTimeMillis();
        long diff=0;
        while (isRunning())
        {
            //get messages from tmp buffer
            transfermsg();
            //process messages
            processmsg();

            //gameloop
            synchronized (players) {
                //set player velocities
                for (var i : players) {
                    synchronized (i) {
                        GameObject gobj = null;
                        for (var it : gameObjects) {
                            if (it.id == i.playerid)
                                gobj = it;
                        }
                        if (gobj!=null) {
                            //TODO: FIX KEYS
                            if (i.keys[key_w]) gobj.vel.y -= 5;
                            if (i.keys[key_s]) gobj.vel.x -= 5;
                            if (i.keys[key_a]) gobj.vel.y += 5;
                            if (i.keys[key_d]) gobj.vel.x += 5;
                        }
                    }
                }
                for (var i: players) {
                    if(i.keys[4]) {
                        GameObject player = null;
                        for (var it : gameObjects) {
                            if (it.id == i.playerid)
                                player = it;
                        }
                        if (player!=null) {
                            if (player.passedtime >= player.cooldown) {
                                //get mouse pos
                                float mx = (float) i.mx;
                                float my = (float) i.my;
                                GameObject bullet = new GameObject();
                                bullet.pos.x = mx;
                                bullet.pos.x = my;
                                bullet.size.x = 20.0f;
                                bullet.size.y = 20.0f;

                                //bullet velocity
                                Vec2f vel = new Vec2f();
                                vel.x = mx - (player.pos.x + player.size.x / 2);
                                vel.y = my - (player.pos.y + player.size.y / 2);
                                bullet.bullet = true;
                                vel.Normalize();
                                bullet.vel = vel;
                                bullet.killable = true; //make bullet killable

                                //bullet position
                                bullet.pos.x = (player.pos.x + player.size.x / 2) + vel.x * 50;
                                bullet.pos.y = (player.pos.y + player.size.y / 2) + vel.y * 80;
                                bullet.vel.x *= 8;
                                bullet.vel.y *= 8;
                                bullet.type = GameObject.gameobjecttype_bullet;
                                gameObjects.add(bullet);
                                createdObjects.add(bullet);
                                player.passedtime = 0;
                            }
                        }
                    }
                }

                updatedObjects.clear();

                //collisions
                collisions.HandleCollisions(gameObjects);
                //move objects and add cooldown time
                for (var i : gameObjects) {
                    i.pos.Add(i.vel);
                    i.passedtime += diff;
                    if (i.vel.x != 0 || i.vel.y != 0) {
                        updatedObjects.add(i);
                    }
                }

                //reset player velocities
                for (var i : players) {
                    GameObject gobj = null;
                    for (var it : gameObjects) {
                        if (it.id == i.playerid)
                            gobj = it;
                    }
                    if (gobj != null) {
                        gobj.vel.y = 0.0f;
                        gobj.vel.x = 0.0f;
                    }
                }

                //bullet logic
                ArrayList<GameObject> killlist = new ArrayList<>();
                for (var i : collisions.collisionlist) {
                    //kill bullets
                    if (i.a.bullet)
                    {
                        killlist.add(i.a);
                    }
                    else
                    {
                        if (i.b.bullet)
                            i.a.health--;
                        if (i.a.health<=0)
                        {
                            killlist.add(i.a);
                        }
                    }
                    if (i.b.bullet)
                    {
                        killlist.add(i.b);
                    }
                    else
                    {
                        if (i.a.bullet)
                            i.b.health--;
                        if (i.b.health<=0)
                        {
                            killlist.add(i.b);
                        }
                    }
                }
                //killed gameobjects
                for (var i : killlist) {
                        if (i.killable) {
                            //if bullet then kill, else it's enemy, move him
                            if (i.bullet) {
                                killedObjects.add(i);
                                gameObjects.remove(i);
                            }
                            else {

                                int startpos = i.id%4;
                                if (startpos == 0) {
                                    i.pos.x = 300.0f;
                                    i.pos.y = 300.0f;
                                }
                                if (startpos == 1) {
                                    i.pos.x = 11*200-300.0f;
                                    i.pos.y = 11*200-300.0f;
                                }
                                if (startpos == 2) {
                                    i.pos.x = 300.0f;
                                    i.pos.y = 11*200-300.0f;
                                }
                                if (startpos == 3) {
                                    i.pos.x = 11*200-300.0f;
                                    i.pos.y = 300.0f;
                                }
                                i.health = 5;
                                updatedObjects.add(i);
                            }
                        }
                }
                collisions.collisionlist.clear();
                //send update to all players
            }

            byte[] bufarr = GameServerNetwork.CreateGameUpdatePackage(this);
            //send update packet to all players

            for (var sock : players) {
                if (sock.created==1) {
                    synchronized (sock) {
                        try {
                            sock.out.write(bufarr);
                        } catch (Exception e) {
                          //  e.printStackTrace();
                        }
                    }
                }
            }

            //sleep
            try {
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }

            long newtime = System.currentTimeMillis();
            diff = newtime - currenttime;
            currenttime=newtime;
        }
    }
    public void processmsg()
    {
        for(var msg : msgbuffer)
        {
            int packetsize = msg.length-4;
            ByteBuffer wrapped = ByteBuffer.wrap(msg); // big-endian by default
            int playerid = wrapped.getInt();
            //get player
            PlayerConnection player=players.get(playerid);
            byte[] packetbuf = new byte[packetsize];
            if (show_packet_size || show_packet_binary) {
                System.out.println("packet received");
            }
            if (show_packet_size)
            {
                System.out.println(packetsize);
            }
            if (show_packet_binary) {
                for (var b: msg)
                {
                    System.out.print(b + " ");
                }
                System.out.println();
            }
            wrapped.get(packetbuf);
            int packettype = ServerPacket.GetPacketType(packetbuf);
            wrapped = ByteBuffer.wrap(packetbuf);
            wrapped.get();
            wrapped.get();
            byte[] packetdata = new byte[packetsize-2];
            wrapped.get(packetdata);
            if (packettype == ServerPacket.packettype_cconnect)
            {
                PacketCConnect packet = new PacketCConnect();
                packet.read(packetdata);
                player.name = packet.playername;
                System.out.println("New player connected: " + player.name);
                GameObject newplayer = new GameObject();
                int startpos = player.id%4;
                if (startpos == 0) {
                    newplayer.pos.x = 300.0f;
                    newplayer.pos.y = 300.0f;
                }
                if (startpos == 1) {
                    newplayer.pos.x = 11*200-300.0f;
                    newplayer.pos.y = 11*200-300.0f;
                }
                if (startpos == 2) {
                    newplayer.pos.x = 300.0f;
                    newplayer.pos.y = 11*200-300.0f;
                }
                if (startpos == 3) {
                    newplayer.pos.x = 11*200-300.0f;
                    newplayer.pos.y = 300.0f;
                }
                newplayer.size.x = 50.0f;
                newplayer.size.y = 80.0f;
                newplayer.killable = true;
                newplayer.health = 5;
                newplayer.type = GameObject.gameobjecttype_player;
                gameObjects.add(newplayer);
                player.playerid = newplayer.id;
                byte[] sendmsg = GameServerNetwork.CreateSConnectPacket(this);
                try {
                    System.out.println("Sending ncreate " + gameObjects.size());
                    player.out.write(sendmsg);
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
                //Send new player to other players
                createdObjects.add(newplayer);
                byte[] newplayerpackage = GameServerNetwork.CreateGameUpdatePackage(this);
                for (var i : players)
                {
                    if (i!=player)
                    {
                        try {
                            i.out.write(newplayerpackage);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                createdObjects.clear();

                player.created = 1;
            }
            if (packettype == ServerPacket.packettype_disconnect)
            {
                System.out.println("Player disconnected: " + player.name);
                GameObject p=null;
                for (var i: gameObjects)
                {
                    if (i.id==player.playerid)
                    {
                        p=i;
                    }
                }
                gameObjects.remove(p);
                player.close();
            }
            if(packettype == ServerPacket.packettype_cinput)
            {
                PacketCInput packet = new PacketCInput();
                packet.read(packetdata);
                player.keys[key_a] = packet.keys[key_a];
                player.keys[key_w] = packet.keys[key_w];
                player.keys[key_s] = packet.keys[key_s];
                player.keys[key_d] = packet.keys[key_d];
                player.keys[4] = packet.keys[4];
                player.mx = packet.mx;
                player.my = packet.my;
            }
            if (packettype == ServerPacket.packettype_ctext)
            {
                PacketCText packet = new PacketCText();
                packet.read(packetdata);

                PacketSText pack = new PacketSText();
                pack.msg = packet.msg;
                pack.name = player.name;
                pack.write();

                int sendpacketsize = 2 + pack.packetbuf.length;
                ByteBuffer sendpacketbuf = ByteBuffer.allocate(sendpacketsize);
                sendpacketbuf.put((byte)ServerPacket.servertype.charAt(0));
                sendpacketbuf.put((byte)ServerPacket.sendtext.charAt(0));
                sendpacketbuf.put(pack.packetbuf);
                byte[] sendpacket = sendpacketbuf.array();
                System.out.println();
                for (var i:players)
                {
                    try {
                        i.out.write(sendpacketbuf.array());
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        msgbuffer.clear();
    }

    public void newConnection(PlayerConnection newplayer)
    {
        players.add(newplayer);
    }

    class GameServerConsole extends CancellableRunnable{
        GameServer server;
        GameServerConsole(GameServer s)
        {
            server=s;
        }
        @Override
        public void run() {
            Scanner scanner = new Scanner(System.in);
            while (isRunning())
            {
                String str = scanner.nextLine();
                if (str.equals("exit"))
                {
                    System.out.println("Console close");
                    server.close();
                }
                if (str.equals("show_size"))
                {
                    server.show_packet_size=!server.show_packet_size;
                }

                if (str.equals("show_binary"))
                {
                    server.show_packet_binary=!server.show_packet_binary;
                }

                if (str.equals("show_players"))
                {
                    for (var p : server.players)
                    {
                        System.out.println(new Integer(p.id).toString() + ":" + p.name + " " + p.sock.getInetAddress());
                    }
                }
            }
        }
    }
}
