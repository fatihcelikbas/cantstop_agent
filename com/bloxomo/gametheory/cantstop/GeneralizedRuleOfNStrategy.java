package com.bloxomo.gametheory.cantstop;

import com.bloxomo.gametheory.*;
import java.util.*;
import com.sirmapsalot.combinatorics.*;

/**
 * Implements a generalized version of Michael Keller's Rule of 28
 * strategy for Can't Stop.  The values of spaces within columns can
 * be changed, as can the values of first placing a neutral marker at
 * various positions.
 *
 * @author Jim Glenn
 * @version 0.1 11/14/2008
 */

public abstract class GeneralizedRuleOfNStrategy implements CantStopStrategy
{
    /**
     * The count that determines when to end a turn.
     */

    private int maxCount;

    /**
     * Whether or not to count penalty points when considering what move
     * makes the most progress.  Penalty points are always considered
     * when determining whether to roll again.
     */

    private boolean countPenalty;

    /**
     * Whether or not to count bonus points when considering what move
     * makes the most progress.  Bonus points are always counted when
     * determining whether to roll again.
     */

    private boolean countBonus;

    /**
     * The game this strategy plays.
     */

    protected CantStopState game;

    /**
     * Creates a strategy with the given maximum count.  The strategy will
     * consider bonus points and penalty points when determining
     * the best pairing of the dice.
     *
     * @param g any state in the game
     * @param max a positive integer
     */

    public GeneralizedRuleOfNStrategy(CantStopState g, int max)
    {
	this(g, max, true, true);
    }

    /**
     * Creates a strategy with the given maximum count and penalty/bonus
     * point policy.
     *
     * @param max a positive integer
     * @param penalty true to count penalty points for moves
     * @param bonus true to count bonus points for moves
     */

    public GeneralizedRuleOfNStrategy(CantStopState g, int max, boolean penalty, boolean bonus)
    {
	game = g;
	maxCount = max;
	countPenalty = penalty;
	countBonus = bonus;
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

    public Multiset pickPairs(CantStopState start,
			      CantStopState progress,
			      DiceRoll roll)
    {
	List moves = start.getLegalMoves(progress,roll);

	if (moves.size() > 0)
	    {
		// check the value of each legal move, keeping track
		// of the best (lowest count)

		int bestValue = Integer.MIN_VALUE;
		Multiset bestMove = null;
		
		Iterator moveIt = moves.iterator();
		while (moveIt.hasNext())
		    {
			Multiset move = (Multiset)(moveIt.next());
			CantStopState next = progress.makeMove(move);
			int points = netChoiceCount(start, next);

			// System.out.println(move + " = " + points);
			   
			if (points > bestValue)
			    {
				bestValue = points;
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
	// roll again (if there are columns and markers available or if
	// the count is less than the max) and we haven't won

	return (((countUsedColumns(start, progress) < start.countNeutralMarkers()
		 && countFreeColumns(progress) > countUsedColumns(start, progress))
		|| netProgressCount(start,progress) < maxCount) && !progress.isFinal());
    }

    /**
     * Returns the number of columns that have neutral markers in them
     * in the given position.
     *
     * @param start the position at the start of the turn
     * @param progress the current position
     * @return the number of columns with markers in different positions in
     * those two states
     */

    private static int countUsedColumns(CantStopState start, CantStopState progress)
    {
	int total = 0;

	for (int c = start.getLowestRoll(); c <= start.getHighestRoll(); c++)
	    {
		if (progress.getMarkerPosition(c) != start.getMarkerPosition(c))
		    {
			total++;
		    }
	    }

	return total;
    }

    /**
     * Returns the number of columns that have not been won in the
     * given state.
     *
     * @param s a state
     * @return the number of unwon columns in that state
     */

    private static int countFreeColumns(CantStopState s)
    {
	int total = 0;

	for (int c = s.getLowestRoll(); c <= s.getHighestRoll(); c++)
	    {
		if (s.getMarkerPosition(c) != s.getColumnLength(c))
		    {
			total++;
		    }
	    }

	return total;
    }

    /**
     * Returns the net point count for the given position.  The net point
     * count is the count for progress plus the count for penalty points.
     *
     * @param start the state at the start of the turn
     * @param progress the state if the turn were ended
     * @return the net point count for that position
     */

    protected int netProgressCount(CantStopState start, CantStopState progress)
    {
	Triple< Integer, Integer, Integer > totals = countProgress(start, progress);

	return totals.getFirst() + totals.getSecond() + totals.getThird();
    }

    /**
     * Returns the net choice point count for the given position.  The
     * net point count is the count for choice minus the count for
     * penalty points.
     *
     * @param start the state at the start of the turn
     * @param progress the state if the turn were ended
     * @return the net point count for that position
     */

    protected int netChoiceCount(CantStopState start, CantStopState progress)
    {
	Triple< Integer, Integer, Integer > totals = countChoice(start, progress);

	return totals.getFirst() - totals.getSecond() - totals.getThird();
    }

    /**
     * Returns the point count for the given position.  The point count
     * is given as a triple containing the point count for progress,
     * the penalty points, and the bonus points.
     *
     * @param start the state at the start of the turn
     * @param progress the state if the turn were ended
     * @return the triple (progress-points, penalty-points, bonus-points)
     */

    protected Triple< Integer, Integer, Integer > countProgress(CantStopState start, CantStopState progress)
    {
	List< Integer > markerColumns = new LinkedList< Integer >();

	List< Integer > freeColumns = new LinkedList< Integer >();

	int progressPoints = 0;

	for (int c = start.getLowestRoll(); c <= start.getHighestRoll(); c++)
	    {
		if (progress.getMarkerPosition(c) != start.getMarkerPosition(c))
		    {
			markerColumns.add(c);

			progressPoints += progressPoints(c,
							 start.getMarkerPosition(c),
							 progress.getMarkerPosition(c));
		    }
		else if (progress.getMarkerPosition(c) < progress.getColumnLength(c))
		    {
			freeColumns.add(c);
		    }
	    }

	Pair < Integer, Integer > markedPoints = markedPenalty(markerColumns);
	Pair < Integer, Integer > freePoints = freePenalty(freeColumns);

	return new Triple< Integer, Integer, Integer > (progressPoints, markedPoints.getFirst() + freePoints.getFirst(), markedPoints.getSecond() + freePoints.getSecond());
    }

    /**
     * Returns the choice point count for the given position.  The point count
     * is given as a triple containing the point count for progress,
     * the penalty points, and the bonus points.
     *
     * @param start the state at the start of the turn
     * @param progress the state if the turn were ended
     * @return the triple (progress-points, penalty-points, bonus-points)
     */

    protected Triple< Integer, Integer, Integer > countChoice(CantStopState start, CantStopState progress)
    {
	List< Integer > markerColumns = new LinkedList< Integer >();

	List< Integer > freeColumns = new LinkedList< Integer >();

	int choicePoints = 0;

	for (int c = start.getLowestRoll(); c <= start.getHighestRoll(); c++)
	    {
		if (progress.getMarkerPosition(c) != start.getMarkerPosition(c))
		    {
			markerColumns.add(c);

			int cp = choicePoints(c,
					      start.getMarkerPosition(c),
					      progress.getMarkerPosition(c));
			choicePoints += cp;
			// System.out.println("col " + c + " " + start.getMarkerPosition(c) + " to " + progress.getMarkerPosition(c) + ": " + cp);
		    }
		else if (progress.getMarkerPosition(c) < progress.getColumnLength(c))
		    {
			freeColumns.add(c);
		    }
	    }

	Pair < Integer, Integer > markedPoints = markedPenalty(markerColumns);
	Pair < Integer, Integer > freePoints = freePenalty(freeColumns);
	Pair < Integer, Integer > usedMarkerPoints = usedMarkerPenalty(markerColumns.size());

	//System.out.println("marked: " + markedPoints);
	//System.out.println("free: " + freePoints);
	//System.out.println("used: " + usedMarkerPoints);

	return new Triple< Integer, Integer, Integer > (choicePoints, markedPoints.getFirst() + freePoints.getFirst() + usedMarkerPoints.getFirst(), markedPoints.getSecond() + freePoints.getSecond() + usedMarkerPoints.getSecond());
    }

    /**
     * Returns the progress points for advancement in a single column.
     * The progress points can vary based on the position of the
     * colored marker and the position of the neutral marker.
     *
     * @param colored the position of the colored marker in the column to score
     * @param neutral the position of the neutral marker in the column to score
     * @param col the column to score
     * @return the progress point for advancement
     */

    protected abstract int progressPoints(int col, int colored, int neutral);

    /**
     * Returns the choice points for advancement in a single column.
     * The choice points can vary based on the position of the
     * colored marker and the position of the neutral marker.
     * This implementation computes the choice points by invoking
     * <CODE>progressPoints</CODE>.
     *
     * @param colored the position of the colored marker in the column to score
     * @param neutral the position of the neutral marker in the column to score
     * @param col the column to score
     * @return the progress point for advancement
     */

    protected int choicePoints(int col, int colored, int neutral)
    {
	return progressPoints(col, colored, neutral);
    }

    /**
     * Returns the penalty and bonus points for a given set of marked columns.
     *
     * @param marked the list of marked columns
     * @return the pair (penalty-points, bonus-points)
     */

    protected abstract Pair< Integer , Integer > markedPenalty(List< Integer > marked);

    /**
     * Returns the penalty and bonus points for a given set of free columns.
     * This implementation returns (0, 0).
     *
     * @param marked the list of marked columns
     * @return the pair (penalty-points, bonus-points)
     */

    protected Pair< Integer, Integer > freePenalty(List< Integer > free)
    {
	return new Pair< Integer, Integer >(0, 0);
    }

    /**
     * Returns the penalty or bonus points for using the given number of
     * markers.  This implementation returns (0, 0).  Overriding classes
     * should return either (x, 0) or (0, y) where x >= 0 and y <= 0.
     *
     * @param used the number of markers used
     * @return the pair (penalty-points, bonus-points)
     */

    protected Pair< Integer, Integer > usedMarkerPenalty(int used)
    {
	return new Pair< Integer, Integer >(0, 0);
    }
}
