package com.bloxomo.gametheory;

import java.io.*;

/**
 * A wrapper that synchronizes access to an existing position value map.
 *
 * @author Jim Glenn
 * @version 0.1 6/7/2007
 */

public class SynchronizedPositionValueMap implements MPPositionValueMap
{
    MPPositionValueMap map;

    public SynchronizedPositionValueMap(MPPositionValueMap m)
    {
	map = m;
    }

    /**
     * Returns the recorded position value for the state with the given
     * index from the given player's point of view.
     *
     * @param index a nonnegative integer
     * @param player a nonnegative integer
     */

    public synchronized double getValue(long index, int player)
    {
	return map.getValue(index, player);
    }

    /**
     * Sets the position value for the given state and player.
     *
     * @param index a nonnegative integer
     * @param player a nonnegative integer
     * @param value the position value of the state with the given index
     * from the given player's point of view
     */

    public synchronized void setValue(long index, int player, double value)
    {
	map.setValue(index, player, value);
    }

    /**
     * Determines if a value has been recorded for the given state and player.
     *
     * @param index a nonnegative integer
     * @param player a nonnegative integer
     * @return true if and only if a position value has been recorded for
     * the state with the given index and the given player
     */

    public synchronized boolean hasValue(long index, int player)
    {
	return map.hasValue(index, player);
    }

    public synchronized void write(DataOutputStream out) throws IOException
    {
	map.write(out);
    }

    public synchronized void read(DataInputStream in) throws IOException
    {
	map.read(in);
    }
}
