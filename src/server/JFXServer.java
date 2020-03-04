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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class JFXServer extends Application {
    private List<TaskClientConnection> connectionList = new ArrayList<>();
    private VBox root;
    private volatile boolean running = true;
    private ServerSocket serverSocket;


    private GridPane createGridPane() {
        GridPane gridpane = new GridPane();
        gridpane.setHgap(10);
        gridpane.setVgap(10);

        int size = 0;
        for (TaskClientConnection taskClientConnection : connectionList) {
            if (taskClientConnection.getName() != null) {
                size++;
            }
        }
        int ii = (int) Math.sqrt(size);
        if (ii * ii < size) {
            ii++;
        }
        for (int i = 0; i < size; i++) {
            TaskClientConnection connection = this.connectionList.get(i);
            Label name = new Label(connection.getName());
            name.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
            name.setAlignment(Pos.CENTER_RIGHT);
            Label status = new Label(connection.getStatus() == ConnectionUtil.ON ? "ON" : "OFF");
            status.setTextFill(connection.getStatus() == ConnectionUtil.ON ? Color.GREEN : Color.GRAY);
            status.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
            status.setAlignment(Pos.CENTER_LEFT);

            HBox hBox = new HBox(10);
            hBox.getChildren().addAll(name, status);

            gridpane.add(hBox, i % ii, i / ii);
        }
        return gridpane;
    }

    @Override
    public void stop() {
        for (TaskClientConnection taskClientConnection : connectionList) {
            taskClientConnection.stop();
        }
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
        root = new VBox();
        root.getChildren().addAll(createGridPane());

        primaryStage.setTitle("My server");
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
                    Socket socket=null;
                    try {
                        socket = this.serverSocket.accept();
                    } catch (IOException ex) {
                        System.out.println("bye");
                    }
                    if (socket !=null) {
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

    public void refresh() {
        Platform.runLater(() -> {
            root.getChildren().clear();
            root.getChildren().addAll(createGridPane());
        });
    }

    public void removeConnection(int id) {
        int idx = -1;
        for (int i = 0; i < this.connectionList.size(); i++) {
            if(connectionList.get(i).getId()==id){
                idx=i;
                break;
            }

        }
        this.connectionList.remove(idx);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

