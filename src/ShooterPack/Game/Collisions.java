package ShooterPack.Game;

import java.util.ArrayList;

//for collision list; a and b object were collided

public class Collisions {
    public static class GameObjectTuple
    {
        public GameObject a;
        public GameObject b;
    }
    public ArrayList<GameObjectTuple> collisionlist;
    public Collisions()
    {
        collisionlist = new ArrayList<>();
    }
    //Rect-Rect collision
    public boolean AABBCollision(GameObject a,GameObject b)
    {
        if (a.pos.x < b.pos.x + b.size.x && a.pos.x+a.size.x > b.pos.x &&
                a.pos.y < b.pos.y + b.size.y && a.pos.y + a.size.y > b.pos.y )
        {
            return true;
        }
        return false;
    }

    public void HandleCollisions(ArrayList<GameObject> arr)
    {
        //loop through all possible object pairs
        for (GameObject obj1: arr)
        {
            if(!obj1.collideable) continue;
            for (GameObject obj2 : arr)
            {
                if (obj1 == obj2) continue;
                if(!obj2.collideable) continue;

                //try to move objects to next frame
                obj1.pos.Add(obj1.vel);
                obj2.pos.Add(obj2.vel);
                boolean wascollision=false;

                //check collision
                wascollision = wascollision = AABBCollision(obj1, obj2);

                //move objects back
                obj1.pos.Sub(obj1.vel);
                obj2.pos.Sub(obj2.vel);

                //if was collision then handle it by removing its velocity velocity
                if (wascollision) {
                    obj1.vel.x = 0.0f;
                    obj1.vel.y = 0.0f;
                    obj2.vel.x = 0.0f;
                    obj2.vel.y = 0.0f;
                    GameObjectTuple newcollision = new GameObjectTuple();
                    newcollision.a = obj1;
                    newcollision.b = obj2;
                    collisionlist.add(newcollision);
                }
            }
        }
    }
}
