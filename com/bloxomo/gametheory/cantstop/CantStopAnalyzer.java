package com.bloxomo.gametheory.cantstop;

import com.bloxomo.gametheory.*;
import java.io.*;
import java.util.*;
import com.sirmapsalot.combinatorics.*;

public class CantStopAnalyzer
{
    public static void main(String[] args)
    {
	int sides = Integer.parseInt(args[0]);
	int shortestColumn = Integer.parseInt(args[1]);

	CantStopState dummy = new CantStopState(sides, shortestColumn);

	StateValueMap values = dummy.getMap();

	// reachable is the set of states that the optimal strategy might be in
	// at the start of a turn; intermediate is the set of states it
	// might be in at any point during a turn

	Set< CantStopState > reachable = new HashSet< CantStopState >();
	Set< CantStopState > intermediate = new HashSet< CantStopState >();
	reachable.add(dummy);
	intermediate.add(dummy);

	try
	    {
		DataInputStream in = new DataInputStream(new FileInputStream("cantstop_" + sides + "_" + shortestColumn + ".dat"));

		values.read(in);
		in.close();
	    }
	catch (IOException e)
	    {
		e.printStackTrace(System.err);
		System.exit(1);
	    }

	for (int layer = 0; layer <= dummy.getTotalSpaces(); layer++)
	    {
		Iterator i = dummy.iterator(layer);

		while (i.hasNext())
		    {
			CantStopState state = (CantStopState)(i.next());

			if (reachable.contains(state) && !state.isFinal())
			    {
				// find successors of state

				Map< CantStopState, Pair< Boolean, Double > > neighbors = findNeighbors(state, values);

				for (Map.Entry< CantStopState, Pair< Boolean, Double > > e : neighbors.entrySet())
				    {
					intermediate.add(e.getKey());

					if (e.getValue().getFirst())
					    reachable.add(e.getKey());
				    }
			    }
			else if (!intermediate.contains(state) && !state.isFinal())
			    {
				System.out.println("ANALYZER: unreachable non-final state " + state);
			    }
		    }
	    }
    }

    /**
     * Returns a map from sucessors of the given state to a pair that records
     * whether to stop at that successor and the probability of reaching
     * that successor from a state that starts at s.
     */

    public static Map< CantStopState, Pair< Boolean, Double > > findNeighbors(CantStopState s, StateValueMap values)
    {
	Map< CantStopState, Pair< Boolean, Double > > result 
	    = new HashMap< CantStopState, Pair< Boolean, Double > >();

	// pReach(q) is the probability we reach state q at some time during
	// a turn starting at s

	Map< CantStopState, Double > pReach = new HashMap< CantStopState, Double >();
	pReach.put(s, 1.0);
	
	Map intermediateValues = s.computeOptimalStrategy(values);
	Collection successors = new LinkedList();
	successors.add(s);
	successors.addAll(s.getNextStates());

	Iterator i = successors.iterator();
	while (i.hasNext())
	    {
		CantStopState intermediate = (CantStopState)(i.next());

		if (pReach.containsKey(intermediate))
		    {
			Map legalMoves = s.makeLegalMoveMap(intermediate);
			
			if (s.computeRollValue(intermediate,
					       legalMoves,
					       new StateValueMapAdapter(intermediateValues),
					       values.getValue(s)).getFirst()
			    >= values.getValue(intermediate))
			    {
				result.put(intermediate, new Pair< Boolean, Double >(true, pReach.get(intermediate)));
			    }
			else
			    {
				result.put(intermediate, new Pair< Boolean, Double >(false, pReach.get(intermediate)));
				
				// for each move, find the best move from intermediate,
				// and increase the probability of that state
				
				Iterator rollIterator = legalMoves.entrySet().iterator();
				while (rollIterator.hasNext())
				    {
					Map.Entry e = (Map.Entry)(rollIterator.next());
					DiceRoll roll = (DiceRoll)(e.getKey());
					List moves = (List)(e.getValue());
					
					if (moves.size() > 0)
					    {
						double bestValue = Double.POSITIVE_INFINITY;
						Multiset bestMove = null;
						
						Iterator moveIt = moves.iterator();
						while (moveIt.hasNext())
						    {
							Multiset move = (Multiset)(moveIt.next());
							CantStopState next = intermediate.makeMove(move);
							double nextValue = ((Double)(intermediateValues.get(next))).doubleValue();
							if (nextValue < bestValue)
							    {
								bestValue = nextValue;
								bestMove = move;
							    }
						    }
						CantStopState next = intermediate.makeMove(bestMove);
						if (pReach.containsKey(next))
						    pReach.put(next, pReach.get(next) + pReach.get(intermediate) * roll.probability());
						else
						    pReach.put(next, pReach.get(intermediate) * roll.probability());
					    }
				    }
			    }
		    }
	    }					

	return result;
    }
}
