package com.bloxomo.gametheory.cantstop;

import com.bloxomo.gametheory.*;
import com.bloxomo.ga.*;
/**
 * A genome for a Rule-of-28 like strategy.  The penalties,
 * the threshold, and the weights for the columns can be evolved.
 *
 * @author Jim Glenn
 * @version 0.1 11/16/2008
 */

public class RuleOfNGenome extends BitStringGenome implements Weights
{
    private CantStopState game;

    private static final int DEFAULT_PROGRESS_BITS = 3;
    private static final int DEFAULT_CHOICE_BITS = 3;
    private static final int DEFAULT_PENALTY_BITS = 4;
    private static final int DEFAULT_THRESHOLD_BITS = 5;

    private static final int NUM_PENALTY_TYPES = 5;

    private int progressWeightBits;
    private int progressWeightsStart;

    private int choiceWeightBits;
    private int choiceWeightsStart;

    private int numWeights;

    private int penaltyBits;
    private int penaltyStart;

    private int thresholdBits;
    private int thresholdStart;

    private boolean useSymmetry;

    private WeightedSpacesWithBasicPenaltyGroupsStrategy phenotype;

    public RuleOfNGenome()
    {
	//       1  2  3  4  5  6  6  5  4  3  2  1   2  -2   4   4   6   28
	super("0010100111001011101101011000110100011010011011001100111011100");
	game = new CantStopState();
	
	progressWeightBits = 3;
	choiceWeightBits = 3;
	penaltyBits = 4;
	thresholdBits = 5;
	useSymmetry = true;
	phenotype = null;

	numWeights = game.countColumns() / 2 + 1;

	progressWeightsStart = 0;
	choiceWeightsStart = progressWeightsStart + numWeights * progressWeightBits;
	penaltyStart = choiceWeightsStart + numWeights * choiceWeightBits;
	thresholdStart = penaltyStart + NUM_PENALTY_TYPES * penaltyBits;
    }

    public RuleOfNGenome(CantStopState g)
    {
	this(g,
	     DEFAULT_PROGRESS_BITS,
	     DEFAULT_CHOICE_BITS,
	     DEFAULT_PENALTY_BITS,
	     DEFAULT_THRESHOLD_BITS,
	     true);
    }

    public RuleOfNGenome(CantStopState g, int prog, int ch, int pen, int thresh, boolean sym)
    {
	super(countBits(g, prog, ch, pen, thresh, sym));

	game = g;

	progressWeightBits = prog;
	choiceWeightBits = ch;
	penaltyBits = pen;
	thresholdBits = thresh;
	useSymmetry = sym;
	phenotype = null;

	numWeights = (!useSymmetry ? game.countColumns() : game.countColumns() / 2 + 1);

	progressWeightsStart = 0;
	choiceWeightsStart = progressWeightsStart + numWeights * progressWeightBits;
	penaltyStart = choiceWeightsStart + numWeights * choiceWeightBits;
	thresholdStart = penaltyStart + NUM_PENALTY_TYPES * penaltyBits;
    }

    public Object clone()
    {
	RuleOfNGenome result = null;

	result = (RuleOfNGenome)(super.clone());
	result.phenotype = null;
	
	return result;
    }

    private static int countBits(CantStopState g,
				 int progressWeightBits,
				 int choiceWeightBits,
				 int penaltyBits,
				 int thresholdBits,
				 boolean useSymmetry)
    {
	int numColumns = (!useSymmetry ? g.countColumns() : g.countColumns() / 2 + 1);

	return (numColumns * (progressWeightBits + choiceWeightBits)
		+ penaltyBits * NUM_PENALTY_TYPES
		+ thresholdBits);
    }

    public double evaluate()
    {
	if (phenotype == null)
	    {
		phenotype = makePhenotype();
	    }

	return -CantStopSimulator.simulateGame(game, phenotype);
    }

    private WeightedSpacesWithBasicPenaltyGroupsStrategy makePhenotype()
    {
	return new WeightedSpacesWithBasicPenaltyGroupsStrategy(game,
								getThreshold(),
								this,
								getPenalty(0),
								getPenalty(1),
								getPenalty(2),
								getPenalty(3),
								getPenalty(4));
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
	int loc;
	if (!useSymmetry)
	    {
		loc = col - game.getLowestRoll();
	    }
	else
	    {
		loc = Math.abs(game.getMiddleColumn() - col);
	    }
	loc = progressWeightsStart + loc * progressWeightBits;

	return expressPositive(loc, loc + progressWeightBits);
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
	int loc;
	if (!useSymmetry)
	    {
		loc = col - game.getLowestRoll();
	    }
	else
	    {
		loc = Math.abs(game.getMiddleColumn() - col);
	    }
	loc = choiceWeightsStart + loc * choiceWeightBits;

	// System.out.println("col = " + col + ", loc = " + loc + ";" + (loc + choiceWeightBits));

	return expressPositive(loc, loc + choiceWeightBits);
    }

    private int getThreshold()
    {
	return expressPositive(thresholdStart, thresholdStart + thresholdBits);
    }

    private int getPenalty(int i)
    {
	return expressInteger(penaltyStart + i * penaltyBits,
			      penaltyStart + (i + 1) * penaltyBits);
    }

    public String toString()
    {
	StringBuffer buf = new StringBuffer("{P=[");

	for (int col = game.getLowestRoll(); col <= game.getHighestRoll(); col++)
	    buf.append(advancedProgressPoints(col, 0) + " ");
	buf.append("] C=[");
	for (int col = game.getLowestRoll(); col <= game.getHighestRoll(); col++)
	    buf.append(advancedChoicePoints(col, 0) + " ");
	buf.append("] O=" + getPenalty(0));
	buf.append(", E=" + getPenalty(1));
	buf.append(", H=" + getPenalty(2));
	buf.append(", L=" + getPenalty(3));
	buf.append(", M=" + getPenalty(4));
	buf.append(", MAX=" + getThreshold() + "}");

	return buf.toString();
    }

    public static void main(String[] args)
    {
	RuleOfNGenome g = new RuleOfNGenome();
	// System.out.println(g.bits);
	System.out.println(g);
	System.out.println(g.mutate(0.05));
	System.out.println(g.evaluate());
	System.out.println(CantStopSimulator.simulateGames(new CantStopState(),
							   g.makePhenotype(),
							   1,
							   true));
    }
					    
}
