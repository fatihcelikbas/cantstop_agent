package com.bloxomo.gametheory.cantstop;

import com.bloxomo.gametheory.*;
import java.io.*;
import java.util.*;
import com.sirmapsalot.combinatorics.*;

import py4j.GatewayServer;

public class GymCantStop {
  private MPCantStopGame g;
  private CantStopStrategy bot;
  private SimpleMPCantStopState start;
  private SimpleMPCantStopState state;
  private DiceRoll roll;
  private boolean done;

  // game params
  private int players;
  private int sides;
  private int len;
  private int delta;
  private int toWin;


  public GymCantStop() {
    players = 2;
    sides = MPCantStopGame.DEFAULT_SIDES;
    len = MPCantStopGame.DEFAULT_SHORTEST_COLUMN;
    delta = MPCantStopGame.DEFAULT_COLUMN_DIFFERENCE;
    toWin = MPCantStopGame.DEFAULT_COLUMNS_TO_WIN;


    g = new MPCantStopGame(players, sides, len, delta, toWin);
    CantStopState singlePlayerGame = new CantStopState(sides, len, toWin);
    bot = new RuleOfN(singlePlayerGame, 28);
    done = false;
  }

  public void init() {
    start = new SimpleMPCantStopState(g);
    state = start;

    roll = new DiceRoll(4, g.countSides());
    roll.roll();
  }

  public void step(List<Integer> list, int stop) {

    int[] counts = new int[list.size()];
    for(int i = 0; i < counts.length; i++) {
      counts[i] = list.get(i);
    }


    CantStopState onePlayerStart = convertToSinglePlayer(g, start, 0);

    Multiset move = new Multiset(counts);

    if(onePlayerStart.isLegalMove(convertToSinglePlayer(g, state, 0), move)) {

      state = state.getNextState(convertToGrouping(move, roll));

      if (state.canStop(0, start) && stop != 0) { // if need to stop
        state = state.endTurn();
        start = state;
      }
      else {
        roll.roll(); // else just roll and leave everything as is
        return;
      }
    }
    else { // if the agent produces an unintelligible result
      state = state.endTurn();
      start = state;
    }


    // now it's the bot's turn to play
    if (!start.isFinal()) {
      onePlayerStart = convertToSinglePlayer(g, start, 1);

      do
          {

        // roll the dice

        DiceRoll roll = new DiceRoll(4, g.countSides());
        roll.roll();

        // get current player's move

        move = bot.pickPairs(onePlayerStart, convertToSinglePlayer(g, state, 1), roll);

        if (move != null)
            {

          // convert move to grouping

          state = state.getNextState(convertToGrouping(move, roll));

          if (state.canStop(1, start)
              && !bot.rollAgain(onePlayerStart, convertToSinglePlayer(g, state, 1)))
              {
                     state = state.endTurn();
              }
            }
        else
            {
                  state = start.endTurn();
            }
          }
      while (state.getTurn() == 1);

      start = state;

    }

    roll.roll();
    done = start.isFinal();

  }

  public long reset() {
    g = new MPCantStopGame(players, sides, len, delta, toWin);
    CantStopState singlePlayerGame = new CantStopState(sides, len, toWin);
    bot = new RuleOfN(singlePlayerGame, 28);

    start = new SimpleMPCantStopState(g);
    state = start;

    roll = new DiceRoll(4, g.countSides());
    roll.roll();

    CantStopState onePlayerState = convertToSinglePlayer(g, state, 0);
    return onePlayerState.getIndex();
  }

  public boolean isDone() {
    return done;
  }

  public long getState() {
    CantStopState onePlayerState = convertToSinglePlayer(g, state, 0);
    return onePlayerState.getIndex();
  }

  public List<Integer> getRoll() {
    int[] arr = roll.toArray();
    List<Integer> list = new ArrayList<>(arr.length);
    for (int i : arr) {
			list.add(Integer.valueOf(i));
		}

    return list;

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

  public static void main(String[] args) {
    GatewayServer gatewayServer = new GatewayServer(new GymCantStop());
    gatewayServer.start();
    System.out.println("Gateway Server Started");
  }

}
