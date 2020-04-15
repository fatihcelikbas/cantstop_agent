package com.bloxomo.gametheory;

import java.util.*;

/**
 * A state of a finite, acyclic game.
 *
 * @author Jim Glenn
 * @version 0.1 9/4/2003
 */

public abstract class GameState implements Comparable
{
    /**
     * Returns the number of total number of states in the same
     * game as this state.  The total includes states that may not be
     * reachable; it should be one more than the largest state index.
     *
     * @return the number of states in the same game as this state
     */

    public abstract long countStates();

    /**
     * Returns the numberic index for this state.  The indices must be such
     * that if state q is reachable from state p then q's index is greater
     * than p's index.
     *
     * @return the index of this state
     */

    public abstract long getIndex();

    /**
     * Returns the state with the given index.
     *
     * @param i a legal index for states of the same kind as this one
     */

    public abstract GameState getState(long i);

    /**
     * Returns a collection of states reachable in one step from this state.
     *
     * @return a <CODE>Collection</CODE> of states reachable in one step
     * from this state
     */

    public abstract Collection getNextStates();

    /**
     * Determines if this state is final.
     *
     * @return true iff this state is final
     */

    public abstract boolean isFinal();

    /**
     * Determines if this state is reachable from the starting state
     * of the corresponding game.  This method must return <CODE>true</CODE>
     * for any reachable state but may also return <CODE>true</CODE>
     * for unreachable states.
     *
     * @return true if this state is reachable
     */

    public boolean isReachable()
    {
	return true;
    }

    /**
     * Returns the value of this state.  This is most meaningful for
     * final states but may be used for nonfinal states as well.
     *
     * @return the value of this state
     */

    public abstract double getFinalValue();

    /**
     * Returns a <CODE>StateValueMap</CODE> for the game this state
     * is in.  This version of the method returns a
     * <CODE>StateValueArray</CODE>, which is sufficient for games
     * up to several million states.
     * If the map returned contains any states their values should be
     * initialized to zero.
     *
     * @return a map from states to values with all entries initially zero
     */

    public StateValueMap getMap()
    {
	return new StateValueArray((int)(countStates()));
    }

    /**
     * Returns the number of layers of states in this game.  A layer is
     * a set of states so that all successors of states in the set are
     * in strictly higher numbered layers.  For a game whose graph does not
     * a DAG, this method will return zero.
     *
     * @return the number of layers of states
     */

    public int countLayers()
    {
	return 0;
    }

    /**
     * Returns an iterator over the given layer of states.
     *
     * @param layer a number from zero to the number of layers minus one,
     * or zero to iterate over all the states in the case of a cyclic
     * state graph
     * @return an iterator over the reachable states in that layer
     */

    public Iterator iterator(int layer)
    {
	if (layer != 0)
	    throw new IllegalArgumentException("Illegal layer number; must be 0");

	return new StateIterator();
    }

    /**
     * An iterator over all the states in the state graph.  Only reachable
     * states are returned.
     */

    protected class StateIterator implements Iterator
    {
	/**
	 * The index of the next state to be returned by this iterator.
	 * If there is no next state, this index will be -1.
	 */

	protected long index;

	public StateIterator()
	{
	    index = countStates() - 1;

	    while (index >= 0 && !(getState(index).isReachable()))
		index--;
	}

	public boolean hasNext()
	{
	    return (index >= 0);
	}

	public Object next()
	{
	    if (index < 0)
		throw new NoSuchElementException();

	    GameState result = getState(index);

	    index--;
	    while (index >= 0 && !(getState(index).isReachable()))
		index--;

	    return result;
	}

	public void remove()
	{
	    throw new UnsupportedOperationException();
	}
    }
}

    
