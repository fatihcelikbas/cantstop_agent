package com.bloxomo.gametheory.cantstop;

import com.bloxomo.gametheory.*;
import com.bloxomo.ga.*;
import java.util.*;

/**
 * A genome for a generalized Rule of 28 type strategy.  These genomes
 * encode quadratic formulas for each column that are used to determine 
 * each squares choice points and progress points.
 *
 * @author Jim Glenn
 * @version 0.1 3/12/2010 from LinearWeightsGenome of 11/19/2008
 */

public class QuadraticWeightsGenome extends BitStringGenome implements Weights
{
    private CantStopState game;

    private static final int DEFAULT_INTERCEPT_BITS = 4;
    private static final int DEFAULT_SLOPE_BITS = 5;
    private static final int DEFAULT_QUADRATIC_BITS = 5;
    private static final int DEFAULT_PENALTY_BITS = 5;
    private static final int DEFAULT_THRESHOLD_BITS = 6;

    private static final int NUM_PENALTY_TYPES = 4;

    public static final double slopes[] = 
    {0.0, 1.0, 1.5, 2.0, 3.0, 4.0, 5.0, 6.0,
     8.0, 10.0, 12.0, 14.0, 16.0, 18.0, 20.0, 24.0,
     28.0, 32.0, 36.0, 40.0, 44.0, 48.0, 52.0, 56.0,
     60.0, 64.0, 72.0, 80.0, 88.0, 96.0, 112.0, 128.0};

    private int interceptBits;
    private int slopeBits;
    private int quadraticBits;

    private int progressInterceptStart;
    private int progressSlopeStart;

    private int choiceInterceptStart;
    private int choiceSlopeStart;
    private int choiceQuadraticStart;

    private int numFormulas;

    private int penaltyBits;
    private int penaltyStart;

    private int thresholdBits;
    private int thresholdStart;

    private boolean useSymmetry;

    private WeightedSpacesWithBasicPenaltyGroupsStrategy phenotype;

    public QuadraticWeightsGenome(CantStopState g,
				  int[] progress,
				  int[] intercepts,
				  double[] slopes,
				  double[] quadratics,
				  int oddPenalty,
				  int evenPenalty,
				  int highPenalty,
				  int markerPenalty,
				  int threshold)
    {
	super(makeBits(progress, intercepts, slopes, quadratics, oddPenalty, evenPenalty, highPenalty, markerPenalty, threshold));

	game = g;
	interceptBits = 3;
	slopeBits = 5;
	quadraticBits = 5;
	penaltyBits = 4;
	thresholdBits = 5;
	useSymmetry = true;
	phenotype = null;

	numFormulas = (!useSymmetry ? game.countColumns() : game.countColumns() / 2 + 1);

	progressInterceptStart = 0;
	progressSlopeStart = progressInterceptStart + numFormulas * interceptBits;
	choiceInterceptStart = progressSlopeStart;
	choiceSlopeStart = choiceInterceptStart + numFormulas * interceptBits;
	choiceQuadraticStart = choiceSlopeStart + numFormulas * slopeBits;
	penaltyStart = choiceQuadraticStart + numFormulas * quadraticBits;
	thresholdStart = penaltyStart + NUM_PENALTY_TYPES * penaltyBits;
    }

    public static String makeBits(int[] progress,
				  int[] intercepts,
				  double[] slopes,
				  double[] quadratics,
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
	for (int i = quadratics.length - 1; i >= 0; i--)
	    {
		bits += toBinary(findSlope(quadratics[i]), DEFAULT_QUADRATIC_BITS);
	    }
	bits += toBinary(oddPenalty + 8, DEFAULT_PENALTY_BITS);
	bits += toBinary(evenPenalty + 8, DEFAULT_PENALTY_BITS);
	bits += toBinary(highPenalty + 8, DEFAULT_PENALTY_BITS);
	bits += toBinary(markerPenalty + 8, DEFAULT_PENALTY_BITS);
	bits += toBinary(threshold, DEFAULT_THRESHOLD_BITS);

	System.out.println(bits);

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

    public QuadraticWeightsGenome()
    {
	//       1  2  3  4  5  6  6  5  4  3  2  1  0  2  3  4  5  6  0  0  0  0  0  0   2  -2   4   6   28
	super("001010011100101110110101100011010001000010011100101110000000000000000000101001101100110011100");

	game = new CantStopState();
	
	interceptBits = 3;
	slopeBits = 3;
	quadraticBits = 3;
	penaltyBits = 4;
	thresholdBits = 5;
	useSymmetry = true;
	phenotype = null;

	numFormulas = (!useSymmetry ? game.countColumns() : game.countColumns() / 2 + 1);

	progressInterceptStart = 0;
	progressSlopeStart = progressInterceptStart + numFormulas * interceptBits;
	choiceInterceptStart = progressSlopeStart;
	choiceSlopeStart = choiceInterceptStart + numFormulas * interceptBits;
	choiceQuadraticStart = choiceSlopeStart + numFormulas * slopeBits;
	penaltyStart = choiceQuadraticStart + numFormulas * quadraticBits;
	thresholdStart = penaltyStart + NUM_PENALTY_TYPES * penaltyBits;
    }

    public QuadraticWeightsGenome(CantStopState g)
    {
	this(g,
	     DEFAULT_INTERCEPT_BITS,
	     DEFAULT_SLOPE_BITS,
	     DEFAULT_QUADRATIC_BITS,
	     DEFAULT_PENALTY_BITS,
	     DEFAULT_THRESHOLD_BITS,
	     true);
    }

    public QuadraticWeightsGenome(CantStopState g, int cept, int slp, int quad, int pen, int thresh, boolean sym)
    {
	super(countBits(g, cept, slp, quad, pen, thresh, sym));

	game = g;

	interceptBits = cept;
	slopeBits = slp;
	quadraticBits = quad;
	penaltyBits = pen;
	thresholdBits = thresh;
	useSymmetry = sym;
	phenotype = null;

	numFormulas = (!useSymmetry ? game.countColumns() : game.countColumns() / 2 + 1);

	progressInterceptStart = 0;
	progressSlopeStart = progressInterceptStart + numFormulas * interceptBits;
	choiceInterceptStart = progressSlopeStart;
	choiceSlopeStart = choiceInterceptStart + numFormulas * interceptBits;
	choiceQuadraticStart = choiceSlopeStart + numFormulas * slopeBits;
	penaltyStart = choiceQuadraticStart + numFormulas * quadraticBits;
	thresholdStart = penaltyStart + NUM_PENALTY_TYPES * penaltyBits;
    }

    public Object clone()
    {
	QuadraticWeightsGenome result = null;

	result = (QuadraticWeightsGenome)(super.clone());
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
				 int quadraticBits,
				 int penaltyBits,
				 int thresholdBits,
				 boolean useSymmetry)
    {
	int numFormulas = (!useSymmetry ? g.countColumns() : g.countColumns() / 2 + 1);

	return (numFormulas * (interceptBits + slopeBits + quadraticBits) // for choices
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
	double x = (double)(space - 1) / (game.getColumnLength(col) - 1); 
	return (int)(getProgressQuadratic(col) * x * x + getProgressSlope(col) * x + getProgressIntercept(col));
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
	double x = (double)(space - 1) / (game.getColumnLength(col) - 1); 
	return (int)(getChoiceQuadratic(col) * x * x + getChoiceSlope(col) * x + getChoiceIntercept(col));
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
    
    private double getChoiceQuadratic(int col)
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
	loc = choiceQuadraticStart + loc * quadraticBits;

	return slopes[expressPositive(loc, loc + slopeBits)];
    }

    private double getProgressSlope(int col)
    {
	return 0.0;
    }

    private double getProgressQuadratic(int col)
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
		buf.append(getChoiceQuadratic(col) + "x^2+" + getChoiceSlope(col) + "x+" + getChoiceIntercept(col) + " ");
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
	QuadraticWeightsGenome g = new QuadraticWeightsGenome(new CantStopState());
	// System.out.println(g.bits);
	System.out.println(g);
	System.out.println(g.getBitString());
	System.out.println(g.getArrays());
	System.out.println(g.mutate(0.05));
	System.out.println(g.getArrays());
	System.out.println(g.evaluate());
	System.out.println(CantStopSimulator.simulateGames(new CantStopState(),
							   g.makePhenotype(),
							   1,
							   true));
    }
					    
}
