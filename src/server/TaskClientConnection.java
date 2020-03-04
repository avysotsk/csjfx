package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TaskClientConnection implements Runnable {
    private String name;
    private int status;
    private int id;
    private Socket socket;
    private JFXServer server;
    private DataInputStream input;
    private DataOutputStream output;
    private volatile boolean running = true;

    public void terminate() {
        this.running = false;
        try {
            if (input != null) input.close();
            if (output != null) output.close();
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

    public String getName() {
        return this.name;
    }

    public int getStatus() {
        return this.status;
    }

    @Override
    public void run() {
        try {
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            while (this.running) {
                String message = null;
                try {
                    message = input.readUTF();
                }catch (IOException e) {}
                if (message!=null && message.indexOf(':') != -1) {
                    String[] temp = message.split(":");
                    String name = temp[0];
                    String cmd = temp[1];
                    switch (cmd) {
                        case ConnectionUtil.CLIENT_ALIVE:
                            if (this.name == null) {
                                this.name = name;
                                this.server.refresh();
                            }
                            break;
                        case ConnectionUtil.CLIENT_START:
                            this.status = ConnectionUtil.ON;
                            this.server.refresh();
                            break;
                        case ConnectionUtil.CLIENT_STOP:
                            this.status = ConnectionUtil.OFF;
                            this.server.refresh();
                            break;
                        case ConnectionUtil.CLIENT_EXIT:
                            this.running = false;
                            this.name = null;
                            this.status = ConnectionUtil.OFF;
                            this.server.refresh();
                            this.server.removeConnection(this.id);
                            break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stop() {
        try {
            output.writeUTF(ConnectionUtil.SERVER_STOP);
            this.terminate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }
}
