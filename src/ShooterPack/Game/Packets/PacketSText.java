package ShooterPack.Game.Packets;

import java.nio.ByteBuffer;

public class PacketSText extends ServerPacket{
    public String name;
    public String msg;
    public PacketSText()
    {
        name = new String();
        msg = new String();
    }
    public void read(byte[] packet)
    {
        ByteBuffer wrapped = ByteBuffer.wrap(packet);
        int num = wrapped.getInt();
        System.out.println(num);
        name = new String(packet,6,num*2);
        wrapped.get();
        wrapped.get();
        for (int i=0; i<num*2;i++) wrapped.get();
        int msgnum = wrapped.getInt();
        System.out.println(msgnum);
        try {
            msg = new String(packet, 6+num*2+4+2, msgnum * 2, "UTF-16");
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public void write()
    {
        int namelen = name.length();
        int msglen = msg.length();
        ByteBuffer dbuf = ByteBuffer.allocate(4 + namelen*2+2 + 4 + msglen*2+2);
        dbuf.putInt(namelen);
        try {
            dbuf.put(name.getBytes("UTF-16"));
            dbuf.putInt(msglen);
            dbuf.put(msg.getBytes("UTF-16"));
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        packetbuf = dbuf.array();
    }
}
