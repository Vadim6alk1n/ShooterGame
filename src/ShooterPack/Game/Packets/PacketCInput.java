package ShooterPack.Game.Packets;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class PacketCInput extends ServerPacket{
    public static final int key_w = 0;
    public static final int key_a = 1;
    public static final int key_s = 2;
    public static final int key_d = 3;
    public static final int mouse_left = 4;

    public int keycount;
    public float mx;
    public float my;
    public boolean keys[];

    public PacketCInput()
    {
        keys = new boolean[5];
    }
    public void read(byte[] packet)
    {
        ByteBuffer wrapped = ByteBuffer.wrap(packet);
        if(wrapped.get()==0) keys[key_w] = false; else keys[key_w] = true;
        if(wrapped.get()==0) keys[key_a] = false; else keys[key_a] = true;
        if(wrapped.get()==0) keys[key_s] = false; else keys[key_s] = true;
        if(wrapped.get()==0) keys[key_d] = false; else keys[key_d] = true;
        if(wrapped.get()==0) keys[4] = false; else keys[4] = true;

        mx = wrapped.getFloat();
        my = wrapped.getFloat();
    }

    public void write()
    {
        ByteBuffer arrbuf = ByteBuffer.allocate(5+4+4);
        for (var v : keys)
        {
            if (v) arrbuf.put((byte)1);
            else arrbuf.put((byte)0);
        }
        arrbuf.putFloat(mx);
        arrbuf.putFloat(my);
        packetbuf = arrbuf.array();
    }


}
