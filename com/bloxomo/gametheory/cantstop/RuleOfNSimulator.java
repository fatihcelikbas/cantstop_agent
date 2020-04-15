package com.bloxomo.gametheory.cantstop;

import com.bloxomo.gametheory.*;
import java.io.*;
import java.util.*;
import com.sirmapsalot.combinatorics.*;

public class RuleOfNSimulator
{
    public static void main(String[] args)
    {
	int[][] pairs = {
	    {2, 1, 19}, {2, 2, 29}, {2, 3, 26},
	    {3, 1, 26}, {3, 2, 25}, {3, 3, 30},
	    {4, 1, 30}, {4, 2, 35}, {4, 3, 40},
	    {5, 1, 26}, {5, 2, 28}, {5, 3, 27},
	    {6, 1, 27}, {6, 2, 28}, {6, 3, 28}
	};

	HashMap< String, Integer > optimal = new HashMap< String, Integer >();
	for (int i = 0; i < pairs.length; i++)
	    {
		optimal.put("" + pairs[i][0] + pairs[i][1], pairs[i][2]);
	    }

	for (int sides = 6; sides >= 2; sides--)
	    for (int shortestColumn = 3; shortestColumn >= 1; shortestColumn--)
		{
		    System.out.println("=== SIDES " + sides + " LENGTH " + shortestColumn + " ===");
		    int opt = optimal.get("" + sides + shortestColumn);

		    for (int thresh = opt + 0; thresh <= opt - 0; thresh++)
			{
			    
			    CantStopState s = new CantStopState(sides, shortestColumn);
			    
			    int[] progress = new int[sides];
			    int[] choice = new int[sides];
			    for (int i = 0; i < choice.length; i++)
				{
				    progress[i] = sides - i;
				    choice[i] = i + 1;
				}
			    
			    CantStopStrategy strat = new RuleOfN(s,
								 progress,
								 choice,
								 2, -2, 4, 4, 6,
								 thresh);
			    System.out.println(thresh + ":" + CantStopSimulator.simulateGames(s, strat, 250000, false));
			}
		}
    }
}
