package com.bloxomo.gametheory.cantstop;

import com.bloxomo.gametheory.*;
/**
 * Determines points (weights) given to individual spaces on the
 * Can't Stop board.  Points are classified two ways: for
 * first marking a space and points for advancing to a space; and
 * for determining progress and hence when to stop a turn, and
 * for determining which columns to choose.
 *
 * @author Jim Glenn
 * @version 0.1 11/16/2008
 */

public interface Weights
{
    /**
     * Returns the progress points for first placing a neutral marker
     * in a column with the colored marker at the given position.
     *
     * @param col a column label
     * @param space the index of a space in that column
     *
     * @return the points for marking that space
     */

    public int markedProgressPoints(int col, int space);

    /**
     * Returns the progress points for advancing a neutral marker to
     * a space.
     *
     * @param col a column label
     * @param space the index of a space in that column
     */

    public int advancedProgressPoints(int col, int space);

    /**
     * Returns the choice points for first placing a neutral marker
     * in a column with the colored marker at the given position.
     *
     * @param col a column label
     * @param space the index of a space in that column
     *
     * @return the points for marking that space
     */

    public int markedChoicePoints(int col, int space);

    /**
     * Returns the choice points for moving a neutral marker to
     * a space.
     *
     * @param col a column label
     * @param space the index of a space in that column
     */

    public int advancedChoicePoints(int col, int space);
}
