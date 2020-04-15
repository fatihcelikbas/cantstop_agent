package com.bloxomo.gametheory.cantstop;

import com.bloxomo.gametheory.*;
import java.io.*;
import java.util.*;
import com.sirmapsalot.combinatorics.*;

/**
 * Computes some simple facts for Can't Stop Strategies.
 * 1) For each roll, what move would be made from the start position.
 * 2) For each set of columns, how far to move in those columns before
 * quitting.
 */

public class CSStrategyExaminer
{
    public static void main(String[] args)
    {
	int sides = 6;
	int shortestColumn = 3;

	try
	    {
		sides = Integer.parseInt(args[0]);
		shortestColumn = Integer.parseInt(args[1]);
	    }
	catch (ArrayIndexOutOfBoundsException e)
	    {
		System.err.println("USAGE: java CSStrategyExaminer diceSides shortestColumn numGames");
		System.exit(1);
	    }
	catch (NumberFormatException e)
	    {
		System.err.println("USAGE: java CSStrategyExaminer diceSides shortestColumn numGames");
		System.exit(1);
	    }

	CantStopState s = new CantStopState(sides, shortestColumn);

	CantStopStrategy strat = null;

	try
	    {
		// strat = new OptimalCantStopStrategy(sides, shortestColumn);
		
		strat = new ParameterizedRuleOfN(new int[] {0,0,11,6,3,2,1,2,3,6,11},
                                                 new double[] {12,5,5,3,3,1,1,1,1,1},
                                                 new int[] {0,0,0,0},
                                                 0,
                                                 0,
                                                 0,
                                                 0,
                                                 1000,
						 true);
	    }
	catch (Exception e)
	    {
		e.printStackTrace(System.err);
		System.exit(1);
	    }

	DiceRoll roll = new DiceRoll(s.getTotalDice(), sides);

	do
	    {
		System.out.println(roll + " " + CantStopState.formatMove(strat.pickPairs(s, s, roll)));
		
		if (roll.hasNext())
		    roll = roll.next();
		else
		    roll = null;
	    }
	while (roll != null);
    }
}
