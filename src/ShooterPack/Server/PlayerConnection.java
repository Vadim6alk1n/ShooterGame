package ShooterPack.Server;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class PlayerConnection extends CancellableRunnable {
    public char id;
    private static char LastID=0;
    public InputStream in;
    public OutputStream out;
    GameServer game;
    public String name;
    public boolean keys[];
    public int playerid;
    float mx;
    float my;
    public int created;
    Socket sock;

    public PlayerConnection(Socket socket,GameServer game)
    {
        id=LastID;
        LastID++;
        this.game = game;
        created = 0;
        keys = new boolean[5];
        sock = socket;
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
        }catch (Exception e){e.printStackTrace();}
    }
    @Override
    public void run() {
        byte[] packetsizebuf = new byte[4];
        while(isRunning())
        {
            try {
                byte[] packet = new byte[1024];
                //Read size of packet
                int packetsize = in.read(packet);
                //add to packet at the beginning playerid
                ByteBuffer dbuf = ByteBuffer.allocate(4+packetsize);
                dbuf.putInt(id);
                dbuf.put(packet,0,packetsize);
                //send packet to gameserver
                game.pushmsg(dbuf.array());
            }catch (SocketException e)
            {
                break;
            } catch ( Exception e)
            {
                e.printStackTrace();
            }

        }
    }

}
