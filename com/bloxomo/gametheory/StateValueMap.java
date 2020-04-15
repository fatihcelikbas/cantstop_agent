package com.bloxomo.gametheory;

import java.io.*;

/**
 * Maps game states to double values.
 *
 * @author Jim Glenn
 * @version 0.1 9/4/2003
 */

public interface StateValueMap
{
    /**
     * Returns the value of the given state.
     *
     * @param s a state in this map
     * @return the value of the given state.
     */

    public double getValue(GameState s);

    /**
     * Sets the value of the given state.  If the state already had a value
     * it is replaced with the new one.
     *
     * @param s a game state
     * @param val the value of that state
     */

    public void setValue(GameState s, double val);

    /**
     * Determines if this map contains the given state.
     *
     * @return true iff this map contains the given state
     */

    public boolean contains(GameState s);

    /**
     * Writes this map to the given stream.
     *
     * @param os the stream to write to
     */

    public void write(DataOutputStream os) throws IOException;

    /**
     * Reads this map from the given stream.  Any data in this map is lost.
     *
     * @param is the stream to read from
     */

    public void read(DataInputStream is) throws IOException;

    /**
     * Flushes any cached data in this map.
     */

    public void flush();
}

	
