package live.notjacob.net.clients;

import java.net.Socket;

public class Client {

    protected final Socket controlSocket;
    protected final String username;

    public Client(Socket controlSocket, String username) {
        this.controlSocket=controlSocket;
        this.username=username;
    }

    public Socket getControlSocket() {
        return controlSocket;
    }
    public String getUsername() {
        return username;
    }

}
