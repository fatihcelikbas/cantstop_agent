package com.bloxomo.gametheory.cantstop;

import com.bloxomo.gametheory.*;
import java.io.*;
import java.util.*;
import com.sirmapsalot.combinatorics.*;

/**
 * Simulates two solitaire players playing against each other.
 */

public class PvPCantStopSimulator
{
    public static void main(String[] args)
    {
	int players = 2;
	int sides = MPCantStopGame.DEFAULT_SIDES;
	int len = MPCantStopGame.DEFAULT_SHORTEST_COLUMN;
	int delta = MPCantStopGame.DEFAULT_COLUMN_DIFFERENCE;
	int toWin = MPCantStopGame.DEFAULT_COLUMNS_TO_WIN;

	int numGames = 1;

	try
	    {
		sides = Integer.parseInt(args[0]);
		len = Integer.parseInt(args[1]);
		if (args.length > 2)
		    delta = Integer.parseInt(args[2]);
		if (args.length > 3)
		    toWin = Integer.parseInt(args[3]);
		if (args.length > 4)
		    numGames = Integer.parseInt(args[4]);
	    }
	catch (ArrayIndexOutOfBoundsException aioob)
	    {
		System.err.println("USAGE: java PvPCantStopGame sides shortest-column [column-diff [columns-to-win [num-games]]]");
		System.exit(1);
	    }

	MPCantStopGame g = new MPCantStopGame(players, sides, len, delta, toWin);
	CantStopState singlePlayerGame = new CantStopState(sides, len, toWin);

	int[] wins = new int[players];


	CantStopStrategy p1 = new RuleOfN(singlePlayerGame, 28);
	CantStopStrategy p2 = new RuleOfN(singlePlayerGame,
    new int[] {7, 7, 3, 2, 2, 1},
    new int[] {7, 0, 2, 0, 4, 3},
    7, 1, 6, 5, 6, 29);
	CantStopStrategy p3
	    = new RuleOfN(singlePlayerGame,
			  new int[] {7, 7, 3, 2, 2, 1},
			  new int[] {7, 0, 2, 0, 4, 3},
			  7, 1, 6, 5, 6, 29);

	// Column Weights won 6358 of 10000 as P2, 7358 as P1
	// Linear Weights won 6223 of 10000 as P2, 7100 as P1
	// against each other: Linear Weights won 5368 of 10000 as P1, 4386 as P2

	for (int i = 0; i < numGames; i++)
	    {
		int winner = simulateGame(g, p3, p2, (numGames == 1));
		wins[winner]++;
	    }

	for (int p = 0; p < players; p++)
	    {
		System.out.println("Player " + p + ": " + wins[p]);
	    }
    }

    public static int simulateGame(MPCantStopGame g,
				   CantStopStrategy p1,
				   CantStopStrategy p2,
				   boolean verbose)
    {
	CantStopStrategy[] players = new CantStopStrategy[] {p1, p2};

	// the state of the game at the start of the current turn

	SimpleMPCantStopState start = new SimpleMPCantStopState(g);

	while (!start.isFinal())
	    {
		if (verbose)
		    {
			System.out.println("START OF TURN: " + start);
		    }

		SimpleMPCantStopState state = start;
		int currentPlayer = start.getTurn();

		// convert start state to single-player version of that state

		CantStopState onePlayerStart = convertToSinglePlayer(g, start, currentPlayer);

		// run turn for this player

		do
		    {

			// roll the dice

			DiceRoll roll = new DiceRoll(4, g.countSides());
			roll.roll();

			if (verbose)
			    {
				System.out.println("rolled " + roll);
			    }

			// get current player's move

			Multiset move = players[currentPlayer].pickPairs(onePlayerStart, convertToSinglePlayer(g, state, currentPlayer), roll);

			if (move != null)
			    {
				if (verbose)
				    {
					System.out.println("moving " + move.toString());
				    }

				// convert move to grouping

				state = state.getNextState(convertToGrouping(move, roll));

				// convert state to CantStopState to check
				// for ending turn

				if (state.canStop(currentPlayer, start)
				    && !players[currentPlayer].rollAgain(onePlayerStart, convertToSinglePlayer(g, state, currentPlayer)))
				    {
					if (verbose)
					    {
						System.out.println("Ending turn.");
					    }

					state = state.endTurn();
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

    /**
     * Converts a multi-player Can't Stop state to a single-player version
     * of that state.  The conversion ignores opponents' pieces except
     * when they are at the top of a column, in which case they are replaced
     * with one of the current player's pieces.
     *
     * @param s the state to convert
     * @param p the index of the player to convert for
     * @return the resulting single-player state
     */

    private static CantStopState convertToSinglePlayer(MPCantStopGame g,
						       SimpleMPCantStopState s,
						       int p)
    {
	// determine position of markers in equivalent state

	int[] markers = new int[g.getLastColumn() + 1];
	int wonByOpponents = 0;

	for (int c = g.getFirstColumn(); c <= g.getLastColumn(); c++)
	    {
		// see if someone has won column c

		if (s.isWon(c))
		    {
			// put marker at top of column

			markers[c] = g.getColumnLength(c);

			if (s.getColumnWinner(c) != p)
			    {
				wonByOpponents++;
			    }
		    }
		else
		    {
			// use player's position

			markers[c] = s.getMarkerPosition(p, c);
		    }
	    }

	// create single-player state

	CantStopState equiv = new CantStopState(g.countSides(),
						g.getColumnLength(g.getFirstColumn()),
						g.getColumnsToWin() + wonByOpponents);

	for (int c = g.getFirstColumn(); c <= g.getLastColumn(); c++)
	    {
		equiv.placeMarker(c, markers[c]);
	    }

	return equiv;
    }

    /**
     * Converts a set of used pairs and the roll they came from to
     * the corresponding grouping.
     *
     * @param used the multiset of used pair totals
     * @param roll the roll they were chosen from
     * @return the corresponding grouping
     */

    private static Grouping convertToGrouping(Multiset used, DiceRoll roll)
    {
	if (roll.countDice() != 4)
	    {
		throw new IllegalArgumentException("Only works for 4 dice.");
	    }

	// figure out unused pair totals

	Multiset unused = new Multiset(used.range());

	if (used.size() == 1)
	    {
		unused.addItem(roll.total() - used.getMajority());
	    }

	return new Grouping(used, unused);
    }
}
