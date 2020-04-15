package com.bloxomo.gametheory;

import java.io.*;
import java.util.*;

public abstract class CachedStateValueFile extends StateValueFile
{
    protected double[][] cache;
    protected boolean[] dirty;
    protected int[] lineID;

    /**
     * Maps line indices to indices in the cache array.
     */

    protected Map lineMap;

    public CachedStateValueFile(String fname, long numStates, int numLines) throws IOException
    {
	this(fname, numStates, numLines, true);
    }

    public CachedStateValueFile(String fname, long numStates, int numLines, boolean init)
	throws IOException
    {
	super(fname, numStates, init);
	
	cache = new double[numLines][getLineLength()];
	dirty = new boolean[numLines];
	lineID = new int[numLines];
	lineMap = new HashMap();

	for (int i = 0; i < lineID.length; i++)
	    lineID[i] = -1;
    }
    
    /**
     * Returns the value of the given state.
     *
     * @param s a state in this map
     * @return the value of the given state.
     */
    
    public synchronized double getValue(GameState s)
    {
	return cache[find(s)][getIndex(s)];
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
	int line = find(s);
	
	cache[line][getIndex(s)] = val;
	dirty[line] = true;
    }
    
    /**
     * Returns the index of the cache line where the value for the
     * given state can be found.  If the cache line is not present,
     * it will be loaded.
     *
     * @param s a state
     * @return the cache line where that state's value is stored
     */
    
    protected int find(GameState s)
    {
	Integer l = (Integer)(lineMap.get(new Integer(getLine(s))));

	if (l == null)
	    {
		int line = replaceLine(s);

		// read cache line
		
		try
		    {
			file.seek((long)cache[line].length * getLine(s) * BYTES_PER_DOUBLE);
			for (int i = 0; i < cache[line].length; i++)
			    cache[line][i] = file.readDouble();
		    }
		catch (IOException e)
		    {
			System.err.println(getClass().getName() + ": error reading into cache");
			e.printStackTrace(System.err);
			System.exit(1);
		    }

		lineMap.remove(new Integer(lineID[line]));
		lineID[line] = getLine(s);
		lineMap.put(new Integer(getLine(s)), new Integer(line));

		return line;
	    }
	else
	    return l.intValue();
    }
    
    /**
     * Replaces a currently loaded cache line to make room for
     * the line that would contain the given state's value.  That
     * new line is not actually loaded.
     *
     * @param s the state to load a line for
     * @return the line that was replaced
     */
    
    protected int replaceLine(GameState s)
    {
	int line = 0;
	while (line < lineID.length && lineID[line] != -1)
	    line++;
	
	// check if we found an empty line
	
	if (line < lineID.length)
	    {
		System.err.println(getClass().getName() + ": filling empty line index " + line + " with " + getLine(s));
		return line;
	    }
	
	// didn't find an empty line -- pick one to replace
	
	line = pickLineToReplace(s);
	
	// write replaced line if it is dirty
	
	if (dirty[line])
	    flush(line);
	
	return line;
    }

    protected void flush(int line)
    {
	try
	    {
		file.seek((long)cache[line].length * lineID[line] * BYTES_PER_DOUBLE);
		for (int i = 0; i < cache[line].length; i++)
		    file.writeDouble(cache[line][i]);
		
		dirty[line] = false;
	    }
	catch (IOException e)
	    {
		System.err.println(getClass().getName() + ": error writing back cache");
		e.printStackTrace(System.err);
		System.exit(1);
	    }
    }

    public void flush()
    {
	for (int l = 0; l < dirty.length; l++)
	    if (lineID[l] != -1 && dirty[l])
		flush(l);
    }
    
    /**
     * Chooses the line to replace in order
     * to load the line containing the given state's value.
     * The line to replace is not removed by this method and
     * the line to load is not loaded.
     *
     * @param s the state to load the line for
     * @return the line selected for replacement
     */
    
    protected abstract int pickLineToReplace(GameState s);
    
    /**
     * Returns the index into a cache line where the given state's
     * value would be stored.
     *
     * @param s a state
     * @return the index in a cache line  where that state's value would
     * be stored
     */
    
    protected abstract int getIndex(GameState s);

    /**
     * Returns the index of the cache line where the value of the given
     * state would be found.
     *
     * @param s a state
     * @return the index of the cache line where that state's value would be found
     */

    protected abstract int getLine(GameState s);

    /**
     * Returns the length of a line in this cache.
     *
     * @return the length of a line in this cache.
     */

    protected abstract int getLineLength();
}
