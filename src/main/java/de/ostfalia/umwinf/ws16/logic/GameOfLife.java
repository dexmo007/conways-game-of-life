package de.ostfalia.umwinf.ws16.logic;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Queue;

/**
 * Conway's Game of Life implementation, offers analysis like pattern recognition
 *
 * @author Henrik Drefs
 */
public class GameOfLife extends Observable implements Cloneable {

    private boolean[][] field;
    private long countAdvances = 0;
    private boolean fieldStatic = false;
    private int cyclicPeriod = -1;
    private Queue<boolean[][]> history;
    /**
     * keep track defines how many stages back the game is saved to be available for pattern recognition
     */
    public static final int DEFAULT_KEEP_TRACK = 100;
    private int keepTrack = DEFAULT_KEEP_TRACK;

    /**
     * Constructor for a {@link GameOfLife}
     *
     * @param rows    number of rows
     * @param columns number of columns
     * @throws IllegalArgumentException if {@code rows} or {@code columns} is invalid
     */
    public GameOfLife(int rows, int columns) {
        if (rows <= 0 || columns <= 0)
            throw new IllegalArgumentException("size invalid");

        field = new boolean[rows][columns];
        history = new LinkedList<>();
    }

    /**
     * sets a cell to a given state, resets analysis and notifies observer about the change
     *
     * @param state new state
     * @param x     x-coordinate of cell to change
     * @param y     y-coordinate of cell to change
     */
    public void setCell(boolean state, int x, int y) {
        field[y][x] = state;
        countAdvances = 0;
        fieldStatic = false;
        cyclicPeriod = -1;
        history.clear();
        setChanged();
        notifyObservers(new ObserverArgs(state, x, y));
    }

    public boolean getCell(int x, int y) {
        return field[y][x];
    }

    /**
     * @return an unmodifiable view of the field
     */
    public boolean[][] getField() {
        return Arrays.copyOf(field, field.length);
    }

    public boolean isFieldStatic() {
        return fieldStatic;
    }

    public boolean isRepeating() {
        return cyclicPeriod != -1;
    }

    public int getCyclicPeriod() {
        return cyclicPeriod;
    }

    public long countAdvances() {
        return countAdvances;
    }

    /**
     * advances the field by 1 generation
     */
    public void advance() {
        if (fieldStatic)
            return;

        countAdvances++;
        boolean[][] nextField = new boolean[getRowCount()][getColumnCount()];
        for (int y = 0; y < field.length; y++) {
            boolean[] row = field[y];
            for (int x = 0; x < row.length; x++) {
                boolean nextState = getNextState(x, y);
                nextField[y][x] = nextState;
                setChanged();
                notifyObservers(new ObserverArgs(nextState, x, y));
            }
        }
        if (Arrays.deepEquals(field, nextField) || allDead()) {
            fieldStatic = true;
        } else if (cyclicPeriod == -1) {
            // check for repetitive pattern
            int i = history.size();
            for (boolean[][] old : history) {
                if (Arrays.deepEquals(old, nextField)) {
                    cyclicPeriod = i;
                    break;
                }
                i--;
            }
        }
        // keep track
        if (history.size() >= keepTrack)
            history.poll();
        history.add(nextField);
        this.field = nextField;
    }

    public boolean allDead() {
        for (boolean[] row : field)
            for (boolean isAlive : row)
                if (isAlive)
                    return false;
        return true;
    }

    public long countAlive() {
        long count = 0;
        for (boolean[] row : field)
            for (boolean isAlive : row)
                if (isAlive)
                    count++;
        return count;
    }

    /**
     * @param x x-coordinate of cell to inspect
     * @param y y-coordinate of cell to inspect
     * @return the state of the given cell in the next generation
     */
    private boolean getNextState(int x, int y) {
        // get number of alive neighbors
        int aliveNeighbors = 0;
        for (int row = y - 1; row <= y + 1; row++) {
            for (int col = x - 1; col <= x + 1; col++) {
                // if this cell or out of bounds
                if ((row == y && col == x) || row < 0 || col < 0 || row >= field.length || col >= field[0].length) {
                    continue;
                }
                if (field[row][col]) {
                    aliveNeighbors++;
                }
            }
        }
        return aliveNeighbors == 3 || field[y][x] && aliveNeighbors == 2;
    }

    public int getKeepTrack() {
        return keepTrack;
    }

    public void setKeepTrack(int keepTrack) {
        this.keepTrack = keepTrack;
    }

    public int getRowCount() {
        return field.length;
    }

    public int getColumnCount() {
        if (field.length == 0)
            return 0;
        return field[0].length;
    }

    public GameOfLife clone() {
        try {
            return (GameOfLife) super.clone();
        } catch (CloneNotSupportedException e) {
            // is supported
            return null;
        }
    }

    /**
     * arguments for observer notification
     */
    public static class ObserverArgs {
        private boolean state;
        private int x;
        private int y;

        ObserverArgs(boolean state, int x, int y) {
            this.state = state;
            this.x = x;
            this.y = y;
        }

        public boolean getState() {
            return state;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }
}
