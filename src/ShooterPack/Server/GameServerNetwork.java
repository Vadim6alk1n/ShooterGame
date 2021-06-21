package ShooterPack.Server;

import ShooterPack.Game.Packets.PacketGameObject;
import ShooterPack.Game.Packets.PacketSUpdate;
import ShooterPack.Game.Packets.ServerPacket;

import java.nio.ByteBuffer;

public class GameServerNetwork {
    public static byte[] CreateGameUpdatePackage(GameServer game)
    {
        PacketSUpdate packet = new PacketSUpdate();
        for (var it : game.updatedObjects) {
            PacketGameObject pgc = new PacketGameObject();
            pgc.x = it.pos.x;
            pgc.y = it.pos.y;
            pgc.w = it.size.x;
            pgc.h = it.size.y;
            pgc.type = it.type;
            pgc.id = it.id;
            packet.updated.add(pgc);
        }
        //   System.out.println(updatedObjects.size());
        game.updatedObjects.clear();
        for (var it : game.createdObjects) {
            PacketGameObject pgc = new PacketGameObject();
            pgc.x = it.pos.x;
            pgc.y = it.pos.y;
            pgc.w = it.size.x;
            pgc.h = it.size.y;
            pgc.type = it.type;
            pgc.id = it.id;
            packet.created.add(pgc);
        }
        game.createdObjects.clear();
        //  System.out.println(createdObjects.size());
        for (var it : game.killedObjects) {
            PacketGameObject pgc = new PacketGameObject();
            pgc.x = it.pos.x;
            pgc.y = it.pos.y;
            pgc.w = it.size.x;
            pgc.h = it.size.y;
            pgc.type = it.type;
            pgc.id = it.id;
            packet.killed.add(pgc);
        }
        game.killedObjects.clear();
        // System.out.println(killedObjects.size());

        packet.write();
        ByteBuffer buf = ByteBuffer.allocate(packet.packetbuf.length + 2);
        buf.put((byte) 'S');
        buf.put((byte) 'U');
        buf.put(packet.packetbuf);
        return buf.array();
    }
    public static byte[] CreateSConnectPacket(GameServer game)
    {
        int objcount = game.gameObjects.size();
        ByteBuffer bytebuf = ByteBuffer.allocate(2+4+4+ PacketGameObject.packetgameobjectsize*objcount);
        bytebuf.put((byte) ServerPacket.servertype.charAt(0));
        bytebuf.put((byte) ServerPacket.newconnection.charAt(0));
        bytebuf.putInt(objcount);
        bytebuf.putInt(game.gameObjects.size()-1);
        for (var i: game.gameObjects)
        {
            bytebuf.putFloat(i.pos.x);
            bytebuf.putFloat(i.pos.y);
            bytebuf.putFloat(i.size.x);
            bytebuf.putFloat(i.size.y);
            bytebuf.putInt(i.type);
            bytebuf.putInt(i.id);
        }
        return bytebuf.array();
    }
}
