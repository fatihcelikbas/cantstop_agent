package com.bloxomo.gametheory.cantstop;

import java.util.*;
import java.io.*;
import com.bloxomo.synchronization.*;
import com.bloxomo.gametheory.*;

public class MTCantStopOptimizer
{
    public static void main(String[] args)
    {
	CantStopState s = null;
	int numThreads = 1;

	// parse command line arguments

	try
	    {
		s = new CantStopState(Integer.parseInt(args[0]),
				      Integer.parseInt(args[1]));

		if (args.length > 2)
		    numThreads = Integer.parseInt(args[2]);
	    }
	catch (ArrayIndexOutOfBoundsException e)
	    {
		System.err.println("USAGE: java MTCantStopOptimizer dice-sides min-column-length [threads]");
		System.exit(1);
	    }
	catch (NumberFormatException e)
	    {
		System.err.println("USAGE: java MTCantStopOptimizer dice-sides min-column-length [threads]");
		System.exit(1);
	    }

	// initialize empty state value map

	StateValueMap values = new SynchronizedStateValueMap(s.getMap());

	// make a barrier to coordinate consumers and main threads

	Barrier b = new Barrier(numThreads + 1);

	// start threads

	MTCantStopQueue q = new MTCantStopQueue(s, values, b, numThreads);
	q.start();

	// wait for consumers to finish

	b.cross();

	try
	    {
		Process ps = Runtime.getRuntime().exec("ps -aF");
		BufferedReader psOutput = new BufferedReader(new InputStreamReader(ps.getInputStream()));
		String line;
		while ((line = psOutput.readLine()) != null)
		    {
			if (line.indexOf("MTCantStopOptimizer") != -1)
			    System.out.println(line);
		    }
	    }
	catch (IOException e)
	    {
		e.printStackTrace(System.err);
	    }

	try
	    {
		DataOutputStream out = new DataOutputStream(new FileOutputStream("cantstop_" + args[0] + "_" + args[1] + ".dat"));
		values.write(out);
		out.close();
	    }
	catch (IOException e)
	    {
		e.printStackTrace(System.err);
	    }
    }

    private static class MTCantStopQueue extends StagedQueue
    {
	private CantStopState dummy;
	private StateValueMap positionValues;
	private static int nextID = 0;
	private Barrier bar;

	private MTCantStopQueue(CantStopState s, StateValueMap values, Barrier b, int n)
	{
	    super(n);

	    dummy = s;
	    positionValues = values;
	    bar = b;
	}

	public Thread createProducer()
	{
	    return new Producer(dummy);
	}

	public Thread createConsumer()
	{
	    return new Consumer(dummy, positionValues, bar);
	}

	private class Producer extends Thread
	{
	    private CantStopState dummy;

	    private Producer(CantStopState s)
	    {
		dummy = s;
	    }

	    public void run()
	    {
		for (int layer = dummy.getTotalSpaces(); layer >= 0; layer--)
		    {
			Iterator i = dummy.iterator(layer);
			while (i.hasNext())
			    {
				CantStopState state = (CantStopState)(i.next());
				enqueue(state);
			    }

			endStage();
		    }
		finish();
	    }
	}

	private class Consumer extends Thread
	{
	    private CantStopState dummy;
	    private StateValueMap positionValues;
	    private Barrier bar;
	    private int id;

	    private Consumer(CantStopState s, StateValueMap values, Barrier b)
	    {
		dummy = s;
		positionValues = values;
		bar = b;
		id = nextID++;
	    }

	    public void run()
	    {
		Object o;
		while ((o = dequeue()) != null)
		    {
			CantStopState state = (CantStopState)o;
			CantStopState mirror;

			if (state.isFinal())
                            {
                                positionValues.setValue(state, 0.0);
                            }
                        else if (state.hashCode() <= (mirror = state.mirror()).hashCode())
                            {
                                double stateValue = state.computeExpectedTurns(positionValues);
				
                                System.out.println(state + " " + state.hashCode());
                                System.out.println(mirror + " " + mirror.hashCode());
                                positionValues.setValue(state, stateValue);
                                positionValues.setValue(mirror, stateValue);
				System.out.println("#" + id + ": " + state + " = " + stateValue);
				System.out.println("#" + id + ": " + mirror + " = " + stateValue);
                            }

			
		    }
		bar.cross();
	    }
	}
    }
}
