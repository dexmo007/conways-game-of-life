package de.ostfalia.umwinf.ws16.logic;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Conway's Game of Life implementation, offers analysis like pattern recognition
 *
 * @author Henrik Drefs
 */
public class GameOfLife extends Observable {

    private GolState[][] field;
    private long countAdvances = 0;
    private boolean fieldStatic = false;
    private int cyclicPeriod = -1;
    private Queue<GolState[][]> history;
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

        field = GolState.field(rows, columns);
        history = new LinkedList<>();
    }

    /**
     * sets a cell to a given state, resets analysis and notifies observer about the change
     *
     * @param state new state
     * @param x     x-coordinate of cell to change
     * @param y     y-coordinate of cell to change
     */
    public void setCell(GolState state, int x, int y) {
        field[y][x] = state;
        countAdvances = 0;
        fieldStatic = false;
        cyclicPeriod = -1;
        history.clear();
        setChanged();
        notifyObservers(new ObserverArgs(state, x, y));
    }

    public GolState getCell(int x, int y) {
        return field[y][x];
    }

    /**
     * @return an unmodifiable view of the field
     */
    public List<List<GolState>> getField() {
        List<List<GolState>> tmp = Arrays.stream(field)
                .map(row -> Collections.unmodifiableList(
                        Arrays.asList(row)))
                .collect(Collectors.toList());
        return Collections.unmodifiableList(tmp);
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
        GolState[][] nextField = GolState.field(field.length, field[0].length);
        for (int y = 0; y < field.length; y++) {
            GolState[] row = field[y];
            for (int x = 0; x < row.length; x++) {
                GolState nextState = getNextState(x, y);
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
            for (GolState[][] old : history) {
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
        return Arrays.stream(field).allMatch(row -> Arrays.stream(row).allMatch(c -> c == GolState.DEAD));
    }

    public long countAlive() {
        return Arrays.stream(field).mapToLong(r -> Arrays.stream(r).filter(s -> s == GolState.ALIVE).count()).sum();
    }

    /**
     * @param x x-coordinate of cell to inspect
     * @param y y-coordinate of cell to inspect
     * @return the state of the given cell in the next generation
     */
    private GolState getNextState(int x, int y) {
        // get number of alive neighbors
        int aliveNeighbors = 0;
        for (int row = y - 1; row <= y + 1; row++)
            for (int col = x - 1; col <= x + 1; col++) {
                // if this cell or out of bounds
                if ((row == y && col == x) || row < 0 || col < 0 || row >= field.length || col >= field[0].length)
                    continue;
                if (field[row][col] == GolState.ALIVE)
                    aliveNeighbors++;
            }

        switch (getCell(x, y)) {
            case DEAD:
                if (aliveNeighbors == 3) {
                    return GolState.ALIVE;
                }
                return GolState.DEAD;
            case ALIVE:
                switch (aliveNeighbors) {
                    case 0:
                    case 1:
                        return GolState.DEAD;
                    case 2:
                    case 3:
                        return GolState.ALIVE;
                    default:
                        return GolState.DEAD;
                }
        }
        return GolState.DEAD;
    }

    public int getKeepTrack() {
        return keepTrack;
    }

    public void setKeepTrack(int keepTrack) {
        this.keepTrack = keepTrack;
    }

    /**
     * arguments for observer notification
     */
    public static class ObserverArgs {
        private GolState state;
        private int x;
        private int y;

        ObserverArgs(GolState state, int x, int y) {
            this.state = state;
            this.x = x;
            this.y = y;
        }

        public GolState getState() {
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
