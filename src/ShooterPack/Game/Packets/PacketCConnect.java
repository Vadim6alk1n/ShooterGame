package ShooterPack.Game.Packets;

import java.nio.ByteBuffer;

public class PacketCConnect extends ServerPacket {
    public String playername;
    public PacketCConnect()
    {
        playername = new String();
    }
    public void read(byte[] packet)
    {
        ByteBuffer wrapped = ByteBuffer.wrap(packet); // big-endian by default
        int num = wrapped.getInt(); // length of the name
        playername = new String(packet,6,num*2);
    }
    public void write()
    {
        int len = playername.length();
        ByteBuffer dbuf = ByteBuffer.allocate(4 + len*2+2);
        dbuf.putInt(len);
        try {
            dbuf.put(playername.getBytes("UTF-16"));
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        packetbuf = dbuf.array();
    }
}
