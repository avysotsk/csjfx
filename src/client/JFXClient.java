package client;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import server.ConnectionUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class JFXClient extends Application {

    private DataOutputStream output;
    private DataInputStream input;
    private Socket socket;
    private ClientReadThread runnable;
    private static String name = null;
    public static String host = null;


    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));
        root.setSpacing(5);

        ToggleButton button = new ToggleButton("Start");
        button.setStyle("-fx-base: green;");
        button.setDisable(true);
        button.setOnAction(new ButtonListener());
        Text text = new Text("Connecting .....");

        root.getChildren().addAll(button, text);

        primaryStage.setTitle(name);
        double WINDOW_WIDTH = 300.0;
        double WINDOW_HEIGHT = 300.0;
        primaryStage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
        primaryStage.setMinWidth(WINDOW_WIDTH);
        primaryStage.setMinHeight(WINDOW_HEIGHT);
        primaryStage.sizeToScene();
        primaryStage.show();

        this.runnable = new ClientReadThread(text, button);
        Thread thread = new Thread(runnable);
        thread.start();
    }

    @Override
    public void stop() {
        try {
            this.runnable.running = false;
            if (output != null) {
                output.writeUTF(name + ":" + ConnectionUtil.CLIENT_EXIT);
                output.flush();
                output.close();
            }
            if (input != null) input.close();
            if (socket != null) socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        host=args[0];
        name=args[1];
        launch(args);
    }

    private class ButtonListener implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent event) {
            ToggleButton button = (ToggleButton) event.getTarget();
            if (button.isSelected()) {
                button.setText("Stop");
                button.setStyle("-fx-base: red;");
                try {
                    output.writeUTF(name + ":" + ConnectionUtil.CLIENT_START);
                    output.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                button.setText("Start");
                button.setStyle("-fx-base: green;");
                try {
                    output.writeUTF(name + ":" + ConnectionUtil.CLIENT_STOP);
                    output.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ClientReadThread implements Runnable {
        private Text text;
        private ToggleButton button;
        private volatile boolean running = true;

        public void terminateByServer() {
            this.running = false;
            this.text.setText("Server not found");
            this.button.setDisable(true);
            try {
                if (input != null) {
                    input.close();
                    input = null;
                }
                if (output != null) {
                    output.close();
                    output = null;
                }
                if (socket != null) {
                    socket.close();
                    output = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            boolean connected = false;
            int i = 1;
            while (!connected && i < 10 && this.running) {
                try {
                    this.text.setText("Connecting ..... " + i);
                    socket = new Socket(host, ConnectionUtil.port);
                    this.text.setText("Connected");
                    this.button.setDisable(false);
                    output = new DataOutputStream(socket.getOutputStream());
                    input = new DataInputStream(socket.getInputStream());

                    output.writeUTF(name + ":" + ConnectionUtil.CLIENT_ALIVE);
                    output.flush();

                    connected = true;
                } catch (IOException ex) {
                    i++;
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ex.printStackTrace();
                }
            }
            if (connected) {
                while (this.running) {
                    try {
                        String message = null;
                        try {
                            message = input.readUTF();
                        } catch (IOException e) {
                            System.out.println("bye");
                        }
                        if (message != null) {
                            switch (message) {
                                case ConnectionUtil.SERVER_PING:
                                    output.writeUTF(name + ":" + ConnectionUtil.CLIENT_ALIVE);
                                    output.flush();
                                    break;
                                case ConnectionUtil.SERVER_STOP:
                                    this.terminateByServer();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                this.text.setText("Server not found");
            }
        }

        public ClientReadThread(Text text, ToggleButton button) {
            this.text = text;
            this.button = button;
        }
    }
}
