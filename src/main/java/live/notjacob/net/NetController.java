package live.notjacob.net;

import live.notjacob.Server;
import live.notjacob.net.clients.Client;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NetController {
    private final List<DirectMessageSocket> sockets = new ArrayList<>();
    public static final List<Client> USERS = new ArrayList<>();
    public static final Map<SocketAddress, PrintWriter> SESSIONS = new HashMap<>();

    public NetController(ServerSocket controlSock, ServerSocket voice, Server serv, int numDirectMessageSockets) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < numDirectMessageSockets; i++) {
            DirectMessageSocket sock = new DirectMessageSocket(executorService, controlSock, serv);
            sock.open();
            sockets.add(sock);
        }

    }

}
