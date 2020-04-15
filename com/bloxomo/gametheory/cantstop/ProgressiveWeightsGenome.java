package com.bloxomo.gametheory.cantstop;

import java.util.*;
import com.bloxomo.gametheory.*;
import com.bloxomo.ga.*;
import com.bloxomo.utility.*;

/**
 * A genome for a generalized Rule-of-28-type strategy.  These genomes
 * encode progress values for each column direcly; choice values are
 * computed using an increment over the previous square (for example,
 * if the choice value for one square is 4 and the increment value for
 * the next square is 2 then the choice value for that square is 6).
 * each squares choice points and progress points.
 *
 * @author Jim Glenn
 * @version 0.1 2/24/2010
 */

public class ProgressiveWeightsGenome extends BitStringGenome implements Weights
{
    private CantStopState game;

    private static final int DEFAULT_PENALTY_BITS = 5;
    private static final int DEFAULT_THRESHHOLD_BITS = 6;
    private static final int DEFAULT_PROGRESS_BITS = 4;
    private static final int DEFAULT_CHOICE_BITS = 6;

    private static final int NUM_PENALTY_TYPES = 4;

    /**
     * The number of bits for each progress value.
     */

    private int progressBits;

    /**
     * The index where the bits for the progress values start.
     */

    private int progressStart;

    /**
     * The number of bits for each choice value.
     */

    private int choiceBits;

    /**
     * The index where the bits for the choice values start.
     */

    private int choiceStart;

    /**
     * For each column, the index where the progress increment values for that
     * column start.  2-based, so column 2's index is in
     * <code>columnChoiceStart[0]</code>.
     */

    private int[] columnChoiceStart;

    /**
     * The number of bits for each penalty value.
     */

    private int penaltyBits;

    /**
     * The index where the bits for the penalty values start.
     */

    private int penaltyStart;

    /**
     * The number of bits for the threshhold value.
     */

    private int threshholdBits;

    /**
     * The index where the bits for the threhhold value start.
     */

    private int threshholdStart;

    /**
     * True if the choice values and progress values are symmetric.
     */

    private boolean useSymmetry;

    /**
     * A cache of choice values.  null if not computed
     */

    private List< List< Integer > > choiceValues;

    /**
     * The proportion of expressed genes.  Only the first
     * x% of choice values will be expressed for any given column;
     * the rest will be 0.  All other genes (progress values, penalties, ...)
     * will be expressed completely.
     */

    private double expressionPct;

    private WeightedSpacesWithBasicPenaltyGroupsStrategy phenotype;

    /**
     * @param g a Can't Stop game
     * @param progress an array with one element per column in g, so
     * that the progress value for column 2 is in progress[0]
     * @param choice an array with lengths corresponding to the
     * lengths of the colunms in g
     */

    public ProgressiveWeightsGenome(CantStopState g,
				    int[] progress,
				    int[][] choice,
				    int oddPenalty,
				    int evenPenalty,
				    int highPenalty,
				    int markerPenalty,
				    int threshhold)
    {
	super(makeBits(progress, choice, oddPenalty, evenPenalty, highPenalty, markerPenalty, threshhold));

	game = g;

	progressBits = DEFAULT_PROGRESS_BITS;
	choiceBits = DEFAULT_CHOICE_BITS;
	penaltyBits = DEFAULT_PENALTY_BITS;
	threshholdBits = DEFAULT_THRESHHOLD_BITS;

	useSymmetry = true;
	phenotype = null;

	expressionPct = 1.0;

	findLoci();
    }

    private void findLoci()
    {
	int sigColumns = game.countColumns();
	if (useSymmetry)
	    {
		sigColumns = (sigColumns + 1) / 2;
	    }
	int lastSigColumn = game.getLowestRoll() + sigColumns - 1;

	progressStart = 0;

	choiceStart = progressStart + progressBits * sigColumns;
	columnChoiceStart = new int[sigColumns];
	columnChoiceStart[0] = choiceStart;
	for (int c = game.getLowestRoll() + 1; c <= lastSigColumn; c++)
	    {
		columnChoiceStart[c - game.getLowestRoll()] = columnChoiceStart[c - game.getLowestRoll() - 1] + choiceBits * game.getColumnLength(c - 1);
	    }

	penaltyStart = columnChoiceStart[sigColumns - 1] + choiceBits * game.getColumnLength(lastSigColumn);
	threshholdStart = penaltyStart + NUM_PENALTY_TYPES * penaltyBits;
    }

    /**
     * @param progress a 2-based array of progress values, so that
     * the progress values for column 2 are in progress[0]
     */

    public static String makeBits(int[] progress,
				  int[][] choice,
				  int oddPenalty,
				  int evenPenalty,
				  int highPenalty,
				  int markerPenalty,
				  int threshhold)
    {
	String bits = "";

	for (int i = 0; i < progress.length; i++)
	    {
		bits += toBinary(progress[i], DEFAULT_PROGRESS_BITS);
	    }

	for (int i = 0; i < choice.length; i++)
	    {
		for (int j = 0; j < choice[i].length; j++)
		    {
			bits += toBinary(choice[i][j], DEFAULT_CHOICE_BITS);
		    }
	    }

	
	int excess = 1 << (DEFAULT_PENALTY_BITS - 1);

	bits += toBinary(oddPenalty + excess, DEFAULT_PENALTY_BITS);
	bits += toBinary(evenPenalty + excess, DEFAULT_PENALTY_BITS);
	bits += toBinary(highPenalty + excess, DEFAULT_PENALTY_BITS);
	bits += toBinary(markerPenalty + excess, DEFAULT_PENALTY_BITS);
	bits += toBinary(threshhold, DEFAULT_THRESHHOLD_BITS);

	return bits;
    }

    private static String toBinary(int i, int len)
    {
	String bits = Integer.toBinaryString(i);

	while (bits.length() < len)
	    bits = "0" + bits;

	if (bits.length() > len)
	    bits = bits.substring(bits.length() - len);

	return bits;
    }

    public ProgressiveWeightsGenome()
    {
	this(new CantStopState(), new int[] {6, 5, 4, 3, 2, 1}, new int[][] { {1, 1, 1}, {1, 1, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1} }, 2, -2, 4, 6, 28);
    }

    public ProgressiveWeightsGenome(CantStopState g)
    {
	this(g,
	     DEFAULT_PROGRESS_BITS,
	     DEFAULT_CHOICE_BITS,
	     DEFAULT_PENALTY_BITS,
	     DEFAULT_THRESHHOLD_BITS,
	     true,
	     1.0);
    }

    public ProgressiveWeightsGenome(CantStopState g, int progBits, int chBits, int penBits, int threshBits, boolean sym, double pct)
    {
	super(countBits(g, progBits, chBits, penBits, threshBits, sym));

	game = g;

	progressBits = progBits;
	choiceBits = chBits;
	penaltyBits = penBits;
	threshholdBits = threshBits
;
	useSymmetry = sym;
	phenotype = null;

	expressionPct = pct;

	findLoci();
    }

    public Object clone()
    {
	ProgressiveWeightsGenome result = null;

	result = (ProgressiveWeightsGenome)(super.clone());
	result.phenotype = null;
	result.choiceValues = null;
	
	return result;
    }

    private static int countBits(CantStopState g,
				 int progressBits,
				 int choiceBits,
				 int penaltyBits,
				 int threshholdBits,
				 boolean useSymmetry)
    {
	int sigColumns;
	int midColumn;
	if (useSymmetry)
	    {
		sigColumns = (g.countColumns() + 1) / 2;
		midColumn = 2 + sigColumns - 1;
	    }
	else
	    {
		sigColumns = g.countColumns();
		midColumn = g.getLowestRoll() + sigColumns / 2;
	    }

	int shortestColumn = g.getColumnLength(g.getLowestRoll());
	int longestColumn = g.getColumnLength(midColumn);
	int numSpaces = (shortestColumn + longestColumn) / 2 * sigColumns;;
	if (!useSymmetry)
	    {
		numSpaces = numSpaces * 2 - longestColumn;
	    }

	return (progressBits * sigColumns          // for progress values
		+ choiceBits * numSpaces           // for choice increments
		+ penaltyBits * NUM_PENALTY_TYPES  // for penalties
		+ threshholdBits);                 // for threshhold
    }

   public double evaluate()
    {
	if (phenotype == null)
	    {
		phenotype = makePhenotype();
	    }

	return -CantStopSimulator.simulateGame(game, phenotype);
    }

    public void setValue(int loc, int val)
    {
	choiceValues = null;
	super.setValue(loc, val);
    }

    public void flip(int loc)
    {
	choiceValues = null;
	super.flip(loc);
    }

    public void alterAllele(int loc)
    {
	choiceValues = null;
	super.alterAllele(loc);
    }

    public WeightedSpacesWithBasicPenaltyGroupsStrategy makePhenotype()
    {
	return new WeightedSpacesWithBasicPenaltyGroupsStrategy(game,
								getThreshhold(),
								this,
								getPenalty(0),
								getPenalty(1),
								getPenalty(2),
								getPenalty(2),
								getPenalty(3));
    }

    /**
     * Returns the progress points for first placing a neutral marker
     * in a column with the colored marker at the given position.
     *
     * @param col a column label
     * @param space the index of a space in that column
     *
     * @return the points for marking that space
     */

    public int markedProgressPoints(int col, int space)
    {
	return advancedProgressPoints(col, space);
    }

    /**
     * Returns the advancement points for moving a neutral marker to
     * a space.
     *
     * @param col a column label
     * @param space the index of a space in that column
     */

    public int advancedProgressPoints(int col, int space)
    {
	if (!useSymmetry)
	    {
		col = col - game.getLowestRoll();
	    }
	else
	    {
		if (col > game.getMiddleColumn())
		    {
			col = game.getHighestRoll() - col;
		    }
		else
		    {
			col = col - game.getLowestRoll();
		    }
	    }
	int loc = progressStart + col * progressBits;

	return expressPositive(loc, loc + progressBits);
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
	return 0;
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
	if (choiceValues == null)
	    {
		makeChoiceValues();
	    }

	if (!useSymmetry)
	    {
		col = col - game.getLowestRoll();
	    }
	else
	    {
		if (col > game.getMiddleColumn())
		    {
			col = game.getHighestRoll() - col;
		    }
		else
		    {
			col = col - game.getLowestRoll();
		    }
	    }

	return choiceValues.get(col).get(space);
    }

    private void makeChoiceValues()
    {
	choiceValues = new ArrayList< List< Integer > >();
	
	int lastCol;
	if (useSymmetry)
	    {
		lastCol = game.getMiddleColumn();
	    }
	else
	    {
		lastCol = game.getHighestRoll();
	    }

	for (int col = game.getLowestRoll(); col <= lastCol; col++)
	    {
		List< Integer > l = new ArrayList< Integer >();
		l.add(0);

		for (int space = 1; space <= game.getColumnLength(col); space++)
		    {
			l.add(getChoiceDelta(col, space) + l.get(l.size() - 1));
		    }

		choiceValues.add(l);
	    }
    }

    /**
     * Returns the difference between the choice value for the given space
     * and the previous space in the same column.
     *
     * @param col a significant column label
     * @param space a positive integer
     */

    private int getChoiceDelta(int col, int space)
    {
	if ((double)space / game.getColumnLength(col) <= expressionPct)
	    {
		int loc = columnChoiceStart[col - game.getLowestRoll()] + (space - 1) * choiceBits;

		return expressPositive(loc, loc + choiceBits);
	    }
	else
	    {
		return 0;
	    }
    }

    private int getThreshhold()
    {
	return expressPositive(threshholdStart, threshholdStart + threshholdBits);
    }

    private int getPenalty(int i)
    {
	return expressInteger(penaltyStart + i * penaltyBits,
			      penaltyStart + (i + 1) * penaltyBits);
    }

    public void express(double pct)
    {
	expressionPct = pct;

	phenotype = null;
    }

    public CreationOperator creator(Map< String, Integer > params)
    {
	int sides = Arguments.setIntegerArgument(params, "sides", 6);
	int length = Arguments.setIntegerArgument(params, "length", 3);

	return new Creator(sides, length);
    }

    private static class Creator implements CreationOperator
    {
	private CantStopState state;

	public Creator(int sides, int length)
	{
	    state = new CantStopState(sides, length);
	}

	public Genotype create()
	{
	    return new ProgressiveWeightsGenome(state);
	}
    }

    public String getArrays()
    {
	StringBuffer buf = new StringBuffer("PROGRESS\n");

	for (int c = game.getLowestRoll(); c <= game.getHighestRoll(); c++)
	    {
		buf.append(c + ": ");
		for (int s = 0; s < game.getColumnLength(c); s++)
		    {
			buf.append(advancedProgressPoints(c, s) + " ");
		    }
		buf.append("\n");
	    }
	buf.append("\nCHOICE\n");
	for (int c = game.getLowestRoll(); c <= game.getHighestRoll(); c++)
	    {
		buf.append(c + ": ");
		for (int s = 0; s < game.getColumnLength(c); s++)
		    {
			buf.append(advancedChoicePoints(c, s) + " ");
		    }
		buf.append("\n");
	    }
	return buf.toString();
    }

    public String toString()
    {
	StringBuffer buf = new StringBuffer("{P=[");

	int lastSigColumn = (useSymmetry ? game.getMiddleColumn() : game.getHighestRoll());

	for (int col = game.getLowestRoll(); col <= lastSigColumn; col++)
	    {
		if (col > game.getLowestRoll())
		    {
			buf.append(" ");
		    }
		buf.append(advancedProgressPoints(col, 0) + " ");
	    }

	buf.append("] C=[");

	for (int col = game.getLowestRoll(); col <= lastSigColumn; col++)
	    {
		if (col > game.getLowestRoll())
		    {
			buf.append(" ");
		    }
		for (int sp = 1; sp <= game.getColumnLength(col); sp++)
		    {
			if (sp > 1)
			    {
				buf.append("+");
			    }
			buf.append("" + getChoiceDelta(col, sp));
		    }
	    }

	buf.append("] O=" + getPenalty(0));
	buf.append(", E=" + getPenalty(1));
	buf.append(", H/L=" + getPenalty(2));
	buf.append(", M=" + getPenalty(3));
	buf.append(", MAX=" + getThreshhold() + "}");

	return buf.toString();
    }

    public static void main(String[] args)
    {
	ProgressiveWeightsGenome g = new ProgressiveWeightsGenome();
	System.out.println(g.bits);
	System.out.println(g);
	System.out.println(g.makePhenotype());
	System.out.println(g.phenotype);
	System.out.println(g.mutate(0.05));
	System.out.println(g.evaluate());

	System.out.println("=== 0%, 50%, 100% expression ===");
	g.express(0.0);
	System.out.println(g);
	g.express(0.5);
	System.out.println(g);
	g.express(1.0);
	System.out.println(g);
	System.out.println("=== 1 game ===");
	System.out.println(CantStopSimulator.simulateGames(new CantStopState(),
							   g.makePhenotype(),
							   1,
							   true));
    }
					    
}
