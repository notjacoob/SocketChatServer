package live.notjacob.net;

import live.notjacob.Packet;
import live.notjacob.Server;
import live.notjacob.net.clients.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;

public class ControlSocket extends ChatAppSocket {

    public static final List<String> WAITING = new ArrayList<>();
    public static final Map<String, String> CHATS = new HashMap<>();

    private PrintWriter out;
    private BufferedReader in;

    public ControlSocket(ExecutorService threadPool, ServerSocket serv, Server servWrapped) {
        super(threadPool, serv, servWrapped);
    }

    @Override
    public void open() {
        Socket client;
        Client clientWrapped;
        try {
            client= serverSocket.accept();
            out= new PrintWriter(client.getOutputStream(), true);
            in= new BufferedReader(new InputStreamReader(client.getInputStream()));
            NetController.SESSIONS.put(client.getRemoteSocketAddress(), out);
            out.println(new Packet("connected"));
            try {
                String connectionInfo = in.readLine();
                Packet p = Packet.from(connectionInfo);
                if (p.topic().equals("connection info")) {
                    clientWrapped=new Client(client, p.getValue("username"));
                    NetController.USERS.add(clientWrapped);
                    CHATS.put(p.getValue("username"), p.getValue("searchingFor"));
                    out.println(new Packet("connection success"));
                    String chatConfirm=in.readLine();
                    out.println(new Packet("pass sockets"));
                    // TODO let other sockets handle connections, keepalive here
                }
            } catch (IOException e) {
                NetController.SESSIONS.remove(client.getRemoteSocketAddress());
                Optional<Client> clientPresent = NetController.USERS.stream().filter(c -> c.getControlSocket().getRemoteSocketAddress().equals(client.getRemoteSocketAddress())).findFirst();
                if (clientPresent.isPresent()) {
                    clientWrapped=clientPresent.get();
                    if (NetController.USERS.contains(clientWrapped)) {
                            NetController.USERS.remove(clientWrapped);
                            if (ControlSocket.CHATS.containsKey(clientWrapped.getUsername())) {
                                String otherUser = Server.getOtherValue(clientWrapped.getUsername(), ControlSocket.CHATS);

                                Optional<Client> otherClientPresent = NetController.USERS.stream().filter(c -> c.getUsername().equals(otherUser)).findFirst();
                                if (otherClientPresent.isPresent()) {
                                    Client otherClient = otherClientPresent.get();
                                    NetController.SESSIONS.get(otherClient.getControlSocket().getRemoteSocketAddress()).println(new Packet("other disconnect"));
                                }
                                ControlSocket.CHATS.remove(clientWrapped.getUsername());


                            } else if (ControlSocket.CHATS.containsValue(clientWrapped.getUsername())) {
                                String otherUser = Server.getOtherValue(clientWrapped.getUsername(), ControlSocket.CHATS);
                                Optional<Client> otherClientPresent = NetController.USERS.stream().filter(c -> c.getUsername().equals(otherUser)).findFirst();
                                if (otherClientPresent.isPresent()) {
                                    Client otherClient = otherClientPresent.get();
                                    NetController.SESSIONS.get(otherClient.getControlSocket().getRemoteSocketAddress()).println(new Packet("other disconnect"));
                                }
                                ControlSocket.CHATS.remove(Server.getOtherValue(clientWrapped.getUsername(), ControlSocket.CHATS));
                            }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void notifyConnect(String username) {
        NetController.SESSIONS.get(getClient(username).getControlSocket().getRemoteSocketAddress()).println(new Packet("chat start"));
    }

    public static Client getClient(String username) {
        return NetController.USERS.stream().filter(c -> c.getUsername().equals(username)).findFirst().orElse(null);
    }

}
