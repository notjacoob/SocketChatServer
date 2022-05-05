package live.notjacob.net;

import live.notjacob.Server;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;

public abstract class ChatAppSocket {
    protected final ExecutorService threadPool;
    protected final ServerSocket serverSocket;
    protected final Server servWrapped;

    public ChatAppSocket(ExecutorService threadPool, ServerSocket serv, Server servWrapped) {
        this.threadPool=threadPool;
        this.serverSocket=serv;
        this.servWrapped=servWrapped;
    }

    abstract void open();
}
