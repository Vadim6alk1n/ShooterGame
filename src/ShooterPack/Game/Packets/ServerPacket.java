package ShooterPack.Game.Packets;

public class ServerPacket {

    //ShooterPack.Server packets
    public final static String servertype = "S";
    //ShooterPack.Server packet types
    public final static String newconnection = "N"; //when client connects, he receives game state and its data
    public final static String newplayerconnection = "P"; //when new player is connected to the game
    public final static String mapupdate = "U"; //send updated map
    public final static String sendtext = "T"; //send text to player

    //ShooterPack.Client packets
    public final static String clienttype = "C";
    //ShooterPack.Client packet types
    public final static String clientconnect = "C"; //client is connecting to server, send name
    public final static String clientinput = "I"; //client pressed something, send it to server
    public final static String clienttext = "T"; //client sended message to other players
    public final static String clientdisconnect = "D"; //client sended message to other players

    public final static int packettype_unknown = 0;
    public final static int packettype_snewconnection = 1;
    public final static int packettype_snewplayer = 2;
    public final static int packettype_supdate = 3;
    public final static int packettype_stext = 4;
    public final static int packettype_cconnect = 5;
    public final static int packettype_cinput = 6;
    public final static int packettype_ctext = 7;
    public final static int packettype_disconnect = 8;

    public byte[] packetbuf;
    public static int GetPacketType(byte[] packet)
    {
        if (packet[0]=='S')
        {
            if (packet[1]=='N')
                return packettype_snewconnection;
            if (packet[1]=='P')
                return packettype_snewplayer;
            if(packet[1]=='U')
                return packettype_supdate;
            if(packet[1]=='T')
                return packettype_stext;
        }
        if (packet[0]=='C')
        {
            if (packet[1]=='C')
                return packettype_cconnect;
            if (packet[1]=='I')
                return packettype_cinput;
            if(packet[1]=='T')
                return packettype_ctext;
            if(packet[1]=='D')
                return packettype_disconnect;
        }
        return packettype_unknown;
    }

}
