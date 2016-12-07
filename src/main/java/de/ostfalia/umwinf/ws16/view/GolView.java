package de.ostfalia.umwinf.ws16.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * @author Henrik Drefs
 */
public class GolView extends Application {

    public static final int WINDOW_SIZE = 600;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/main.fxml"));
        stage.setTitle("Conway's Game Of Life");
        stage.setScene(new Scene(root, WINDOW_SIZE, WINDOW_SIZE));
        stage.show();
    }
}
