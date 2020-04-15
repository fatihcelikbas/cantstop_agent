package com.bloxomo.gametheory;

import java.util.*;
import java.io.*;

/**
 * Converts a JCF <CODE>Map</CODE> to a <CODE>StateValueMap</CODE>.
 * This is only suitable for small maps from games with few states.
 *
 * @author Jim Glenn
 * @version 0.1 1/13/2005
 */

public class StateValueMapAdapter implements StateValueMap
{
    /**
     * The map.
     */

    private Map map;

    public StateValueMapAdapter(Map m)
    {
	map = m;
    }
    
    /**
     * Returns the value of the given state.
     *
     * @param s a state in this map
     * @return the value of the given state.
     */

    public double getValue(GameState s)
    {
	Double val = (Double)(map.get(s));

	if (val != null)
	    return val.doubleValue();
	else
	    return 0.0;
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
	map.put(s, new Double(val));
    }

    /**
     * Determines if this map contains the given state.
     *
     * @return true iff this map contains the given state
     */

    public boolean contains(GameState s)
    {
	return map.containsKey(s);
    }

    /**
     * Writes this map to the given stream.
     */

    public void write(DataOutputStream os) throws IOException
    {
	// create a map from state indices to values

	Map copy = new HashMap();

	long max = 0;
	
	Iterator i = map.entrySet().iterator();
	while (i.hasNext())
	    {
		Map.Entry e = (Map.Entry)(i.next());
		GameState st = (GameState)(e.getKey());

		copy.put(new Long(st.getIndex()), e.getValue());

		if (st.getIndex() > max)
		    max = st.getIndex();
	    }

	// write to file

	for (long index = 0; index <= max; index++)
	    os.writeDouble(((Double)(copy.get(new Long(index)))).doubleValue());
    }

    /**
     * Reads this map from the given stream.  Any data in this map is lost.
     * The input stream must contain the serialization of a <CODE>Map</CODE>
     * object.
     *
     * @param is the stream to read from
     */

    public void read(DataInputStream is) throws IOException
    {
	try
	    {
		map = (Map)(new ObjectInputStream(is)).readObject();
	    }
	catch (ClassNotFoundException e)
	    {
		throw new InvalidClassException("Could not find class file");
	    }
    }

    public void flush()
    {
    }
}
