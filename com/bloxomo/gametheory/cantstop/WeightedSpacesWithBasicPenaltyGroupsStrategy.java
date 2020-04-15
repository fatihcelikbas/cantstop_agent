package com.bloxomo.gametheory.cantstop;

import com.bloxomo.gametheory.*;
import java.util.*;

import com.sirmapsalot.combinatorics.*;

/**
 * A Can't Stop strategy that counts progress points with each space
 * on the board given an independent progress score and marked score.
 * The score for the first space marked is divided into points for
 * marking and points for advancement.
 * 
 * @author Jim Glenn 
 * @version 0.1 11/15/2008
 */

public class WeightedSpacesWithBasicPenaltyGroupsStrategy
    extends GeneralizedRuleOfNStrategy
{
    /**
     * An array giving the progress points for advancing j spaces
     * from space i in column c in progress[c][i][j - 1].
     */

    protected int[][][] progress;

    /**
     * An array giving the choice points for advancing j spaces
     * from space i in column c in choice[c][i][j - 1].
     */

    protected int[][][] choice;

    /**
     * The penalty points for all odds.
     */

    protected int oddPenalty;

    /**
     * The penalty points for all even.
     */

    protected int evenPenalty;

    /**
     * The penalty points for all high.
     */

    protected int highPenalty;

    /**
     * The penalty points for all low.
     */

    protected int lowPenalty;

    /**
     * The penalty points for using a marker.
     */

    protected int usedMarkerPenalty;

    /**
     * Creates a new Can't Stop strategy with each space assigned
     * its own progress points and marked points.  Penalties will be
     * zero.
     *
     * @param p an array giving the progress points for advancing from
     * i to j in column c in p[c][i][j]
     * @param m an array giving the progress points for marking in column
     * c when the colored marker is at i in m[c][i].
     */

    public WeightedSpacesWithBasicPenaltyGroupsStrategy(CantStopState g,
							int max,
							Weights weights)
    {
	this(g, max, weights, 0, 0, 0, 0, 0);
    }

    /**
     * Creates a new Can't Stop strategy with each space assigned
     * its own progress points and marked points.  Penalties will be
     * zero.
     *
     * @param weights the points for advancement and marking
     */

    public WeightedSpacesWithBasicPenaltyGroupsStrategy(CantStopState g,
							int max,
							Weights weights,
							int odd,
							int even,
							int high,
							int low,
							int used)
    {
	super(g, max);

	oddPenalty = odd;
	evenPenalty = even;
	highPenalty = high;
	lowPenalty = low;
	usedMarkerPenalty = used;

	// make progress array

	progress = new int[game.countColumns()][][];
	choice = new int[game.countColumns()][][];
	for (int c = game.getLowestRoll(); c <= game.getHighestRoll(); c++)
	    {
		// System.out.println("=== COLUMN " + c + " ===");

		int ci = columnIndex(c);
		int len = game.getColumnLength(c);
		progress[ci] = new int[len][];
		choice[ci] = new int[len][];

		for (int from = 0; from < len; from++)
		    {
			progress[ci][from] = new int[len - from];
			choice[ci][from] = new int[len - from];
			
			progress[ci][from][0] = (weights.markedProgressPoints(c, from + 1)
						 + weights.advancedProgressPoints(c, from + 1));
			choice[ci][from][0] = (weights.markedChoicePoints(c, from + 1)
					       + weights.advancedChoicePoints(c, from + 1));

			for (int advance = 2; advance <= len - from; advance++)
			    {
				progress[ci][from][advance - 1]
				    = (progress[ci][from][advance - 2]
				       + weights.advancedProgressPoints(c, from + advance));
				choice[ci][from][advance - 1]
				    = (choice[ci][from][advance - 2]
				       + weights.advancedChoicePoints(c, from + advance));
			    }
			
			/*
			System.out.print("space " + from + ":");
			for (int adv = 0; adv < progress[ci][from].length; adv++)
			    {
				System.out.print(" " + progress[ci][from][adv]);}
			System.out.println();
			*/

		    }
	    }
    }

    /**
     * Returns the progress points for advancement in a single column.
     * The progress points can vary based on the position of the
     * colored marker and the position of the neutral marker.
     *
     * @param col the column to score
     * @param colored the position of the colored marker in the column to score
     * @param neutral the position of the neutral marker in the column to score
     * @return the progress point for advancement
     */

    protected int progressPoints(int col, int colored, int neutral)
    {
	return progress[col - 2][colored][neutral - colored - 1];
    }

    /**
     * Returns the choice points for advancement in a single column.
     *
     * @param col the column to score
     * @param colored the position of the colored marker in the column to score
     * @param neutral the position of the neutral marker in the column to score
     */

    protected int choicePoints(int col, int colored, int neutral)
    {
	return choice[col - 2][colored][neutral - colored - 1];
    }

    /**
     * Returns the penalty and bonus points for a given set of marked columns.
     *
     * @param marked the list of marked columns
     * @return the pair (penalty-points, bonus-points)
     */

    protected Pair< Integer , Integer > markedPenalty(List< Integer > marked)
    {
	// no penalty or bonus if neutral markers left to use

	if (marked.size() < game.countNeutralMarkers())
	    return new Pair< Integer, Integer >(0, 0);

	int penalty = 0;
	int bonus = 0;

	// count types of columns used

	int evens = 0;
	int odds = 0;
	int highs = 0;
	int lows = 0;
	int middleColumn = (game.getHighestRoll() + game.getLowestRoll()) / 2;

	for (Integer c : marked)
	    {
		if (c % 2 == 0)
		    evens++;
		else
		    odds++;

		if (c <= middleColumn)
		    lows++;

		if (c >= middleColumn)
		    highs++;
	    }

	// add positive values to penalty, negatives to bonus

	if (evens == marked.size())
	    {
		// System.out.println("All even");
		if (evenPenalty > 0)
		    penalty += evenPenalty;
		else
		    bonus += evenPenalty;
	    }
	else if (odds == marked.size())
	    {
		// System.out.println("All odd");
		if (oddPenalty > 0)
		    penalty += oddPenalty;
		else
		    bonus += oddPenalty;
	    }
	if (lows == marked.size())
	    {
		// System.out.println("All low");
		if (lowPenalty > 0)
		    penalty += lowPenalty;
		else
		    bonus += lowPenalty;
	    }
	if (highs == marked.size())
	    {
		// System.out.println("All high");

		if (highPenalty > 0)
		    penalty += highPenalty;
		else
		    bonus += highPenalty;
	    }
	
	return new Pair< Integer, Integer >(penalty, bonus);
    }

    protected Pair< Integer, Integer > usedMarkerPenalty(int used)
    {
	return new Pair< Integer, Integer >(used * usedMarkerPenalty, 0);
    }

    /**
     * Returns the array index used for the given column.
     *
     * @param col a column label
     * @return the corresponding array index
     */

    private int columnIndex(int col)
    {
	return col - 2;
    }
}
