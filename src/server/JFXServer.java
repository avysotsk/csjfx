package server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class JFXServer extends Application {
    private List<TaskClientConnection> connectionList = new ArrayList<>();
    private GridPane gridpane;
    private volatile boolean running = true;
    private ServerSocket serverSocket;
    private String[] clients = {
            "Masha",
            "Petr",
            "Olga",
            "Julia",
            "Marina",
            "Anton",
            "Oleg",
            "Vasily",
            "Tamara",
            "Linia",
            "Nikolay",
            "Alena",
            "Andrew"
    };

    private HBox createClient(String clientName) {
        Label name = new Label(clientName);
        name.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
        name.setAlignment(Pos.CENTER_RIGHT);
        Label status = new Label(ConnectionUtil.CLIENT_EXIT);
        status.setTextFill(Color.GRAY);
        status.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
        status.setAlignment(Pos.CENTER_LEFT);

        HBox hBox = new HBox(10);
        hBox.getChildren().addAll(name, status);

        return hBox;
    }

    private HBox getClient(String clientName) {
        int idx = -1;
        for (int i = 0; i < clients.length; i++) {
            if (clients[i].equals(clientName)) {
                idx = i;
                break;
            }
        }
        if (idx == -1) {
            return null;
        } else {
            return (HBox) this.gridpane.getChildren().get(idx);
        }
    }

    private String getServerIP() {
        String IP;
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("google.com", 80));
            IP = socket.getLocalAddress().getHostAddress();
            socket.close();
        } catch (IOException e) {
            IP = "";
        }
        return IP;
    }


    @Override
    public void stop() {
        for (TaskClientConnection taskClientConnection : connectionList) {
            taskClientConnection.terminate();
        }
        connectionList.clear();
        this.running = false;
        if (this.serverSocket != null) {
            try {
                this.serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox();

        this.gridpane = new GridPane();
        this.gridpane.setHgap(10);
        this.gridpane.setVgap(10);

        int ii = (int) Math.sqrt(this.clients.length);
        if (ii * ii < this.clients.length) {
            ii++;
        }
        for (int i = 0; i < this.clients.length; i++) {
            this.gridpane.add(this.createClient(this.clients[i]), i % ii, i / ii);
        }

        root.getChildren().addAll(this.gridpane);

        primaryStage.setTitle("My server: " + this.getServerIP());
        double WINDOW_WIDTH = 500.0;
        double WINDOW_HEIGHT = 500.0;
        primaryStage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
        primaryStage.setMinWidth(WINDOW_WIDTH);
        primaryStage.setMinHeight(WINDOW_HEIGHT);
        primaryStage.sizeToScene();
        primaryStage.show();

        new Thread(() -> {
            try {
                this.serverSocket = new ServerSocket(ConnectionUtil.port);

                while (this.running) {
                    Socket socket = null;
                    try {
                        socket = this.serverSocket.accept();
                    } catch (IOException ex) {
                        System.out.println("bye");
                    }
                    if (socket != null) {
                        TaskClientConnection connection = new TaskClientConnection(socket, this, connectionList.size());
                        System.out.println("new connecton");
                        connectionList.add(connection);

                        //create a new thread
                        Thread thread = new Thread(connection);
                        thread.start();
                    }
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }


    public void removeConnection(int id) {
        int idx = -1;
        for (int i = 0; i < this.connectionList.size(); i++) {
            if (connectionList.get(i).getId() == id) {
                idx = i;
                break;
            }
        }
        this.connectionList.remove(idx);
    }

    public void setClientStatus(String name, String status) {
        Platform.runLater(() -> {
            HBox uiclient = this.getClient(name);
            if (uiclient != null) {
                Label label = (Label) uiclient.getChildren().get(1);
                label.setText(status);
                switch (status) {
                    case ConnectionUtil.CLIENT_ALIVE:
                        label.setTextFill(Color.BLUE);
                        break;
                    case ConnectionUtil.CLIENT_START:
                        label.setTextFill(Color.GREEN);
                        break;
                    case ConnectionUtil.CLIENT_STOP:
                        label.setTextFill(Color.RED);
                        break;
                    case ConnectionUtil.CLIENT_EXIT:
                        label.setTextFill(Color.GRAY);
                        break;
                }
            }

        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}

