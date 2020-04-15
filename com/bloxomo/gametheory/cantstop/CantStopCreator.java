package com.bloxomo.gametheory.cantstop;

import java.util.*;
import java.lang.reflect.*;
import com.bloxomo.gametheory.*;
import com.bloxomo.ga.*;
import com.bloxomo.utility.*;

public class CantStopCreator extends DefaultCreator
{
    public static final int DEFAULT_SIDES = 6;
    public static final int DEFAULT_SHORTEST_COLUMN_LENGTH = 3;

    private CantStopState start;

    /**
     * Creates a creation operator for the class with the given name.
     * Instances of that class will play the game represented by the
     * given state.
     *
     * @param cn the name of a genotype class
     * @param s a Can't Stop state
     */

    public CantStopCreator(String cn, CantStopState s)
    {
	super(cn);

	start = s;
    }

    /**
     * Creates a creation operator for the class with the given name.
     * Instances of that class will play the game with parameters set
     * by the map argument.
     *
     * @param cn the name of a genotype class
     * @param params a map with optional "sides" and "length" keys,
     * where the value for "sides" (if present) is at least 2 and the
     * value for "length" (if present) is positive.
     */

    public CantStopCreator(String cn, Map< String, Integer > params)
    {
	super(cn);

	int sides = Arguments.setIntegerArgument(params, "sides", DEFAULT_SIDES);
	int length = Arguments.setIntegerArgument(params, "length", DEFAULT_SHORTEST_COLUMN_LENGTH);

	start = new CantStopState(sides, length);
    }

    public Genotype create()
    {
	try
	    {
		Constructor cons = cl.getConstructor(new Class[] {CantStopState.class});
		return (Genotype)(cons.newInstance(new Object[] {start}));
	    }
	catch (NoSuchMethodException e)
	    {
		System.out.println(cl.getName() + " does not have a constructor that takes a CantStopState");
		System.exit(1);
		return null;
	    }
	catch (SecurityException e)
	    {
		e.printStackTrace(System.err);
		return null;
	    }
	catch (InstantiationException e)
	    {
		e.printStackTrace(System.err);
		return null;
	    }
	catch (IllegalAccessException e)
	    {
		e.printStackTrace(System.err);
		return null;
	    }
	catch (InvocationTargetException e)
	    {
		e.printStackTrace(System.err);
		return null;
	    }
	catch (ExceptionInInitializerError e)
	    {
		e.printStackTrace(System.err);
		return null;
	    }
    }
}
