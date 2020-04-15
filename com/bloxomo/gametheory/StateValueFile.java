package com.bloxomo.gametheory;

import java.io.*;

/**
 * A map of states to values implemented using a file.  The file
 * will contain one entry for each possible state.
 *
 * @author Jim Glenn
 * @version 0.1 10/23/2003
 */

public class StateValueFile implements StateValueMap
{
    /**
     * The file that stores the information for this map.  The value
     * for state 0 will be written as the first 8 bytes of the file.
     * In general, state i will be written at offset 8i from the start of
     * the file.
     */

    protected RandomAccessFile file;

    /**
     * The number of states in this map.
     */

    protected long numStates;

    /**
     * The number of bytes in the <CODE>double</CODE> type.
     */

    public static final int BYTES_PER_DOUBLE = 8;

    public StateValueFile(String fname, long size) throws IOException
    {
	this(fname, size, true);
    }

    /**
     * Constructs a new map that holds the given number of states.
     * The states are assumed to be numbered consecutively starting from zero.
     *
     * @param fname the name of the file used to store data for this new map
     * @param size the number of states in this new map
     * @param init true to indicate the new map should be zeroed
     */

    public StateValueFile(String fname, long size, boolean init) throws IOException
    {
	numStates = size;

	file = new RandomAccessFile(fname, "rw");

	// create a new file filled with 0.0's if we were told to or
	// if the existing one was too short

	if (init || file.length() < size * BYTES_PER_DOUBLE)
	    {
		long zeroBits = Double.doubleToLongBits(0.0);
		
		// lets make a big array filled with the 8 bytes that make up the
		// double 0.0!
		
		byte[] zeroBuffer = new byte[1 << 20];
		for (int copy = 0; copy < zeroBuffer.length / BYTES_PER_DOUBLE; copy++)
		    {
			long mask = 0xFF00000000000000L;
			int shift = 56;
			
			for (int b = 0; b < BYTES_PER_DOUBLE; b++)
			    {
				zeroBuffer[copy * BYTES_PER_DOUBLE + b] = (byte)((zeroBits & mask) >>> shift);
				mask = mask >>> 8;
				shift = shift - 8;
				if (shift < 0)
				    {
					mask = 0xFF00000000000000L;
					shift = 56;
				    }
			    }
		    }
		
		
		long bytesWritten = 0;
		
		while (bytesWritten < size * BYTES_PER_DOUBLE)
		    {
			file.write(zeroBuffer);
			bytesWritten += zeroBuffer.length;
		    }
		
		//file.getChannel().force(true);
	    }
    }

    /**
     * Returns the value of the given state.
     *
     * @param s a state in this map
     * @return the value of the given state.
     */

    public synchronized double getValue(GameState s)
    {
	try
	    {
		file.seek(s.getIndex() * BYTES_PER_DOUBLE);
		return file.readDouble();
	    }
	catch (IOException e)
	    {
		System.err.println("StateValueFile.getValue: could not read from file at offset " + s.getIndex() * BYTES_PER_DOUBLE);
		System.err.println(e);
		e.printStackTrace(System.err);
		System.exit(1);
		return 0.0;
	    }
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
	try
	    {
		file.seek(s.getIndex() * BYTES_PER_DOUBLE);
		file.writeDouble(val);
	    }
	catch (IOException e)
	    {
		System.err.println("StateValueFile.setValue: could not write to file at offset " + s.getIndex() * BYTES_PER_DOUBLE);
		System.err.println(e);
		e.printStackTrace(System.err);
		System.exit(1);
	    }
    }

    /**
     * Determines if this map contains the given state.
     *
     * @return true iff this map contains the given state
     */

    public boolean contains(GameState s)
    {
	return (s.getIndex() >= 0 && s.getIndex() < numStates);
    }

    /**
     * Writes this map to the given stream.
     * 
     * @param os the stream to write to
     */

    public synchronized void write(DataOutputStream os) throws IOException
    {
	//file.getChannel().force(true);
	file.seek(0);

	for (long i = 0; i < numStates; i++)
	    os.writeDouble(file.readDouble());
    }

    /**
     * Reads this map from the given stream.
     *
     * @param is the stream to read from
     */

    public synchronized void read(DataInputStream is) throws IOException
    {
	file.seek(0);

	for (long i = 0; i < numStates; i++)
	    file.writeDouble(is.readDouble());

	//file.getChannel().force(true);
    }

    public void flush()
    {
    }
}

    
