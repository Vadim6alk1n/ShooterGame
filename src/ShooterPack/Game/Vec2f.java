package ShooterPack.Game;

abstract class Vec2<T> {
    public T x;
    public T y;
    public Vec2(){}
    public Vec2(Vec2<T> vec){x=vec.x;y=vec.y;};
    public abstract void Add(Vec2<T> vec);
}

public class Vec2f extends Vec2<Float>{
    public Vec2f() {x=0.0f;y=0.0f;}
    public Vec2f(Vec2<Float> vec){super(vec);};
    public void Add(Vec2<Float> vec)
    {
        x+=vec.x;
        y+=vec.y;
    }
    public void Sub(Vec2<Float> vec)
    {
        x-=vec.x;
        y-=vec.y;
    }
    public void ReduceLength(Float l)
    {
        float len = (float)Math.sqrt(x*x+y*y);
        if(len <= 0.0001) return;
        len = (len-l)/len;
        x*=len;
        y*=len;
    }
    public void Normalize()
    {
        float len = (float)Math.sqrt(x*x+y*y);
        if (len!=0)
        {
            x/=len;
            y/=len;
        }
    }
    public float Length()
    {
        return (float)Math.sqrt(x*x+y*y);
    }
    public void Turn()
    {
        x=-x;
        y=-y;
    }
}
class Vec2i extends Vec2<Integer>{
    Vec2i() {x=0;y=0;}
    public Vec2i(Vec2<Integer> vec){super(vec);};
    public void Add(Vec2<Integer> vec)
    {
        x+=vec.x;
        y+=vec.y;
    }
}

