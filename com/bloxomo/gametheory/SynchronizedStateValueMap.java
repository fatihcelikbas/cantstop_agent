package com.bloxomo.gametheory;

import java.io.*;

/**
 * Maps game states to double values.
 *
 * @author Jim Glenn
 * @version 0.1 6/7/2007 made synchronized wrapper from original dated 9/4/2003
 */

public class SynchronizedStateValueMap implements StateValueMap
{
    /**
     * The map wrapped in this one.
     */

    private StateValueMap map;

    /**
     * Creates a new, synchronized map that wraps the given map.
     *
     * @param m a StateValueMap
     */

    public SynchronizedStateValueMap(StateValueMap m)
    {
	map = m;
    }

    /**
     * Returns the value of the given state.
     *
     * @param s a state in this map
     * @return the value of the given state.
     */

    public synchronized double getValue(GameState s)
    {
	return map.getValue(s);
    }

    /**
     * Sets the value of the given state.  If the state already had a value
     * it is replaced with the new one.
     *
     * @param s a game state
     * @param val the value of that state
     */

    public synchronized void setValue(GameState s, double val)
    {
	map.setValue(s, val);
    }

    /**
     * Determines if this map contains the given state.
     *
     * @return true iff this map contains the given state
     */

    public synchronized boolean contains(GameState s)
    {
	return map.contains(s);
    }

    /**
     * Writes this map to the given stream.
     *
     * @param os the stream to write to
     */

    public synchronized void write(DataOutputStream os) throws IOException
    {
	map.write(os);
    }

    /**
     * Reads this map from the given stream.  Any data in this map is lost.
     *
     * @param is the stream to read from
     */

    public synchronized void read(DataInputStream is) throws IOException
    {
	map.read(is);
    }

    /**
     * Flushes any cached data in this map.
     */

    public synchronized void flush()
    {
	map.flush();
    }
}

	
