package com.bloxomo.gametheory.cantstop;

import java.util.*;
import com.bloxomo.gametheory.*;
import com.bloxomo.ga.*;
import com.bloxomo.utility.*;

import com.bloxomo.statistics.*; // used in main

/**
 * A genome for a generalize Rule of 28 type strategy.  These genomes
 * encode linear formulas for each column that are used to determine 
 * each squares choice points and progress points.
 *
 * @author Jim Glenn
 * @version 0.1 11/19/2008
 */

public class LinearWeightsGenome extends BitStringGenome implements Weights
{
    private CantStopState game;

    private static final int DEFAULT_INTERCEPT_BITS = 4;
    private static final int DEFAULT_SLOPE_BITS = 5;
    private static final int DEFAULT_PENALTY_BITS = 5;
    private static final int DEFAULT_THRESHHOLD_BITS = 6;

    private static final int NUM_PENALTY_TYPES = 4;

    public static final double slopes[] = 
    {0.0, 1.0, 1.5, 2.0, 3.0, 4.0, 5.0, 6.0,
     8.0, 10.0, 12.0, 14.0, 16.0, 18.0, 20.0, 24.0,
     28.0, 32.0, 36.0, 40.0, 44.0, 48.0, 52.0, 56.0,
     60.0, 64.0, 72.0, 80.0, 88.0, 96.0, 112.0, 128.0};

    private int interceptBits;
    private int slopeBits;

    private int progressInterceptStart;
    private int progressSlopeStart;

    private int choiceInterceptStart;
    private int choiceSlopeStart;

    private int numFormulas;

    private int penaltyBits;
    private int penaltyStart;

    private int thresholdBits;
    private int thresholdStart;

    private boolean useSymmetry;

    private WeightedSpacesWithBasicPenaltyGroupsStrategy phenotype;

    public LinearWeightsGenome(CantStopState g,
			       int[] progress,
			       int[] intercepts,
			       double[] slopes,
			       int oddPenalty,
			       int evenPenalty,
			       int highPenalty,
			       int markerPenalty,
			       int threshold)
    {
	super(makeBits(progress, intercepts, slopes, oddPenalty, evenPenalty, highPenalty, markerPenalty, threshold));

	game = g;
	interceptBits = DEFAULT_INTERCEPT_BITS;
	slopeBits = DEFAULT_SLOPE_BITS;
	penaltyBits = DEFAULT_PENALTY_BITS;
	thresholdBits = DEFAULT_THRESHHOLD_BITS;
	useSymmetry = true;
	phenotype = null;

	numFormulas = (!useSymmetry ? game.countColumns() : game.countColumns() / 2 + 1);

	progressInterceptStart = 0;
	progressSlopeStart = progressInterceptStart + numFormulas * interceptBits;
	choiceInterceptStart = progressSlopeStart;
	choiceSlopeStart = choiceInterceptStart + numFormulas * interceptBits;
	penaltyStart = choiceSlopeStart + numFormulas * slopeBits;
	thresholdStart = penaltyStart + NUM_PENALTY_TYPES * penaltyBits;
    }

    public static String makeBits(int[] progress,
				  int[] intercepts,
				  double[] slopes,
				  int oddPenalty,
				  int evenPenalty,
				  int highPenalty,
				  int markerPenalty,
				  int threshold)
    {
	String bits = "";

	for (int i = progress.length - 1; i >= 0; i--)
	    {
		bits += toBinary(progress[i], DEFAULT_INTERCEPT_BITS);
	    }
	for (int i = intercepts.length - 1; i >= 0; i--)
	    {
		bits += toBinary(intercepts[i], DEFAULT_INTERCEPT_BITS);
	    }
	for (int i = slopes.length - 1; i >= 0; i--)
	    {
		bits += toBinary(findSlope(slopes[i]), DEFAULT_SLOPE_BITS);
	    }
	bits += toBinary(oddPenalty + (1 << (DEFAULT_PENALTY_BITS - 1)), DEFAULT_PENALTY_BITS);
	bits += toBinary(evenPenalty + (1 << (DEFAULT_PENALTY_BITS - 1)), DEFAULT_PENALTY_BITS);
	bits += toBinary(highPenalty + (1 << (DEFAULT_PENALTY_BITS - 1)), DEFAULT_PENALTY_BITS);
	bits += toBinary(markerPenalty + (1 << (DEFAULT_PENALTY_BITS - 1)), DEFAULT_PENALTY_BITS);
	bits += toBinary(threshold, DEFAULT_THRESHHOLD_BITS);

	return bits;
    }

    private static int findSlope(double m)
    {
	int closest = -1;
	for (int i = 0; i < slopes.length; i++)
	    {
		if (closest == -1 || Math.abs(slopes[i] - m) < Math.abs(slopes[closest] - m))
		    closest = i;
	    }
	return closest;
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

    public LinearWeightsGenome()
    {
	//       1  2  3  4  5  6  6  5  4  3  2  1  0  2  3  4  5  6   2  -2   4   6   28
	super("001010011100101110110101100011010001000010011100101110101001101100110011100");

	game = new CantStopState();
	
	interceptBits = 3;
	slopeBits = 3;
	penaltyBits = 4;
	thresholdBits = 5;
	useSymmetry = true;
	phenotype = null;

	numFormulas = (!useSymmetry ? game.countColumns() : game.countColumns() / 2 + 1);

	progressInterceptStart = 0;
	progressSlopeStart = progressInterceptStart + numFormulas * interceptBits;
	choiceInterceptStart = progressSlopeStart;
	choiceSlopeStart = choiceInterceptStart + numFormulas * interceptBits;
	penaltyStart = choiceSlopeStart + numFormulas * slopeBits;
	thresholdStart = penaltyStart + NUM_PENALTY_TYPES * penaltyBits;
    }

    public LinearWeightsGenome(CantStopState g)
    {
	this(g,
	     DEFAULT_INTERCEPT_BITS,
	     DEFAULT_SLOPE_BITS,
	     DEFAULT_PENALTY_BITS,
	     DEFAULT_THRESHHOLD_BITS,
	     true);
    }

    public LinearWeightsGenome(CantStopState g, int cept, int slp, int pen, int thresh, boolean sym)
    {
	super(countBits(g, cept, slp, pen, thresh, sym));

	game = g;

	interceptBits = cept;
	slopeBits = slp;
	penaltyBits = pen;
	thresholdBits = thresh;
	useSymmetry = sym;
	phenotype = null;

	numFormulas = (!useSymmetry ? game.countColumns() : game.countColumns() / 2 + 1);

	progressInterceptStart = 0;
	progressSlopeStart = progressInterceptStart + numFormulas * interceptBits;
	choiceInterceptStart = progressSlopeStart;
	choiceSlopeStart = choiceInterceptStart + numFormulas * interceptBits;
	penaltyStart = choiceSlopeStart + numFormulas * slopeBits;
	thresholdStart = penaltyStart + NUM_PENALTY_TYPES * penaltyBits;
    }

    public Object clone()
    {
	LinearWeightsGenome result = null;

	result = (LinearWeightsGenome)(super.clone());
	result.phenotype = null;
	
	return result;
    }

    /**
     * Returns a creation operator for this class.  The creator returned
     * will create individuals to play the default version of Can't Stop.
     *
     * @return a creation operator for this class
     */

    public CreationOperator creator()
    {
	return new CantStopCreator(getClass().getName(),
				   new CantStopState(CantStopCreator.DEFAULT_SIDES,
						     CantStopCreator.DEFAULT_SHORTEST_COLUMN_LENGTH));
    }

    /**
     * Returns a creation operator for this class with the given parameters.
     * The creator returned will play the version of Can't Stop defined
     * by the "sides" and "length" values in the given map, with missing
     * values replaced by the defaults as specified by the CantStopCreator
     * class.
     *
     * @param a map with optional "sides" and "length" keys, each with
     * a value (if present) that is greater than 1 and 0 respectively
     *
     * @return a creation operator for this class
     */

    public CreationOperator creator(Map< String, Integer > params)
    {
	return new CantStopCreator(getClass().getName(), params);
    }

    private static int countBits(CantStopState g,
				 int interceptBits,
				 int slopeBits,
				 int penaltyBits,
				 int thresholdBits,
				 boolean useSymmetry)
    {
	int numFormulas = (!useSymmetry ? g.countColumns() : g.countColumns() / 2 + 1);

	return (numFormulas * (interceptBits + slopeBits) // for choices
		+ numFormulas * interceptBits             // for progress
		+ penaltyBits * NUM_PENALTY_TYPES         // for penalties
		+ thresholdBits);                         // for threshold
    }

    public double evaluate()
    {
	if (phenotype == null)
	    {
		phenotype = makePhenotype();
	    }

	return -CantStopSimulator.simulateGame(game, phenotype);
    }

    public WeightedSpacesWithBasicPenaltyGroupsStrategy makePhenotype()
    {
	return new WeightedSpacesWithBasicPenaltyGroupsStrategy(game,
								getThreshold(),
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
	return (int)(getProgressSlope(col) * space / (game.getColumnLength(col) - 1) + getProgressIntercept(col));
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
	return (int)(getChoiceSlope(col) * (space - 1) / (game.getColumnLength(col) - 1) + getChoiceIntercept(col));
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

    private double getChoiceSlope(int col)
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
	loc = choiceSlopeStart + loc * slopeBits;

	return slopes[expressPositive(loc, loc + slopeBits)];
    }

    private double getProgressSlope(int col)
    {
	return 0.0;
    }

    private int getChoiceIntercept(int col)
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
	loc = choiceInterceptStart + loc * interceptBits;

	return expressPositive(loc, loc + interceptBits);
    }

    private int getProgressIntercept(int col)
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
	loc = progressInterceptStart + loc * interceptBits;

	return expressPositive(loc, loc + interceptBits);
    }

    public String getArrays()
    {
	StringBuffer buf = new StringBuffer("PROGRESS\n");

	for (int c = game.getLowestRoll(); c <= game.getHighestRoll(); c++)
	    {
		buf.append(c + ": ");
		for (int s = 1; s <= game.getColumnLength(c); s++)
		    {
			buf.append(advancedProgressPoints(c, s) + " ");
		    }
		buf.append("\n");
	    }
	buf.append("\nCHOICE\n");
	for (int c = game.getLowestRoll(); c <= game.getHighestRoll(); c++)
	    {
		buf.append(c + ": ");
		for (int s = 1; s <= game.getColumnLength(c); s++)
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

	for (int col = game.getLowestRoll(); col <= game.getHighestRoll(); col++)
	    {
		buf.append(getProgressIntercept(col) + " ");
	    }
	buf.append("] C=[");
	for (int col = game.getLowestRoll(); col <= game.getHighestRoll(); col++)
	    {
		buf.append(getChoiceSlope(col) + "x+" + getChoiceIntercept(col) + " ");
	    }
	buf.append("] O=" + getPenalty(0));
	buf.append(", E=" + getPenalty(1));
	buf.append(", H=" + getPenalty(2));
	buf.append(", L=" + getPenalty(2));
	buf.append(", M=" + getPenalty(3));
	buf.append(", MAX=" + getThreshold() + "}");

	return buf.toString();
    }

    public static void main(String[] args)
    {
	/**
	int[] progress = {7, 6, 4, 3, 2, 1};
	int[] intercepts = {7, 1, 2, 1, 3, 4};
	double[] slopes = {64, 24, 28, 8, 18, 12};
	// 1, 0, -4, 4, 24
	*/

	// 8.787132 over 250000 games (std. dev 2.4974779606956807)
	int[] progress = {15, 13, 8, 8, 4, 3};
	int[] intercepts = {15, 0, 3, 0, 7, 5};
	double[] slopes = {72, 48, 56, 44, 28, 32};
	// 11, -1, 4, 12, 61

	LinearWeightsGenome g = new LinearWeightsGenome(new CantStopState(),
							progress,
							intercepts,
							slopes,
							11,
							-1,
							4,
							12,
							61);
	// System.out.println(g.bits);
	System.out.println(g);
	System.out.println(g.getArrays());
	//System.out.println(g.mutate(0.05));
	//System.out.println(g.getArrays());
	//System.out.println(g.evaluate());

	DataSet stats = new MemorylessDataSet();
	
	for (int game = 0; game < 1; game++)
	    {
		double turns = CantStopSimulator.simulateGames(new CantStopState(),
							       g.makePhenotype(),
							       1,
							       true).mean();
		stats.addSample(turns);
	    }
	System.out.println(stats);
    }
					    
}
