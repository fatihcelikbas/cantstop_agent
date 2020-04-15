package com.bloxomo.gametheory.cantstop;

import com.bloxomo.gametheory.*;
import com.bloxomo.ga.*;
public class CantStopRuleOfNGenotype extends BitStringGenome
{
    public static final int COLUMN_VALUE_BITS = 4;
    public static final int COLUMN_WEIGHT_BITS = 4;
    public static final int MARKER_VALUE_BITS = 3;
    public static final int PENALTY_BITS = 3;
    public static final int PENALTY_BASIS = 4;
    public static final int THRESHOLD_BITS = 8;

    private ParameterizedRuleOfN phenotype;
    private CantStopState gameStart;

    public CantStopRuleOfNGenotype(CantStopState s)
    {
	super(COLUMN_VALUE_BITS * s.countColumns()
	      + COLUMN_WEIGHT_BITS * s.countColumns()
	      + MARKER_VALUE_BITS * (s.countNeutralMarkers() + 1)
	      + PENALTY_BITS * 4
	      + THRESHOLD_BITS);

	gameStart = s;

	makePhenotype();
    }
	
    public Object clone()
    {
	CantStopRuleOfNGenotype result = (CantStopRuleOfNGenotype)(super.clone());

	result.makePhenotype();

	return result;
    }

    private int getBits(int start, int count)
    {
	int value = 0;
	for (int i = start; i < start + count; i++)
	    {
		value = value * 2;
		if (bits.get(i))
		    value++;
	    }

	return value;
    }

    private void makePhenotype()
    {
	int startBit = 0; // the first bit of the gene we're working on
	int[] columnValues = new int[gameStart.getHighestRoll() + 1];
	for (int c = gameStart.getLowestRoll(); c <= gameStart.getHighestRoll(); c++)
	    {
		columnValues[c] = getBits(startBit, COLUMN_VALUE_BITS);
		startBit += COLUMN_VALUE_BITS;
	    }

	double[] columnWeights = new double[gameStart.countColumns()];
	for (int i = 0; i < columnWeights.length; i++)
	    {
		columnWeights[i] = 1.0 + getBits(startBit, COLUMN_WEIGHT_BITS) / 8.0;
		startBit += COLUMN_WEIGHT_BITS;
	    }
	
	int[] markerValues = new int[gameStart.countNeutralMarkers() + 1];
	for (int i = 0; i < markerValues.length; i++)
	    {
		markerValues[i] = getBits(startBit, MARKER_VALUE_BITS);
		startBit += MARKER_VALUE_BITS;
	    }

	int allOddValue = getBits(startBit, PENALTY_BITS) - PENALTY_BASIS;
	int allEvenValue = getBits(startBit + PENALTY_BITS, PENALTY_BITS) - PENALTY_BASIS;
	int allLowValue = getBits(startBit + 2 * PENALTY_BITS, PENALTY_BITS) - PENALTY_BASIS;
	int allHighValue = getBits(startBit + 3 * PENALTY_BITS, PENALTY_BITS) - PENALTY_BASIS;

	startBit += 4 * PENALTY_BITS;

	int threshold = getBits(startBit, THRESHOLD_BITS);

	phenotype = new ParameterizedRuleOfN(columnValues,
					     columnWeights,
					     markerValues,
					     allOddValue,
					     allEvenValue,
					     allLowValue,
					     allHighValue,
					     threshold);
    }

    public double evaluate()
    {
	return -CantStopSimulator.simulateGame(gameStart, phenotype);
    }

    public String toString()
    {
	return phenotype.toString();
    }

    public static void main(String[] args)
    {
	Genotype g = new CantStopRuleOfNGenotype(new CantStopState(5, 2));

	System.out.println(g);
	System.out.println(g.evaluate());
    }
}

