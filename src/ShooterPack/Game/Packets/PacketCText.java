package ShooterPack.Game.Packets;

import java.nio.ByteBuffer;

public class PacketCText extends ServerPacket{
    public String msg;
    public PacketCText()
    {
        msg = new String();
    }
    public void read(byte[] packet)
    {
        ByteBuffer wrapped = ByteBuffer.wrap(packet); // big-endian by default
        int num = wrapped.getInt(); // 1
        try {
            msg = new String(packet, 6, num * 2, "UTF-16");
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public void write()
    {
        int len = msg.length();
        ByteBuffer dbuf = ByteBuffer.allocate(4 + len*2+2);
        dbuf.putInt(len);
        try {
            dbuf.put(msg.getBytes("UTF-16"));
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        packetbuf = dbuf.array();
    }
}
