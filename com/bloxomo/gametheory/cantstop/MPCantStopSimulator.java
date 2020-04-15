package com.bloxomo.gametheory.cantstop;

import com.bloxomo.gametheory.*;
import java.io.*;
import java.util.*;
import com.sirmapsalot.combinatorics.*;

public class MPCantStopSimulator
{
    public static void main(String[] args)
    {
	int players = MPCantStopGame.DEFAULT_PLAYERS;
	int sides = MPCantStopGame.DEFAULT_SIDES;
	int len = MPCantStopGame.DEFAULT_SHORTEST_COLUMN;
	int delta = MPCantStopGame.DEFAULT_COLUMN_DIFFERENCE;
	int toWin = MPCantStopGame.DEFAULT_COLUMNS_TO_WIN;

	int numGames = 1;

	try
	    {
		players = Integer.parseInt(args[0]);
		sides = Integer.parseInt(args[1]);
		len = Integer.parseInt(args[2]);
		if (args.length > 3)
		    delta = Integer.parseInt(args[3]);
		if (args.length > 4)
		    toWin = Integer.parseInt(args[4]);
		if (args.length > 5)
		    numGames = Integer.parseInt(args[5]);
	    }
	catch (ArrayIndexOutOfBoundsException aioob)
	    {
		System.err.println("USAGE: java MPCantStopGame players sides shortest-column [column-diff [columns-to-win [num-games]]]");
		System.exit(1);
	    }

	MPCantStopGame g = new MPCantStopGame(players, sides, len, delta, toWin);

	MPPositionValueMap values = g.getPositionValueMap();

	try
	    {
		DataInputStream in = new DataInputStream(new FileInputStream("mp_cant_stop_" + players + "_" + sides + "_" + len + "_" + delta + "_" + toWin + ".dat"));
		values.read(in);
	    }			 
	catch (IOException e)
	    {
		System.err.println("Could not open data file");
		e.printStackTrace(System.err);
		System.exit(1);
	    }

	int[] wins = new int[players];

	for (int i = 0; i < numGames; i++)
	    {
		int winner = simulateGame(g, (numGames <= 10), values);
		wins[winner]++;
	    }

	for (int p = 0; p < players; p++)
	    {
		System.out.println("Player " + p + ": " + wins[p]);
	    }
	System.err.println("FOR TESTING SMALL 3-PLAYER VERSION ONLY -- SEE CODE TO CHANGE BACK");
    }

    private static List< Map< Pair< Long, DiceRoll >, Grouping > > ms;
    private static List< Set< Long > > es;

    public static int simulateGame(MPCantStopGame g, boolean verbose, MPPositionValueMap values)
    {
	// the state of the game at the start of the current turn
	MPCantStopGame.MPCantStopState start = g.makeAnchor(0);

	// find strategies for initial game state for each player

	if (ms == null)
	    {
		 ms = new ArrayList< Map< Pair< Long, DiceRoll >, Grouping > >();
		 es = new ArrayList< Set< Long > >();

		 for (int p = 0; p < g.countPlayers(); p++)
		     {
			 Map< Pair< Long, DiceRoll >, Grouping > moveStrat = new HashMap< Pair< Long, DiceRoll >, Grouping >();
			 Set< Long > endStrat = new HashSet< Long >();
			 
			 start.findStrategy(values, moveStrat, endStrat, p);
			 
			 ms.add(moveStrat);
			 es.add(endStrat);
		     }
	    }

	while (!start.isFinal())
	    {
		if (verbose)
		    {
			System.out.println("START OF TURN: " + start);
		    }

		MPCantStopGame.MPCantStopState state = start;
		int currentPlayer = state.getTurn();

		// figure out current player's strategy for this turn

		/*
		Map< Pair< Long, DiceRoll >, Grouping > moveStrat = new HashMap< Pair< Long, DiceRoll >, Grouping >();
		Set< Long > endStrat = new HashSet< Long >();
		*/

		// assume we never choose to stop, so start state is always
		// the initial game state

		Map< Pair< Long, DiceRoll >, Grouping > moveStrat = ms.get(currentPlayer);
		Set< Long > endStrat = es.get(currentPlayer);

		/*
		System.out.println(moveStrat);
		System.out.println(endStrat);
		*/

		do
		    {
			
			// roll the dice

			DiceRoll roll = new DiceRoll(4, g.countSides());
			roll.roll();

			if (verbose)
			    {
				System.out.println("rolled " + roll);
			    }

			// figrue out how to group dice

			Grouping move = moveStrat.get(new Pair< Long, DiceRoll >(state.getIndex(), roll));


			if (move != null)
			    {
				if (verbose)
				    {
					System.out.println("moving " + move.toString());
				    }

				state = state.getNextState(move);
				
				if (endStrat.contains(state.getIndex()))
				    {
					state = state.endTurn();

					if (verbose)
					    {
						System.out.println("ending turn");
					    }
				    }
			    }
			else
			    {
				// no possible moves

				if (verbose)
				    {
					System.out.println("BLEW IT!!!");
				    }

				state = start.endTurn();
			    }
		    }
		while (state.getTurn() == currentPlayer);

		start = state;
	    }

	return start.getWinner();
    }
}
