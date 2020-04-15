package com.bloxomo.gametheory.cantstop;

import com.bloxomo.gametheory.*;
import com.sirmapsalot.combinatorics.*;

/**
 * A state in a game of multi-player Can't Stop.
 * States implement the Can't Stop Can't Stop variant.
 * This is intended to be a more lightweight version of
 * MPCantStopGame.MPCantStopState intended for simulating games rather
 * than generating solutions.
 *
 * @author Jim Glenn
 * @version 0.1 2/22/2009
 */

public class SimpleMPCantStopState implements Cloneable
{
    /**
     * The game this state belongs to.
     */

    private MPCantStopGame game;

    /**
     * The positions of the markers in this state.
     * Each row is for one player; positions are numbered starting with
     * 0 for "off the board"; column indices start at 0, so in the standard
     * game the position of the markers in column 2 on the board will
     * be in column 0 in the array.
     */

    private int[][] markers;

    /**
     * The index of the player whose turn it is.
     */

    private int currentPlayer;

    /**
     * Creates the initial state of the given game.
     *
     * @param g a game
     */

    public SimpleMPCantStopState(MPCantStopGame g)
    {
	game = g;
       
	currentPlayer = 0;

	markers = new int[g.countPlayers()][g.getLastColumn() - g.getFirstColumn() + 1];
    }

    /**
     * Returns a deep copy of this state.
     *
     * @return a deep copy opf this state
     */

    public Object clone()
    {
	try
	    {
		SimpleMPCantStopState copy = (SimpleMPCantStopState)super.clone();
		
		// make a deep copy of the markers array
		
		copy.markers = new int[markers.length][markers[0].length];
		for (int p = 0; p < game.countPlayers(); p++)
		    {
			System.arraycopy(markers[p], 0, copy.markers[p], 0, markers[p].length);
		    }
		
		return copy;
	    }
	catch (CloneNotSupportedException cantHappen)
	    {
		return null;
	    }
    }

    /**
     * Returns the position of the given player's marker within the given column.
     *
     * @param p the index of a player
     * @param c the label of a column
     * @return the position of that player's marker in that column
     */

    public int getMarkerPosition(int p, int c)
    {
	return markers[p][c - game.getFirstColumn()];
    }

    /**
     * Returns the index of the current player.
     *
     * @return the current player
     */

    public int getTurn()
    {
	return currentPlayer;
    }

    /**
     * Determines if any player has won the given column.
     *
     * @param c the label of a column
     * @return true iff that column has been won
     */

    public boolean isWon(int c)
    {
	return (getColumnWinner(c) != -1);
    }

    /**
     * Determines which player has won the given column.  Returns -1 if the
     * column is still in play.
     */

    public int getColumnWinner(int c)
    {
	// compute the array index of the column

	int i = c - game.getFirstColumn();

	// search for the winning player

	int winner = 0;
	while (winner < game.countPlayers() && markers[winner][i] != game.getColumnLength(c))
	    {
		winner++;
	    }

	if (winner == game.countPlayers())
	    {
		return -1;
	    }
	else
	    {
		return winner;
	    }
    }

    /**
     * Returns the state that results from ending the current player's turn
     * from this state.
     *
     * @return the state after ending the turn of the current player
     * in this state
     */

    public SimpleMPCantStopState endTurn()
    {
	SimpleMPCantStopState next = (SimpleMPCantStopState)clone();

	next.currentPlayer = (currentPlayer + 1) % game.countPlayers();

	return next;
    }

    /**
     * Determines if the current player can stop in this state.
     * This implementation enforces the "Can't Stop Can't Stop" variation
     * in which a player can't stop with a marker in the same position
     * as another player.
     *
     * @return true iff the current player can stop
     */

    public boolean canStop(int p, SimpleMPCantStopState start)
    {
	// the original method in MPCantStopGame.MPCantStopState took
	// those two parameters; why?

	for (int i = 0; i < markers[currentPlayer].length; i++)
	    {
		for (int p2 = 0; p2 < game.countPlayers(); p2++)
		    {
			if (currentPlayer != p2
			    && markers[currentPlayer][i] > 0
			    && markers[currentPlayer][i] == markers[p2][i])
			    {
				return false;
			    }
		    }
	    }

	return true;
    }

    /**
     * Returns the result of moving according to the given move.
     *
     * @param move a legal move from this state
     * @return the resulting state
     */

    SimpleMPCantStopState getNextState(Grouping move)
    {
	SimpleMPCantStopState next = (SimpleMPCantStopState)clone();

	Multiset usedPairs = move.getUsed();

	for (int c = game.getFirstColumn(); c <= game.getLastColumn(); c++)
	    {
		next.markers[currentPlayer][c - game.getFirstColumn()] += usedPairs.countItem(c);
	    }

	return next;
    }

    private int[] countColumnsWon()
    {
	int[] columnsWon = new int[game.countPlayers()];
	for (int c = game.getFirstColumn(); c <= game.getLastColumn(); c++)
	    {
		int columnWinner = getColumnWinner(c);
		if (columnWinner != -1)
		    {
			columnsWon[columnWinner]++;
		    }
	    }

	return columnsWon;
    }

    /**
     * Determines if this state represents a state in which a player has
     * won the game.
     *
     * @return true iff this state if a terminal state
     */

    public boolean isFinal()
    {
	return (getWinner() != -1);
    }

    /**
     * Determines which player has won in this state.  Returns -1 if np
     * player has won.
     *
     * @return the index of the player who has won the game in this state
     */

    public int getWinner()
    {
	// compute the number of columns won by each player
	
	int[] columnsWon = countColumnsWon();

	// search for a winner

	int winner = 0;
	while (winner < game.countPlayers()
	       && columnsWon[winner] < game.getColumnsToWin())
	    {
		winner++;
	    }

	if (winner < game.countPlayers())
	    {
		return winner;
	    }
	else
	    {
		return -1;
	    }
    }

    /**
     * Returns a printable representation of this state.
     *
     * @return a printable representation of this state
     */

    public String toString()
    {
	StringBuffer buf = new StringBuffer();
	
	buf.append("(");
	for (int p = 0; p < game.countPlayers(); p++)
	    {
		if (currentPlayer == p)
		    buf.append("<");
		else
		    buf.append("[");

		for (int c = game.getFirstColumn(); c <= game.getLastColumn(); c++)
		    {
			buf.append("" + getMarkerPosition(p, c));
			if (c < game.getLastColumn())
			    buf.append(" ");
		    }

		if (currentPlayer == p)
		    buf.append(">");
		else
		    buf.append("]");
		
		if (p < game.countPlayers() - 1)
		    buf.append(" ");
	    }
	buf.append(")");

	return buf.toString();
    }
}

    
