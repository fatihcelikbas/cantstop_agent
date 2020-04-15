package com.bloxomo.gametheory.cantstop;

import com.bloxomo.gametheory.*;
// TO DO: make CantStopState an inner class of CantStop game and move
// all the stuff like placeValue to the outer class leaving the inner
// class with only the things that are unique to it (this will mess
// up dynamic loading, eh?)

// RESOLVED (1): check efficiency of bounded multiset iterator -- instead
// use subset iterator for multiset -- for (0 0 3 3 1 0 0) once we've chosen
// say columns 1, 2, and 3 to move, there's a bijection from subsets of (1 3 2)
// (the number of spaces left to move in those columns) to the actual moves
// (by doing the subtraction)

// RESOLVED (1): improve the efficiency of the list concatenation in getNextMoves
// by making a new collection class that can easily iterate through a list
// of lists

// RESOLVED(1): abandon the totalMoves loop in getNextMoves in favor of just
// going through all the possible ways to increase the selected columns
// (make a new iterator for bounded multiset that would iterate through
// 000 001 010 011 020 021 030 031 100...)

// NOTE (1): getNextMoves is not the problem -- only a small fraction of the
// time spent (2.5s of 120s for (3,3)) is spent in getNextMoves

import com.sirmapsalot.combinatorics.*;
import java.util.*;

/**
 * A state in a game of Can't Stop.
 *
 * @author Jim Glenn
 * @version 0.1 11/22/2005
 */

public class CantStopState extends GameState implements Cloneable, Comparable
{
    /**
     * The number of dice that are grouped to make column indices.
     */

    private static final int NUM_DICE = 2;

    private final int numDice;

    private final int columnsToWin;

    /**
     * The number of groups of dice to roll.  In the standard game,
     * 4 dice are rolled and made into pairs, so there are 2 groups
     * of 2 dice each.
     */

    private static final int NUM_DICE_GROUPS = 2;

    /**
     * The number of columns needed to win the game.
     */

    public static final int COLUMNS_TO_WIN = 3;

    /**
     * The number of columns players can advance markers in during a single
     * turn.
     */

    public static final int MARKERS_PER_TURN = 3;

    /**
     * The number of sides on the dice this game is played with.
     */

    private final int numSides;

    /**
     * Maps column values to multiset elements.  This is done in order
     * to group the largest values at the beginning of the multiset.
     * Doing so makes the order of the states work out better for
     * distributed computations: when sending a group of states, the
     * total number of successor states will be lower.
     */

    private final int[] columnToMultisetMap;

    /**
     * The inverse of <CODE>columnToMultisetMap</CODE>
     */

    private final int[] multisetToColumnMap;

    /**
     * The maximum number of spaces in each column.
     */

    private final int[] columnLength;

    /**
     * The place values used in the integer encoding scheme.
     */

    private final long[] placeValue;

    /**
     * A map from rolls to possible (not necessarily legal) moves for
     * this state and any state in a game with the same rules.
     */

    private final Map rollList;

    /**
     * A unique integer index for this state. -1 if it hasn't been
     * computed yet.
     */

    private long uniqueIndex = -1;

    /**
     * The position of the markers in this state.  If the marker in column
     * <CODE>c</CODE> has advanced x spaces, then there will be
     * x occurrences of the element corresponding to <CODE>c</CODE> in
     * the multiset.  Note that the element corresponding to <CODE>c</CODE> is
     * not necessarily <CODE>c</CODE> and is instead given by
     * <CODE>columnToMultisetMap[c]</CODE>.
     */

    private BoundedMultiset markers;

    public CantStopState()
    {
	this(6, 3, NUM_DICE, COLUMNS_TO_WIN);
    }

    public CantStopState(long index)
    {
	this();

	uniqueIndex = index;

	for (int column = getHighestRoll(); column >= getLowestRoll(); column--)
	    {
		if (index >= placeValue[column])
		    markers.addItems(columnToMultisetMap[column], (int)(index / placeValue[column]));
		index = index % placeValue[column];
	    }
    }

    public CantStopState(String s)
    {
	this();

	StringTokenizer tok = new StringTokenizer(s);
	int column = getLowestRoll();

	while (tok.hasMoreTokens() && column <= getHighestRoll())
	    {
		placeMarker(column, Integer.parseInt(tok.nextToken()));
		column++;
	    }
    }

    public CantStopState(int sides, int minLength, int toWin)
    {
	this(sides, minLength, NUM_DICE, toWin);
    }

    public CantStopState(int sides, int minLength)
    {
	this(sides, minLength, NUM_DICE, COLUMNS_TO_WIN);
    }

    /**
     * Creates a state in a game of Can't Stop played with
     * the given parameters.  The state will have no markers advanced.
     *
     * @param sides the number of sides on the dice
     * @param minLength the length of the column for 2's
     */

    public CantStopState(int sides, int minLength, int dice, int toWin)
    {
	// System.out.println("Index = " + uniqueIndex);

	numDice = dice;
	numSides = sides;
	columnsToWin = toWin;

	int midRoll = (getLowestRoll() + getHighestRoll()) / 2;

	columnLength = new int[getHighestRoll() + 1];
	columnToMultisetMap = new int[getHighestRoll() + 1];
	multisetToColumnMap = new int[getHighestRoll() - getLowestRoll() + 1];

	columnLength[getLowestRoll()] = minLength;
	columnLength[getHighestRoll()] = minLength;

	// initialize column lengths

	for (int c = numDice + 1; c <= midRoll; c++)
	    columnLength[c] = columnLength[c - 1] + 2;
	for (int c = getHighestRoll() - 1; c >= midRoll + 1; c--)
	    columnLength[c] = columnLength[c + 1] + 2;

	// initialize column to multiset map

	int firstUnused = 1;
	int secondHalf = midRoll;
	columnToMultisetMap[midRoll] = 0;
	if (numDice % 2 != 0 && (sides + 1) % 2 != 0)
	    {
		columnToMultisetMap[midRoll + 1] = 1;
		firstUnused = 2;
		secondHalf = midRoll + 1;
	    }

	for (int i = 1; midRoll - i >= numDice; i++)
	    {
		columnToMultisetMap[midRoll - i] = firstUnused + (i - 1) * 2;
		columnToMultisetMap[secondHalf + i] = firstUnused + (i - 1) * 2 + 1;
	    }

	// initialize multiset to column map

	for (int c = getLowestRoll(); c <= getHighestRoll(); c++)
	    multisetToColumnMap[columnToMultisetMap[c]] = c;

	// figure placeValues

	placeValue = new long[getHighestRoll() + 1];
	placeValue[getLowestRoll()] = 1;
	for (int c = getLowestRoll() + 1; c <= getHighestRoll(); c++)
	    placeValue[c] = placeValue[c - 1] * (columnLength[c - 1] + 1);

	// initialize markers

	int[] bounds = new int[getHighestRoll() - getLowestRoll() + 1];
	for (int i = 0; i < bounds.length; i++)
	    bounds[i] = columnLength[multisetToColumnMap[i]];

	markers = new BoundedMultiset(bounds);

	// make map of rolls to potential moves

	rollList = getRollList(false);

	uniqueIndex = computeIndex();
    }

    /**
     * Returns a clone of this state.
     *
     * @return a clone of this state
     */

    public Object clone()
    {
	try
	    {
		CantStopState result = (CantStopState)(super.clone());

		// copy the non-final fields of this state

		result.markers = (BoundedMultiset)(markers.clone());

		return result;
	    }
	catch (CloneNotSupportedException ex)
	    {
		return null;
	    }
    }

    /**
     * Returns a mirror image of this state.
     *
     * @return a mirror image of this state
     */

    public CantStopState mirror()
    {
	try
	    {
		CantStopState result = (CantStopState)(super.clone());

		// make result's markers the mirror image of this state's

		result.markers = (BoundedMultiset)(markers.clone());
		result.markers.removeAll();
		for (int c = getLowestRoll(); c <= getHighestRoll(); c++)
		    result.placeMarker(getHighestRoll() - c + getLowestRoll(),
				       getMarkerPosition(c));

		// result has this state's hashcode; recompute it

		result.uniqueIndex = result.computeIndex();

		return result;
	    }
	catch (CloneNotSupportedException ex)
	    {
		return null;
	    }
    }

    public DiceRoll rollDice()
    {
	DiceRoll result = new DiceRoll(numDice * NUM_DICE_GROUPS, numSides);
	result.roll();
	return result;
    }

    /**
     * Returns the highest total roll possible.
     *
     * @return the highest total roll possible
     */

    public int getHighestRoll()
    {
	return numDice * numSides;
    }

    /**
     * Returns the lowest total roll possible.
     *
     * @return the lowest total roll possible
     */

    public int getLowestRoll()
    {
	return numDice;
    }

    /**
     * Returns the middle roll.
     *
     * @return the middle roll
     */

    public int getMiddleColumn()
    {
	return (getLowestRoll() + getHighestRoll()) / 2;
    }

    /**
     * Returns the number of neutral markers.  In other words, returns the
     * number of columns that may be used during a single turn.
     *
     * @return the number of neutral markers
     */

    public int countNeutralMarkers()
    {
	return MARKERS_PER_TURN;
    }

    /**
     * Returns the number of columns in this state.
     *
     * @return the number of columns in this state
     */

    public int countColumns()
    {
	return getHighestRoll() - getLowestRoll() + 1;
    }

    /**
     * Returns the length of the given column.
     *
     * @param col a column label
     * @return the length of that column
     */

    public int getColumnLength(int col)
    {
	return columnLength[col];
    }

    /**
     * Returns the number of dice used in this state's game.  This is
     * the number of dice per group times the number of groups to
     * be made.  For the standard game this is 2*2=4.
     *
     * @return the number of dice used in the game
     */

    public int getTotalDice()
    {
	return numDice * NUM_DICE_GROUPS;
    }

    /**
     * Returns an integer code for this state.
     *
     * @return an integer code for this state
     */

    public long getIndex()
    {
	if (uniqueIndex != -1)
	    {
		return uniqueIndex;
	    }
	else
	    {
		// System.out.println("Computing index for " + this);
		long index = computeIndex();

		uniqueIndex = index;

		return index;
	    }
    }

    public long computeIndex()
    {
		long index = 0;

		for (int column = getLowestRoll(); column <= getHighestRoll(); column++)
		    index += placeValue[column] * getMarkerPosition(column);

		return index;
    }

    public long countStates()
    {
	// figure out the highest possible index...

	long highestIndex = 0;
	for (int c = getLowestRoll(); c <= getHighestRoll(); c++)
	    highestIndex += placeValue[c] * columnLength[c];

	// ...and add one

	return highestIndex + 1;
    }

    public GameState getState(long index)
    {
	CantStopState result = (CantStopState)clone();

	result.uniqueIndex = index;

	for (int column = getHighestRoll(); column >= getLowestRoll(); column--)
	    {
		if (index >= placeValue[column])
		    result.placeMarker(column, (int)(index / placeValue[column]));
		index = index % placeValue[column];
	    }
	throw new IllegalArgumentException();

	// urn result;
    }

    public int hashCode()
    {
	return (int)getIndex();
    }

    /**
     * Returns the position of the marker in the given column.
     *
     * @param column a column index
     * @return the position of the marker in that column
     */

    public int getMarkerPosition(int column)
    {
	return markers.countItem(columnToMultisetMap[column]);
    }

    /**
     * Determines if this state represents an end-of-game state.  Can't Stop
     * is over when three markers have reached the end of a column.
     *
     * @return true iff the game is over
     */

    public boolean isFinal()
    {
	return (countWonColumns() >= columnsToWin);
    }

    public int countWonColumns()
    {
	int atEnd = 0;

	for (int c = getLowestRoll(); c <= getHighestRoll(); c++)
	    if (getMarkerPosition(c) == columnLength[c])
		atEnd++;

	return atEnd;
    }

    public double getFinalValue()
    {
	return 0.0;
    }

    /**
     * Places the marker in the given column at the given position.
     *
     * @param column a column index
     * @param position a position in that column
     */

    public void placeMarker(int column, int position)
    {
	// get the corresponding element in the multiset

	int elt = columnToMultisetMap[column];

	// get how many there were

	int oldCount = markers.countItem(elt);

	// update index

	if (uniqueIndex != -1)
	    uniqueIndex += (position - oldCount) * placeValue[column];

	// remove from multiset

	markers.removeItems(elt, markers.countItem(elt));
	markers.addItems(elt, position);

	/*
	 * What was this here for?  Debugging?

	if (uniqueIndex == 0)
	    throw new IllegalArgumentException();
	*/
    }

    /**
     * Returns the state obtained by making the given legal move.
     *
     * @param move a legal move from this state
     * @return the result of making that move
     */

    public CantStopState makeMove(Multiset move)
    {
	CantStopState result = (CantStopState)clone();

	for (int c = getLowestRoll(); c <= getHighestRoll(); c++)
	    if (move.countItem(c) > 0)
		result.placeMarker(c, getMarkerPosition(c) + move.countItem(c));

	return result;
    }

    public int countNextStates()
    {
	int totalPositions = 1; // 1 to count the anchor itself

	// get the list of incomplete columns

	LinkedList columnsInPlay = new LinkedList();
	for (int c = getLowestRoll(); c <= getHighestRoll(); c++)
	    if (getMarkerPosition(c) != columnLength[c])
		columnsInPlay.add(new Integer(c));

	// iterate over all 1-, 2-, and 3- subsets of columns in play

	for (int markers = 1;
	     markers <= MARKERS_PER_TURN && markers <= columnsInPlay.size();
	     markers++)
	    {
		Iterator i = new SubsetIterator(columnsInPlay, markers);

		while (i.hasNext())
		    {
			Collection subset = (Collection)(i.next());

			// turn collection into an array of primitive ints

			int[] columnsToMove = new int[subset.size()];
			Iterator j = subset.iterator();
			for (int index = 0; index < columnsToMove.length; index++)
			    columnsToMove[index] = ((Integer)(j.next())).intValue();

			// compute the number of positions that move
			// each marker in the current set at least once

			int positionsMovingSubset = 1;
			for (int index = 0; index < columnsToMove.length; index++)
			    {
				positionsMovingSubset *= columnLength[columnsToMove[index]] - getMarkerPosition(columnsToMove[index]);
			    }

			// System.out.println(this + " " + subset + " " + positionsMovingSubset);

			totalPositions += positionsMovingSubset;
		    }
	    }

	return totalPositions;
    }

    /**
     * Returns a list of states reachable in one step from this state.
     * Can't Stop is cyclic, so any state is reachable from itself in
     * one step.  This state is not, however, included in the list
     * returned.  The list will be sorted by the total number of
     * spaces moved by the markers.
     *
     * @return a <CODE>Collection</CODE> of states reachable in one step
     * from this state
     */

    public Collection getNextStates()
    {
	// compute total number of spaces to go in all columns

	int spacesToGo = 0;
	for (int c = getLowestRoll(); c <= getHighestRoll(); c++)
	    spacesToGo += (columnLength[c] - getMarkerPosition(c));

	// create lists for holding next moves
	// with certain number of roll

	List[] nextStates = new List[spacesToGo + 1];
	for (int k = 0; k < nextStates.length; k++)
	    nextStates[k] = new LinkedList();

	// get the list of incomplete columns

	LinkedList columnsInPlay = new LinkedList();
	for (int c = getLowestRoll(); c <= getHighestRoll(); c++)
	    if (getMarkerPosition(c) != columnLength[c])
		columnsInPlay.add(new Integer(c));

	// iterate over all 1-, 2-, and 3- subsets of columns in play

	for (int markers = 1;
	     markers <= MARKERS_PER_TURN && markers <= columnsInPlay.size();
	     markers++)
	    {
		Iterator i = new SubsetIterator(columnsInPlay, markers);

		while (i.hasNext())
		    {
			Collection subset = (Collection)(i.next());

			// turn collection into an array of primitive ints

			int[] columnsToMove = new int[subset.size()];
			Iterator j = subset.iterator();
			for (int index = 0; index < columnsToMove.length; index++)
			    columnsToMove[index] = ((Integer)(j.next())).intValue();

			// prepare array of number of maximum spaces to move in
			// each column; this will be one less than
			// the actual max; it is assumed each selected marker
			// moves at least one space

			int[] maxToMove = new int[columnsToMove.length];
			for (int c = 0; c < maxToMove.length; c++)
			    maxToMove[c] = columnLength[columnsToMove[c]] - getMarkerPosition(columnsToMove[c]) - 1;

			// get total number of spaces that can be moved

			int maxTotalMoved = 0;
			for (int c = 0; c < maxToMove.length; c++)
			    maxTotalMoved += maxToMove[c];

			for (int totalMoved = 0;
			     totalMoved <= maxTotalMoved;
			     totalMoved++)
			    {
				BoundedMultiset spacesMoved
				    = new BoundedMultiset(maxToMove, totalMoved);
				boolean done = false;

				do
				    {
					// prepare copy of this state...

					CantStopState next = (CantStopState)clone();

					// ...and move its markers in
					// the columns in
					// columnsToMove the amounts
					// given by the spacesMoved

					for (int c = 0; c < columnsToMove.length; c++)
					    {
						next.placeMarker(columnsToMove[c], getMarkerPosition(columnsToMove[c]) + spacesMoved.countItem(c) + 1);

					    }


					nextStates[totalMoved + columnsToMove.length].add(next);

					if (!spacesMoved.hasNext())
					    done = true;
					else
					    spacesMoved.goNext();
				    }
				while (!done);
			    }
		    }
	    }

	// concatenate all 1-space, 2-space, etc next moves into
	// one list
	// NOTE: we could do this with a list of lists to save the time
	// of concatenating all the lists together -- although since
	// we're going to have to iterate over all the next states
	// several times anyway, one extra traversal to concatenate
	// shouldn't make a big difference

	List result = new LinkedList();
	for (int k = 0; k < nextStates.length; k++)
	    result.addAll(nextStates[k]);

	return result;
    }

    /**
     * Returns a map of each possible roll to the different ways of grouping
     * the dice in that roll.  The map will be from <CODE>DiceRoll</CODE>s
     * to <CODE>Set</CODE>s of <CODE>Multiset</CODE>s, where each multiset
     * gives the number of each total you can get, so for the roll 1 2 2 3
     * the set would be {{3, 5}, {4, 4}}.
     *
     * @return a map of each roll to the ways to group it
     */

    public Map getRollList()
    {
	return getRollList(false);
    }

    /**
     * Returns a map of each possible roll to the different ways of grouping
     * the dice in that roll.  The map will be from <CODE>DiceRoll</CODE>s
     * to <CODE>Set</CODE>s of <CODE>Multiset</CODE>s, where each multiset
     * gives the number of each total you can get, so for the roll 1 2 2 3
     * the set would be {{3, 5}, {4, 4}}.  The sets can optionally include
     * non-empty subsets of the possible groupings; in this case the set for
     * 1 2 2 3 would be {{3}, {4}, {5}, {3, 5}, {4, 4}}.
     *
     * @param includeSubsets true to include subsets
     * @return a map of each roll to the ways to group it
     */

    public Map getRollList(boolean includeSubsets)
    {
	Map result = new HashMap();

	DiceRoll roll = new DiceRoll(numDice * NUM_DICE_GROUPS, numSides);

	result.put(roll, getPossibleGroupings(roll, numDice, includeSubsets));

	while (roll.hasNext())
	    {
		roll = roll.next();

		result.put(roll, getPossibleGroupings(roll, numDice, includeSubsets));
	    }

	return result;
    }

    /**
     * Returns a set of multisets giving the different ways to group
     * the given dice.
     *
     * @param roll a dice roll
     * @param groupSize a positive divisor of the number of dice
     * @return a set of groupings of the given dice
     */

    private static Set getPossibleGroupings(DiceRoll roll, int groupSize)
    {
	return getPossibleGroupings(roll, groupSize, false);
    }

    /**
     * Returns a set of multisets giving the different ways to group
     * the given dice.
     *
     * @param roll a dice roll
     * @param groupSize a positive divisor of the number of dice
     * @return a set of groupings of the given dice
     */

    private static Set getPossibleGroupings(DiceRoll roll, int groupSize, boolean includeSubsets)
    {
	if (roll.size() == groupSize)
	    {
		Set result = new TreeSet();
		Multiset total = new Multiset(roll.size() * roll.countSides());
		total.addItem(roll.total());
		result.add(total);

		return result;
	    }
	else
	    {
		Set totals = new TreeSet();

		// iterate over groupSize groupings of roll

		Iterator i = roll.subrollIterator(groupSize);
		while (i.hasNext())
		    {
			DiceRoll group = (DiceRoll)(i.next());

			// remove group from roll

			DiceRoll diff = roll.difference(group);

			// get groupings of roll

			Set subtotals = getPossibleGroupings(diff, groupSize);

			// combine with group

			Iterator j = subtotals.iterator();
			while (j.hasNext())
			    {
				Multiset s = (Multiset)(j.next());

				totals.add(s.plus(group.total()));

				if (includeSubsets)
				    totals.add(s.clone());
			    }
		    }

		return totals;
	    }
    }

    /**
     * Determines if moving the given markers is legal, assuming they have been moved
     * to the new positions indicated.
     *
     * @param markers a state reachable in one turn
     * @param moves a set of columns to move markers in
     */

    public boolean isLegalMove(CantStopState markers, Multiset moves)
    {
	int movedMarkers = 0;
	int newlyMovedMarkers = 0;

	for (int c = getLowestRoll(); c <= getHighestRoll(); c++)
	    {
		if (getMarkerPosition(c) != markers.getMarkerPosition(c))
		    movedMarkers++;
		else if (moves.countItem(c) != 0)
		    newlyMovedMarkers++;

		if (moves.countItem(c) + markers.getMarkerPosition(c) > columnLength[c])
		    return false;
	    }

	return (movedMarkers + newlyMovedMarkers) <= MARKERS_PER_TURN;
    }

    private static class StronglyConnectedComponent
    {
	/**
	 * The anchor of this SCC.
	 */

	private CantStopState anchor;

	/**
	 * An array containing all the states in this SCC, and a map
	 * from states to the indices where they are stored in the array.
	 * The anchor position will be the first entry in this array.
	 */

	private CantStopState[] states;
	private Map< CantStopState, Integer > statesIndices;

	/**
	 * A list of rolls and their probabilities.
	 */

	private Pair< DiceRoll, Double >[] rolls;

	/**
	 * An array of lists of legal moves for each pair (roll, state)
	 * where roll is a roll and state is a state in this SCC,
	 * and an array that, for each state in this SCC, gives the
	 * probability of blowing it when rolling from that state.
	 */

	private List< Integer >[][] legalMoves;
	double[] pBlowingIt;

	/**
	 * For each state in this SCC, its "stop value" (the value of
	 * the corresponding anchor), and its current estimated value.
	 */

	double[] stopValues;
	double[] estimatedValues;

	public StronglyConnectedComponent(CantStopState a, StateValueMap values)
	{
	    anchor = a;

	    makeStateArray();

	    makeRollArray();

	    makeLegalMovesArray();

	    makeValuesArrays(values);
	}

	private void makeStateArray()
	{
	    Collection< CantStopState > scc = (Collection< CantStopState >)(anchor.getNextStates());
	    states = new CantStopState[scc.size() + 1];
	    statesIndices = new HashMap< CantStopState, Integer >();

	    int i = 0;

	    // put anchor in first position in the array

	    states[i] = anchor;
	    statesIndices.put(anchor, i);
	    i++;

	    // put all other states in the array

	    for (CantStopState s : scc)
		{
		    states[i] = s;
		    statesIndices.put(s, i);
		    i++;
		}
	}

	private void makeRollArray()
	{
	    rolls = new Pair[anchor.rollList.size()];

	    Iterator i = anchor.rollList.keySet().iterator();
	    int index = 0;
	    while (i.hasNext())
		{
		    DiceRoll r = (DiceRoll)(i.next());

		    rolls[index] = new Pair< DiceRoll, Double >(r, r.probability());

		    index++;
		}
	}

	private void makeLegalMovesArray()
	{
	    legalMoves = new List[rolls.length][states.length];
	    pBlowingIt = new double[states.length];

	    for (int rIndex = 0; rIndex < rolls.length; rIndex++)
		for (int sIndex = 0; sIndex < states.length; sIndex++)
		    {
			legalMoves[rIndex][sIndex] = new ArrayList< Integer >();

			Set moves = (Set)(anchor.rollList.get(rolls[rIndex].getFirst()));

			List< Multiset > l = (List< Multiset >)(anchor.getLegalMoves(states[sIndex], moves));

			if (l.size() == 0)
			    pBlowingIt[sIndex] += rolls[rIndex].getSecond();

			// convert list of moves (multisets) to
			// list of indices of resulting states

			for (Multiset m : l)
			    {
				legalMoves[rIndex][sIndex].add(statesIndices.get(states[sIndex].makeMove(m)));
			    }
		    }
	}

	private void makeValuesArrays(StateValueMap values)
	{
	    stopValues = new double[states.length];
	    estimatedValues = new double[states.length];

	    for (int sIndex = 0; sIndex < states.length; sIndex++)
		{
		    stopValues[sIndex] = values.getValue(states[sIndex]);
		}
	}

	private void computeEstimates(double anchorEstimate)
	{
	    // go over states from farthest from anchor to anchor

	    for (int sIndex = states.length - 1; sIndex >= 0; sIndex--)
		{
		    // if blowing it, return to anchor

		    estimatedValues[sIndex] = pBlowingIt[sIndex] * anchorEstimate;

		    // go over all rolls

		    for (int rIndex = 0; rIndex < rolls.length; rIndex++)
			{
			    if (legalMoves[rIndex][sIndex].size() > 0)
				{
				    // figure out move that minimizes value

				    double bestValue = Double.POSITIVE_INFINITY;

				    for (Integer next : legalMoves[rIndex][sIndex])
					{
					    bestValue = Math.min(bestValue, estimatedValues[next]);
					}

				    estimatedValues[sIndex] += bestValue * rolls[rIndex].getSecond();
				}

			    // consider stopping at the current position
			    // (except for anchor)

			    if (sIndex != 0)
				estimatedValues[sIndex] = Math.min(estimatedValues[sIndex], stopValues[sIndex]);
			}
		}

	    // add 1 for anchor state to represent the cost of starting a turn

	    estimatedValues[0] += 1.0;
	}

	private double solve(double initialEstimate)
	{
	    final int MAX_ITERATIONS = 30;
	    int iterations = 0;

	    List< Pair< Double, Double > > steps = new ArrayList< Pair< Double, Double> >(MAX_ITERATIONS);

	    Set< Double > visited = new HashSet< Double >();

	    // we keep going until we reach a fixed point (x, x) or we
	    // go through the same x twice in a row (x, y) (x, y)

	    while (steps.size() == 0
		   || (steps.get(steps.size() - 1).getFirst().doubleValue() != steps.get(steps.size() - 1).getSecond().doubleValue()
		       && (steps.size() < 2 || steps.get(steps.size() - 1).getFirst().doubleValue() != steps.get(steps.size() - 2).getFirst().doubleValue())
		       && !visited.contains(steps.get(steps.size() - 1).getFirst())))
		{
		    if (steps.size() > 0)
			visited.add(steps.get(steps.size() - 1).getFirst());

		    double nextX;

		    if (steps.size() == 0)
			nextX = initialEstimate;
		    else if (steps.size() == 1)
			nextX = steps.get(steps.size() - 1).getSecond();
		    else
			{
			    double x1 = steps.get(steps.size() - 2).getFirst();
			    double y1 = steps.get(steps.size() - 2).getSecond();
			    double x2 = steps.get(steps.size() - 1).getFirst();
			    double y2 = steps.get(steps.size() - 1).getSecond();

			    double slope = (y2 - y1) / (x2 - x1);
			    double intercept = y1 - slope * x1;

			    if (y1 != y2 && slope != 1.0)
				nextX = intercept / (1 - slope);
			    else
				nextX = y1;
			}

		    // compute new value from nextX

		    computeEstimates(nextX);

		    steps.add(new Pair< Double, Double >(nextX, estimatedValues[0]));
		    System.err.println("> " + nextX);
		    iterations++;
		}

	    System.out.println(anchor + ": " + iterations + " iterations");

	    return estimatedValues[0];
	}

	public String toString()
	{
	    StringBuffer result = new StringBuffer();

	    result.append("ANCHOR = " + anchor);
	    result.append("\n\n");

	    result.append("STATES\n");
	    for (int sIndex = 0; sIndex < states.length; sIndex++)
		{
		    result.append(states[sIndex]);
		    result.append("\n");
		}
	    result.append("\n");

	    result.append("LEGAL MOVES\n");
	    for (int sIndex = 0; sIndex < states.length; sIndex++)
		{
		    result.append(states[sIndex]);
		    result.append("\n");

		    for (int rIndex = 0; rIndex < rolls.length; rIndex++)
			{
			    result.append("\t");
			    result.append(rolls[rIndex].getFirst());
			    result.append(":");

			    for (int k = 0; k < legalMoves[rIndex][sIndex].size(); k++)
				{
				    result.append(" ");
				    result.append(states[legalMoves[rIndex][sIndex].get(k)]);
				}
			    result.append("\n");
			}
		}

	    return result.toString();
	}
    }

    /**
     * Computes the expected number of turns left when starting at this
     * turn and following the optimal strategy.
     *
     * @param values the expected values for turns left for
     * all states that succeed this one
     */

    public double computeExpectedTurns(StateValueMap values)
    {
	return (new StronglyConnectedComponent(this, values)).solve(1.0);

	/*
	return ((Double)(computeOptimalStrategy(values).get(this))).doubleValue();
	*/
    }

    public Map computeOptimalStrategy(StateValueMap values)
    {
	return computeOptimalStrategy(values, DEFAULT_VERBOSITY);
    }

    /**
     * Codes for verbosity.
     */

    public static final int VERBOSITY_SILENT = 0;
    public static final int VERBOSITY_VALUES = 1;
    public static final int VERBOSITY_FULL = 2;
    public static final int DEFAULT_VERBOSITY = VERBOSITY_FULL;

    public Map computeOptimalStrategy(StateValueMap values, int verbosity)
    {
	final int MAX_ITERATIONS = 30;
	int iterations = 0;

	double initialEstimate = 1.0;

	List< Pair< Double, Double > > steps = new ArrayList< Pair< Double, Double> >(MAX_ITERATIONS);

	Pair< Map< CantStopState, Double >, Map< CantStopState, Pair< Boolean, Double > > > results = null;
	Set< Double > visited = new HashSet< Double >();

	// we keep going until we reach a fixed point (x, x) or we
	// go through the same x twice in a row (x, y) (x, y)

	while (steps.size() == 0
	       || (steps.get(steps.size() - 1).getFirst().doubleValue() != steps.get(steps.size() - 1).getSecond().doubleValue()
		   && (steps.size() < 2 || steps.get(steps.size() - 1).getFirst().doubleValue() != steps.get(steps.size() - 2).getFirst().doubleValue())
		   && !visited.contains(steps.get(steps.size() - 1).getFirst())))
	    {
		if (steps.size() > 0)
		    visited.add(steps.get(steps.size() - 1).getFirst());

		double nextX;

		if (steps.size() == 0)
		    nextX = initialEstimate;
		else if (steps.size() == 1)
		    nextX = steps.get(steps.size() - 1).getSecond();
		else
		    {
			double x1 = steps.get(steps.size() - 2).getFirst();
			double y1 = steps.get(steps.size() - 2).getSecond();
			double x2 = steps.get(steps.size() - 1).getFirst();
			double y2 = steps.get(steps.size() - 1).getSecond();

			double slope = (y2 - y1) / (x2 - x1);
			double intercept = y1 - slope * x1;

			if (y1 != y2 && slope != 1.0)
			    nextX = intercept / (1 - slope);
			else
			    nextX = y1;
		    }

		results = computeOptimalTurnsLeft(nextX, values);

		steps.add(new Pair< Double, Double >(nextX, results.getFirst().get(this)));

		iterations++;
		if (verbosity >= VERBOSITY_VALUES)
		    System.out.println(nextX);
	    }

	double value = steps.get(steps.size() - 1).getSecond();

	if (verbosity >= VERBOSITY_VALUES)
	    System.out.println(this + ": " + iterations + " iterations");

	// output report on stop vs. roll

	Map< CantStopState, Double > intermediateValues = results.getFirst();
	Map< CantStopState, Pair< Boolean, Double > > rollOrStop = results.getSecond();

	if (verbosity >= VERBOSITY_FULL)
	    {
		for (Map.Entry< CantStopState, Pair< Boolean, Double > > e : rollOrStop.entrySet())
		    {
			if (!e.getKey().isFinal())
			    {
				System.out.print("ROLL_OR_STOP: " + this + ":" + e.getKey());

				if (e.getValue().getFirst())
				    {
					System.out.print(":STOP:");
				    }
				else
				    {
					System.out.print(":ROLL:");
				    }

				System.out.println(spaceDifference(e.getKey())
						   + ":"
						   + (value - values.getValue(e.getKey()))
						   + ":"
						   + e.getValue().getSecond());
			    }
		    }
	    }

	return intermediateValues;
    }

    private Pair< Map< CantStopState, Double >, Map< CantStopState, Pair< Boolean, Double > > > computeOptimalTurnsLeft(double estimate, StateValueMap values)
    {
	List nextStates = (List)getNextStates();

	// an array of map from rolls to legal moves; one entry for each
	// substate (next state)

	Map[] legalMoves = makeLegalMovesMap(nextStates);

	Map initialMoves = makeLegalMoveMap(this);

	/* print states and moves for debugging
	Iterator i = nextStates.iterator();
	int k = 0;
	while (i.hasNext())
	    {
		Object o = i.next();
		System.out.println(o);
		System.out.println(legalMoves[k]);
		k++;
	    }
	*/

	Map< CantStopState, Double > intermediateValues = new HashMap< CantStopState, Double >();
	Map< CantStopState, Pair< Boolean, Double > > rollOrStop = new HashMap< CantStopState, Pair< Boolean, Double > >();

	ListIterator intermediateStateIterator = nextStates.listIterator(nextStates.size());
	int intermediateStateIndex = nextStates.size();
	while (intermediateStateIterator.hasPrevious())
	    {
		CantStopState intermediateState
		    = (CantStopState)(intermediateStateIterator.previous());
		intermediateStateIndex--;

		// compute the value of rolling the dice from the
		// current intermediate state

		Pair< Double, Double > stats = computeRollValue(intermediateState,
								legalMoves[intermediateStateIndex],
								new StateValueMapAdapter(intermediateValues),
								estimate);

		double stateValue = stats.getFirst();

		// now consider ending the turn at the intermediate state


		double stopValue = values.getValue(intermediateState);

		if (stateValue > stopValue)
		    {
			stateValue = stopValue;

			rollOrStop.put(intermediateState, new Pair< Boolean, Double >(true, stats.getSecond()));
		    }
		else
		    {
			rollOrStop.put(intermediateState, new Pair< Boolean, Double >(false, stats.getSecond()));
		    }

		// save the value for use by states that can reach this one

		intermediateValues.put(intermediateState, new Double(stateValue));
	    }

	// now compute the value of the start state for this turn

	double startValue = 1 + computeRollValue(this,
						 initialMoves,
						 new StateValueMapAdapter(intermediateValues),
						 estimate).getFirst();

	intermediateValues.put(this, new Double(startValue));
	return new Pair< Map< CantStopState, Double >, Map< CantStopState, Pair< Boolean, Double > > >(intermediateValues, rollOrStop);
    }

    /**
     * Determines the number of spaces moved from this space to the given state.
     *
     * @param next a successor of this state
     * @return the total number of spaces moved between this state and <CODE>next</CODE>
     */

    public int spaceDifference(CantStopState next)
    {
	int total = 0;

	for (int col = getLowestRoll(); col <= getHighestRoll(); col++)
	    total += (next.getMarkerPosition(col) - getMarkerPosition(col));

	return total;
    }

    /**
     * Computes the expected number of turns left when starting
     * at the given intermediate state, given that the decision has
     * been made to roll the dice again.
     *
     * @param intermediate an intermediate state
     * @param a map from rolls to legal moves for that state
     * @param intermediateValues a map from intermediate states
     * (at least including those reachable in one roll from the
     * given state) to their expected number of turns left
     * @param startValue the (possibly estimated) expected value
     * for the state the given intermediate state started from
     * @return the expected number of turns required to win,
     * assuming the decision to roll again has been made, and
     * the probability of blowing it on the next roll, as an
     * ordered pair in that order.
     */



    public Pair< Double, Double> computeRollValue(CantStopState intermediate,
				   Map rollMap,
				   StateValueMap intermediateValues,
				   double startValue)
    {
	return computeRollValue(intermediate, rollMap, intermediateValues, startValue, false);
    }

    public Pair< Double, Double > computeRollValue(CantStopState intermediate,
						 Map rollMap,
						 StateValueMap intermediateValues,
						 double startValue, boolean debug)
    {
	if (debug) System.out.println("computeRollValue: " + intermediate);
	double stateValue = 0.0;

	// so we can try to find rules about when to roll vs. when to stop
	// based on forward progress vs. P(blowing it)

	double pBlowingIt = 0.0;

	Iterator rollMapIterator = rollMap.entrySet().iterator();
	while (rollMapIterator.hasNext())
	    {
		Map.Entry e = (Map.Entry)(rollMapIterator.next());
		DiceRoll roll = (DiceRoll)(e.getKey());
		Collection moves = (Collection)(e.getValue());

		if (debug) System.out.println("moves for " + roll + " are " + moves);

		double rollValue;

		if (moves.size() == 0)
		    {
			// if no moves for roll, go back to start state

			rollValue = startValue;
			pBlowingIt += roll.probability();
		    }
		else
		    {
			// minimize value over states reachable from this one
			// given the current roll

			rollValue = Double.POSITIVE_INFINITY;

			Iterator moveIterator = moves.iterator();
			while (moveIterator.hasNext())
			    {
				Multiset move = (Multiset)(moveIterator.next());
				CantStopState moveTo = intermediate.makeMove(move);
				double moveToValue = intermediateValues.getValue(moveTo);
				rollValue = Math.min(rollValue, moveToValue);
			    }
		    }

		if (debug) System.out.println(roll.probability() + " " + rollValue);

		stateValue += roll.probability() * rollValue;
	    }

	return new Pair< Double, Double >(stateValue, pBlowingIt);
    }

    /**
     * Makes a map from all of the states in the given collection to
     * the legal moves from that state.
     */

    public Map[] makeLegalMovesMap(Collection nextStates)
    {
	Map[] legalMoves = new Map[nextStates.size()];

	// for each successor state, we make a new map from rolls to legal moves
	// by removing the illegal moves from the rollList map

	int nextStatesIndex = 0;
	Iterator nextStatesIterator = nextStates.iterator();
	while (nextStatesIterator.hasNext())
	    {
		CantStopState next = (CantStopState)(nextStatesIterator.next());
		Map movesMap = makeLegalMoveMap(next);

		legalMoves[nextStatesIndex] = movesMap;
		nextStatesIndex++;
	    }

	return legalMoves;
    }

    /**
     * Returns a map from rolls to sets of legal moves for a turn
     * that starts at this state and has reached the given
     * intermediate state.
     */

    public Map makeLegalMoveMap(CantStopState next)
    {
	Map result = new HashMap();

	Iterator rolls = rollList.entrySet().iterator();
	while (rolls.hasNext())
	    {
		Map.Entry e = (Map.Entry)(rolls.next());
		DiceRoll roll = (DiceRoll)(e.getKey());
		Set moves = (Set)(e.getValue());

		result.put(roll, getLegalMoves(next, moves));
	    }

	return result;
    }

    /**
     * Returns the list of legal moves in the given situation.
     *
     * @param next the position of the neutral markers
     * @param roll a roll
     * @return the list of legal moves
     */

    public List getLegalMoves(CantStopState next, DiceRoll roll)
    {
	// get all moves possible with the given roll

	Set moves = (Set)(rollList.get(roll));

	// determine which are legal

	return getLegalMoves(next, moves);
    }

    /**
     * Determines which of a given set of moves is legal in the
     * current state witht the given progress.
     *
     * @param next a state marking progress from this state
     * @param moves a set of possible moves, including illegal ones
     * @return a list of legal moves
     */

    private List getLegalMoves(CantStopState next, Set moves)
    {
	// make a new list from moves omitting illegal ones

	List legal = new LinkedList();
	Iterator moveIterator = moves.iterator();
	while (moveIterator.hasNext())
	    {
		// I want this to only include the full moves; if
		// a given move is illegal, I will then check the
		// submoves myself.

		Multiset move = (Multiset)(moveIterator.next());
		if (isLegalMove(next, move))
		    legal.add(move);
		else
		    {
			// get all the subsets of the current move;
			// check if they are legal

			// here I am assuming that the move is
			// two columns, so we're going to look at the
			// individual columns by themselves

			Iterator it = move.iterator();

			int firstColumn = ((Integer)(it.next())).intValue();
			int secondColumn = ((Integer)(it.next())).intValue();

			Multiset firstColumnMove = (Multiset)(move.clone());
			firstColumnMove.removeAll();
			firstColumnMove.addItem(firstColumn);

			if (isLegalMove(next, firstColumnMove))
			    legal.add(firstColumnMove);

			if (secondColumn != firstColumn)
			    {
				Multiset secondColumnMove = (Multiset)(move.clone());
				secondColumnMove.removeAll();
				secondColumnMove.addItem(secondColumn);

				if (isLegalMove(next, secondColumnMove))
				    legal.add(secondColumnMove);
			    }
		    }
	    }

	return legal;
    }

    /**
     * Returns a list of the states reachable in a single roll
     * from this state.
     */

    private List getSingleRollMoves()
    {
	Set nextMoves = new TreeSet();

	Iterator rollIterator = rollList.entrySet().iterator();
	while (rollIterator.hasNext())
	    {
		Map.Entry e = (Map.Entry)(rollIterator.next());

		Iterator legalMoveIterator = getLegalMoves(this, (Set)(e.getValue())).iterator();
		while (legalMoveIterator.hasNext())
		    nextMoves.add(makeMove((Multiset)(legalMoveIterator.next())));
	    }

	return new LinkedList(nextMoves);
    }

    public int compareTo(Object o)
    {
	return toString().compareTo(o.toString());
    }

    public boolean equals(Object o)
    {
	return (o instanceof CantStopState
		&& ((CantStopState)o).getIndex() == getIndex());
    }

    public String toString()
    {
	StringBuffer result = new StringBuffer("[");

	result.append(getMarkerPosition(getLowestRoll()));

	for (int column = getLowestRoll() + 1; column <= getHighestRoll(); column++)
	    {
		result.append(" " + getMarkerPosition(column));
	    }

	result.append(']');

	return result.toString();
    }

    public int getTotalSpaces()
    {
	int total = 0;

	for (int c = getLowestRoll(); c <= getHighestRoll(); c++)
	    total += columnLength[c];

	return total;
    }

    public Iterator iterator(int spacesMoved)
    {
	return new CantStopIterator(this, spacesMoved);
    }

    private static class CantStopIterator implements Iterator
    {
	private CantStopState nextState;

	private CantStopIterator(CantStopState rep, int spacesMoved)
	{
	    nextState = (CantStopState)(rep.clone());

	    int[] bounds = new int[rep.getHighestRoll() - rep.getLowestRoll() + 1];
	    for (int i = 0; i < bounds.length; i++)
		bounds[i] = rep.columnLength[rep.multisetToColumnMap[i]];

	    nextState.markers = new BoundedMultiset(bounds, spacesMoved);
	    nextState.uniqueIndex = -1;
	}

	public boolean hasNext()
	{
	    return nextState != null;
	}

	public Object next()
	{
	    CantStopState result = (CantStopState)(nextState.clone());

	    if (nextState.markers.hasNext())
		{
		    nextState.markers.goNext();
		    nextState.uniqueIndex = -1;
		}
	    else
		nextState = null;

	    return result;
	}

	public void remove()
	{
	    throw new UnsupportedOperationException();
	}
    }


    public static void main(String[] args)
    {
	/*
	CantStopState s = new CantStopState(Integer.parseInt(args[0]),
					    Integer.parseInt(args[1]),
					    Integer.parseInt(args[2]));

	for (int i = 2; i < s.columnLength.length; i++)
	    System.out.print(s.columnLength[i] + " ");
	System.out.println();

	for (int i = 2; i < s.columnToMultisetMap.length; i++)
	    System.out.print(s.columnToMultisetMap[i] + " ");
	System.out.println();

	for (int i = 2; i < s.placeValue.length; i++)
	    System.out.print(s.placeValue[i] + " ");
	System.out.println();

	for (int i = 0; i < s.multisetToColumnMap.length; i++)
	    System.out.print(s.multisetToColumnMap[i] + " ");
	System.out.println();

	for (int index = 0; index < 100; index++)
	    System.out.println(new CantStopState(index) + " " + (new CantStopState(index)).getIndex());
	*/

	/*
	CantStopState s = new CantStopState(3, 1);
	System.out.println(s.getNextStates());
	s.computeOptimalTurnsLeft(0.0, null);
	*/

	/*
	CantStopState s = new CantStopState(args[0]);
	System.out.println(s.getNextStates());
	*/

	/*
	CantStopState s = new CantStopState(args[0]);
	System.out.println(s.getSingleRollMoves());
	*/

	/*
	System.out.println(getPossibleGroupings(DiceRoll.parseRoll(args[0], 6),
						Integer.parseInt(args[1])));
	*/

	/*
	CantStopState s = new CantStopState(Integer.parseInt(args[0]),
					    Integer.parseInt(args[1]));
	Iterator i = s.getRollList(false).entrySet().iterator();
	while (i.hasNext())
	    {
		Map.Entry e = (Map.Entry)(i.next());
		System.out.println(e.getKey());
		System.out.println(e.getValue());
		System.out.println();
	}
	*/

	/*
	CantStopState s = new CantStopState(Integer.parseInt(args[0]),
					    Integer.parseInt(args[1]));
	for (int layer = s.getTotalSpaces(); layer >= 0; layer--)
	    {
		System.out.println("LAYER " + layer);
		Iterator i = s.iterator(layer);
		while (i.hasNext())
		    System.out.println(i.next());
		System.out.println();
	    }
	*/

	/*
	DiceRoll roll = new DiceRoll(3, "1111");
	System.out.println(getPossibleGroupings(roll, 2, true));
	*/

	/*
	CantStopState start = new CantStopState(3, 1);
	CantStopState curr = (CantStopState)(start.clone());
	curr.placeMarker(2, 1);
	curr.placeMarker(3, 3);
	Multiset moves = new Multiset(6);
	moves.addItem(5);
	moves.addItem(6);
	System.out.println(start.isLegalMove(curr, moves));
	*/

	/*
	CantStopState s = new CantStopState(3, 1);
	CantStopState test = new CantStopState(3, 1);
	test.placeMarker(4, 5);
	test.placeMarker(5, 3);
	System.out.println("test = " + test);
	System.out.println("mirror = " + test.mirror());
	// System.out.println(s.makeLegalMoveMap(test));
	*/

	/*
	 * For each state, finds the states in the corresponding SCC.
	 * Reports the total size and average size of the SCCs.
	 */

	CantStopState s = new CantStopState(Integer.parseInt(args[0]),
					    Integer.parseInt(args[1]));

	long totalNeighbors = 0;
	long totalStates = 0;

	for (int layer = 0; layer <= s.getTotalSpaces(); layer++)
	    {
		Iterator i = s.iterator(layer);

		while (i.hasNext())
		    {
			CantStopState state = (CantStopState)(i.next());

			if (!state.isFinal())
			    {
				// Collection neighbors = state.getNextStates();
				// totalNeighbors += neighbors.size();
				int neighbors = state.countNextStates();
				totalNeighbors += neighbors;
				totalStates++;

				System.out.println(state + " " + neighbors);
			    }
		    }
	    }

	System.out.println("Non-final states  : " + totalStates);
	System.out.println("Total size of SCCs: " + totalNeighbors);
	System.out.println("Mean size of SCCs : " + (double)totalNeighbors / totalStates);

	/*
	CantStopState s = new CantStopState(args[0]);
	System.out.println(s);
	System.out.println(new StronglyConnectedComponent(s));
	*/
    }

    public static String formatMove(Multiset move)
    {
	StringBuffer result = new StringBuffer();

	for (int pairTot = 2; pairTot <= move.range(); pairTot++)
	    {
		if (move.countItem(pairTot) > 0)
		    {
			for (int i = 0; i < move.countItem(pairTot); i++)
			    {
				if (result.length() > 0)
				    result.append('/');
				result.append(pairTot);
			    }
		    }
	    }

	return "[" + result.toString() + "]";
    }
}
