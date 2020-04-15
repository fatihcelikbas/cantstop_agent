package com.bloxomo.gametheory.cantstop;

import java.io.*;
import java.util.*;
import com.bloxomo.gametheory.*;
import com.bloxomo.statistics.*;
import com.sirmapsalot.combinatorics.*;

public class CantStopSimulator
{
    public static void main(String[] args)
    {
	int sides = 6;
	int shortestColumn = 3;
	int numGames = 1;

	try
	    {
		sides = Integer.parseInt(args[0]);
		shortestColumn = Integer.parseInt(args[1]);
		numGames = Integer.parseInt(args[2]);
	    }
	catch (ArrayIndexOutOfBoundsException e)
	    {
		System.err.println("USAGE: java CantStopSimulator diceSides shortestColumn numGames [[-]verbose]");
		System.exit(1);
	    }
	catch (NumberFormatException e)
	    {
		System.err.println("USAGE: java CantStopSimulator diceSides shortestColumn numGames [[-]verbose [strat [parameters]]] ");
		System.exit(1);
	    }

	int thresh = 28;
	boolean verbose = (numGames <= 10);
	if (args.length >= 4)
	    {
		if (args[3].equals("verbose"))
		    verbose = true;
		else if (args[3].equals("-verbose"))
		    verbose = false;
	    }


	CantStopState s = new CantStopState(sides, shortestColumn);

	CantStopStrategy strat = null;

	if (args.length >= 5)
	    {
		if (args[4].equals("RuleOfN"))
		    {
			if (args.length >= 6)
			    {
				Scanner scan = new Scanner(args[5]);
				int[] progress = new int[sides];
				int[] choice = new int[sides];
				for (int i = 0; i < progress.length; i++)
				    progress[i] = scan.nextInt();
				for (int i = 0; i < choice.length; i++)
				    choice[i] = scan.nextInt();
				strat = new RuleOfN(s,
						    progress,
						    choice,
						    scan.nextInt(),
						    scan.nextInt(),
						    scan.nextInt(),
						    scan.nextInt(),
						    scan.nextInt(),
						    scan.nextInt());
			    }
		    }
		else if (args[4].equals("LinearWeights"))
		    {
			if (args.length >= 6)
			    {
				Scanner scan = new Scanner(args[5]);
				int[] progress = new int[sides];
				int[] intercepts = new int[sides];
				double[] slopes = new double[sides];
				for (int i = 0; i < progress.length; i++)
				    progress[i] = scan.nextInt();
				for (int i = 0; i < intercepts.length; i++)
				    intercepts[i] = scan.nextInt();
				for (int i = 0; i < slopes.length; i++)
				    slopes[i] = scan.nextDouble();
				LinearWeightsGenome gene
				    = new LinearWeightsGenome(s,
							      progress,
							      intercepts,
							      slopes,
							      scan.nextInt(),
							      scan.nextInt(),
							      scan.nextInt(),
							      scan.nextInt(),
							      scan.nextInt());
				System.out.println(gene);
				strat = gene.makePhenotype();
		    }
		    }
	    }

	if (strat == null)
	    {
		int[] progress = {6, 5, 4, 3, 2, 1};
		int[] choice = {1, 2, 3, 4, 5, 6};
		
		
		strat = new RuleOfN(s,
				    progress,
				    choice,
				    2, -2, 4, 4, 6,
				    28);
	    }

	/*
	try
	    {
		// strat = new OptimalCantStopStrategy(sides, shortestColumn);
		// strat = new RuleOfN(s, 28);

		strat = new ParameterizedRuleOfN(new int[] {0,0,11,6,3,2,1,2,3,6,11},
						 new double[] {12,5,5,3,3,1,1,1,1},
						 new int[] {0,0,0,0},
						 0,
						 0,
						 0,
						 0,
						 10000,
						 true);
	    }
	catch (Exception e)
	    {
		e.printStackTrace(System.err);
		System.exit(1);
	    }
	*/
	System.out.println(simulateGames(s, strat, numGames, verbose));
    }


    public static DataSet simulateGames(CantStopState s, CantStopStrategy strat, int numGames, boolean verbose)
    {
	DataSet stats = new MemorylessDataSet();

	int totalMoves = 0;
	long totalMovesSquared = 0;
	for (int game = 0; game < numGames; game++)
	    {
		stats.addSample(simulateGame(s, strat, verbose));
	    }

	return stats;
    }


    public static int simulateGame(CantStopState s, CantStopStrategy strat)
    {
	return simulateGame(s, strat, false);
    }

    public static int simulateGame(CantStopState s, CantStopStrategy strat, boolean verbose)
    {
	int turns = 0;

	while (!s.isFinal())
	    {
		if (verbose)
		    {
			System.out.println("TURN " + (turns + 1) + ": " + s);
		    }

		CantStopState intermediate = (CantStopState)(s.clone());

		do
		    {
			DiceRoll roll = s.rollDice();
			if (verbose)
			    {
				System.out.println(" rolled " + roll);
			    }
			
			List moves = (List)(s.makeLegalMoveMap(intermediate).get(roll));

			if (moves.size() > 0)
			    {
				Multiset move = strat.pickPairs(s, intermediate, roll);

				intermediate = intermediate.makeMove(move);
				if (verbose)
				    {
					System.out.println(" using " + CantStopState.formatMove(move) + " to get to " + intermediate);
				    }
			    }
			else
			    {
				intermediate = s;
			    }
		    }
		while (!intermediate.equals(s)
		       && strat.rollAgain(s, intermediate));

		s = intermediate;
		turns++;
	    }

	return turns;
    }
}
