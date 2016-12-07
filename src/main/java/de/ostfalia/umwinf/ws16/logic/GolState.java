package de.ostfalia.umwinf.ws16.logic;

import java.util.Arrays;

/**
 * all possible states of a cell in the GOL field
 */
public enum GolState {

    ALIVE, DEAD;

    public GolState toggle() {
        if (this == ALIVE)
            return DEAD;
        return ALIVE;
    }

    /**
     * @param r number of rows
     * @param c number of columns
     * @return a GOL field with all states set to {@code NONE}
     */
    public static GolState[][] field(int r, int c) {
        GolState[][] field = new GolState[r][c];
        for (GolState[] row : field)
            Arrays.fill(row, DEAD);
        return field;
    }
}
