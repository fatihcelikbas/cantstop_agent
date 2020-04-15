package com.bloxomo.gametheory;

import java.io.*;

/**
 * A map that maps everything to zero.
 *
 * @author Jim Glenn
 * @version 0.1 10/28/2003
 */

public class ZeroMap implements StateValueMap
{
    /**
     * Returns zero.
     *
     * @param s a state in this map
     * @return zero
     */

    public double getValue(GameState s)
    {
	return 0.0;
    }

    /**
     * Does nothing, as <CODE>ZeroMap</CODE> maps everything to zero all the
     * time.
     * 
     *
     * @param s a game state
     * @param val the value of that state
     */

    public void setValue(GameState s, double val)
    {
    }

    /**
     * Determines if this map contains the given state.
     *
     * @return true iff this map contains the given state
     */

    public boolean contains(GameState s)
    {
	return true;
    }

    /**
     * Does nothing.  Why would you want to write a zero map?
     *
     * @param os the stream to write to
     */

    public void write(DataOutputStream os) throws IOException
    {
    }

    /**
     * Does nothing.  This map will always map everything to zero,
     * so what's the point of reading it?
     *
     * @param is the stream to read from
     */

    public void read(DataInputStream is) throws IOException
    {
    }

    public void flush()
    {
    }
}
