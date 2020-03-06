package server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class TaskClientConnection implements Runnable {
    private int id;
    private Socket socket;
    private JFXServer server;
    private DataInputStream input;
    private volatile boolean running = true;

    public void terminate() {
        this.running = false;
        try {
            if (input != null) input.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TaskClientConnection(Socket socket, JFXServer server, int id) {
        this.socket = socket;
        this.server = server;
        this.id = id;
    }


    @Override
    public void run() {
        try {
            input = new DataInputStream(socket.getInputStream());

            while (this.running) {
                String message = null;
                try {
                    message = input.readUTF();
                } catch (IOException e) {
                    System.out.println("Looks like client is dead");
                }
                if (message != null && message.indexOf(':') != -1) {
                    String[] temp = message.split(":");
                    String name = temp[0];
                    String cmd = temp[1];
                    switch (cmd) {
                        case ConnectionUtil.CLIENT_ALIVE:
                            this.server.setClientStatus(name, ConnectionUtil.CLIENT_ALIVE);
                            break;
                        case ConnectionUtil.CLIENT_START:
                            this.server.setClientStatus(name, ConnectionUtil.CLIENT_START);
                            break;
                        case ConnectionUtil.CLIENT_STOP:
                            this.server.setClientStatus(name, ConnectionUtil.CLIENT_STOP);
                            break;
                        case ConnectionUtil.CLIENT_EXIT:
                            this.running = false;
                            this.server.setClientStatus(name, ConnectionUtil.CLIENT_EXIT);
                            this.server.removeConnection(this.id);
                            break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public int getId() {
        return id;
    }
}
