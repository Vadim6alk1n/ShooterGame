package ShooterPack.Game.Packets;

import ShooterPack.Game.GameObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class PacketSConnect extends ServerPacket{
    public int objcount;
    public int playerid;
    public ArrayList<PacketGameObject> allobjects;
    public PacketSConnect()
    {
        allobjects = new ArrayList<>();
    }
    public void read(byte[] packet)
    {
        ByteBuffer wrapped = ByteBuffer.wrap(packet);
        objcount = wrapped.getInt();
        playerid = wrapped.getInt();
        allobjects = new ArrayList<>();
        for (int i=0;i<objcount;i++)
        {
            PacketGameObject newobj = new PacketGameObject();
            newobj.x = wrapped.getFloat();
            newobj.y = wrapped.getFloat();
            newobj.w = wrapped.getFloat();
            newobj.h = wrapped.getFloat();
            newobj.type = wrapped.getInt();
            newobj.id = wrapped.getInt();
            allobjects.add(newobj);
        }
    }
    public void write()
    {
        //int len = playername.length();
        objcount = allobjects.size();
        ByteBuffer arrbuf = ByteBuffer.allocate(8+PacketGameObject.packetgameobjectsize*objcount);
        arrbuf.putInt(objcount);
        arrbuf.putInt(playerid);
        for(PacketGameObject i : allobjects)
        {
            arrbuf.putFloat(i.x);
            arrbuf.putFloat(i.y);
            arrbuf.putFloat(i.w);
            arrbuf.putFloat(i.h);
            arrbuf.putInt(i.type);
            arrbuf.putInt(i.id);
        }
        packetbuf = arrbuf.array();
    }
}
