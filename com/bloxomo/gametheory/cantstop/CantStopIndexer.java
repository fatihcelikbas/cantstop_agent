package com.bloxomo.gametheory.cantstop;

import com.bloxomo.gametheory.*;
import java.util.*;
import com.sirmapsalot.combinatorics.Multiset;

/**
 * An indexer for states in Can't Stop.  In addition to providing the following
 * methods, an indexer must index states so that
 * 1) the index of the initial state of the game is 0; and
 * 2) if state s2 is reachable from state s1 then index(s2) > index(s1)
 *
 * @author Jim Glenn
 * @version 0.1 4/24/2007
 */

public interface CantStopIndexer
{
    /**
     * Returns the index of the state reached from the state
     * with the given index in this indexing scheme and moving
     * according to the given grouping of the dice.
     *
     * @param startState the index of the start state in this scheme
     * @param move the grouping of the dice to move according to
     * @return the index of the resulting state
     */
    
    public long getNextState(long startState, Grouping move);

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
    
    public long indexAfterMoving(long state, int p, int[] advanceColumns, Multiset advanceDistance);
    
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
    
    public long changeTurn(long state, int p);
	
    /**
     * Returns the position of the given player's marker in the
     * given column in the state with the given index in this
     * indexing scheme.
     *
     * @param state a state index in this scheme
     * @param p a player index
     * @param c a column label
     */
    
    public int getMarkerPosition(long state, int p, int c);
	
    /**
     * Returns the player whose turn it is in the state with the
     * given index using this indexing scheme.
     *
     * @param i a state index in this scheme
     * @return the player whose turn it is
     */
    
    public int getTurn(long state);

    /**
     * Returns the highest possible index using this indexing scheme.
     *
     * @return the highest possible index
     */

    public long getHighestIndex();

    /**
     * Returns the highest possible index of an anchor position using
     * this indexing scheme.
     *
     * @return the highest possible anchor index
     */

    public long getHighestAnchor();

    /**
     * Converts an anchor index to a state index.
     *
     * @param anchor the index of an anchor
     * @return the index of the corresponding state
     */

    public long anchorToState(long anchor);

    /**
     * Converts a state index to an anchor index.
     *
     * @param state the index of a state that is legal at the end of a turn
     * @return the index of the corresponding anchor
     */

    public long stateToAnchor(long state);

    /**
     * Returns the index of the state with the markers in the
     * given positions and the given player's turn.
     *
     * @param markers the position of the player's markers,
     * with one row per player
     * @param turn the players whose turn it is
     * @return the index of the corresponding state
     */
    
    public long getIndex(int[][] markers, int turn);

    /**
     * Returns the number of layers this indexer partitions the states
     * into.  The exact partition is dependent on the indexing scheme
     * used by a particular implementation.  However, any scheme must
     * ensure that if anchor u is reachable from anchor v (and u and v
     * differ in more than just whose turn it is) then v is in a
     * lower-numbered layer than u.
     *
     * @return the number of layers the states in this game are partitioned
     * into
     */

    public int countLayers();

    /**
     * Returns an iterator over the indices of the anchors in the
     * given layer.
     *
     * @param layer a valid layer index (0 <= layer < countLayers)
     * @return an iterator over that layer
     */

    public Iterator< Long > layerIterator(int layer);
}

