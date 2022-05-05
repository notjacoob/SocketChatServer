package live.notjacob.net;

import live.notjacob.Server;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class VoiceSocket extends ChatAppSocket {

    public static final List<String> WAITING = new ArrayList<>();
    public static final Map<String, String> VOICE_CHATS = new HashMap<>();

    public VoiceSocket(ExecutorService threadPool, ServerSocket serv, Server servWrapped) {
        super(threadPool, serv, servWrapped);
    }

    public void open() {
        threadPool.submit(() -> {
            System.out.println("[server]: Thread " + Thread.currentThread().getName() + " waiting for voice...");
            Socket client;
            try {
                client = serverSocket.accept();
                open(); // TODO
                AudioInputStream in = AudioSystem.getAudioInputStream(client.getInputStream());
                AudioSystem.write(in, AudioFileFormat.Type.WAVE, client.getOutputStream());
            } catch (IOException | UnsupportedAudioFileException e) {
                e.printStackTrace();
            }
        });
    }


}
