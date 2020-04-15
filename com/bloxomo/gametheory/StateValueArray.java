package com.bloxomo.gametheory;

import java.io.*;

/**
 * A map of states to values implemented using an array.  The array
 * will contain one element for each possible state.
 *
 * @author Jim Glenn
 * @version 0.1 9/4/2003
 */

public class StateValueArray implements StateValueMap
{
    /**
     * Holds values for each state.  The value of the state with
     * index i is held in array element i.
     */

    private double[] values;

    /**
     * Constructs a new map that holds the given number of states.
     * The states are assumed to be numbered consecutively starting from zero.
     *
     * @param size the number of states in this new map
     */

    public StateValueArray(int size)
    {
	values = new double[size];
    }

    /**
     * Returns the value of the given state.
     *
     * @param s a state in this map
     * @return the value of the given state.
     */

    public double getValue(GameState s)
    {
	return values[(int)(s.getIndex())];
    }

    /**
     * Sets the value of the given state.  If the state already had a value
     * it is replaced with the new one.
     *
     * @param s a game state
     * @param val the value of that state
     */

    public void setValue(GameState s, double val)
    {
	values[(int)(s.getIndex())] = val;
    }

    /**
     * Determines if this map contains the given state.
     *
     * @return true iff this map contains the given state
     */

    public boolean contains(GameState s)
    {
	return (s.getIndex() >= 0 && s.getIndex() < values.length);
    }

    /**
     * Writes this map to the given stream.
     * 
     * @param os the stream to write to
     */

    public void write(DataOutputStream os) throws IOException
    {
	for (int i = 0; i < values.length; i++)
	    os.writeDouble(values[i]);
    }

    /**
     * Reads this map from the given stream.
     *
     * @param is the stream to read from
     */

    public void read(DataInputStream is) throws IOException
    {
	for (int i = 0; i < values.length; i++)
	    values[i] = is.readDouble();
    }

    public void flush()
    {
    }
}

    
