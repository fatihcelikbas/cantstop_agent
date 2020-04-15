package com.sirmapsalot.combinatorics;

import java.util.*;

/**
 * A multiset of elements from {0, ..., n} with a bound on the number of
 * occurrences of each element.
 *
 * @author Jim Glenn
 * @version 0.1 11/22/2005
 */

public class BoundedMultiset extends Multiset
{
    /**
     * The maximum occurrences of each element allowed in this set.
     */

    protected int[] maxOccurrences;

    /**
     * maxGreaterOrEqual[e] = sum{i=e..maxValue} maxOccurrences[i]
     */

    private int[] maxGreaterOrEqual;

    /**
     * Creates an empty multiset with the given bounds.
     *
     * @param b an array of nonnegative integers
     */

    public BoundedMultiset(int[] max)
    {
	super(max.length - 1);

	maxOccurrences = new int[max.length];
	System.arraycopy(max, 0, maxOccurrences, 0, max.length);

	computeMaxGE();
    }

    /**
     * Creates a multiset with the given bounds on the number of
     * occurrences of each elements and the given number of elements
     * total.  The elements will be allocated so they are all as small
     * as possible.
     *
     * @param max an array of nonnegative integers
     * @param howMany a nonnegative integer not exceeding the total in
     * <CODE>max</CODE>
     */

    public BoundedMultiset(int[] max, int howMany)
    {
	this(max);

	// position howMany elements in the lowest possible positions

	int elt = 0;
	while (howMany > 0)
	    {
		addItems(elt, Math.min(maxOccurrences[elt], howMany));
		howMany -= distribution[elt];

		elt++;
	    }
    }

    private void computeMaxGE()
    {
	maxGreaterOrEqual = new int[maxValue + 1];
	
	maxGreaterOrEqual[maxValue] = maxOccurrences[maxValue];
	for (int e = maxValue - 1; e >= 0; e--)
	    maxGreaterOrEqual[e] = maxOccurrences[e] + maxGreaterOrEqual[e + 1];
    }

    public Object clone()
    {
	BoundedMultiset result = (BoundedMultiset)(super.clone());

	result.maxOccurrences = new int[maxOccurrences.length];
	System.arraycopy(maxOccurrences, 0, result.maxOccurrences, 0, maxOccurrences.length);
	computeMaxGE();

	return result;
    }

    /**
     * Determines if this set has a successor amopng those with
     * the same number of elements.  Lexicographic order is used
     * as the ordering.
     * 
     * @return true iff this set has a successor
     */

    public boolean hasNext()
    {
	if (numItems == 0)
	    return false;

	// find the lowest element in this set

	int lowest = 0;
	while (distribution[lowest] == 0)
	    lowest++;

	// check if potential elements after that are not at their max

	int item = lowest + 1;
	while (item <= maxValue && distribution[item] == maxOccurrences[item])
	    item++;

	// there is a successor if we found item s.t. dist[item] < max[item]

	return (item <= maxValue);
    }

    /**
     * Turns this multiset into its successor.  The successor is
     * defined to be the multiset with the same number of elements
     * that is next in lexicographic order.
     *
     * @return this multiset
     */

    public Multiset goNext()
    {
	// find largest d s.t. dist[d] > 0

	int lastNonZero = maxValue;
	while (distribution[lastNonZero] == 0)
	    lastNonZero--;

	if (lastNonZero < maxValue && maxGreaterOrEqual[lastNonZero + 1] > 0)
	    {
		// subtract one element of value lastNonZero
		// and add one of next possible value

		distribution[lastNonZero]--;
		
		int toIncrement = lastNonZero + 1;
		while (distribution[toIncrement] == maxOccurrences[toIncrement])
		    toIncrement++;

		distribution[toIncrement]++;
	    }
	else
	    {
		int backupFrom = lastNonZero;

		do
		    {
			// find next non-zero before backupFrom
			
			int nextNonZero = backupFrom - 1;
			while (distribution[nextNonZero] == 0)
			    nextNonZero--;

			// convert all elements of value backupFrom
			// and one element of value nextNonZero to
			// elements of value (nextNonZero + 1)
			// ...x.....y...  -> ...(x-1)(y+1)...
			// ...^     ^
			// ...|.....+backupFrom
			//    |
			// ...+nextNonZero

			int numToBackUp = distribution[backupFrom];
			distribution[nextNonZero]--;
			distribution[backupFrom] = 0;
			distribution[nextNonZero + 1] = numToBackUp + 1;

			backupFrom = nextNonZero + 1;
		    }
		while (distribution[backupFrom] > maxGreaterOrEqual[backupFrom]);

		int e = backupFrom;

		while (distribution[e] > maxOccurrences[e])
		    {
			distribution[e + 1] = distribution[e] - maxOccurrences[e];
			distribution[e] = maxOccurrences[e];
			
			e++;
		    }
	    }

	resetNonZeroD();

	return this;
    }

    /**
     * Adds one occurrence of the given item to this distribution.
     *
     * @param d the item to add
     * @return a reference to this set
     */ 

    public Multiset addItem(int d)
    {
	if (distribution[d] + 1 > maxOccurrences[d])
	    throw new SetException("too many occurrences of " + d);
	else
	    return super.addItem(d);
    }    

    /**
     * Adds the given number of the given item to this distribution.
     *
     * @param d the item to add
     * @param howMany how many of that item to add
     * @return a reference to this set
     */ 

    public Multiset addItems(int d, int howMany)
    {
	if (distribution[d] + howMany > maxOccurrences[d])
	    throw new SetException("too many occurrences of " + d);
	else
	    return super.addItems(d, howMany);
    }    

    public static void main(String[] args)
    {
	BoundedMultiset s = new BoundedMultiset(new int[] {1, 2, 3, 2, 1},
						Integer.parseInt(args[0]));

	System.out.println(s);
	while (s.hasNext())
	    {
		s.goNext();
		System.out.println(s);
	    }
    }

}
