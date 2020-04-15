package com.bloxomo.gametheory.cantstop;

import com.bloxomo.gametheory.*;
import java.util.*;
import com.sirmapsalot.combinatorics.*;

/**
 * Implements a generalized version of the Rule of 28 Strategy from
 * Michael Keller (http://www.solitairelaboratory.com/cantstop.html).
 */

public class RuleOfN
    extends WeightedSpacesWithBasicPenaltyGroupsStrategy
{
    public static final int ODD_PENALTY = 2;
    public static final int EVEN_BONUS = -2;
    public static final int HIGH_PENALTY = 4;
    public static final int LOW_PENALTY = 4;
    public static final int USED_MARKER_PENALTY = 6;

    public RuleOfN(CantStopState g, int n)
    {
	super(g, n, new ColumnWeights(g), ODD_PENALTY, EVEN_BONUS, HIGH_PENALTY, LOW_PENALTY, USED_MARKER_PENALTY);
    }

    public RuleOfN(CantStopState g,
		   int[] columnWeights,
		   int oddPenalty,
		   int evenPenalty,
		   int highPenalty,
		   int lowPenalty,
		   int usedMarkerPenalty,
		   int threshold)
    {
	super(g, threshold, new ColumnWeightsArray(columnWeights), oddPenalty, evenPenalty, highPenalty, lowPenalty, usedMarkerPenalty);
    }

    public RuleOfN(CantStopState g,
		   int[] progressWeights,
		   int[] choiceWeights,
		   int oddPenalty,
		   int evenPenalty,
		   int highPenalty,
		   int lowPenalty,
		   int usedMarkerPenalty,
		   int threshold)
    {
	super(g, threshold, new ColumnWeightsArray(progressWeights, choiceWeights), oddPenalty, evenPenalty, highPenalty, lowPenalty, usedMarkerPenalty);
    }

    private static class ColumnWeightsArray implements Weights
    {
	private int[] progressWeights;
	private int[] choiceWeights;

	private ColumnWeightsArray(int[] p)
	{
	    progressWeights = new int[p.length];
	    System.arraycopy(p, 0, progressWeights, 0, p.length);
	    choiceWeights = progressWeights;
	}

	private ColumnWeightsArray(int[] p, int[] c)
	{
	    progressWeights = new int[p.length];
	    System.arraycopy(p, 0, progressWeights, 0, p.length);

	    choiceWeights = new int[c.length];
	    System.arraycopy(c, 0, choiceWeights, 0, c.length);
	}

	public int advancedProgressPoints(int col, int neutral)
	{
	    if (col - 2 < progressWeights.length)
		return progressWeights[col - 2];
	    else
		return progressWeights[2 * progressWeights.length - col];
	}

	public int markedProgressPoints(int col, int neutral)
	{
	    return advancedProgressPoints(col, neutral);
	}

	public int advancedChoicePoints(int col, int neutral)
	{
	    if (col - 2 < choiceWeights.length)
		return choiceWeights[col - 2];
	    else
		return choiceWeights[2 * choiceWeights.length - col];
	}

	public int markedChoicePoints(int col, int neutral)
	{
	    return advancedChoicePoints(col, neutral);
	}
    }

    private static class ColumnWeights implements Weights
    {
	private CantStopState game;

	private ColumnWeights(CantStopState g)
	{
	    game = g;
	}

	/**
	 * Returns the progress points for advancement in a single column.
	 * The progress points can vary based on the position of the
	 * colored marker and the position of the neutral marker.
	 *
	 * @param neutral the position of the neutral marker in the column to score
	 * @param colored the position of the colored marker in the column to score
	 * @param col the column to score
	 * @return the progress point for advancement
	 */
	
	public int advancedProgressPoints(int col, int neutral)
	{
	    return columnValue(col);
	}
	
	/**
	 * Returns the progress points for first marking a column.
	 * The progress points can vary based on the position of the
	 * colored marker.  This implementation returns 0.
	 *
	 * @param colored the position of the colored marker in the column to score
	 * @param col the column to score
	 * @return the progress point for marking
	 */
	
	public int markedProgressPoints(int col, int colored)
	{
	    return columnValue(col);
	}
	
	/**
	 * Returns the choice points for first placing a neutral marker
	 * in a column with the colored marker at the given position.
	 *
	 * @param col a column label
	 * @param space the index of a space in that column
	 *
	 * @return the points for marking that space
	 */
	
	public int markedChoicePoints(int col, int space)
	{
	    return columnValue(col);
	}
	
	/**
	 * Returns the choice points for moving a neutral marker to
	 * a space.
	 *
	 * @param col a column label
	 * @param space the index of a space in that column
	 */
	
	public int advancedChoicePoints(int col, int space)
	{
	    return columnValue(col);
	}

	/**
	 * Returns the value of a given column.  The middle column's value
	 * is 1 and values increase by 1 os you move outward.
	 *
	 * @return the value of a given column
	 */
	
	private int columnValue(int col)
	{
	    int middleColumn = (game.getHighestRoll() + game.getLowestRoll()) / 2;
	    
	    return (Math.abs(col - middleColumn) + 1);
	}
    }

    public static void main(String[] args)
    {
	new RuleOfN(new CantStopState(), 28);
    }
}
