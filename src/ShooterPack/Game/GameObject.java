package ShooterPack.Game;

import ShooterPack.Game.Vec2f;
import javafx.scene.image.Image;

public class GameObject {
    public static int LastId=0;
    public final static int gameobjecttype_player = 1;
    public final static int gameobjecttype_wall = 2;
    public final static int gameobjecttype_bullet = 3;
    public final static int gameobjecttype_enemy = 4;
//    public final static int gameobjecttype_wall = 2;
  //  public final static int gameobjecttype_bullet = 2;
    public GameObject(Image image)
    {
        init();
        sprite = image;
    }
    public GameObject()
    {
        init();
    }

    public GameObject(float x,float y,float w,float h,int type)
    {
        init();
        pos.x=x;
        pos.y=y;
        size.x=w;
        size.y=h;
        this.type = type;
    }
    public GameObject(Image image,float x,float y,float w,float h)
    {
        init();
        sprite=image;
        pos.x=x;
        pos.y=y;
        size.x=w;
        size.y=h;
    }
    public void init()
    {
        id = LastId;
        LastId++;
        sprite = null;
        collideable = true;
        pos = new Vec2f();
        size = new Vec2f();
        vel = new Vec2f();
        bullet=false;
        health = 1;
        killable=false;
        cooldown=100;
        passedtime=0;
    }
    public int id;
    public int type;
    public Vec2f pos;
    public Vec2f size;
    public Vec2f vel;
    public float health;

    public boolean bullet;
    public boolean killable;

    //for shooting
    public long cooldown;
    public long passedtime;

    public boolean collideable;
    public Image sprite;
}
