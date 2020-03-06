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

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class JFXClient extends Application {

    private DataOutputStream output = null;
    private Socket socket = null;
    private static String name = null;
    public static String host = null;

    private Text statusText = new Text();


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


        root.getChildren().addAll(button, this.statusText);

        primaryStage.setTitle(name);
        double WINDOW_WIDTH = 300.0;
        double WINDOW_HEIGHT = 300.0;
        primaryStage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
        primaryStage.setMinWidth(WINDOW_WIDTH);
        primaryStage.setMinHeight(WINDOW_HEIGHT);
        primaryStage.sizeToScene();
        primaryStage.show();

        // try to create socket and send "I'm alive" message to server
        try {
            this.socket = new Socket(host, ConnectionUtil.port);
            this.output = new DataOutputStream(socket.getOutputStream());
            this.statusText.setText("Connected");
            button.setDisable(false);
            this.sendMessage(name + ":" + ConnectionUtil.CLIENT_ALIVE);
        } catch (IOException ex) {
            // if can't set the connection
            this.closeSocket();
            button.setDisable(true);
        }
    }

    private void sendMessage(String msg) {
        if (this.output != null) {
            try {
                this.output.writeUTF(msg);
                this.output.flush();
            } catch (IOException e) {
                this.closeSocket();
            }
        }
    }

    private void closeSocket() {
        this.statusText.setText("Server not found");
        try {
            if (this.output != null) {
                this.output.close();
                this.output = null;
            }
            if (this.socket != null) {
                this.socket.close();
                this.output = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        this.sendMessage(name + ":" + ConnectionUtil.CLIENT_EXIT);
        this.closeSocket();
        super.stop();
        System.out.println("bye");
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
                sendMessage(name + ":" + ConnectionUtil.CLIENT_START);
            } else {
                button.setText("Start");
                button.setStyle("-fx-base: green;");
                sendMessage(name + ":" + ConnectionUtil.CLIENT_STOP);
            }
        }
    }
}
