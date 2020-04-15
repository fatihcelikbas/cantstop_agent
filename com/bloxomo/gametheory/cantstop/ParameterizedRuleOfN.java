package com.bloxomo.gametheory.cantstop;

import com.bloxomo.gametheory.*;
import java.util.*;
import com.sirmapsalot.combinatorics.*;

/**
 * Implements a generalized version of the Rule of 28 Strategy from
 * Michael Keller (http://www.solitairelaboratory.com/cantstop.html).
 *
 * This version parameterizes the number of points to count for each
 * column, the number of points to count for each marker used, the
 * number of points to count (or discount) for various combinations of
 * columns, the weights to assign to the closest columns, and the
 * point threshold that determines when to end a turn.
 *
 * The strategy scores a position as follows:
 * 1) for each column, let s[c] = (length[c] - progress[c]) * value[c]
 * 2) sort the columns by decreasing s to get c[0], c[1],...
 * 3) compute the sum over i=0,...,c.length of c[i] * weight[i]
 * 4) add in markers[k] where k is the number of markers used
 * 5) if k = max value, add in odds/evens and high/low as appropriate
 * 6) make the move that decreases the score by the most
 * 7) decide to stop if the change in score from the start of the turn
 * exceeds the threshold
 */


public class ParameterizedRuleOfN implements CantStopStrategy
{
    /**
     * The value of a remaining square in each column.
     */

    private int[] columnValues;

    /**
     * The weight of the columns, from the closest (fewest points)
     * to the farthest (most points to finish).
     */

    private double[] columnWeights;

    /**
     * The points to add for used markers
     */

    private int[] markerValues;

    /**
     * The points to add for having used all markers and having all
     * odd or all even columns.
     */

    private int allOddValue;
    private int allEvenValue;

    /**
     * The points to add for having used all markers in all high or
     * all low columns.
     */

    private int allHighValue;
    private int allLowValue;

    /**
     * The threshold for ending a turn.
     */

    private int threshold;

    /**
     * Falg set to true iff this strategy will end a turn once it has
     * finished a column.
     */

    private boolean stopOnCompletedColumn;

    public ParameterizedRuleOfN(int[] val, double[] weight, int[] markers, int odd, int even, int high, int low, int thresh)
    {
	this(val, weight, markers, odd, even, high, low, thresh, false);
    }

    public ParameterizedRuleOfN(int[] val, double[] weight, int[] markers, int odd, int even, int high, int low, int thresh, boolean stop)
    {
	columnValues = new int[val.length];
	System.arraycopy(val, 0, columnValues, 0, val.length);

	columnWeights = new double[weight.length];
	System.arraycopy(weight, 0, columnWeights, 0, weight.length);

	markerValues = new int[markers.length];
	System.arraycopy(val, 0, markerValues, 0, markers.length);

	allOddValue = odd;
	allEvenValue = even;

	allHighValue = high;
	allLowValue = low;
	
	threshold = thresh;

	stopOnCompletedColumn = stop;
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
		// check the value of each legal move, keeping track
		// of the best (lowest count)

		int bestValue = Integer.MAX_VALUE;
		Multiset bestMove = null;
		
		Iterator moveIt = moves.iterator();
		while (moveIt.hasNext())
		    {
			Multiset move = (Multiset)(moveIt.next());
			CantStopState next = progress.makeMove(move);
			int nextValue = score(start, next);
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

    private int score(CantStopState start, CantStopState progress)
    {
	// sort columns based on closeness to finishing them from the
	// start state

	int[] startScores = new int[start.getHighestRoll() + 1];

	for (int c = start.getLowestRoll(); c <= start.getHighestRoll(); c++)
	    {
		startScores[c - start.getLowestRoll()]
		    = ((start.getColumnLength(c) - start.getMarkerPosition(c))
		       * columnValues[c]);
	    }

	// choose weights for column based on order of score in the _start_ state
	// it is important for comparing states that the weight of a column
	// doesn't keep changing during a turn (otherwise one state might
	// look better simply because the weights on the columns changed)

	int[] rank = new int[start.getHighestRoll() + 1];

	for (int c = start.getLowestRoll(); c <= start.getHighestRoll(); c++)
	    {
		for (int k = start.getLowestRoll(); k <= start.getHighestRoll(); k++)
		    {
			if (startScores[k] < startScores[c]
			    || (startScores[k] == startScores[c] && k < c))
			    {
				rank[c]++;
			    }
		    }
	    }

	double[] weights = new double[start.getHighestRoll() + 1];
	
	for (int c = start.getLowestRoll(); c <= start.getHighestRoll(); c++)
	    {
		weights[c] = columnWeights[rank[c]];
	    }

	// compute the score for each column

	int[] columnScores = new int[start.getHighestRoll() - start.getLowestRoll() + 1];

	for (int c = start.getLowestRoll(); c <= start.getHighestRoll(); c++)
	    {
		columnScores[c - start.getLowestRoll()]
		    = ((start.getColumnLength(c) - progress.getMarkerPosition(c))
		       * columnValues[c]);
	    }

	// compute the weighted total of column scores

	double total = 0.0;

	for (int i = 0; i < columnScores.length; i++)
	    {
		total += columnScores[i] * weights[i];
	    }

	// add in penalty for using markers and combinations of columns
	
	// first, figure out what columns have been used and which
	// are available to be played
	
	List< Integer > markerColumns = new LinkedList< Integer >();
	List< Integer > freeColumns = new LinkedList< Integer >();

	for (int c = start.getLowestRoll(); c <= start.getHighestRoll(); c++)
	    {
		if (progress.getMarkerPosition(c) != start.getMarkerPosition(c))
		    {
			markerColumns.add(c);
		    }
		else if (progress.getMarkerPosition(c) < progress.getColumnLength(c))
		    {
			freeColumns.add(c);
		    }
	    }

	// next, count the number of odd, even, high, and low columns
	// that have neutral markers

	int evenCount = 0;
	int oddCount = 0;
	int highCount = 0;
	int lowCount = 0;
	int middleColumn = (start.getHighestRoll() + start.getLowestRoll()) / 2;

	for (Integer c : markerColumns)
	    {
		if (c % 2 == 0)
		    evenCount++;
		else
		    oddCount++;

		if (c <= middleColumn)
		    lowCount++;

		if (c >= middleColumn)
		    highCount++;
	    }

	// now, add in penalties if all possible markers have been placed
	// (which includes the case in which we haven't placed all
	// the neutral markers but all the other columns have been won)

	if (markerColumns.size() == start.countNeutralMarkers()
	    || freeColumns.size() == markerColumns.size())
	    {
		if (evenCount == markerColumns.size())
		    total -= allEvenValue;
		else if (oddCount == markerColumns.size())
		    total += allOddValue;
		
		if (lowCount == markerColumns.size())
		    total += allLowValue;
		
		if (highCount == markerColumns.size())
		    total += allHighValue;
	    }
	
	// finally, add in the penalty for using markers

	total += markerValues[markerColumns.size()];

	return (int)total;
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
	// roll again if (there are columns and markers available or if
	// the count is less than the max) and we haven't won

	return (score(start, start) - score(start, progress) < threshold
		&& !progress.isFinal()
		&& !(stopOnCompletedColumn && progress.countWonColumns() > start.countWonColumns()));
    }

    public String toString()
    {
	StringBuffer result = new StringBuffer();

	result.append("[");
	for (int c = 2; c < columnValues.length; c++)
	    {
		if (c > 2)
		    result.append(", ");
		result.append(columnValues[c]);
	    }
	result.append("], [");

	for (int i = 0; i < columnWeights.length; i++)
	    {
		if (i > 0)
		    result.append(", ");
		result.append(columnWeights[i]);
	    }
	result.append("], [");

	for (int i = 0; i < markerValues.length; i++)
	    {
		if (i > 0)
		    result.append(", ");
		result.append(markerValues[i]);
	    }
	result.append("], ");

	return result.toString() + allOddValue + " " + allEvenValue + " " + allHighValue + " " + allLowValue + " " + threshold;
    }
}


