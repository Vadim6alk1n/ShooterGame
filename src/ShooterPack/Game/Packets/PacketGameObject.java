package ShooterPack.Game.Packets;

public class PacketGameObject {
    public PacketGameObject()
    {
        x=0;
        y=0;
        w=0;
        h=0;
        type=0;
        id=0;
    }
    public PacketGameObject(float x,float y,float w,float h,int type,int id)
    {
        this.x=x;
        this.y=y;
        this.w=w;
        this.h=h;
        this.type=type;
        this.id = id;
    }

    public static final int packetgameobjectsize = 24;
    public float x;
    public float y;
    public float w;
    public float h;
    public int type;
    public int id;
}
