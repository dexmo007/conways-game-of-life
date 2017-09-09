package de.ostfalia.umwinf.ws16.view;

import de.ostfalia.umwinf.ws16.logic.GameOfLife;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.Observable;
import java.util.Observer;

/**
 * displays a {@link GameOfLife} in a {@link GridPane} wrapped by a {@link BorderPane} using colored cells
 */
public abstract class GolGrid extends BorderPane implements Observer {

    private GameOfLife gol;
    private Rectangle[][] rectangles;
    private GridPane backingGrid;
    private int columns;
    private int rows;
    /**
     * simulation running flag
     */
    private boolean running = false;
    /**
     * copies the GoL instance when simulation is started, so it can be resetted
     */
    private GameOfLife copy;

    private static final int STROKE_WIDTH = 2;

    public GolGrid(int columns, int rows) {
        setFieldSize(columns, rows);
    }

    public abstract void onStart();

    public abstract void onStop();

    public abstract void handlePattern(String pattern);

    /**
     * ensures that message handling is invoked by an FX Application thread
     *
     * @param pattern message to be handled
     */
    private void handlePatternInternal(String pattern) {
        Platform.runLater(() -> handlePattern(pattern));
    }

    /**
     * starts a simulation that advances every {@code period}
     *
     * @param period period between advances
     * @throws IllegalStateException    if a simulation is already running
     * @throws IllegalArgumentException if period is invalid (less or equal to 0)
     */
    public void startSimulation(long period) {
        if (running)
            throw new IllegalStateException("Simulation already running");
        if (period <= 0)
            throw new IllegalArgumentException("Invalid period");
        copy = gol.clone();
        handlePattern("");
        // remove grid lines for simulation
        for (Rectangle[] row : rectangles)
            for (Rectangle rectangle : row)
                rectangle.setStroke(deadColor());
        running = true;
        Thread t = new Thread(new Task<Void>() {
            private boolean repeating = false;

            @Override
            protected Void call() throws Exception {
                Thread.sleep(period);
                while (running) {
                    gol.advance();

                    if (gol.allDead()) {
                        running = false;
                        handlePatternInternal(String.format("extinct (after %d)", gol.countAdvances()));
                        break;
                    }
                    if (gol.isFieldStatic()) {
                        running = false;
                        handlePatternInternal(String.format("static (after %d)", gol.countAdvances()));
                        break;
                    }
                    if (gol.isRepeating() && !repeating) {
                        repeating = true;
                        handlePatternInternal(String.format(
                                "cyclic (period: %d, after %d)", gol.getCyclicPeriod(), gol.countAdvances() - gol.getCyclicPeriod()));
                    }
                    Thread.sleep(period);
                }
                for (Rectangle[] row : rectangles)
                    for (Rectangle rectangle : row)
                        rectangle.setStroke(aliveColor());
                return null;
            }

            @Override
            protected void succeeded() {
                onStop();
            }
        });
        t.setDaemon(true);
        t.start();
        onStart();
    }

    /**
     * stops a running simulation
     */
    public void stopSimulation() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * re-sizes the field
     *
     * @throws IllegalStateException    if a simulation is running
     * @throws IllegalArgumentException x or y is invalid (less than or equal to 0)
     */
    public void setFieldSize(int x, int y) {
        if (running)
            throw new IllegalStateException("simulation is running");
        if (x <= 0 || y <= 0)
            throw new IllegalArgumentException("size invalid");
        this.columns = x;
        this.rows = y;
        applyField();
    }

    /**
     * clears the field
     *
     * @throws IllegalStateException if a simulation is running
     */
    public void clear() {
        if (running)
            throw new IllegalStateException("simulation is running");

        applyField();
    }

    public void reset() {
        if (running)
            stopSimulation();
        if (copy != null)
            applyField(copy);
    }

    /**
     * creates a new {@link GridPane} and a new {@link GameOfLife} for the changed size
     */
    public void applyField(final GameOfLife gol) {
        backingGrid = new GridPane();
        setCenter(backingGrid);
        backingGrid.layoutBoundsProperty().addListener(new ChangeListener<Bounds>() {
            @Override
            public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
                if (newValue.getHeight() <= 0.0)
                    return;

                GolGrid.this.gol = gol;
                gol.addObserver(GolGrid.this);
                boolean[][] field = gol.getField();
                rows = gol.getRowCount();
                columns = gol.getColumnCount();
                rectangles = new Rectangle[rows][columns];
                final ClickHandler clickHandler = new ClickHandler();
                double rectSize = calcRectangleSize(newValue.getHeight(), newValue.getWidth());
                for (int i = 0; i < field.length; i++) {
                    boolean[] row = field[i];
                    Rectangle[] rectRow = new Rectangle[columns];
                    for (int j = 0; j < row.length; j++) {
                        Rectangle r = new Rectangle(rectSize, rectSize, colorOf(field[i][j]));
                        r.setStroke(aliveColor());
                        // save coordinates in node's id
                        r.setId(i + ":" + j);
                        r.addEventHandler(MouseEvent.MOUSE_CLICKED, clickHandler);
                        rectRow[j] = r;
                    }
                    rectangles[i] = rectRow;
                    backingGrid.addRow(i, (Node[]) rectRow);
                }
                backingGrid.layoutBoundsProperty().removeListener(this);

            }
        });
    }

    private void applyField() {
        applyField(new GameOfLife(rows, columns));
    }

    /**
     * @param height available height for grid
     * @param width  available width for grid
     * @return resulting size for each rectangle
     */
    private double calcRectangleSize(double height, double width) {
        double maxHeight = (height - rows * STROKE_WIDTH) / rows;
        double maxWidth = (width - columns * STROKE_WIDTH) / columns;
        return Math.min(maxHeight, maxWidth);
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    public GameOfLife getGameOfLife() {
        return gol;
    }

    /**
     * called on {@code gameOfLife.setCell(state, x, y)}, updates the color of the referenced cell
     *
     * @param o   game instance
     * @param arg changed cell arguments
     */
    @Override
    public void update(Observable o, Object arg) {
        GameOfLife.ObserverArgs args = (GameOfLife.ObserverArgs) arg;
        rectangles[args.getY()][args.getX()].setFill(colorOf(args.getState()));
    }

    /**
     * converts state to a display {@link Color}, may be overwritten for customization
     */
    final public Color colorOf(boolean state) {
        return state ? aliveColor() : deadColor();
    }

    protected Color aliveColor() {
        return Color.BLACK;
    }

    protected Color deadColor() {
        return Color.TRANSPARENT;
    }

    /**
     * handles the toggling of cells via mouse click
     */
    private class ClickHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            // don't change field during simulation
            if (running) {
                return;
            }
            // get coordinates from the source's id
            Rectangle rect = (Rectangle) event.getSource();
            String[] splitId = rect.getId().split(":");
            int i = Integer.parseInt((splitId[0]));
            int j = Integer.parseInt(splitId[1]);
            boolean current = gol.getCell(j, i);
            gol.setCell(!current, j, i);
        }
    }

}
