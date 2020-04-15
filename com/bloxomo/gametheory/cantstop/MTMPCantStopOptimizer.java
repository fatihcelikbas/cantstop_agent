package com.bloxomo.gametheory.cantstop;

import java.util.*;
import java.io.*;
import com.bloxomo.gametheory.*;
import com.bloxomo.synchronization.*;
import com.sirmapsalot.combinatorics.*;

public class MTMPCantStopOptimizer
{
    public static void main(String[] args)
    {
	// parse command line arguments

	int players = MPCantStopGame.DEFAULT_PLAYERS;
	int sides = MPCantStopGame.DEFAULT_SIDES;
	int len = MPCantStopGame.DEFAULT_SHORTEST_COLUMN;
	int delta = MPCantStopGame.DEFAULT_COLUMN_DIFFERENCE;
	int toWin = MPCantStopGame.DEFAULT_COLUMNS_TO_WIN;

	int numThreads = 2;

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
	// parse arguments for collusion and threads

	for (int a = 0; a < args.length; a++)
	    {
		if (args[a].startsWith("--threads="))
		    {
			numThreads = Integer.parseInt(args[a].substring(args[a].indexOf("=") + 1));
		    }
		else if (args[a].startsWith("--collusion="))
		    {
			StringTokenizer tok = new StringTokenizer(args[a], "=,");
			tok.nextToken(); // skip "--collusion=" token
			g.setCollusionTarget(Integer.parseInt(tok.nextToken()),
					     Integer.parseInt(tok.nextToken()));
			
		    }
	    }

	MPPositionValueMap positionValues = g.getPositionValueMap();
	    // = new SynchronizedPositionValueMap(g.getPositionValueMap());

	Barrier b = new Barrier(numThreads + 1);

	StagedQueue q = new MTMPCantStopQueue(g, positionValues, b, numThreads);
	q.start();
	b.cross();

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

	try
	    {
		Process ps = Runtime.getRuntime().exec("ps -aF");
		BufferedReader psOutput = new BufferedReader(new InputStreamReader(ps.getInputStream()));
		String line;
		while ((line = psOutput.readLine()) != null)
		    {
			if (line.indexOf("MTCantStopOptimizer") != -1)
			    System.out.println(line);
		    }
	    }
	catch (IOException e)
	    {
	    }
    }

    private static class MTMPCantStopQueue extends StagedQueue
    {
	private MPCantStopGame game;
	private MPPositionValueMap positionValues;
	private Barrier bar;
	private static int nextID = 0;

	private MTMPCantStopQueue(MPCantStopGame g, MPPositionValueMap values, Barrier b, int n)
	{
	    super(n);

	    game = g;
	    bar = b;
	    positionValues = values;
	}

	public Thread createProducer()
	{
	    return new Producer(game);
	}

	public Thread createConsumer()
	{
	    return new Consumer(game, positionValues, bar);
	}

	private class Producer extends Thread
	{
	    private MPCantStopGame game;

	    private Producer(MPCantStopGame g)
	    {
		game = g;
	    }

	    public void run()
	    {
		for (int layer = game.countLayers() - 1; layer >= 0; layer--)
		    {
			Iterator< Long > i = game.layerIterator(layer);
			while (i.hasNext())
			    enqueue(i.next());
			endStage();
		    }
		finish();
	    }
	}

	private class Consumer extends Thread
	{
	    private MPCantStopGame game;
	    private MPPositionValueMap positionValues;
	    private Barrier bar;
	    private int id;

	    private Consumer(MPCantStopGame g, MPPositionValueMap values, Barrier b)
	    {
		game = g;
		positionValues = values;
		bar = b;
		id = nextID++;
	    }

	    public void run()
	    {
		Object o;
		while ((o = dequeue()) != null)
		    {
			long index = ((Long)o).longValue();
			
			MPCantStopGame.MPCantStopState s = game.makeAnchor(index);
			MPCantStopGame.MPCantStopState rep = s.getRepresentative();

			if (!s.isFinal()
			    && s.isLegalAnchor()
			    && s.getTurn() == 0
			    && !positionValues.hasValue(s.getIndex(), 0)
			    && !positionValues.hasValue(rep.getIndex(), 0))
			    {
				System.out.println("#" + id + ": Anchor " + s);

				// solve for position values of anchors in the
				// same SCC as s

				double[][] values = s.solve(positionValues);
				
				// now record those estimates in positionValues
				
				Set< MPCantStopGame.MPCantStopState > equivStates = s.getEquivalentStates();
			
				for (MPCantStopGame.MPCantStopState equiv : equivStates)
				    {
					for (int pointOfView = 0; pointOfView < game.countPlayers(); pointOfView++)
					    {
						for (int pTurn = 0; pTurn < game.countPlayers(); pTurn++)
						    {
							positionValues.setValue(equiv.setTurn(pTurn).getIndex(), pointOfView, values[pointOfView][pTurn]);
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
				System.out.println("Anchor " + s + " is a win for " + s.getWinner());
				
				int winner = s.getWinner();

				for (int p = 0; p < game.countPlayers(); p++)
				    {
					for (int pTurn = 0; pTurn < game.countPlayers(); pTurn++)
					    if (p == winner)
						{
						    positionValues.setValue(s.setTurn(pTurn).getIndex(), p, 1.0);
						}
					    else
						{
						    positionValues.setValue(s.setTurn(pTurn).getIndex(), p, 0.0);
						}
				    }
			    }
			else if (positionValues.hasValue(s.getIndex(), 0))
			    {
				System.out.println("Anchor " + s + " is equivalent to a previously computed state");
			    }
			else if (positionValues.hasValue(rep.getIndex(), 0))
			    {
				System.out.println("Anchor " + s + " is equivalent to a previously computed state");
				
				for (int pointOfView = 0; pointOfView < game.countPlayers(); pointOfView++)
				    {
					for (int pTurn = 0; pTurn < game.countPlayers(); pTurn++)
					    {
						double value = positionValues.getValue(rep.setTurn(pTurn).getIndex(), pointOfView);
						positionValues.setValue(s.setTurn(pTurn).getIndex(), pointOfView, value);
						positionValues.setValue(s.getSymmetricState().setTurn(pTurn).getIndex(), pointOfView, value);
					    }
				    }
			    }
		    }
		bar.cross();
	    }
	}
    }
}

    
