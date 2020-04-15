package com.bloxomo.gametheory.cantstop;

import com.bloxomo.gametheory.*;
import java.io.*;
import java.util.*;

import com.sirmapsalot.combinatorics.*;

public class OptimalCantStopStrategy implements CantStopStrategy
{
    /**
     * The position values of the starts of turns for the game this
     * strategy is playing.
     */

    private StateValueMap values;

    /**
     * A representative state from the game this strategy is playing.
     */

    private CantStopState dummy;

    /**
     * The state for which intermediate values was computed.  This is
     * used as a one-state cache so if consecutive invocations of
     * <CODE>pickPairs</CODE> pass in the same starting position we don't
     * have to recompute the optimal strategy for that turn.
     */

    private CantStopState solvedState;

    /**
     * The optimal strategy for a turn that begins at <CODE>solvedState</CODE>.
     */

    private Map intermediateValues;

    /**
     * Creates a strategy that follows the optimal strategy.  The position
     * values of the starts of turns must have been computed and saved
     * in a file whose name is "cantstop_{sides}_{shortest}".
     *
     * @param sides the number of sides on the dice to play with
     * @param shortest the length of the shortest column on the board
     * to play on
     */

    public OptimalCantStopStrategy(int sides, int shortest) throws IOException
    {
	dummy = new CantStopState(sides, shortest);
	values = dummy.getMap();

	DataInputStream in = new DataInputStream(new FileInputStream("cantstop_" + sides + "_" + shortest + ".dat"));

	values.read(in);
	in.close();
    }

    /**
     * Determines how to pair the dice after a roll.  The state is
     * given as the state at the start of the turn and the current state
     * the position of the white markers is indicated by the difference
     * in those two positions.
     *
     * @param start the state at the start of the turn
     * @param progress the current state
     * @param roll the roll
     * @return the way to split the dice, or null if no move is possible
     */

    public Multiset pickPairs(CantStopState start, CantStopState progress, DiceRoll roll)
    {
	List moves = (List)(start.makeLegalMoveMap(progress).get(roll));
	
	if (moves.size() > 0)
	    {
		solve(start);

		// check the value of each legal move, keeping track
		// of the best

		double bestValue = Double.POSITIVE_INFINITY;
		Multiset bestMove = null;
		
		Iterator moveIt = moves.iterator();
		while (moveIt.hasNext())
		    {
			Multiset move = (Multiset)(moveIt.next());
			CantStopState next = progress.makeMove(move);
			double nextValue = ((Double)(intermediateValues.get(next))).doubleValue();
			if (nextValue < bestValue)
			    {
				bestValue = nextValue;
				bestMove = move;
			    }
		    }
		
		return bestMove;
	    }
	else
	    {
		return null;
	    }
    }

    /**
     * Computes the optimal strategy for a turn starting from the given
     * state and saves it in the cache.
     *
     * @param s the state to solve
     */

    private void solve(CantStopState s)
    {
	if (solvedState == null || !solvedState.equals(s))
	    {
		intermediateValues = s.computeOptimalStrategy(values);
		solvedState = s;
	    }
    }

    /**
     * Determines whether to roll again or not.  The state is
     * given as the state at the start of the turn and the current state
     * the position of the white markers is indicated by the difference
     * in those two positions.
     *
     * @param start the state at the start of the turn
     * @param progress the current state
     * @return true iff the strategy says to roll again
     */

    public boolean rollAgain(CantStopState start, CantStopState progress)
    {
	return (start.computeRollValue(progress,
				       start.makeLegalMoveMap(progress),
				       new StateValueMapAdapter(intermediateValues),
				       values.getValue(start)).getFirst()
		< values.getValue(progress));
    }
}

