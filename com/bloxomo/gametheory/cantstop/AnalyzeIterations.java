package com.bloxomo.gametheory.cantstop;

import com.bloxomo.gametheory.*;
import java.util.*;
import java.io.*;

/**
 * Reads a CantStopOptimizer log file and computes statistics
 * based on the number of iterations required.  Splits
 * are given based on the position value and distance from
 * the start state.
 */

public class AnalyzeIterations
{
    private static final int MAX_ITERATIONS = 30;
    private static final double MIN_VALUE = 1.0;
    private static final double BUCKET_WIDTH = 0.1;
    private static final int MAX_DISTANCE = 105;
    private static final int NUM_BUCKETS = 20;

    public static void main(String[] args) throws IOException
    {
	BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

	String line;

	int[][] countByDistance = new int[MAX_DISTANCE + 1][MAX_ITERATIONS + 1];
	int[][] countByValue = new int[NUM_BUCKETS][MAX_ITERATIONS + 1];

	int highestIterations = 0;
	int lowestIterations = Integer.MAX_VALUE;
	int highestDistance = 0;
	int highestBucket = 0;

	while ((line = in.readLine()) != null)
	    {
		// skip any lines reporting values like
		// [0 0 0 0 1] = 1.3806429211652174

		while (line != null && line.indexOf('=') != -1)
		    line = in.readLine();

		if (line != null)
		    {
			/*
			// now we're reading the estimates
			// for a state

			Set seen = new HashSet();
			seen.add("1.0");

			String last = "1.0";

			// estimates stop when we see a repeated value

			while (!seen.contains(line))
			    {
				seen.add(line);
				last = line;
				line = in.readLine();
			    }


			if (!last.equals(line))
			    {
				System.out.println("Cycle detected between " +
						   last + " and " + line +
						   "; difference is " +
						   Math.abs(Double.parseDouble(line) - Double.parseDouble(last)));
				
				// iterations = MAX_ITERATIONS;
				iterations = seen.size() + 1;
			    }
			else
			    iterations = seen.size() + 1;

			// skip ahead to end of iterations

			double positionValue = Double.parseDouble(line);
			*/
			
			while (line.indexOf(':') == -1)
			    line = in.readLine();
			
			// now we've got a line that looks like
			// [1 0 0 0 0]: 4 iterations
			// we want the [1 0 0 0 0] part so we
			// can get the distance from starting position
			// we also want the 4 for # of iterations

			StringTokenizer tok = new StringTokenizer(line, ":i");
			tok.nextToken();
			int iterations = Integer.parseInt(tok.nextToken().trim());

			tok = new StringTokenizer(line, "]");
			tok = new StringTokenizer(tok.nextToken().substring(1));
			
			int distance = 0;
			while (tok.hasMoreTokens())
			    distance += Integer.parseInt(tok.nextToken());
			
			countByDistance[distance][iterations]++;

			// skip lines reporting indices

			in.readLine();
			in.readLine();

			// get line with value of position
			// (form [1 2 1] = 1.1428571428571428)
			// and get position

			line = in.readLine();
			tok = new StringTokenizer(line, "=");
			tok.nextToken();
			double positionValue = Double.parseDouble(tok.nextToken());

			int bucket = (int)((positionValue - MIN_VALUE) / BUCKET_WIDTH);
			countByValue[bucket][iterations]++;
			
			if (iterations < MAX_ITERATIONS)
			    highestIterations = Math.max(iterations, highestIterations);
			lowestIterations = Math.min(lowestIterations, iterations);
			highestDistance = Math.max(highestDistance, distance);
			highestBucket = Math.max(highestBucket, bucket);
	    }
    }

	System.out.println("BY DISTANCE");

	for (int i = 0; i <= highestDistance; i++)
	    {
		System.out.print(i + " ");

		// compute total for this row

		int rowTotal = countByDistance[i][MAX_ITERATIONS];
		for (int j = lowestIterations; j <= highestIterations; j++)
		    rowTotal += countByDistance[i][j];

		System.out.print(rowTotal + " ");

		int totalIterations = 0;

		for (int j = lowestIterations; j <= highestIterations; j++)
		    {
			System.out.print(countByDistance[i][j] + " ");
			totalIterations += countByDistance[i][j] * j;
		    }
		System.out.println(countByDistance[i][MAX_ITERATIONS]
				   + " " + totalIterations / (double)rowTotal);
	    }

	System.out.println("BY VALUE");

	for (int i = 0; i <= highestBucket; i++)
	    {
		double bucketStart = MIN_VALUE + BUCKET_WIDTH * i;
		System.out.print(bucketStart + "-" + (bucketStart + BUCKET_WIDTH) + " ");

		// compute total for this row

		int rowTotal = countByValue[i][MAX_ITERATIONS];
		for (int j = lowestIterations; j <= highestIterations; j++)
		    rowTotal += countByValue[i][j];

		System.out.print(rowTotal + " ");

		int totalIterations = 0;

		for (int j = lowestIterations; j <= highestIterations; j++)
		    {
			System.out.print(countByValue[i][j] + " ");
			totalIterations += countByValue[i][j] * j;
		    }
		System.out.println(countByValue[i][MAX_ITERATIONS]
				   + " " + totalIterations / (double)rowTotal);
	    }
    }
}
