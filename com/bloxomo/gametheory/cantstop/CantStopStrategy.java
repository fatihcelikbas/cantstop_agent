package com.bloxomo.gametheory.cantstop;

import com.bloxomo.gametheory.*;
import com.sirmapsalot.combinatorics.*;

/**
 * A strategy for solitaire Can't Stop.
 */

public interface CantStopStrategy
{
    /**
     * Determines how to pair the dice after a roll.  The state is
     * given as the state at the start of the turn and the current state
     * the position of the white markers is indicated by the difference
     * in those two positions.
     *
     * @param start the state at the start of the turn
     * @param progress the current state
     * @param roll the roll
     * @return the way to split the dice, or null if no move is possible
     */

    public Multiset pickPairs(CantStopState start, CantStopState progress, DiceRoll roll);

    /**
     * Determines whether to roll again or not.  The state is
     * given as the state at the start of the turn and the current state
     * the position of the white markers is indicated by the difference
     * in those two positions.
     *
     * @param start the state at the start of the turn
     * @param progress the current state
     * @return true iff the strategy says to roll again
     */

    public boolean rollAgain(CantStopState start, CantStopState progress);
}
