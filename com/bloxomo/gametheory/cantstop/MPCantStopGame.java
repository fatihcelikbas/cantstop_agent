package com.bloxomo.gametheory.cantstop;

import com.bloxomo.gametheory.*;
import com.sirmapsalot.combinatorics.*;
import java.util.*;
import Jama.*;
import java.io.*;

/**
 * A multiplayer game of Can't Stop.  The <CODE>MPCantStopGame</CODE>
 * class models variations of the game (different number of players,
 * sides on dice, etc.) and their rules.  The
 * <CODE>MPCantStopState</CODE> class models the game board.
 *
 * There is a distincition between column <I>label</I> and column
 * <I>index</I>.  A label is the number that appears on the game board
 * (that is, 2, 3, ..., 12).  A column index refers to an internal reordering
 * of the columns start starts at 0.  For example, in the canonical ordering,
 * the first column on the game board (the one labelled "2"), would have
 * index 0.  Note that all of the public methods use column <I>labels</I>.
 *
 * @author Jim Glenn
 * @version 0.1 10/9/2006
 */

public class MPCantStopGame
{
    /**
     * The number of players in this game.
     */

    protected int numPlayers;

    /**
     * The number of sides on the dice.
     */

    protected int numSides;

    /**
     * The number of dice in this game.
     */

    protected int numDice;

    /**
     * The number of dice to group together after a roll in this game.
     */

    protected int diceGroupSize;

    /**
     * The number of neutral markers used in this game.
     */

    protected int numMarkers;

    /**
     * The length of the shortest column in this game.
     */

    protected int shortestColumnLength;

    /**
     * The length of the longest column in this game.
     */

    protected int longestColumnLength;

    /**
     * The difference in the lengths of adjacent columns in this game.
     */

    protected int columnLengthDifference;

    /**
     * The number of columns needed to win the game.
     */

    protected int columnsToWin;

    /**
     * The indexing scheme used for states in this game.
     */

    protected CantStopIndexer indexer;

    /**
     * The number of groups of dice to make after each roll.
     */

    private int numDiceGroups;

    /**
     * The number of the first column in this game.
     */

    private int firstColumn;

    /**
     * The number of the last column in this game.
     */

    private int lastColumn;

    /**
     * The number of columns in this game.
     */

    private int numColumns;

    /**
     * The target of collusion for each player.  The current implementation
     * only allows a particular player to collude against another, so that
     * a player's goal may be to make another player lose.  -1 means
     * the corresponding player is not colluding.
     */

    private int[] collusionTarget;

    /**
     * The indices of each of the numbered columns in this game.
     * <CODE>columnIndex[c]</CODE> is the index of column number
     * <CODE>c</CODE>.
     */

    private int[] columnIndex;

    /**
     * The inverse of <CODE>columnIndex</CODE>.  That is, if
     * <CODE>columnLabel[i]</CODE> equals <CODE>x</CODE> then <CODE>x</CODE>
     * is the label of the column whose index is <CODE>i</CODE>.
     */

    private int[] columnLabel;

    /**
     * The lengths of the columns in this game.  <CODE>columnLength[i]</CODE>
     * is the length of the column whose index is <CODE>i</CODE>.
     */

    private int[] columnLength;

    /**
     * A list of the possible rolls in this game.
     */

    private RollList rolls;

    /**
     * Default parameters of a Can't Stop game.
     */

    public static final int DEFAULT_PLAYERS = 2;
    public static final int DEFAULT_SIDES = 6;
    public static final int DEFAULT_DICE = 4;
    public static final int DEFAULT_GROUP_SIZE = 2;
    public static final int DEFAULT_MARKERS = 3;
    public static final int DEFAULT_SHORTEST_COLUMN = 3;
    public static final int DEFAULT_COLUMN_DIFFERENCE = 2;
    public static final int DEFAULT_COLUMNS_TO_WIN = 3;

    /**
     * Creates a game with the default parameters.
     */

    public MPCantStopGame()
    {
	this(DEFAULT_PLAYERS,
	     DEFAULT_SIDES,
	     DEFAULT_DICE,
	     DEFAULT_GROUP_SIZE,
	     DEFAULT_MARKERS,
	     DEFAULT_SHORTEST_COLUMN,
	     DEFAULT_COLUMN_DIFFERENCE,
	     DEFAULT_COLUMNS_TO_WIN);
    }

    /**
     * Creates a game with the given kind of dice, number of players,
     * and shortest columns.  Other game parameters are set to their
     * default values.
     */

    public MPCantStopGame(int players, int sides, int shortest)
    {
	this(players,
	     sides,
	     DEFAULT_DICE,
	     DEFAULT_GROUP_SIZE,
	     DEFAULT_MARKERS,
	     shortest,
	     DEFAULT_COLUMN_DIFFERENCE,
	     DEFAULT_COLUMNS_TO_WIN);
    }

    /**
     * Creates a game with the given kind of dice, number of players,
     * length of shortest columns, and difference in length between
     * columns.  Other game parameters are set to their default
     * values.
     */

    public MPCantStopGame(int players, int sides, int shortest, int delta, int toWin)
    {
	this(players,
	     sides,
	     DEFAULT_DICE,
	     DEFAULT_GROUP_SIZE,
	     Math.min(DEFAULT_MARKERS, toWin),
	     shortest,
	     delta,
	     toWin);
    }

    /**
     * Creates a game with the given parameters.
     */

    public MPCantStopGame(int players, int sides, int dice, int group,
			  int markers, int len, int diff, int toWin)
    {
	numPlayers = players;
	numSides = sides;
	numDice = dice;
	diceGroupSize = group;
	numMarkers = markers;
	shortestColumnLength = len;
	columnLengthDifference = diff;
	columnsToWin = toWin;

	rolls = new RollList();

	collusionTarget = new int[players];
	for (int i = 0; i < players; i++)
	    {
		collusionTarget[i] = -1;
	    }

	init();
    }

    /**
     * Initializes the bookkeeping fields of this game.
     */

    private void init()
    {
	numDiceGroups = numDice / diceGroupSize;

	firstColumn = diceGroupSize;
	lastColumn = numSides * diceGroupSize;
	numColumns = lastColumn - firstColumn + 1;

	System.out.println(firstColumn + "-" + lastColumn);

	// compute column indices: first column is index 0, second is index 1,
	// and so on

	columnIndex = new int[lastColumn + 1];
	columnLabel = new int[numColumns];
	for (int col = 0; col < firstColumn; col++)
	    columnIndex[col] = -1;
	for (int col = firstColumn; col <= lastColumn; col++)
	    {
		columnIndex[col] = col - firstColumn;
		columnLabel[col - firstColumn] = col;
	    }

	// compute the column lengths

	columnLength = new int[numColumns];

	columnLength[getColumnIndex(firstColumn)] = shortestColumnLength;
	columnLength[getColumnIndex(lastColumn)] = shortestColumnLength;

	// middleColumn is the number of the largest column;
	// the /2 is at the end to avoid premature truncation;
	// it is OK if there are an even number of columns

	int middleColumn = (1 + numSides) * diceGroupSize / 2;

	for (int col = firstColumn + 1; col <= middleColumn; col++)
	    {
		// compute length of current column

		columnLength[getColumnIndex(col)] = (columnLength[getColumnIndex(col - 1)]
						     + columnLengthDifference);

		// compute length of mirror image of current column

		int mirrorColumn = lastColumn - (col - firstColumn);
		int mirrorIndex = getColumnIndex(mirrorColumn);

		columnLength[mirrorIndex] = (columnLength[getColumnIndex(mirrorColumn + 1)]
					     + columnLengthDifference);
	    }

	longestColumnLength = columnLength[columnIndex[middleColumn]];

	for (int col = firstColumn; col <= lastColumn; col++)
	    System.out.println(col + " " + columnLength[getColumnIndex(col)]);

	indexer = new SeparateDigitIndexer();
    }

    /**
     * Computes the index of the column with the given label.
     *
     * @param label a valid column label
     * @return the index of the column labelled <CODE>col</CODE>
     */

    public int getColumnIndex(int label)
    {
	return columnIndex[label];
    }

    /**
     * Returns the label of the column with the given index.
     *
     * @param index a value column index
     * @return that column's label
     */

    public int getColumnLabel(int index)
    {
	return columnLabel[index];
    }

    /**
     * Computes the length of the column with the given label.
     *
     * @param label a valid column label
     * @return the length of the column labelled <CODE>col</CODE>
     */

    public int getColumnLength(int label)
    {
	return columnLength[columnIndex[label]];
    }

    /**
     * Returns the number of columns needed to win this game.
     *
     * @return the number of columns needed to win this game
     */

    public int getColumnsToWin()
    {
	return columnsToWin;
    }

    /**
     * Returns the label of the first column in this game.
     *
     * @return the label of the first column in this game.
     */

    public int getFirstColumn()
    {
	return firstColumn;
    }

    /**
     * Returns the label of the last column in this game.
     *
     * @return the label of the last column in this game.
     */

    public int getLastColumn()
    {
	return lastColumn;
    }

    /**
     * Returns the column that is the same as the given on, reflected
     * around the center column.
     *
     * @param c a column label
     * @return the label of the column symmetric with c
     */

    public int getReflectedColumn(int c)
    {
	return lastColumn - (c - getFirstColumn());
    }

    /**
     * Returns the number of players in this game.
     *
     * @return the number of players in this game
     */

    public int countPlayers()
    {
	return numPlayers;
    }

    /**
     * Returns the number of sides on the dice in this game.
     *
     * @return the number of sides on the dice in this game
     */

    public int countSides()
    {
	return numSides;
    }

    /**
     * Returns the number of dice in this game.
     *
     * @return the number of dice in this game
     */

    public int countDice()
    {
	return numDice;
    }

    /**
     * Returns the number of groups of dice in this game.
     *
     * @return the number of groups of dice in this game
     */

    public int countGroups()
    {
	return numDiceGroups;
    }

    /**
     * Returns the number of dice in each group in this game.
     *
     * @return the number of dice in each group in this game
     */

    public int getGroupSize()
    {
	return diceGroupSize;
    }

    /**
     * Returns the number of neutral markers in this game.
     *
     * @return the number of neutral markers in this game
     */

    public int countMarkers()
    {
	return numMarkers;
    }

    /**
     * Determines if the given player is colluding against another.
     *
     * @param p a player index
     * @return true iff that player is colluding
     */

    public boolean isColluding(int p)
    {
	return (collusionTarget[p] != -1);
    }

    /**
     * Returns the index of the player the given player is colluding against.
     *
     * @param p a player index
     * @return the index of the player the given player is colluding against
     */

    public int getCollusionTarget(int p)
    {
	return collusionTarget[p];
    }

    /**
     * Sets the target of collusion for the given player.
     *
     * @param p a player index
     * @param t a player index
     */

    public void setCollusionTarget(int p, int t)
    {
	if (p < 0 || t < 0 || p >= numPlayers || t >= numPlayers)
	    throw new IllegalArgumentException("Invalid player index");

	collusionTarget[p] = t;
    }

    /**
     * Returns the player after the given player.
     *
     * @param p a player index
     * @return the index of the player whose turn it is after player p
     */

    public int nextPlayer(int p)
    {
	return (p + 1) % numPlayers;
    }

    /**
     * Returns the player whose turn it is the given number of turns after
     * the given player.
     *
     * @param p a player index
     * @param t a nonnegative integer
     * @return the index of the player whose turn it is t turns after p's turn
     */

    public int nextPlayer(int p, int t)
    {
	return (p + t) % numPlayers;
    }

    /**
     * Returns a map suitable for recording position values for this game.
     * The map will initially record 0 for all states.
     *
     * @return a map suitable for recording position values for this game.
     */

    public MPPositionValueMap getPositionValueMap()
    {
	return new MPArrayPositionValueMap(indexer.getHighestAnchor() + 1, numPlayers);
    }

    /**
     * Returns the number of states in this game.
     *
     * @return the number of states in this game
     */

    public long countStates()
    {
	return indexer.getHighestIndex() + 1;
    }

    public long getHighestAnchor()
    {
	return indexer.getHighestAnchor();
    }

    /**
     * Returns the number of layers the anchors in this game are
     * partitioned into.  The exact partition is dependent on the
     * indexing scheme used by this implementation.  However, any
     * partition will be such that if anchor u is reachable from anchor v
     * (and u and v differ in more than just whose turn it is)
     * then v is in a lower-numbered layer than u.
     *
     * @return the number of layers the states in this game are partitioned
     * into
     */

    public int countLayers()
    {
	return indexer.countLayers();
    }

    /**
     * Returns the anchor with the given index.
     *
     * @param index an index in the anchor indexing scheme
     * @return the corresponding anchor
     */

    public MPCantStopState makeAnchor(long index)
    {
	return new MPCantStopState(indexer.anchorToState(index));
    }

    /**
     * Returns an iterator over the indices of the anchors in the given
     * layer in this game.
     *
     * @param layer a valid layer index (0 <= layer < countLayers())
     * @return an iterator over the given layer
     */

    public Iterator< Long > layerIterator(int layer)
    {
	return indexer.layerIterator(layer);
    }
    
    /**
     * A map from game states and players to position values implemented
     * using an array.
     *
     * @author Jim Glenn
     * @version 0.1 3/9/2007
     */
    
    public class MPArrayPositionValueMap implements MPPositionValueMap
    {
	private double[][] values;
	
	/**
	 * Creates a position value map with a position value of zero
	 * recorded for all positions and all players.
	 *
	 * @param numAnchors the number of anchors the new map must
	 * record values for
	 * @param numPlayers the number of players the new map must
	 * record values for
	 */
	
	public MPArrayPositionValueMap(long numAnchors, int numPlayers)
	{
	    values = new double[numPlayers][(int)numAnchors];

	    for (int p = 0; p < numPlayers; p++)
		for (int a = 0; a < numAnchors; a++)
		values[p][a] = -1.0;
	}
	
	/**
	 * Returns the recorded position value for the state with the given
	 * index from the given player's point of view.
	 *
	 * @param index a nonnegative integer
	 * @param player a nonnegative integer
	 */
	
	public double getValue(long index, int player)
	{
	    /*
	    if (values[(int)indexer.stateToAnchor(index)][player] == -1.0)
		throw new IllegalStateException("" + index + " " + player);
	    */

	    return values[player][(int)(indexer.stateToAnchor(index))];
	}
	
	/**
	 * Sets the position value for the given state and player.
	 *
	 * @param index a nonnegative integer
	 * @param player a nonnegative integer
	 * @param value the position value of the state with the given index
	 * from the given player's point of view
	 */
	
	public void setValue(long index, int player, double value)
	{
	    values[player][(int)(indexer.stateToAnchor(index))] = value;
	}

	/**
	 * Determines if a value has been recorded for the given state
	 * and player.
	 *
	 * @param index a nonnegative integer
	 * @param player a nonnegative integer
	 * @return true if and only if a position value has been recorded for
	 * the state with the given index and the given player
	 */

	public boolean hasValue(long index, int player)
	{
	    return (values[player][(int)indexer.stateToAnchor(index)] != -1.0);
	}

	public void write(DataOutputStream out) throws IOException
	{
	    for (int p = 0; p < values.length; p++)
		for (int a = 0; a < values[p].length; a++)
		    out.writeDouble(values[p][a]);
	}

	public void read(DataInputStream in) throws IOException
	{
	    for (int p = 0; p < values.length; p++)
		for (int a = 0; a < values[p].length; a++)
		    values[p][a] = in.readDouble();
	}
    }

    private class Component
    {
	private long[][] component;
	private MPCantStopState[][] states;
	private double[][][] endTurnValues;

	/**
	 * Initial estimate of position values of anchors.
	 * <CODE>anchorEstimates[p][a]</CODE> is the probability
	 * that p wins from a's anchor in this component.
	 */

	private double[][] anchorEstimates;

	/**
	 * Maps states to indicies in the <CODE>states</CODE> array.
	 * Only the second index is recorded; presumably the first index
	 * can be obtained from the turn information.
	 */

	private Map< MPCantStopState, Integer > stateIndex;
	
	/**
	 * Parallel array to <CODE>component</CODE> such that
	 * estimates[p][i][t] gives the estimate of the position value
	 * of state component[p][i] for player (p+t)[?] given that it is
	 * p's turn.  That probability is given as a linear function of
	 * anchorEstimates[p][p + 1].
	 */
	
	private Estimate[][][] estimates;
	
	private MPPositionValueMap computedValues;

	/**
	 * Creates a subgraph of this game consisting of those in the
	 * same strongly connected
	 * component as those with some common configuration of
	 * pieces (but different players' turns).
	 * <CODE>comp</CODE> must be arranged so the states
	 * reachable by player <CODE>p</CODE> are in order of
	 * topological sort in <CODE>component[p]</CODE> (and so the
	 * anchors will be in <CODE>component[p][0]</CODE>).
	 * <CODE>est[p][q]</CODE> should be the initial estimate of the
	 * probability player <CODE>p</CODE> will win given that the
	 * game has reached the anchor in the given component where it
	 * is player <CODE>q</CODE>'s turn (that is,
	 * <CODE>component[q][0]</CODE>).
	 *
	 * @param component the SCC induced by a set of anchors
	 * @param est the initial estimates of the position values of the anchors
	 * in component
	 */
	
	private Component(long[][] comp, double[][] est, MPPositionValueMap map)
	{
	    component = comp;
	    setAnchorEstimates(est);
	    computedValues = map;

	    stateIndex = new HashMap< MPCantStopState, Integer >();
	    
	    estimates = new Estimate[component.length][][];
	    for (int p = 0; p < estimates.length; p++)
		{
		    estimates[p] = new Estimate[comp[p].length][];
		    for (int i = 0; i < estimates[p].length; i++)
			{
			    estimates[p][i] = new Estimate[numPlayers - 1];
			    for (int t = 0; t < numPlayers - 1; t++)
				estimates[p][i][t] = new Estimate(1.0, 0.0);
			}
		}
	    
	    // I give up -- I don't think I can do this effectively
	    // without eating up memory with the MPCantStopStateObjects
	    
	    states = new MPCantStopState[component.length][];
	    for (int p = 0; p < states.length; p++)
		{
		    states[p] = new MPCantStopState[component[p].length];
		    for (int i = 0; i < states[p].length; i++)
			{
			    states[p][i] = new MPCantStopState(component[p][i]);
			    stateIndex.put(states[p][i], i);
			}
		}

	    // look up and save the turn-ended position values for future
	    // reference

	    endTurnValues = new double[component.length][][];
	    for (int p = 0; p < states.length; p++)
		{
		    endTurnValues[p] = new double[component[p].length][];

		    // copy from anchor estimates to endTurnValues

		    endTurnValues[p][0] = new double[numPlayers];
		    for (int player = 0; player < numPlayers; player++)
			{
			    endTurnValues[p][0][player] = anchorEstimates[player][nextPlayer(p)];
			}

		    // look up value of previously computed states

		    for (int i = 1; i < endTurnValues[p].length; i++)
			{
			    endTurnValues[p][i] = new double[numPlayers];
			    for (int player = 0; player < numPlayers; player++)
				{
				    if (states[p][i].canStop(states[p][0]))
					{
					    MPCantStopState turnEnded = states[p][i].endTurn();
					    //2 System.out.print(turnEnded);
					    double valueOfEndingTurn = computedValues.getValue(turnEnded.getIndex(), player);
					    //2 System.out.println(" = " + valueOfEndingTurn);

					    endTurnValues[p][i][player] = valueOfEndingTurn;
					}
				}
			}
		}
	}

	public void setAnchorEstimates(double[][] est)
	{
	    anchorEstimates = est;
	}

	/**
	 * Returns a (presumably better) estimate of the position
	 * values of the anchors in this component.
	 *
	 * @param computedValues a map giving position values of
	 * components reachable from this one
	 * @return a new estimate of the position values of the anchors in
	 * component
	 */
	
	private double[][] estimatePositionValues()
	{
	    return estimatePositionValues(null, null, -1);
	}

	private double[][] estimatePositionValues(Map< Pair< Long, DiceRoll >, Grouping > moveStrat, Set< Long > endStrat, int stratPlayer)
	{
	  // go through the subcomponent for each player
	    
	    for (int p = 0; p < numPlayers; p++)
		{
		    //1 System.out.println("=== PLAYER " + p + " ===");

		    // create array of what player p thinks of state
		    // states[p][q] -- playerValue[q] is player p's
		    // valuation of state states[p][q], which may or
		    // may not be the probability that p wins from
		    // states[p][q] depending on p's goal

		    double[] playersValue = new double[states[p].length];

		    // go through the subcomponent in order of
		    // reverse topological sort
		    
		    for (int i = states[p].length - 1; i >= 0; i--)
			{
			    playersValue[i] = 0.0;

			    //1 System.out.println("Working on " + states[p][i]);

			    // go through all possible rolls
			    
			    double pBlowingIt = 0.0;
			    Estimate value[] = new Estimate[numPlayers - 1];
			    for (int t = 0; t < numPlayers - 1; t++)
				value[t] = new Estimate(0.0, 0.0);
			    
			    RollList.RollIterator ri = rolls.new RollIterator();

			    while (ri.hasNext())
				{
				    DiceRoll roll = ri.next();

				    //1 System.out.print("On roll " + roll + " ");
				    
				    // go through all legal moves for this
				    // roll, finding the one that maximizes
				    // the position value
				    
				    Estimate bestEstimate = null;
				    Grouping bestMove = null;
				    double bestValue = Double.NEGATIVE_INFINITY;
				    MPCantStopState bestDestination = null;

				    Set< Grouping > possibleMoves = ri.getGroupings();
				    
				    for (Grouping move : possibleMoves)
					{
					    if (states[p][i].isLegalMove(states[p][0], move))
						{
						    MPCantStopState possibleDest = states[p][i].getNextState(move);
						    // double possibleValue = getPositionValue(p, indexOf(possibleDest));
						    double possibleValue = playersValue[indexOf(possibleDest)];

						    if (bestEstimate == null
							|| possibleValue > bestValue)
							{
							    bestEstimate = estimates[p][indexOf(possibleDest)][0];
							    bestDestination = possibleDest;
							    bestValue = possibleValue;
							    bestMove = move;
							}
						}
					}
				    
				    if (bestEstimate != null)
					{
					    // this roll has a best move, so
					    // update values according to the
					    // value of that best move

					    playersValue[i] += bestValue * ri.getProbability();

					    value[0] = value[0].plus(bestEstimate.multiply(ri.getProbability()));
					    for (int t = 1; t < numPlayers - 1; t++)
						value[t] = value[t].plus(estimates[p][indexOf(bestDestination)][t].multiply(ri.getProbability()));

					    // System.out.println("use " + bestMove + " (value[0] " + bestValue + ")");

					    // record choice

					    if (moveStrat != null && p == stratPlayer)
						{
						    Pair< Long, DiceRoll > key = new Pair< Long, DiceRoll >(states[p][i].getIndex(), roll);
						    moveStrat.put(new Pair< Long, DiceRoll >(states[p][i].getIndex(), roll), bestMove);
						}
					}
				    else
					{
					    // this roll has no best move,
					    // so therefore _no_ move;
					    // accumulate the probability of
					    // blowing it

					    pBlowingIt += ri.getProbability();

					    //1 System.out.println("blowing it");
					}
				    
				}
			    
			    //1 System.out.println("P(blowing it) = " + pBlowingIt);
			    //1 System.out.println("value of blowing it = " + playersValuationOfLosingTurn(p));

			    // should roll this into the loop

			    playersValue[i] += pBlowingIt * playersValuationOfLosingTurn(p);

			    estimates[p][i][0] = value[0].plus(new Estimate(1.0, 0.0).multiply(pBlowingIt));
			    for (int t = 1; t < numPlayers - 1; t++)
				estimates[p][i][t] = value[t].plus(new Estimate(1.0, 0.0).multiply(pBlowingIt));

			    // System.out.println("Value of rolling = " + getPositionValue(p, i) + " = " + estimates[p][i][0]);
			    
			    // check against ending turn
			    
			    if (i > 0 && states[p][i].canStop(states[p][0]))
				{
				    // double valueOfEndingTurn = endTurnValues[p][i][p];
				    double valueOfEndingTurn = playersValuationOfEndingTurn(p, i);

				    //1 System.out.println("Value of ending = " + valueOfEndingTurn);
				    
				    if (playersValue[i] < valueOfEndingTurn)
					{
					    //1 System.out.println("Should end turn");

					    playersValue[i] = valueOfEndingTurn;
					    estimates[p][i][0] = new Estimate(0.0, endTurnValues[p][i][p]);
					    for (int t = 1; t < numPlayers - 1; t++)
						estimates[p][i][t] = new Estimate(0.0, endTurnValues[p][i][nextPlayer(p, t)]);

					    // record choice to end turn

					    if (endStrat != null && p == stratPlayer)
						{
						    endStrat.add(states[p][i].getIndex());
						}
					}
				    else
					{
					    //1 System.out.println("Should roll again");
					}

				}
			}
		}

	    // we've got to convert from our double subscript scheme
	    // to a single subscript scheme -- we're thinking of
	    // anchorEstimates[p][q] as P(player p wins from player q's anchor)
	    // and we'll let that be variable p * numPlayers + q
	    // in the system below

	    Matrix coeff = new Matrix(numPlayers * numPlayers,
				      numPlayers * numPlayers);
	    Matrix constants = new Matrix(numPlayers * numPlayers, 1);

	    // set up a mapping from (anchor, player) pairs to
	    // variable indices

	    int[][] varIndex = new int[numPlayers][numPlayers];

	    for (int p = 0; p < numPlayers; p++)
		for (int a = 0; a < numPlayers; a++)
		    {
			varIndex[p][a] = p * numPlayers + a;
		    }

	    // for two players (where a = anchorEstimates, e = estimates)
	    // a[0][0] = e[0][0][0].slope * a[0][1] + e[0][0][0].intercept
	    // a[1][1] = e[1][0][0].slope * a[1][0] + e[1][0][0].intercept
	    // a[anchor+turn][anchor] = e[anchor][0][turn].slope * a[player][anchor + 1]
	    //                     + e[anchor][0][turn].intercept
	    
	    for (int anchor = 0; anchor < numPlayers; anchor++)
		{
		    for (int turn = 0; turn < numPlayers - 1; turn++)
			{
			    int player = nextPlayer(anchor, turn);
			    int row = anchor * (numPlayers - 1) + turn;

			    coeff.set(row,
				      varIndex[player][anchor],
				      1.0);
			    coeff.set(row,
				      varIndex[player][nextPlayer(anchor)],
				      -estimates[anchor][0][turn].getSlope());
			    constants.set(row,
					  0,
					  estimates[anchor][0][turn].getIntercept());
			}
		}
			    
	    // set equations of the form a[0][0] + a[1][0] + a[2][0] = 1

	    for (int anchor = 0; anchor < numPlayers; anchor++)
		{
		    int row = numPlayers * (numPlayers - 1) + anchor;
		    
		    for (int p = 0; p < numPlayers; p++)
			coeff.set(row,
				  varIndex[p][anchor],
				  1.0);

		    constants.set(row, 0, 1.0);
		}

	    Matrix soln = coeff.solve(constants);

	    double[][] result = new double[numPlayers][numPlayers];

	    for (int player = 0; player < numPlayers; player++)
		for (int anchor = 0; anchor < numPlayers; anchor++)
		    result[player][anchor] = soln.get(varIndex[player][anchor], 0);

	    System.out.print("New estimates:");
	    for (int player = 0; player < numPlayers; player++)
		System.out.print(" " +  result[player][0]);
	    System.out.println();


	    return result;
	}

	/**
	 * Returns player p's valuation of ending his turn at position i.
	 *
	 * @param p a player index
	 * @param i the index of a state in player p's subcomponent
	 * @return player p's valuation of stopping at that state
	 */

	private double playersValuationOfEndingTurn(int p, int i)
	{
	    if (isColluding(p))
		{
		    return -endTurnValues[p][i][getCollusionTarget(p)];
		}
	    else
		{
		    return endTurnValues[p][i][p];
		}
	}

	/**
	 * Returns player p's valuation of losing his turn in this component.
	 *
	 * @param p a player index
	 * @return player p's valuation of losing a turn
	 */

	private double playersValuationOfLosingTurn(int p)
	{
	    if (isColluding(p))
		{
		    return -anchorEstimates[getCollusionTarget(p)][nextPlayer(p)];
		}
	    else
		{
		    return endTurnValues[p][0][p];
		}
	}

	/**
	 * Returns the position value of the given state in the
	 * given player's subcomponent.  The value is returned
	 * as the <CODE>double</CODE> obtained by plugging in the
	 * appropriate anchor position value into the current
	 * <CODE>Estimate</CODE> for the state.
	 *
	 * @param p a player index
	 * @param i the index of a state in player p's subcomponent
	 * @return the current estimate of that state's position value
	 * for player p
	 */
	
	private double getPositionValue(int p, int i)
	{
	    return estimates[p][i][0].evaluate(anchorEstimates[p][nextPlayer(p)]);
	}

	private int indexOf(MPCantStopState s)
	{
	    return stateIndex.get(s);
	}
    }
    
    private static class Estimate
    {
	private double slope;
	private double intercept;
	
	public Estimate(double m, double b)
	{
	    slope = m;
	    intercept = b;
	}
	
	public Estimate multiply(double c)
	{
	    return new Estimate(slope * c, intercept * c);
	}
	
	public Estimate plus(Estimate other)
	{
	    return new Estimate(slope + other.slope, intercept + other.intercept);
	}
	
	public double evaluate(double x)
	{
	    return slope * x + intercept;
	}

	public double getSlope()
	{
	    return slope;
	}

	public double getIntercept()
	{
	    return intercept;
	}

	public String toString()
	{
	    return slope + "x + " + intercept;
	}
    }
    
    /**
     * A state indexing scheme that uses a non-homogeneous place value
     * system in which a digit is used for each (player, column) pair;
     * the digit represents the player's position in the column.  There
     * is also a digit used for the current turn.  This has the advantage
     * of being straightforward but the disadvantage of, in the Can't
     * Stop Can't Stop variation, of encoding many illegal states.
     */
    
    private class SeparateDigitIndexer implements CantStopIndexer
    {
	/**
	 * The place values of the columns for the purposes of
	 * computing the indices of states.
	 * <CODE>columnValue[i]</CODE> is the place value of
	 * column index <CODE>i</CODE>.
	 */
	
	private long[] columnValue;
	
	/**
	 * The place values of the players for the purposes of computing
	 * the state indices.
	 */
	
	private long[] playerValue;
	
	/**
	 * The place value of each digit in this scheme.
	 */
	
	private long[] placeValue;
	
	/**
	 * The number of ways to arrange all the colored markers
	 * in this game.
	 */
	
	private long markerArrangements;

	/**
	 * The number of legal arrangements for all the players' markers
	 * within each column for an anchor.
	 */

	private int[] columnArrangements;
	
	/**
	 * The number of legal arrangements for all the player's markers
	 * for anchors.
	 */

	private long anchorArrangements;

	/**
	 * The place value of a column for computing anchor indices.
	 */

	private long[] anchorColumnValue;

	/**
	 * A list of all the legal positions in a single column for
	 * anchor positions.
	 */

	private List< List< Integer > > positions;

	/**
	 * A map from positions to indicies in the <CODE>positions</CODE> list.
	 */

	private Map< List< Integer >, Integer > columnArrangementToIndex;

	/**
	 * Creates an indexer for this game.
	 */
	
	public SeparateDigitIndexer()
	{
	    // compute the place values of the columns
	    
	    columnValue = new long[numColumns];
	    columnValue[0] = 1;
	    for (int i = 1; i < columnValue.length; i++)
		columnValue[i] = columnValue[i - 1] * (columnLength[i - 1] + 1);
	    
	    long singleColorArrangements = (columnValue[columnValue.length - 1]
					    * (columnLength[columnValue.length - 1] + 1));
	    
	    // compute the place values of the players
	    
	    playerValue = new long[numPlayers];
	    playerValue[0] = 1;
	    for (int p = 1; p < numPlayers; p++)
		playerValue[p] = playerValue[p - 1] * singleColorArrangements;
	    
	    markerArrangements = (singleColorArrangements
				  * playerValue[numPlayers - 1]);
	    
	    // compute the place value of each digit (including
	    // turn and dummy value for dummy most significant digit
	    // [non-existent digit after turn])
	    
	    placeValue = new long[numPlayers * numColumns + 2];
	    for (int p = 0; p < numPlayers; p++)
		for (int i = 0; i < numColumns; i++)
		    placeValue[p * numColumns + i] = columnValue[i] * playerValue[p];
	    placeValue[numPlayers * numColumns] = markerArrangements;
	    placeValue[numPlayers * numColumns + 1] = markerArrangements * numPlayers;

	    // compute number of marker arrangements within each column

	    columnArrangements = new int[numColumns];
	    for (int i = 0; i < numColumns; i++)
		{
		    int columnTotal = 0;

		    // CURRENT: changed to columnLength[i] - 1 and
		    // columnTotal + numPlayers below so as to not count
		    // configurations where one player has won a column
		    // and the others are still in that column

		    for (int onBoard = 0; onBoard <= numPlayers; onBoard++)
			columnTotal += Combinatorics.choose(columnLength[i] - 1, onBoard).intValue() * (Combinatorics.choose(numPlayers, onBoard).intValue() * Combinatorics.factorial(onBoard).intValue());

		    columnArrangements[i] = columnTotal + numPlayers;
		    System.out.println("column " + columnLabel[i] + " length " + columnLength[i] + " arrangements " + columnArrangements[i]);
		}

	    // compute the number of legal marker arrangements for anchors

	    anchorArrangements = 1;
	    for (int i = 0; i < numColumns; i++)
		anchorArrangements *= columnArrangements[i];

	    // compute the place value of each column for anchor indices

	    anchorColumnValue = new long[numColumns];
	    anchorColumnValue[0] = 1;
	    for (int i = 1; i < numColumns; i++)
		anchorColumnValue[i] = anchorColumnValue[i - 1] * columnArrangements[i - 1];

	    // Enumerate all of the legal positions within a single column.
	    // These are sorted by maximum advance so the same enumeration
	    // can be used for all columns.  The enumeration works
	    // by changing 0's in previous positions to the current maximum
	    // position.

	    positions = new ArrayList< List< Integer > >();
	    List< Integer > zero = new ArrayList< Integer >();
	    for (int i = 0; i < numPlayers; i++)
		zero.add(0);

	    positions.add(zero);

	    for (int max = 1; max <= longestColumnLength; max++)
		{
		    List< List< Integer > > newPositions = new ArrayList< List< Integer > >();
		    for (List< Integer > oldPosition : positions)
			{
			    for (int p = 0; p < numPlayers; p++)
				if (oldPosition.get(p) == 0)
				    {
					List< Integer > newPosition = new ArrayList< Integer >(oldPosition);
					newPosition.set(p, max);
					newPositions.add(newPosition);
				    }
			}
		    positions.addAll(newPositions);
		}
	    System.out.println(positions);

	    // create map from positions to indices in positions list

	    columnArrangementToIndex = new HashMap< List< Integer >, Integer >();
	    for (int i = 0; i < positions.size(); i++)
		columnArrangementToIndex.put(positions.get(i), i);
		    
	}
	
	/**
	 * Returns the highest possible index using this indexing scheme.
	 *
	 * @return the highest possible index
	 */
	
	public long getHighestIndex()
	{
	    return placeValue[numPlayers * numColumns + 1] - 1;
	}
	
	/**
	 * Returns the value of the given digit in the given index.
	 *
	 * @param index an index in this scheme
	 * @param d the index of the digit to extract
	 */
	
	private int getDigit(long index, int d)
	{
	    return (int)(index % placeValue[d + 1] / placeValue[d]);
	}

	/**
	 * Returns the index of the digit corresponding to the given
	 * player and column.  The least significant digit is digit 0.
	 *
	 * @param p a player index
	 * @param i a column index
	 * @return the index of the corresponding digit
	 */

	private int getDigitIndex(int p, int i)
	{
	    return p * numColumns + i;
	}
	
	/**
	 * Returns the player whose turn it is in the state with the
	 * given index using this indexing scheme.
	 *
	 * @param i a state index in this scheme
	 * @return the player whose turn it is
	 */
	
	public int getTurn(long state)
	{
	    return (int)(state / markerArrangements);
	}
	
	/**
	 * Returns the position of the given player's marker in the
	 * given column in the state with the given index in this
	 * indexing scheme.
	 *
	 * @param state a state index in this scheme
	 * @param p a player index
	 * @param c a column label
	 */
	
	public int getMarkerPosition(long state, int p, int c)
	{
	    return getDigit(state, getDigitIndex(p, columnIndex[c]));
	}

	/**
	 * Returns the index of the state with the markers in the
	 * given positions and the given player's turn.
	 *
	 * @param markers the position of the player's markers,
	 * with one row per player
	 * @param turn the players whose turn it is
	 * @return the index of the corresponding state
	 */

	public long getIndex(int[][] markers, int turn)
	{
	    long index = 0;

	    for (int p = 0; p < numPlayers; p++)
		for (int i = 0; i < numColumns; i++)
		    index += markers[p][i] * placeValue[getDigitIndex(p, i)];

	    index += turn * placeValue[numPlayers * numColumns];

	    return index;
	}
	
	/**
	 * Returns the index of the state with the same marker
	 * positions as the state with the given index in this
	 * indexing scheme but with the turn belonging to the
	 * given player.
	 *
	 * @param state a state index in this scheme
	 * @param p a player index
	 * @return the index of the resulting state
	 */
	
	public long changeTurn(long state, int p)
	{
	    return state + (p - getTurn(state)) * markerArrangements;
	}
	
	/**
	 * Returns the index of the state with the markers moved from their
	 * positions in this state by the amounts given.
	 *
	 * @param p a player index
	 * @param advanceColumns an array containing the labels of the
	 * columns to advance in
	 * @param advanceDistance an array containing the distance to move
	 * in those columns
	 * @return the index of the resulting state
	 */
	
	public long indexAfterMoving(long state, int p, int[] advanceColumns, Multiset advanceDistance)
	{
	    state = changeTurn(state, p);
	    
	    for (int i = 0; i < advanceColumns.length; i++)
		{
		    int col = advanceColumns[i];
		    int colIndex = getColumnIndex(col);
		    int spacesInCol = advanceDistance.countItem(i) + 1;
		    int position = getMarkerPosition(state, p, col) + spacesInCol;
		    state += (placeValue[p * numColumns + colIndex]
			      * spacesInCol);
		    if (position >= getColumnLength(col))
			{
			    // zero out other player's positions

			    for (int p2 = 0; p2 < numPlayers; p2++)
				if (p2 != p)
				    state -= (placeValue[p2 * numColumns + colIndex] * getMarkerPosition(state, p2, col));
			}
		}
	    
	    return state;
	}
	
	/**
	 * Returns the index of the state reached from the state
	 * with the given index in this indexing scheme and moving
	 * according to the given grouping of the dice.
	 *
	 * @param startState the index of the start state in this scheme
	 * @param move the grouping of the dice to move according to
	 * @return the index of the resulting state
	 */
	
	public long getNextState(long startState, Grouping move)
	{
	    long nextState = startState;
	    
	    Multiset columnsToMove = move.getUsed();
	    int turn = getTurn(startState);
	    
	    for (int c = getFirstColumn(); c <= getLastColumn(); c++)
		{
		    if (columnsToMove.countItem(c) > 0)
			{
			    int colIndex = getColumnIndex(c);
			    int spacesInCol = columnsToMove.countItem(c);
			    
			    int position = spacesInCol + getMarkerPosition(startState, turn, c);
			    nextState += placeValue[turn * numColumns + colIndex] * spacesInCol;
			    if (position == getColumnLength(c))
				{
				    for (int p = 0; p < numPlayers; p++)
					if (p != turn)
					    nextState -= placeValue[p * numColumns + colIndex] * getMarkerPosition(startState, p, c);
				}
			}
		}
	    
	    return nextState;
	}

	/**
	 * Returns the highest possible index of an anchor position using
	 * this indexing scheme.
	 *
	 * @return the highest possible anchor index
	 */
	
	public long getHighestAnchor()
	{
	    return anchorArrangements * numPlayers - 1;
	}

	/**
	 * Converts an anchor index to a state index.
	 *
	 * @param anchor the index of an anchor
	 * @return the index of the corresponding state
	 */
	
	public long anchorToState(long anchor)
	{
	    long state = 0;

	    for (int i = 0; i < numColumns; i++)
		{
		    int arrangementIndex = (int)(anchor % columnArrangements[i]);
		    anchor = anchor / columnArrangements[i];
		    List< Integer > position = positions.get(arrangementIndex);

		    for (int p = 0; p < numPlayers; p++)
			state += position.get(p) * placeValue[p * numColumns + i];
		}

	    // anchor has been reduced to turn by now

	    return state + anchor * placeValue[numPlayers * numColumns];
	}

	/**
	 * Converts a state index to an anchor index.
	 *
	 * @param state the index of a state that is legal at the end of a turn
	 * @return the index of the corresponding anchor
	 */
	
	public long stateToAnchor(long state)
	{
	    long anchorIndex = 0;
	    for (int i = 0; i < numColumns; i++)
		{
		    List< Integer > columnPosition = new ArrayList< Integer >();
		    for (int p = 0; p < numPlayers; p++)
			columnPosition.add(getMarkerPosition(state, p, columnLabel[i]));

		    /*
		    System.out.println(columnPosition);
		    System.out.println(columnArrangementToIndex.get(columnPosition));
		    System.out.println(anchorColumnValue[i]);
		    System.out.println(i);
		    */

		    try
			{
			    anchorIndex += columnArrangementToIndex.get(columnPosition) * anchorColumnValue[i];
			}
		    catch (NullPointerException e)
			{
			    System.out.println(columnPosition);
			    System.out.println(columnArrangementToIndex.get(columnPosition));
			    System.out.println(anchorColumnValue[i]);
			    throw e;
			}
		}

	    return anchorIndex + anchorArrangements * getTurn(state);
	}

	public int countLayers()
	{
	    // compute the highest possible sum of digits (exluding turn digit)

	    int total = 0;
	    for (int i = 0; i < numColumns; i++)
		total += columnArrangements[i] - 1;

	    return total + 1;
	}

	public Iterator< Long > layerIterator(int layer)
	{
	    return new LayerIterator(layer);
	}

	/**
	 * An iterator through a layer of states in this game.
	 */
	
	private class LayerIterator implements Iterator< Long >
	{
	    /**
	     * An iterator through the multisets that correspond to the states.
	     * The multiset [c1 c2 ... ck] represents the state whose index
	     * is c1 c2 ... ck.
	     */
	    
	    private Iterator multisetIterator;

	    /**
	     * Creates an iterator over the given layer of anchors.
	     *
	     * @param layer a valid layer index
	     */
	    
	    public LayerIterator(int layer)
	    {
		// build a multiset m where m.count(i) is the maximum
		// value of a digit in position i

		Multiset m = new Multiset(numColumns - 1);

		for (int i = 0; i < numColumns; i++)
		    m.addItems(i, columnArrangements[i] - 1);

		multisetIterator = m.subsetIterator(layer);
	    }

	    /**
	     * Determines if this iterator has a next index.
	     *
	     * @return true iff this iterator has a next index
	     */

	    public boolean hasNext()
	    {
		return multisetIterator.hasNext();
	    }

	    /**
	     * Returns the next index reached by this iterator
	     *
	     * @return the next index
	     */

	    public Long next()
	    {
		Multiset nextMultiset = (Multiset)(multisetIterator.next());

		// convert multiset to index

		long index = 0;
		for (int i = 0; i < numColumns; i++)
		    index += nextMultiset.countItem(i) * anchorColumnValue[i];

		return index;
	    }

	    /**
	     * Unsupported.
	     *
	     * @throws UnsupportedOperationException
	     */

	    public void remove()
	    {
		throw new UnsupportedOperationException();
	    }
	}
    }

    /**
     * A state at the beginning of a turn in this game.  Note that states
     * between turns can be modelled by a pair (anchor, intermediate)
     * where anchor  the state at the beginning of a turn and intermediate
     * is the state that would begin the next turn if the current player
     * chose to end the turn.
     */

    public class MPCantStopState
    {
	/**
	 * The positions of each player's markers in each column.
	 * The first dimension is the player, so <CODE>markers[p][i]</CODE>
	 * is the position of player <CODE>p</CODE>'s marker in column
	 * index <CODE>i</CODE>.
	 */

	private int[][] markers;

	/**
	 * The index of the player whose turn it is in this state.
	 */

	private int turn;

	/**
	 * An array that keeps track of which columns have been won and by
	 * whom.  -1 means the column has not been won.
	 */

	private int[] columnWinner;

	/**
	 * An array that keeps track of how many columns have been won by
	 * each player.
	 */

	private int[] columnsWon;

	/**
	 * The total number of columns won in this state.
	 */

	private int totalColumnsWon;

	/**
	 * The number of players who have won columns in this state.
	 */

	private int playersWinningColumns;

	/**
	 * The unique index of this state.  Note that a 64-bit
	 * <CODE>long</CODE> will not be big enough for the official
	 * game (2 players, 4 six-sided dice, and column lengths 3, 5,
	 * 7... needs 67 bits using the straigtforward 1st attempt at an
	 * encoding scheme).
	 */

	private long index;

	/**
	 * Creates the start state of this game.
	 */

	private MPCantStopState()
	{
	    this(0);
	}

	private MPCantStopState(int[][] markers, int turn)
	{
	    this(indexer.getIndex(markers, turn));
	}

	/**
	 * Creates the state in this game with the given index.
	 *
	 * @param index a valid state index
	 */

	private MPCantStopState(long i)
	{
	    // save index

	    index = i;

	    // treat the index as a number in a mixed radix system with
	    // place values as in columnValue[] and playerValue[]

	    turn = indexer.getTurn(index);

	    // mark columns not won (will be reset by initMarkers if
	    // needed)

	    columnWinner = new int[numColumns];
	    for (int j = 0; j < numColumns; j++)
		columnWinner[j] = -1;

	    // initialize counts of columns won by each player;
	    // this will be set in initMarkers

	    columnsWon = new int[numPlayers];

	    for (int p = 0; p < numPlayers; p++)
		for (int c = firstColumn; c <= lastColumn; c++)
		    if (getMarkerPosition(p, c) == columnLength[columnIndex[c]])
			{
			    totalColumnsWon++;
			    columnsWon[p]++;
			    if (columnsWon[p] == 1)
				playersWinningColumns++;
			    columnWinner[columnIndex[c]] = p;
			}
	}

	/**
	 * Returns the index of this state.
	 *
	 * @return the index of this state
	 */

	public long getIndex()
	{
	    return index;
	}

	/**
	 * Returns the state that results when the current player in this
	 * state ends the turn.
	 *
	 * @return the state after ending the turn
	 */

	public MPCantStopState endTurn()
	{
	    return new MPCantStopState(indexWithTurn((turn + 1) % numPlayers));
	}

	/**
	 * Returns the state with the same marker positions as this state
	 * but with it being the given player's turn
	 *
	 * @param p a player index
	 */

	public MPCantStopState setTurn(int p)
	{
	    return new MPCantStopState(indexWithTurn(p));
	}

	/**
	 * Returns the index of the state with the same marker positions
	 * as this state but with turn belonging to the given player.
	 *
	 * @param p a player index
	 * @return the index of the state with this state's marker positions
	 * and p's turn
	 */

	private long indexWithTurn(int p)
	{
	    return indexer.changeTurn(index, p);
	}

	/**
	 * Returns the state reached from this state by moving according to
	 * the given grouping of the dice.
	 *
	 * @param move the grouping of dice to move according to
	 * @return the resulting state
	 */

	public MPCantStopState getNextState(Grouping move)
	{
	    return new MPCantStopState(indexer.getNextState(index, move));
	}

	/**
	 * Determines the location of the given player's marker in
	 * the given column.
	 *
	 * @param p a player index
	 * @param col a column label
	 */

	public int getMarkerPosition(int p, int col)
	{
	    return indexer.getMarkerPosition(index, p, col);
	}

	/**
	 * @param moveStrat a map for each player giving the best grouping
	 * of the dice to use at each possible state to move to (given by
	 * index)
	 * @param endStrat for each player, a set of states that they
	 * should end the turn if they reach it (states are given by index)
	 */

	public void findStrategy(MPPositionValueMap positionValues,
				 Map< Pair< Long, DiceRoll >, Grouping > moveStrat,
				 Set< Long > endStrat,
				 int player)
	{
	    double[][] x = new double[numPlayers][numPlayers];

	    for (int p = 0; p < numPlayers; p++)
		for (int q = 0; q < numPlayers; q++)
		    {
			x[p][q] = positionValues.getValue(setTurn(q).getIndex(), p);
		    }
	    
	    Component c = new Component(makeComponent(), x, positionValues);

	    c.estimatePositionValues(moveStrat, endStrat, player);
	}

	/**
	 * Solves for the position values of the anchors in the
	 * same component as this anchor.  This anchor should be
	 * the representative for the component, in other words,
	 * this anchor should be player 0's turn.
	 *
	 * @param positionValues a map giving the position values of anchors
	 * reachable from this one but not in the same component
	 * @return an array <CODE>result</CODE> such that
	 * <CODE>result[p][a]</CODE> is the probability player p wins
	 * from the anchor where it is a's turn.
	 */

	public double[][] solve(MPPositionValueMap positionValues)
	{
	    final int MAX_ITERATIONS = 50;
	    final double STOPPING_DISTANCE = 1E-14;

	    // initial estimate of P(Pi wins from Pj's anchor) = 1/n

	    double[][] x = new double[numPlayers][numPlayers];
	    for (int p = 0; p < numPlayers; p++)
		for (int q = 0; q < numPlayers; q++)
		    {
			x[p][q] = 1.0 / numPlayers;
		    }
	    
	    Component c = new Component(makeComponent(), x, positionValues);
	    
	    double[][] newEstimates = null;
	    double[][] lastEstimates = null;
	    double distance = Double.POSITIVE_INFINITY;
	    int iterations = 0;
	    do
		{
		    lastEstimates = newEstimates;
		    newEstimates = c.estimatePositionValues();
		    
		    c.setAnchorEstimates(newEstimates);

		    // compute distance from last estimate

		    if (lastEstimates != null)
			{
			    distance = 0.0;
			    for (int row = 0; row < newEstimates.length; row++)
				for (int col = 0; col < newEstimates.length; col++)
				    distance += Math.pow(newEstimates[row][col] - lastEstimates[row][col],2 );

			    distance = Math.sqrt(distance);
			}

		    iterations++;
		}
	    while (distance > STOPPING_DISTANCE && iterations < MAX_ITERATIONS);

	    System.out.println(iterations + " iterations");

	    return newEstimates;
	}

	private int[] computeComponentSize()
	{
	    // Compute the number of states coming off each anchor so we
	    // don't have to put up with resizing a vector (we don't use
	    // a linked list because we use direct indexing).
	    // size[p] accumulates the size of the component for each player.

	    int[] size = new int[numPlayers];

	    // make a collection of the available columns

	    Collection< Integer > availableColumns
		= new LinkedList< Integer >();

	    for (int c = getFirstColumn(); c <= getLastColumn(); c++)
		{
		    if (!isWon(c))
			availableColumns.add(c);
		}

	    // iterate over all size 1, ..., numMarkers size subsets
	    // of the available columns

	    for (int r = 0;
		 r <= Math.min(numMarkers, availableColumns.size()); 
		 r++)
		{
		    Iterator advanceIterator
			= new SubsetIterator(availableColumns, r);

		    while (advanceIterator.hasNext())
			{
			    Collection< Integer > advanceCollection
				= (Collection< Integer >)(advanceIterator.next());

			    // make an array out of the collection

			    int[] advanceColumns
				= new int[advanceCollection.size()];
			    Iterator< Integer > columnIterator
				= advanceCollection.iterator();
			    for (int i = 0; i < advanceColumns.length; i++)
				advanceColumns[i] = columnIterator.next();

			    // for each player, compute the number
			    // of states that represent the advance of
			    // at least one space in the given columns

			    for (int p = 0; p < numPlayers; p++)
				{
				    int totalForTheseColumns = 1;

				    for (int i = 0; i < advanceColumns.length; i++)
					totalForTheseColumns *= (getColumnLength(advanceColumns[i]) - getMarkerPosition(p, advanceColumns[i]));

				    size[p] += totalForTheseColumns;

				    /*
				      // shows, for each selection of columns,
				      // the number of moves using those
				      // columns

				    System.out.println("Player " + p +
						       " columns "
						       + advanceCollection
						       + " moves = "
						       + totalForTheseColumns);
				    */
				}
			}
		}

	    return size;
	}
	
	/**
	 * Computes the maximal strongly connected component that
	 * contains the given anchor.  The states in the SCC are given
	 * by their index.  The first dimension corresponds to turn.
	 * The states will be arranged in the second dimension so that
	 * if state v is reachable from state u without going through
	 * an anchor, then v has a higher index than u (so the
	 * ordering in the second dimension is a topological sort of
	 * the SCC once you remove the edges going into the anchors).
	 * As a result, the anchor positions will be in index
	 * <CODE>[p][0]</CODE> where <CODE>p</CODE> is the player whose
	 * turn it is.
	 */

	private long[][] makeComponent()
	{
	    long[][] component = new long[numPlayers][];

	    // Compute the number of states coming off each anchor so we
	    // don't have to put up with resizing a vector (we don't use
	    // a linked list because we use direct indexing).
	    // size[p] accumulates the size of the component for each player.

	    int[] size = computeComponentSize();

	    Collection< Integer > availableColumns
		= new LinkedList< Integer >();

	    for (int c = getFirstColumn(); c <= getLastColumn(); c++)
		{
		    if (!isWon(c))
			availableColumns.add(c);
		}

	    /* code moved to separate method

	    // make a collection of the available columns


	    // iterate over all size 1, ..., numMarkers size subsets
	    // of the available columns

	    for (int r = 0;
		 r <= Math.min(numMarkers, availableColumns.size()); 
		 r++)
		{
		    Iterator advanceIterator
			= new SubsetIterator(availableColumns, r);

		    while (advanceIterator.hasNext())
			{
			    Collection< Integer > advanceCollection
				= (Collection< Integer >)(advanceIterator.next());

			    // make an array out of the collection

			    int[] advanceColumns
				= new int[advanceCollection.size()];
			    Iterator< Integer > columnIterator
				= advanceCollection.iterator();
			    for (int i = 0; i < advanceColumns.length; i++)
				advanceColumns[i] = columnIterator.next();

			    // for each player, compute the number
			    // of states that represent the advance of
			    // at least one space in the given columns

			    for (int p = 0; p < numPlayers; p++)
				{
				    int totalForTheseColumns = 1;

				    for (int i = 0; i < advanceColumns.length; i++)
					totalForTheseColumns *= (getColumnLength(advanceColumns[i]) - getMarkerPosition(p, advanceColumns[i]));

				    size[p] += totalForTheseColumns;

				    }
			   }
                 }
	    */

	    // set the size of the second dimension of the component array
	    // to just fit all the states coming off each anchor

	    for (int p = 0; p < numPlayers; p++)
		{
		    component[p] = new long[size[p]];
		}

	    // Now do it all over again, except instead of just counting
	    // states, we put their indices in the array.  Adding states
	    // in increasing order of number of columns used and by
	    // increasing spaces moved within a column guarantees we have
	    // a correct topological sort.

	    int[] nextIndex = new int[numPlayers];
	    
	    // first, take care of the anchor positions -- compute the index
	    // of the current anchor assuming it is player 0's turn, and get
	    // the indices of the other anchors from that

	    for (int p = 0; p < numPlayers; p++)
		{
		    component[p][0] = indexWithTurn(p);
		    nextIndex[p] = 1;
		}

	    // iterate over all size 1, ..., numMarkers size subsets
	    // of the available columns

	    for (int r = 1;
		 r <= Math.min(numMarkers, availableColumns.size()); 
		 r++)
		{
		    Iterator advanceIterator
			= new SubsetIterator(availableColumns, r);

		    while (advanceIterator.hasNext())
			{
			    Collection< Integer > advanceCollection
				= (Collection< Integer >)(advanceIterator.next());

			    // make an array out of the collection

			    int[] advanceColumns
				= new int[r];
			    Iterator< Integer > columnIterator
				= advanceCollection.iterator();
			    for (int i = 0; i < r; i++)
				advanceColumns[i] = columnIterator.next();

			    // for each player, generate all the states
			    // that that player can get to by moving in the
			    // selected columns

			    for (int p = 0; p < numPlayers; p++)
				{
				    // compute the number of spaces player p
				    // can move in the selected columns; the
				    // multiset stores one minus these values
				    // because we must move at least one
				    // space in each selected column --
				    // maxSpacesMoved.count(i) is the number
				    // of _extra_ spaces that can be moved in
				    // column advanceColumns[i]

				    Multiset maxSpacesMoved
					= new Multiset(r - 1);

				    for (int i = 0; i < r; i++)
					{
					    int maxToMove = getColumnLength(advanceColumns[i]) - getMarkerPosition(p, advanceColumns[i]) - 1;
					    maxSpacesMoved.addItems(i, maxToMove);
					}

				    // iterate over all subsets of
				    // maxSpacesMoved

				    Iterator toMoveIterator
					= maxSpacesMoved.subsetIterator();

				    while (toMoveIterator.hasNext())
					{
					    Multiset spacesToMove = (Multiset)(toMoveIterator.next());

					    // compute the index of the
					    // corresponding state by going
					    // over all the columns to move in
					    // and adding the appropriate place
					    // value to the anchor's index

					    long indexOfNext = indexer.indexAfterMoving(index, p, advanceColumns, spacesToMove);

					    component[p][nextIndex[p]] = indexOfNext;
					    nextIndex[p]++;
					}
				}
			}
		}

	    return component;
	}

	/**
	 * Returns the index of the player whose turn it is in this state.
	 *
	 * @return the index of the player whose turn it is
	 */

	public int getTurn()
	{
	    return turn;
	}

	/**
	 * Determines if someone has won the given column.
	 *
	 * @param col a column label
	 * @return true iff a player has won that column
	 */

	public boolean isWon(int col)
	{
	    return (columnWinner[getColumnIndex(col)] != -1);
	}

	/**
	 * Determines the index of the player who has won the given column.
	 *
	 * @param col a column label
	 * @return the index of the player who won that column, or -1 if no
	 * one has yet won that column
	 */

	public int getColumnWinner(int col)
	{
	    return columnWinner[getColumnIndex(col)];
	}

	/**
	 * Determines if this state is a final state (that is, one in which
	 * the game has been won by one of the players).
	 *
	 * @return true iff this state is final
	 */

	public boolean isFinal()
	{
	    int p = 0;
	    while (p < numPlayers && columnsWon[p] < columnsToWin)
		p++;

	    return (p < numPlayers);
	}

	/**
	 * Determines if this is the representative state among all
	 * equivalent states.  Noe that this notion of
	 * equivalence does not take into consideration that it is
	 * not necessarily important who has won each column.
	 *
	 * @return true iff this is the least state among all equivalaent
	 * states
	 */

	public boolean isRepresentative()
	{
	    // state is least if, in each closed column, the winner of
	    // that column is the only player who has made progress

	    for (int c = getFirstColumn(); c <= getLastColumn(); c++)
		if (getColumnWinner(c) != -1)
		    for (int p = 0; p < numPlayers; p++)
			if (p != getColumnWinner(c) && getMarkerPosition(p, c) > 0)
			    return false;

	    return true;
	}
			
	/**
	 * Returns the representative of this state.
	 */

	public MPCantStopState getRepresentative()
	{
	    int[][] markers = new int[numPlayers][numColumns];

	    for (int p = 0; p < numPlayers; p++)
		for (int c = getFirstColumn(); c <= getLastColumn(); c++)
		    if (getColumnWinner(c) == -1 || p == getColumnWinner(c))
			markers[p][getColumnIndex(c)] = getMarkerPosition(p, c);
		    else
			markers[p][getColumnIndex(c)] = 0;

	    return new MPCantStopState(markers, getTurn());
	}

	public boolean isLegalAnchor()
	{
	    // see if there is a (p1, c) (p2, c) so that p1 != p2
	    // and markers[p1][c] == markers[p1][c]
	    // and markers[p1][c] > 1

	    for (int p1 = 0; p1 < numPlayers; p1++)
		for (int p2 = 0; p2 < numPlayers; p2++)
		    if (p1 != p2)
			{
			    for (int c = firstColumn; c <= lastColumn; c++)
				if (getMarkerPosition(p1, c) > 0
				    && getMarkerPosition(p1, c) == getMarkerPosition(p2, c))
				    return false;
			}

	    return true;
	}

	/**
	 * Determines which player has won if the game reaches this state.
	 *
	 * @return the index of the winning player, or -1 if no one has won
	 */

	public int getWinner()
	{
	    int p = 0;
	    while (p < numPlayers && columnsWon[p] < columnsToWin)
		p++;
		
	    if (p < numPlayers)
		return p;
	    else
		return -1;	
	}

	/**
	 * Determines if the given player may stop the turn, given that it
	 * began at the given starting state.
	 *
	 * @param p the player who has moved
	 * @param start the state at the start of the turn
	 */

	public boolean canStop(int p, MPCantStopState start)
	{
	    // this implements the "Can't Stop Can't Stop" variation in
	    // which players cannot have their colored markers occupying
	    // the same space (so that if a netural marker is placed on
	    // an opponent's colored marker, the current player can't stop
	    // the turn)

	    // check if another player has a marker at the same position
	    // as player p

	    for (int otherPlayer = 0; otherPlayer < numPlayers; otherPlayer++)
		{
		    if (otherPlayer != p)
			{
			    for (int c = firstColumn; c <= lastColumn; c++)
				if (getMarkerPosition(p, c) > 0
				    && getMarkerPosition(p, c) == getMarkerPosition(otherPlayer,c))
				    {
					return false;
				    }
			}
		}

	    return true;
	}

	/**
	 * Determines if it is legal to move according to the used pairs
	 * in the given grouping, assuming that this state was reached from
	 * the given start-of-turn state.
	 *
	 * @param start a state representing the start of the current turn
	 * @param totals the grouping of dice
	 * @return true iff the move is legal
	 */

	private boolean isLegalMove(MPCantStopState start, Grouping totals)
	{
	    // a move is legal iff
	    // 1) the columns are not won
	    // 2) there is room to move in columns
	    // 3) the columns have already been moved in or there are neutral
	    //    markers available
	    // 4) the move is maximal (that is, no unused total in the given
	    //    grouping could also be used)
	    
	    Multiset used = totals.getUsed();
	    Multiset unused = totals.getUnused();

	    int neutralMarkersNeeded = 0;
	    int neutralMarkersAlreadyUsed = 0;

	    // check 1) and 2) while computing values needed for 3)

	    for (int col = getFirstColumn(); col <= getLastColumn(); col++)
		{
		    if (getMarkerPosition(turn, col) != start.getMarkerPosition(turn, col))
			neutralMarkersAlreadyUsed++;

		    if (used.countItem(col) > 0)
			{
			    if (isWon(col))
				return false;
			    if (getMarkerPosition(turn, col) + used.countItem(col) > getColumnLength(col))
				return false;

			    if (getMarkerPosition(turn, col) == start.getMarkerPosition(turn, col))
				neutralMarkersNeeded++;
			}
		}

	    // check 3): we don't need more neutral markers than we have left

	    if (neutralMarkersNeeded + neutralMarkersAlreadyUsed > numMarkers)
		return false;

	    // check 4): we're not ignoring a group we could use
	    // (if we _can_ move after grouping the dice, we _must_)

	    Iterator unusedIterator = unused.iterator();
	    while (unusedIterator.hasNext())
		{
		    Multiset superset = (Multiset)(used.clone());
		    superset.addItem(((Integer)(unusedIterator.next())).intValue());


		    if (isLegalMove(start, new Grouping(superset, new Multiset(getLastColumn()))))
			return false;
		}

	    return true;
	}
	
	/**
	 * Determines if the current player may stop the turn, given that it
	 * began at the given starting state.
	 *
	 * @param start the state at the start of the turn
	 */

	public boolean canStop(MPCantStopState start)
	{
	    return canStop(turn, start);
	}

	/**
	 * Returns a set of indices of states that are equivalent to this one.
	 *
	 * @return a set of indices of states that are equivalent to this one
	 */

	public Set< MPCantStopState > getEquivalentStates()
	{
	    Set< MPCantStopState > equivalentStates = new HashSet< MPCantStopState >();
	    
	    equivalentStates.add(this);
	    equivalentStates.add(getSymmetricState());

	    if (totalColumnsWon == 0)
		{
		}
	    else
		{
		    // some columns have been won; find all assignments of
		    // winners to columns such that keep the number of
		    // columns won for each player constant

		    // create a list of the columns won

		    List< Integer > finishedColumns = new ArrayList< Integer >();
		    for (int c = getFirstColumn(); c <= getLastColumn(); c++)
			if (getColumnWinner(c) != -1)
			    finishedColumns.add(c);

		    // create a list of column winners

		    int[] copyOfColumnsWon = new int[numPlayers];
		    System.arraycopy(columnsWon, 0, copyOfColumnsWon, 0, numPlayers);

		    // get rearrangements of winners

		    List< List < Integer > > rearrangements
			= Combinatorics.findArrangements(copyOfColumnsWon);

		    // assign columns won according to each rearrangement

		    int[][] markers = new int[numPlayers][numColumns];
		    for (int p = 0; p < numPlayers; p++)
			for (int i = 0; i < numColumns; i++)
			    markers[p][i] = getMarkerPosition(p, getColumnLabel(i));

		    for (List< Integer > arrangement : rearrangements)
			{
			    // System.out.println("Arrangement: " + arrangement);
			    for (int finishedIndex = 0;
				 finishedIndex < finishedColumns.size();
				 finishedIndex++)
				{
				    int winner = arrangement.get(finishedIndex);
				    int label = finishedColumns.get(finishedIndex);
				    // System.out.println(winner + " " + label);

				    for (int p = 0; p < numPlayers; p++)
					if (p == winner)
					    markers[p][getColumnIndex(label)] = getColumnLength(label);
					else
					    markers[p][getColumnIndex(label)] = 0;
				}

			    MPCantStopState equiv = new MPCantStopState(markers, getTurn());
			    // System.out.println("State: " + equiv);
			    equivalentStates.add(equiv);
			    equivalentStates.add(equiv.getSymmetricState());
			}
		}

	    return equivalentStates;
	}

	/**
	 * Returns the state that is symmetric to this one.
	 */

	public MPCantStopState getSymmetricState()
	{
	    int[][] markers = new int[numPlayers][numColumns];
	    for (int p = 0; p < numPlayers; p++)
		for (int c = firstColumn; c <= lastColumn; c++)
		    {
			int reflect = getReflectedColumn(c);
			markers[p][columnIndex[reflect]] = getMarkerPosition(p, c);
		    }

	    return new MPCantStopState(markers, getTurn());
	}

	/**
	 * Returns the hash code for this state.
	 */

	public int hashCode()
	{
	    return (int)index ^ (int)(index >> 32);
	}

	/**
	 * Determines if this state is equal to the given object.
	 * This does not currently account for comparisons between states
	 * in different games.
	 */

	public boolean equals(Object o)
	{
	    if (o instanceof MPCantStopState)
		{
		    MPCantStopState s = (MPCantStopState)o;
		    
		    return index == s.index;
		}
	    else
		{
		    return false;
		}
	}
	
	/**
	 * Returns a printable string representation of this state.
	 *
	 * @return a string representation of this state
	 */

	public String toString()
	{
	    StringBuffer buf = new StringBuffer();

	    buf.append("(");
	    for (int p = 0; p < numPlayers; p++)
		{
		    if (turn == p)
			buf.append("<");
		    else
			buf.append("[");
		    for (int c = firstColumn; c <= lastColumn; c++)
			{
			    buf.append("" + getMarkerPosition(p, c));
			    if (c < lastColumn)
				buf.append(" ");
			}
		    if (turn == p)
			buf.append(">");
		    else
			buf.append("]");

		    if (p < numPlayers - 1)
			buf.append(" ");
		}
	    buf.append(")");

	    return buf.toString();
	}
    }
    
    public static void main(String[] args)
    {
	int players = DEFAULT_PLAYERS;
	int sides = DEFAULT_SIDES;
	int len = DEFAULT_SHORTEST_COLUMN;
	int delta = DEFAULT_COLUMN_DIFFERENCE;
	int toWin = DEFAULT_COLUMNS_TO_WIN;

	try
	    {
		players = Integer.parseInt(args[0]);
		sides = Integer.parseInt(args[1]);
		len = Integer.parseInt(args[2]);
		if (args.length > 3)
		    delta = Integer.parseInt(args[3]);
		if (args.length > 4)
		    toWin = Integer.parseInt(args[4]);
	    }
	catch (ArrayIndexOutOfBoundsException aioob)
	    {
		System.err.println("USAGE: java MPCantStopGame players sides shortest-column [column-diff [columns-to-win]]");
		System.exit(1);
	    }

	MPCantStopGame g = new MPCantStopGame(players, sides, len, delta, toWin);

	// parse arguments for collusion

	for (int a = 0; a < args.length; a++)
	    {
		if (args[a].startsWith("--collusion="))
		    {
			StringTokenizer tok = new StringTokenizer(args[a], "=,");
			tok.nextToken(); // skip "--collusion=" token
			g.setCollusionTarget(Integer.parseInt(tok.nextToken()),
					     Integer.parseInt(tok.nextToken()));

		    }
	    }

	System.out.println("Number of states: " + g.countStates());
	System.out.println("Number of anchors: " + (g.indexer.getHighestAnchor() + 1));

	// positionValues(s, p) is player p's valuation of state s
	
	MPPositionValueMap positionValues = g.getPositionValueMap();

	// System.out.println("Got map");
	for (long i = g.indexer.getHighestAnchor(); i >= 0; i--)
	    {
		/*
		if (i % 100000 == 0)
		    System.out.println(i);
		*/

		MPCantStopState s = g.new MPCantStopState(g.indexer.anchorToState(i));

		if (s.isFinal() && s.isLegalAnchor())
		    {
			int winner = s.getWinner();

			for (int p = 0; p < players; p++)
			    {
				if (p == winner)
				    {
					positionValues.setValue(s.getIndex(), p, 1.0);
				    }
				else
				    {
					positionValues.setValue(s.getIndex(), p, 0.0);
				    }
			    }
		    }
	    }

	long totalPositions = 0;

	for (long i = (g.indexer.getHighestAnchor() + 1)/ players - 1; i >= 0; i--)
	    {
		MPCantStopState s = g.new MPCantStopState(g.indexer.anchorToState(i));
		/*
		if (g.indexer.stateToAnchor(s.getIndex()) != i)
		    throw new ArithmeticException("inverse not correct " + i + " " + s.getIndex() + " " + g.indexer.stateToAnchor(s.getIndex()) + " " + s);
		*/
		
		MPCantStopState rep = s.getRepresentative();

		if (!s.isFinal()
		    && s.isLegalAnchor()
		    && s.getTurn() == 0
		    && !positionValues.hasValue(s.getIndex(), 0)
		    && !positionValues.hasValue(rep.getIndex(), 0))
		    {
			System.out.println("Anchor " + s);

			// solve for position values of anchors in the
			// same SCC as s

			double[][] values = s.solve(positionValues);
			int[] size = s.computeComponentSize();
			for (int p = 0; p < players; p++)
			    totalPositions += size[p];
			
			// now record those estimates in positionValues

			Set< MPCantStopState > equivStates = s.getEquivalentStates();
			
			/*
			System.out.println("Equivalent states");
			System.out.println(equivStates);
			*/

			for (MPCantStopState equiv : equivStates)
			    {
				for (int pointOfView = 0; pointOfView < players; pointOfView++)
				    {
					for (int pTurn = 0; pTurn < players; pTurn++)
					    {
						positionValues.setValue(equiv.setTurn(pTurn).getIndex(), pointOfView, values[pointOfView][pTurn]);
						// positionValues.setValue(equiv.setTurn(pTurn).getIndex(), pointOfView, 0.0);
					    }
				    }
			    }
		    }
		else if (!s.isLegalAnchor())
		    {
			System.out.println("Anchor " + s + " is illegal");
		    }
		else if (s.getTurn() != 0)
		    {
			System.out.println("Anchor " + s + " is not player 0's turn");
		    }
		else if (s.isFinal())
		    {
			totalPositions += players;
			System.out.println("Anchor " + s + " is a win for " + s.getWinner());
		    }
		else if (positionValues.hasValue(s.getIndex(), 0))
		    {
			System.out.println("Anchor " + s + " is equivalent to a previously computed state");
		    }
		else if (positionValues.hasValue(rep.getIndex(), 0))
		    {
			System.out.println("Anchor " + s + " is equivalent to a previously computed state");

			for (int pointOfView = 0; pointOfView < players; pointOfView++)
			    {
				for (int pTurn = 0; pTurn < players; pTurn++)
				    {
					double value = positionValues.getValue(rep.setTurn(pTurn).getIndex(), pointOfView);
					positionValues.setValue(s.setTurn(pTurn).getIndex(), pointOfView, value);
					positionValues.setValue(s.getSymmetricState().setTurn(pTurn).getIndex(), pointOfView, value);
				    }
			    }
		    }
	    }
	System.out.println("Total positions: " + totalPositions);

	try
	    {
		DataOutputStream out = new DataOutputStream(new FileOutputStream("mp_cant_stop_" + players + "_" + sides + "_" + len + "_" + delta + "_" + toWin + ".dat"));
		positionValues.write(out);
		out.close();
	    }
	catch (IOException e)
	    {
		System.err.println("Failed to write position values");
		e.printStackTrace(System.err);
	    }

	/*
	for (long i = 0; i < g.markerArrangements * players; i++)
	    {
		MPCantStopState s = g.new MPCantStopState(i);
		System.out.print(s + " ");

		long[][] scc = s.makeComponent();
		for (int p = 0; p < players; p++)
		    System.out.print(scc[p].length + " ");
		System.out.println();

		MPCantStopState s = g.new MPCantStopState(i);
		Graph scc = s.makeComponentGraph();
		System.out.println(s);
		System.out.println(scc);
		System.out.println();

		for (int p = 0; p < players; p++)
		    for (int k = 0; k < scc[p].length; k++)
			System.out.println(g.new MPCantStopState(scc[p][k]));

		System.out.println();
	    }
	*/

	// System.out.println(g.new RollList());
    }

    /**
     * A list of all of the possible rolls in this game, along with their
     * probabilities and groupings.
     */

    private class RollList
    {
	/**
	 * The rolls.
	 */

	private DiceRoll[] roll;

	/**
	 * The probability of each roll.
	 */

	private double[] prob;

	/**
	 * The ways to group the dice in each roll.
	 */

	private List < Set< Grouping > > groups;

	/**
	 * Creates the list of rolls for this game.
	 */

	private RollList()
	{
	    // figure out how many rolls there are and init arrays accordingly

	    int length = Combinatorics.choose(numSides + numDice - 1, numDice).intValue();

	    roll = new DiceRoll[length];
	    prob = new double[length];
	    groups = new ArrayList< Set< Grouping > >(length);

	    // make the rolls

	    int i = 0;
	    DiceRoll r = new DiceRoll(numDice, numSides);

	    roll[i++] = r;

	    while (r.hasNext())
	    {
		r = r.next();

		roll[i++] = r;
	    }
	    
	    // compute the probability of each roll

	    for (int j = 0; j < length; j++)
		prob[j] = roll[j].probability();

	    // compute the possible groupings of each roll

	    for (int j = 0; j < length; j++)
		{
		    Set< Grouping > groupings = new TreeSet< Grouping >();

		    // get all possible full groups that can be made

		    Set< Multiset > fullGroupings = getPossibleGroupings(roll[j], diceGroupSize);

		    // for each full group...

		    Iterator< Multiset > totalsIterator = fullGroupings.iterator();
		    while (totalsIterator.hasNext())
			{
			    // ...iterate over all subsets of totals to use...

			    Multiset totals = totalsIterator.next();

			    Iterator usedIterator = totals.subsetIterator();
			    while (usedIterator.hasNext())
				{
				    // ...adding them and the unused totals
				    // as a Grouping object

				    Multiset used = (Multiset)(usedIterator.next());
				    Multiset unused = totals.difference(used);

				    if (used.size() > 0)
					groupings.add(new Grouping(used, unused));
				}
			}

		    groups.add(groupings);
		}
	}
	
	/**
	 * Returns a set of multisets giving the different ways to fully group
	 * the given dice.  "Fully" group means to group with no dice left
	 * over (that is, no totals made unused).
	 *
	 * @param roll a dice roll
	 * @param groupSize a positive divisor of the number of dice
	 * @return a set of groupings of the given dice
	 */
	
	private Set< Multiset > getPossibleGroupings(DiceRoll roll, int groupSize)
	{
	    if (roll.size() == groupSize)
		{
		    Set result = new TreeSet< Multiset >();
		    Multiset total = new Multiset(roll.size() * roll.countSides());
		    total.addItem(roll.total());
		    result.add(total);
		    
		    return result;
		}
	    else
		{
		    Set< Multiset > totals = new TreeSet< Multiset >();
		    
		    // iterate over groupSize groupings of roll
		    
		    Iterator i = roll.subrollIterator(groupSize);
		    while (i.hasNext())
			{
			    DiceRoll group = (DiceRoll)(i.next());
			    
			    // remove group from roll
			    
			    DiceRoll diff = roll.difference(group);

			    // get groupings of roll
			    
			    Set< Multiset > subtotals = getPossibleGroupings(diff, groupSize);
			    
			    // combine with group
			    
			    Iterator j = subtotals.iterator();
			    while (j.hasNext())
				{
				    Multiset s = (Multiset)(j.next());
				    
				    totals.add(s.plus(group.total()));
				}
			}
		    
		    return totals;
		}
	}

	/**
	 * Returns a printable representation of this list.
	 *
	 * @return a printable representation of this list.
	 */

	public String toString()
	{
	    StringBuffer result = new StringBuffer();

	    for (int i = 0; i < roll.length; i++)
		{
		    result.append(roll[i].toString() + " " + prob[i]);
		    result.append("\n");
		    result.append(groups.get(i).toString());
		    result.append("\n");
		    result.append("\n");
		}

	    return result.toString();
	}

	public class RollIterator implements Iterator< DiceRoll >
	{
	    private int nextIndex;

	    public RollIterator()
	    {
		nextIndex = 0;
	    }

	    public boolean hasNext()
	    {
		return (nextIndex < roll.length);
	    }

	    public DiceRoll next()
	    {
		nextIndex++;

		return roll[nextIndex - 1];
	    }

	    /**
	     * Returns the probability of the last roll returned by
	     * <CODE>next</CODE>.
	     */

	    public double getProbability()
	    {
		return prob[nextIndex - 1];
	    }

	    /**
	     * Returns the possible groupings (complete and partial)
	     * of the last roll returned by next.
	     */

	    public Set< Grouping > getGroupings()
	    {
		return groups.get(nextIndex - 1);
	    }

	    public void remove()
	    {
		throw new UnsupportedOperationException();
	    }
	}
    }

}
