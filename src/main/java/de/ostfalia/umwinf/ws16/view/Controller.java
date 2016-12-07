package de.ostfalia.umwinf.ws16.view;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Controller for main gol view
 */
public class Controller implements Initializable {

    private static final long HALF_SECOND = 500;
    private long timePeriod = HALF_SECOND;
    private static final int DEFAULT_SIZE = 20;

    private GolGrid golGrid;

    @FXML
    private BorderPane borderPane;
    @FXML
    private Button startButton;
    @FXML
    private TextField timeField;
    @FXML
    private TextField xField;
    @FXML
    private TextField yField;
    @FXML
    private Label patternLabel;
    @FXML
    private Label statusLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        timeField.setText(String.valueOf(timePeriod));
        golGrid = new GolGrid(DEFAULT_SIZE, DEFAULT_SIZE) {
            private long startTime;
            private DateFormat df = new SimpleDateFormat("mm:ss,SSS");

            @Override
            public void onStart() {
                startTime = System.currentTimeMillis();
                startButton.setText("Stop");
                statusLabel.setText("Simulation running");
            }

            @Override
            public void onStop() {
                startButton.setText("Start");
                long timeRun = System.currentTimeMillis() - startTime;
                statusLabel.setText("Simulation ran for " + df.format(new Date(timeRun)));
            }

            @Override
            public void handlePattern(String pattern) {
                patternLabel.setText(pattern);
            }
        };
        xField.setText(String.valueOf(golGrid.getColumns()));
        yField.setText(String.valueOf(golGrid.getRows()));
        borderPane.setCenter(golGrid);
    }

    @FXML
    public void start() {
        if (golGrid.isRunning()) {
            golGrid.stopSimulation();
            return;
        }
        if (timePeriod <= 0)
            new Alert(Alert.AlertType.ERROR, "Enter a valid time period!", ButtonType.OK).show();
        golGrid.startSimulation(timePeriod);
    }

    @FXML
    public void advanceOnce() {
        golGrid.getGameOfLife().advance();
    }

    @FXML
    public void timeChanged() {
        try {
            timePeriod = Long.parseLong(timeField.getText());
            timeField.setStyle("-fx-text-fill: black");
        } catch (NumberFormatException e) {
            timeField.setStyle("-fx-text-fill: red");
            timePeriod = -1;
        }
    }

    @FXML
    public void applyFieldSize() {
        try {
            int x = Integer.parseInt(xField.getText());
            int y = Integer.parseInt(yField.getText());
            golGrid.setFieldSize(x, y);
        } catch (IllegalArgumentException e) {
            new Alert(Alert.AlertType.ERROR, "Enter valid field sizes!", ButtonType.OK).show();
        }
    }

    @FXML
    public void clearField() {
        golGrid.clear();
        statusLabel.setText("");
        patternLabel.setText("");
    }
}
