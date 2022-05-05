package live.notjacob.net;

import live.notjacob.Packet;
import live.notjacob.Server;
import live.notjacob.net.clients.Client;
import live.notjacob.net.clients.DirectMessageClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DirectMessageSocket extends ChatAppSocket {

    private static final List<DirectMessageClient> USERS = new ArrayList<>();

    public DirectMessageSocket(ExecutorService threadPool, ServerSocket serv, Server servWrapped) {
        super(threadPool, serv, servWrapped);
        ScheduledExecutorService chatRouter = Executors.newSingleThreadScheduledExecutor();
        chatRouter.scheduleAtFixedRate(() -> ControlSocket.WAITING.forEach(s -> {
            String opp = ControlSocket.CHATS.get(s);
            if (USERS.stream().anyMatch(u->u.getUsername().equals(opp))) {
                ControlSocket.WAITING.remove(s);
                DirectMessageSocket.notifyConnect(s);
                DirectMessageSocket.notifyConnect(opp);
            }
        }), 1, 1, TimeUnit.SECONDS);
    }

    public void open() {
        threadPool.submit(() -> {
            Socket client;
            try {
                client = serverSocket.accept();
                PrintWriter out = new PrintWriter(client.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String username = in.readLine();
                Client wrappedClient = NetController.USERS.stream().filter(c -> c.getUsername().equals(username) && c .getControlSocket().getRemoteSocketAddress()==client.getRemoteSocketAddress()).findFirst().orElse(null);
                if (wrappedClient != null) {
                    DirectMessageClient dmClient = new DirectMessageClient(wrappedClient.getControlSocket(), client, username);
                    USERS.add(dmClient);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void messageLoop(String username, BufferedReader r, SocketAddress address) {
        try {
            Packet message = Packet.from(r.readLine());
            System.out.println(Thread.currentThread().getName() + ": moving packet [" + message + "]");
            if (message.topic().equals("message")) {
                String otherUser = Server.getOtherValue(username, ControlSocket.CHATS);
                NetController.SESSIONS.get(getControlSocketFor(otherUser).getRemoteSocketAddress()).println(message);
                messageLoop(username, r, address);
            } else if (message.topic().equals("disconnected")) {
                System.out.println("Disconnect detect");
                String otherUser = Server.getOtherValue(username, ControlSocket.CHATS);
                if (ControlSocket.CHATS.containsKey(username)) {
                    ControlSocket.CHATS.remove(username);
                } else {
                    ControlSocket.CHATS.remove(otherUser);
                }
                Client c = NetController.USERS.stream().filter(cc -> Objects.equals(cc.getUsername(), username)).findFirst().orElse(null);
                Socket sock = getDMSocketFor(username);
                PrintWriter writer = NetController.SESSIONS.get(sock.getRemoteSocketAddress());
                NetController.SESSIONS.remove(sock.getRemoteSocketAddress());
                //NetController.USERS.remove(c);
                writer.println(new Packet("disconnect safe"));
                writer.close();
                sock.close();
                NetController.SESSIONS.get(c.getControlSocket().getRemoteSocketAddress()).println(new Packet("other disconnect"));
            }
        } catch (IOException e) {
            System.out.println(Thread.currentThread().getName() + ": " + e.getMessage());
            NetController.SESSIONS.remove(address);
            if (NetController.USERS.containsKey(username)) {
                NetController.USERS.entrySet().stream().filter(x -> Objects.equals(x.getKey(), username)).findFirst().ifPresent(s -> {
                    if (ControlSocket.CHATS.containsKey(username)) {
                        String otherUser = Server.getOtherValue(username, ControlSocket.CHATS);
                        NetController.SESSIONS.get(NetController.USERS.get(otherUser).getRemoteSocketAddress()).println(new Packet("other disconnect"));
                        ControlSocket.CHATS.remove(username);
                    } else if (ControlSocket.CHATS.containsValue(username)) {
                        String otherUser = Server.getOtherValue(username, ControlSocket.CHATS);
                        NetController.SESSIONS.get(NetController.USERS.get(otherUser).getRemoteSocketAddress()).println(new Packet("other disconnect"));
                        ControlSocket.CHATS.remove(Server.getOtherValue(username, ControlSocket.CHATS));
                    }
                });
            }
        }
    }

    private Socket getControlSocketFor(String username) {
        Optional<Client> client = NetController.USERS.stream().filter(c -> Objects.equals(c.getUsername(), username)).findFirst();
        return client.map(Client::getControlSocket).orElse(null);
    }
    private Socket getDMSocketFor(String username) {
        return USERS.stream().filter(c -> Objects.equals(c.getUsername(), username)).findFirst().map(DirectMessageClient::getDirectMessageSocket).orElse(null);
    }
    public static void notifyConnect(String username) {
        NetController.SESSIONS.get(NetController.USERS.get(username).getRemoteSocketAddress()).println(new Packet("chat start"));
    }

}
