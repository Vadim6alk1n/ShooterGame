package ShooterPack.Server;

public class CancellableRunnable extends Thread {
    public boolean running;
    CancellableRunnable()
    {
        running=true;
    }
    public synchronized void close()
    {
        running=false;
    }
    public synchronized boolean isRunning()
    {
        return running;
    }
}
