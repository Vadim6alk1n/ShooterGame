package ShooterPack.Client;

import ShooterPack.Game.GameObject;
import ShooterPack.Game.Packets.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static ShooterPack.Game.Packets.PacketCInput.*;

public class ShooterNetwork {
    public static void ReadServerNewConnection(ShooterGame game, PacketSConnect packet)
    {
        int n=0;
        for (var i : packet.allobjects)
        {
            if (n==packet.playerid)
            {
                n++;
                continue;
            }
            GameObject newobj = new GameObject();
            newobj.pos.x = i.x;
            newobj.pos.y = i.y;
            newobj.size.x = i.w;
            newobj.size.y = i.h;
            newobj.id = i.id;
            if (i.type == GameObject.gameobjecttype_wall)
            {
                newobj.sprite = game.walltex;
            }
            if (i.type == GameObject.gameobjecttype_bullet)
            {
                newobj.sprite = game.bullettex;
            }
            if (i.type == GameObject.gameobjecttype_enemy)
            {
                newobj.sprite = game.skeletontex;
            }
            if (i.type == GameObject.gameobjecttype_player)
            {
                newobj.sprite = game.playertex;
            }
            game.gameobjects.add(newobj);
            n++;
            System.out.println("newobj");
        }
        game.player = new GameObject();
        PacketGameObject playerobj = packet.allobjects.get(packet.playerid);
        game.player.pos.x = playerobj.x;
        game.player.pos.y = playerobj.y;
        game.player.size.x = playerobj.w;
        game.player.size.y = playerobj.h;
        game.player.id = playerobj.id;
        game.player.sprite = game.playertex;
        game.player.type = GameObject.gameobjecttype_player;
        //game.player.cooldown = 1000;

        //game.player.size.x=50.0f;
        //game.player.size.y=50.0f;
        game.gameobjects.add(game.player);
        System.out.println(game.player.pos.x + ":" + game.player.pos.y);
    }
    public static byte[] CreateInputPacket(ShooterGame game)
    {
        int packetsize = 2 + 5 + 4 + 4;
        ByteBuffer buf = ByteBuffer.allocate(packetsize);
        buf.put((byte) ServerPacket.clienttype.charAt(0));
        buf.put((byte) ServerPacket.clientinput.charAt(0));
        buf.put((byte) (game.keyPressed[key_w]?1:0));
        buf.put((byte) (game.keyPressed[key_a]?1:0));
        buf.put((byte) (game.keyPressed[key_s]?1:0));
        buf.put((byte) (game.keyPressed[key_d]?1:0));
        buf.put((byte) (game.keyPressed[4]?1:0));
        buf.putFloat(game.mouseX + game.camX - game.canvasScreenWidth/2);
        buf.putFloat(game.mouseY + game.camY - game.canvasScreenHeight/2);
        return buf.array();
    }
    public static void ReadServerUpdate(ShooterGame game, PacketSUpdate packet)
    {
        for (var it : packet.updated)
        {
            GameObject obj = null;
            for( var i : game.gameobjects)
            {
                if (i.id == it.id)
                    obj = i;
            }
            if (obj!= null) {
                obj.pos.x = it.x;
                obj.pos.y = it.y;
                obj.size.x = it.w;
                obj.size.y = it.h;
            }
        }
        ArrayList<GameObject> killlist = new ArrayList<>();
        for (var it : packet.killed)
        {
            GameObject obj = null;

            for (var i : game.gameobjects) {
                if (i.id == it.id)
                    obj = i;
            }
            if (obj!= null) {
              killlist.add(obj);
            }
        }
        for (var it : killlist)
        {
            game.gameobjects.remove(it);
        }

        for (var i : packet.created)
        {
            GameObject newobj = new GameObject();
            newobj.pos.x = i.x;
            newobj.pos.y = i.y;
            newobj.size.x = i.w;
            newobj.size.y = i.h;
            newobj.id = i.id;
            if (i.type == GameObject.gameobjecttype_wall)
            {
                newobj.sprite = game.walltex;
            }
            if (i.type == GameObject.gameobjecttype_bullet)
            {
                newobj.sprite = game.bullettex;
            }
            if (i.type == GameObject.gameobjecttype_enemy)
            {
                newobj.sprite = game.skeletontex;
            }
            if (i.type == GameObject.gameobjecttype_player)
            {
                newobj.sprite = game.playertex;
            }
            game.gameobjects.add(newobj);
        }
    }
    public static byte[] CreateCTextPacket(ShooterGame game)
    {
        PacketCText pack = new PacketCText();
        pack.msg = game.textField.getText();
        pack.write();
        int packetsize = 2 + 4 + pack.packetbuf.length;
        ByteBuffer buf = ByteBuffer.allocate(packetsize);
        buf.put((byte) ServerPacket.clienttype.charAt(0));
        buf.put((byte) ServerPacket.clienttext.charAt(0));
        buf.put(pack.packetbuf);
        return buf.array();
    }
}
