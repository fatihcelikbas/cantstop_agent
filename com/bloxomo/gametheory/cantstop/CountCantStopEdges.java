package com.bloxomo.gametheory.cantstop;

import com.bloxomo.gametheory.*;
import java.util.*;

/**
 * Used to count the number of edges that need to be examined in a game
 * of Can't Stop.  Note that to compute the optimal strategy it may be
 * necessary to examine some of the edges more than once (since we
 * use Newton's method to converge on the value of the entry point of
 * each stringly connected component).
 *
 * Currently, this really only counts non-final positions.  This is
 * done by iterating through all anchors and determining how many positions
 * are in each anchor's component.
 *
 * Since it also iterates through all the edges, this is also used
 * as a benchmark for the edge-finding algorithms and the data structures
 * used to hold the edges.
 *
 * @author Jim Glenn
 * @version 0.1 1/18/2006
 */

public class CountCantStopEdges
{
    public static void main(String[] args)
    {
	CantStopState s = null;
	
	try
	    {
		s = new CantStopState(Integer.parseInt(args[0]),
				      Integer.parseInt(args[1]));
	    }
	catch (Exception e)
	    {
		System.err.println("USAGE: java CountCantStopEdges num-sides min-column-length");
		System.exit(1);
	    }

	// keep track of number of states and total edges for each layer

	long[] states = new long[s.getTotalSpaces() + 1];
	long[] edges = new long[s.getTotalSpaces() + 1];
	long totalStates = 0;
	long totalEdges = 0;

	for (int layer = s.getTotalSpaces(); layer >= 0; layer--)
	    {
		Iterator i = s.iterator(layer);
		while (i.hasNext())
		    {
			CantStopState state = (CantStopState)(i.next());


			if (!state.isFinal())
			    {
				states[layer]++;
				totalStates++;

				Collection next = s.getNextStates();

				// THIS IS NOT REALLY RIGHT YET!
				// FOR EACH NEXT STATE WE NEED TO KNOW
				// HOW MANY POSSIBLE SUCCESSOR STATES THERE
				// ARE AND WHETHER IT CAN PUT US BACK TO THE
				// ENTRY POINT OF THE SCC

				edges[layer] += next.size();
				totalEdges += next.size();

				Iterator n = next.iterator();
				while (n.hasNext())
				    n.next();
			    }
		    }

		System.out.println("Layer " + layer + ": " + states[layer] + " " + edges[layer]);

	    }
	System.out.println("TOTAL   :" + totalStates + " " + totalEdges);
    }
}
