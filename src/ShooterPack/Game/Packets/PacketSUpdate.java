package ShooterPack.Game.Packets;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class PacketSUpdate extends ServerPacket {
    public ArrayList<PacketGameObject> killed;
    public ArrayList<PacketGameObject> updated;
    public ArrayList<PacketGameObject> created;
    public PacketSUpdate()
    {
        killed = new ArrayList<>();
        updated = new ArrayList<>();
        created = new ArrayList<>();
    }
    public void read(byte[] packet)
    {
        ByteBuffer wrapped = ByteBuffer.wrap(packet);

        int killsize = wrapped.getInt();
        killed = new ArrayList<>();
        for (int i=0;i<killsize;i++)
        {
            PacketGameObject newobj = new PacketGameObject();
            newobj.x = wrapped.getFloat();
            newobj.y = wrapped.getFloat();
            newobj.w = wrapped.getFloat();
            newobj.h = wrapped.getFloat();
            newobj.type = wrapped.getInt();
            newobj.id = wrapped.getInt();
            killed.add(newobj);
        }
        int updatesize = wrapped.getInt();
        updated = new ArrayList<>();
        for (int i=0;i<updatesize;i++)
        {
            PacketGameObject newobj = new PacketGameObject();
            newobj.x = wrapped.getFloat();
            newobj.y = wrapped.getFloat();
            newobj.w = wrapped.getFloat();
            newobj.h = wrapped.getFloat();
            newobj.type = wrapped.getInt();
            newobj.id = wrapped.getInt();
            updated.add(newobj);
        }
        int createsize = wrapped.getInt();
        created = new ArrayList<>();
        for (int i=0;i<createsize;i++)
        {
            PacketGameObject newobj = new PacketGameObject();
            newobj.x = wrapped.getFloat();
            newobj.y = wrapped.getFloat();
            newobj.w = wrapped.getFloat();
            newobj.h = wrapped.getFloat();
            newobj.type = wrapped.getInt();
            newobj.id = wrapped.getInt();
            created.add(newobj);
        }
    }
    public void write()
    {
        //int len = playername.length();
        int killsize = killed.size();
        int updatesize = updated.size();
        int createsize = created.size();

        ByteBuffer arrbuf = ByteBuffer.allocate(4+PacketGameObject.packetgameobjectsize*killsize + 4 +
                PacketGameObject.packetgameobjectsize*updatesize + 4 + PacketGameObject.packetgameobjectsize*createsize);
        arrbuf.putInt(killsize);
        for(PacketGameObject i : killed)
        {
            arrbuf.putFloat(i.x);
            arrbuf.putFloat(i.y);
            arrbuf.putFloat(i.w);
            arrbuf.putFloat(i.h);
            arrbuf.putInt(i.type);
            arrbuf.putInt(i.id);
        }
        arrbuf.putInt(updatesize);
        for(PacketGameObject i : updated)
        {
            arrbuf.putFloat(i.x);
            arrbuf.putFloat(i.y);
            arrbuf.putFloat(i.w);
            arrbuf.putFloat(i.h);
            arrbuf.putInt(i.type);
            arrbuf.putInt(i.id);
        }
        arrbuf.putInt(createsize);
        for(PacketGameObject i : created)
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
