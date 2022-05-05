package live.notjacob;

import live.notjacob.net.NetController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Server {

    private ServerSocket directMessageSocket;
    private ServerSocket voiceSocket;
    private ServerSocket controlSocket;
    private NetController netController;

    public void run() throws IOException {
        directMessageSocket = new ServerSocket(4321);
        voiceSocket = new ServerSocket(4322);
        netController = new NetController(directMessageSocket, voiceSocket, this, 3);
    }
    public static String getOtherValue(String value, Map<String, String> map) {
        if (map.containsKey(value)) {
            return map.get(value);
        } else if (map.containsValue(value)) {
            return map.keySet().stream().filter(s -> s.equals(value)).findFirst().get();
        } else return null;
    }
}