package ShooterPack.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ShooterServer {
    final static int socketport = 55555;
    final static String serverip = "localhost";
    final static int maxclientcount = 4;
    final static int maxthreads = maxclientcount + 3;
    public static void main(String[] args)
    {
        ServerSocket listener;
        ArrayList<CancellableRunnable> pool = new ArrayList<>();
        try {
            //create listener
            listener = new ServerSocket(socketport);
            System.out.println("ShooterServer is starting..");
            //create gameserver and its console
            GameServer game = new GameServer(pool);
            pool.add(game);
            game.start();

            //new connection listener
            CancellableRunnable connectionlistener = new CancellableRunnable() {
                @Override
                public void run() {
                    while(isRunning())
                    {
                        try {
                            Socket newconnection = listener.accept();
                            PlayerConnection newplayer = new PlayerConnection(newconnection,game);
                            game.newConnection(newplayer);
                            pool.add(newplayer);
                            newplayer.start();
                        }catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            };
            pool.add(connectionlistener);
            connectionlistener.start();
            //main thread endlessloop
            while(game.isRunning())
            {
            }
            System.out.println("ShooterPack.Server is closing..");
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            for (var i : pool)
            {
                try
                {
                    i.close();
                    i.join(100);
                }
                catch (Exception e){e.printStackTrace();}
            }
            System.exit(0);
        }
    }
}
