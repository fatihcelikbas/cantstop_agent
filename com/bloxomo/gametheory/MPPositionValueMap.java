package com.bloxomo.gametheory;

import java.io.*;

/**
 * A map from positions in a multiplayer game to their values.  Each
 * position will have a different position value for each player
 * (for example, if position values are probabilities of winning,
 * each player's probability of winning is recorded for each state.
 *
 * @author Jim Glenn
 * @version 0.1 3/9/2007 late because of a wild day in the ACC/Big East
 */

public interface MPPositionValueMap
{
    /**
     * Returns the recorded position value for the state with the given
     * index from the given player's point of view.
     *
     * @param index a nonnegative integer
     * @param player a nonnegative integer
     */

    public double getValue(long index, int player);

    /**
     * Sets the position value for the given state and player.
     *
     * @param index a nonnegative integer
     * @param player a nonnegative integer
     * @param value the position value of the state with the given index
     * from the given player's point of view
     */

    public void setValue(long index, int player, double value);

    /**
     * Determines if a value has been recorded for the given state and player.
     *
     * @param index a nonnegative integer
     * @param player a nonnegative integer
     * @return true if and only if a position value has been recorded for
     * the state with the given index and the given player
     */

    public boolean hasValue(long index, int player);

    public void write(DataOutputStream out) throws IOException;

    public void read(DataInputStream in) throws IOException;
}
