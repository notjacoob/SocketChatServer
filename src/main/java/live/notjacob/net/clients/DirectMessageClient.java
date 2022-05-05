package live.notjacob.net.clients;

import java.net.Socket;

public class DirectMessageClient extends Client {

    private final Socket directMessageSocket;
    private final String username;

    public DirectMessageClient(Socket controlSocket, Socket directMessageSocket, String username) {
        super(controlSocket, username);
        this.directMessageSocket=directMessageSocket;
    }

    public Socket getDirectMessageSocket() {
        return directMessageSocket;
    }
}
