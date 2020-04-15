package com.sirmapsalot.combinatorics;

import java.util.*;

/**
 * Iterates through all <CODE>r</CODE>-subsets of a given collection
 * of objects and size <CODE>r</CODE>.  All objects in the collection
 * are assumed to be distinct.
 *
 * @author Jim Glenn
 * @version 0.1 6/30/2003
 */

public class SubsetIterator implements Iterator
{
    /**
     * The collection to iterator through, as an array.
     */

    private Object[] items;

    /**
     * The objects in the current subset, recorded as indices into
     * <CODE>items</CODE>.
     */

    private int[] selectedIndices;

    /**
     * True iff hasNext is true.
     */
    
    private boolean done;

    /**
     * Creates an iterator that will return all size-<CODE>r</CODE>
     * subsets of the given collection.
     *
     * @param c a collection of objects
     * @param r an integer between 0 and the size of <CODE>c</CODE>
     */

    public SubsetIterator(Collection c, int r)
    {
	// convert c to an array
	items = c.toArray();

	// initialize selected items to items[0], items[1], ...
	selectedIndices = new int[r];
	for (int i = 0; i < selectedIndices.length; i++)
	    selectedIndices[i] = i;
	
	done = false;
    }

    /**
     * Determines if there is a subset after the current one.
     *
     * @return true iff the is a next subset
     */

    public boolean hasNext()
    {
	// next will have stored -1 in the first element to indicate
	// termination

	return (!done);
    }

    /**
     * Returns the next subset found by thhis iterator.
     *
     * @return the next subset, as a <CODE>Collection</CODE>
     * @throws NoSuchElementException if this iteration has no more subsets
     */

    public Object next()
    {
	if (hasNext())
	    {
		// create the collection of the items at the given locations

		LinkedList subset = new LinkedList();
		for (int j = 0; j < selectedIndices.length; j++)
		    subset.add(items[selectedIndices[j]]);

		// Find a value in selectedIndices to increment.  The last
		// subset will be ..., size-2, size-1; we find the last
		// index such that the value there is not what it would
		// be in the last subset.

		
		int i = selectedIndices.length - 1;
		while (i >= 0 && selectedIndices[i] == items.length - (selectedIndices.length - i))
		    i--;

		if (i >= 0)
		    {
			// update items for next subset -- i is now the index
			// to increment; all subsequent indices are one more
			// than the previous (ex: 1 2 4 5 becomes 1 3 4 5)
			
			selectedIndices[i]++;
			for (i = i + 1; i < selectedIndices.length; i++)
			    selectedIndices[i] = selectedIndices[i - 1] + 1;
		    }
		else
		    done = true;

		return subset;
	    }
	else
	    {
		throw new NoSuchElementException();
	    }
    }

    /**
     * Unsupported operation.
     *
     * @throws UnsupportedOperationException
     */

    public void remove()
    {
	throw new UnsupportedOperationException();
    }

    /**
     * Test driver for SubsetIterator.
     *
     * @param args ignored
     */

    public static void main(String[] args)
    {
	LinkedList l = new LinkedList();

	for (int n = 1; n <= 6; n++)
	    {
		l.add(new Integer(n));
	    }

	for (int r = 0; r <= 6; r++)
	    {
		System.out.println("*** SIZE " + r + " ***");
		SubsetIterator i = new SubsetIterator(l, r);
		while (i.hasNext())
		    {
			System.out.println(i.next());
		    }
	    }
    }
}
