package com.sirmapsalot.combinatorics;

import java.math.*;
import java.util.*;

/**
 * A multiset of {0, ..., n}.
 *
 * @author Jim Glenn
 * @version 0.1 2/11/2003
 * @version 0.2 8/27/2003 adapted from Multiset
 */

public class Multiset implements Cloneable, Comparable
{
    /**
     * The number of items in this multiset.
     */

    protected int numItems;

    /**
     * The maximum item that can be added to this multiset.
     */

    protected int maxValue;

    /**
     * The least <CODE>d</CODE> such that this set has > 0
     * <CODE>d</CODE>'s.
     */

    protected int nonZeroD;

    /**
     * An array such that entry d gives the number of d's in this distribution.
     */

    protected int[] distribution;

    /**
     * Initializes new empty set whose maximum item is d.
     *
     * @param d the maximum digit value
     */

    public Multiset(int d)
    {
	this(0, d);
    }

    /**
     * Initializes this new set to <CODE>k</CODE> zeros where
     * the max item possible is <CODE>d</CODE>
     *
     * @param k the number of items
     * @param d the maximum item value
     */

    public Multiset(int k, int d)
    {
	numItems = k;
	maxValue = d;

	distribution = new int[maxValue + 1];
	distribution[0] = k;

	if (k == 0)
	    nonZeroD = -1;
	else
	    nonZeroD = 0;

	for (d = 1; d <= maxValue; d++)
	    distribution[d] = 0;
    }

    /**
     * Constructs a multiset according to the given array.
     *
     * @param a an array containing the item counts for this new set
     */

    public Multiset(int[] counts)
    {
	numItems = 0;
	maxValue = counts.length - 1;

	// copy array

	distribution = new int[maxValue + 1];
	for (int d = 0; d <= maxValue; d++)
	    {
		distribution[d] = counts[d];
		numItems += counts[d];
	    }
	
	// find lowest non-zero item count

	resetNonZeroD();
    }

    /**
     * Initializes this new distribution to be a copy of the given one.
     *
     * @param toCopy the distribution to copy
     */

    public Multiset(Multiset toCopy)
    {
	numItems = toCopy.numItems;
	maxValue = toCopy.maxValue;
	nonZeroD = toCopy.nonZeroD;

	distribution = new int[maxValue + 1];
	for (int d = 0; d <= maxValue; d++)
	    distribution[d] = toCopy.distribution[d];
    }

    /**
     * Returns a deep copy of this set.
     *
     * @return a copy of this set
     */

    public Object clone()
    {
	try
	    {
		Multiset copy = (Multiset)(super.clone());

		copy.distribution = new int[maxValue + 1];
		for (int d = 0; d <= maxValue; d++)
		    copy.distribution[d] = distribution[d];

		return copy;
	    }
	catch (CloneNotSupportedException cantHappen)
	    {
		return null;
	    }
    }

    /**
     * Determines if this set as a successor among those sets
     * with the same number of elements.  Lexicographic order
     * is used to order within those sets.
     * In other words, determines if this set contains an element
     * that is not the largest possible.
     */

    public boolean hasNext()
    {
	return (distribution[maxValue] != numItems);
    }

    /**
     * Turns this set into the one immediately after it
     * in lexicographic order.
     *
     * @return a reference to this set
     * @throws ArrayIndexOutOfBoundsException if this set has
     * no successor
     */

    public Multiset goNext()
    {
	// find least digit value that has a non-zero count
	// (now nicely kept track of)
	int value = nonZeroD;

	// zero out that value, but remember how many there were
	int howMany = distribution[value];
	distribution[value] = 0;

	// add one to next higher digit value
	distribution[value + 1]++;

	// return all other occurrences to zeros
	distribution[0] = howMany - 1;

	if (howMany > 1)
	    nonZeroD = 0;
	else
	    nonZeroD++;

	return this;
    }

    /**
     * Returns a new set that is immediately after this one
     * in lexicographic order.
     *
     * @return the next set
     * @throws ArrayIndexOutOfBoundsException if this set has
     * no successor
     */

    public Multiset next()
    {
	return ((Multiset)(clone())).goNext();
    }

    /**
     * Removes one of the given item from this distribution.
     *
     * @param d the item to remove
     * @return a reference to this set
     * @throws SetException if there aren't any of the given
     * item in this set
     */

    public Multiset removeItem(int d) throws SetException
    {
	if (d > maxValue || distribution[d] == 0)
	    throw new SetException("minus: no occurrences of " + d);

	numItems--;
	distribution[d]--;

	if (nonZeroD == d && distribution[d] == 0)
	    {
		// removed last of the digit we were keeping track of

		resetNonZeroD();
	    }
	
	return this;
    }

    /**
     * Removes the given number of the given item from this distribution.
     *
     * @param d the item to remove
     * @param count the number of that item to remove
     * @return a reference to this set
     * @throws SetException if there aren't enough of the given
     * item in this set
     */

    public Multiset removeItems(int d, int count) throws SetException
    {
	if (d > maxValue || distribution[d] < count)
	    throw new SetException("minus: no occurrences of " + d);

	numItems -= count;
	distribution[d] -= count;

	if (nonZeroD == d && distribution[d] == 0)
	    {
		// removed last of the digit we were keeping track of

		resetNonZeroD();
	    }
	
	return this;
    }

    /**
     * Removes all items from this set.
     */

    public void removeAll()
    {
	numItems = 0;
	nonZeroD = -1;

	for (int i = 0; i <= maxValue; i++)
	    distribution[i] = 0;
    }
	
    /**
     * Returns a new set that has one less of the given item
     * than this one.
     *
     * @param d the item to remove
     * @return a new set with one less <CODE>d</CODE> than this one
     * @throws SetException if this set does not have
     * the given item
     */ 

    public Multiset minus(int d) throws SetException
    {
	Multiset result = (Multiset)(clone());
	
	return result.removeItem(d);
    }    

    /**
     * Adds one occurrence of the given item to this distribution.
     *
     * @param d the item to add
     * @return a reference to this set
     */ 

    public Multiset addItem(int d)
    {
	numItems++;
	distribution[d]++;

	if (d < nonZeroD || nonZeroD == -1)
	    nonZeroD = d;
	
	return this;
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
	numItems += howMany;
	distribution[d] += howMany;

	if ((d < nonZeroD || nonZeroD == -1) && howMany > 0)
	    nonZeroD = d;
	
	return this;
    }    

    /**
     * Returns a new set that has one more of the given item
     * than this one.
     *
     * @param d the item to add
     * @return a new set with one more <CODE>d</CODE> than this one
     */ 

    public Multiset plus(int d)
    {
	Multiset result = (Multiset)clone();
	
	return result.addItem(d);
    }    

    /**
     * Returns a new set that has more of the given
     * item than this one.
     *
     * @param howMany how many digits to add
     * @param d which digit to add
     * @return a digit set with the digits added
     */

    public Multiset add(int howMany, int d)
    {
	Multiset result = (Multiset)clone();
	
	result.distribution[d] += howMany;
	result.numItems += howMany;
	if (howMany > 0 && result.nonZeroD < d)
	    result.nonZeroD = d;

	return result;
    }

    /**
     * Returns a new multiset that contains the elements from this set and
     * the given set.
     *
     * @param s the set to add elements from
     */

    public Multiset add(Multiset s)
    {
	Multiset result = (Multiset)clone();

	for (int d = 0; d <= s.maxValue && d <= maxValue; d++)
	    result.addItems(d, s.countItem(d));

	return result;
    }
    
    /**
     * Returns the number of digits in this distribution.
     *
     * @return the size of this multiset
     */

    public int size()
    {
	return numItems;
    }

    /**
     * Returns the maximum value possible for this multiset.  This is not
     * the same as the maximum value actually contained in this multiset.
     *
     * @return the maximum value for this multiset
     */

    public int range()
    {
	return maxValue;
    }

    /**
     * Returns the number of the given item in this distribution.
     *
     * @param d the item to count
     * @return the number of <CODE>d</CODE>'s in this set
     */

    public int countItem(int d)
    {
	if (d < 0 || d > maxValue)
	    return 0;
	else
	    return distribution[d];
    }

    /**
     * Returns the most frequently occurring item in this set.
     * Ties are broken arbitrarily.
     *
     * @return the most frequently occurring item
     */

    public int getMajority()
    {
	int max = distribution[0];
	int result = 0;

	for (int i = 1; i < distribution.length; i++)
	    {
		if (distribution[i] > max)
		    {
			max = distribution[i];
			result = i;
		    }
	    }

	return result;
    }


    /**
     * Returns the least frequently occurring item in this set.
     * Ties are broken arbitrarily.  If this set is empty, returns
     * -1.
     *
     * @return the least frequently occurring item
     */

    public int getMinority()
    {
	int min = distribution[0];
	int result = 0;

	for (int i = 1; i < distribution.length; i++)
	    {
		if (min <= 0 || (distribution[i] < min && distribution[i] > 0))
		    {
			min = distribution[i];
			result = i;
		    }
	    }

	if (min == 0)
	    return -1;
	else
	    return result;
    }

    /**
     * Counts the most frequently occurring item in this set.
     *
     * @return the count of the most frequently occurring item
     */

    public int countMajority()
    {
	return countItem(getMajority());
    }


    /**
     * Counts the least frequently occurring item in this set.
     *
     * @return the count of the least frequently occurring item
     */

    public int countMinority()
    {
	int min = getMinority();
	if (min == -1)
	    return 0;
	else
	    return countItem(min);
    }

    /**
     * Resets the <CODE>nonZeroD</CODE> field of this distribution.
     */

    protected void resetNonZeroD()
    {
	if (numItems == 0)
	    nonZeroD = -1;
	else
	    {
		nonZeroD = 0;
		while (nonZeroD <= maxValue && distribution[nonZeroD] == 0)
		    nonZeroD++;
	    }
    }

    /**
     * Computes the number of arrangements of this multiset.  This assumes
     * that the order of the arrangement matters but that items with
     * the same value are indistinguishable.
     *
     * @return the number of arrangements of this multiset
     */

    public BigInteger count()
    {
	// this is like counting the ways to rearrange MISSISSIPPI:
	// choose 1 out of 11 for M
	// choose 4 out of remaining 10 for I's
	// choose 4 out of remaining 6 for S's
	// must put 2 P's in remaining 2 spots

	int placesLeft = numItems;
	BigInteger total = BigInteger.ONE;

	for (int d = 0; d < maxValue; d++)
	    {
		total = total.multiply(Combinatorics.choose(placesLeft, distribution[d]));
		placesLeft -= distribution[d];
	    }

	return total;
    }

    /**
     * An exception caused by an illegal distribution.
     */

    public static class SetException extends RuntimeException
    {
	/**
	 * Initializes this new exception to hold the given message.
	 *
	 * @param mess the message to report with this exception
	 */

	public SetException(String mess)
	{
	    super(mess);
	}
    }

    public int compareTo(Object o)
    {
	return toString().compareTo(o.toString());
    }
    
    /**
     * Returns a represention of this set in a string.
     *
     * @return a string representation of this set
     */

    public String toString()
    {
	StringBuffer result = new StringBuffer("[");

	for (int d = maxValue; d >= 0; d--)
	    result.append(distribution[d]);
	
	result.append(']');

	return result.toString();
    }

    /**
     * Returns a new set equal to this one except
     * that any digit occurring more than the given maximum is replaced
     * with the maximum.
     *
     * @param max a nonnegative integer
     */

    public Multiset clip(int max)
    {
	Multiset result = (Multiset)clone();
	result.numItems = 0;

	for (int i = 0; i < result.distribution.length; i++)
	    {
		result.distribution[i] = Math.min(result.distribution[i], max);
		result.numItems += result.distribution[i];
	    }

	if (result.distribution[result.nonZeroD] == 0)
	    result.nonZeroD = -1;

	return result;
    }

    /**
     * Determines if the given multiset is a subset of this one.
     *
     * @param s a multiset
     * @return true iff the given multiset is a subset of this one
     */

    public boolean subset(Multiset s)
    {
	int d = 0;
	int checked = 0; // for keeping track of leftover items
	int sChecked = 0;

	while (d <= maxValue
	       && d <= s.maxValue
	       && countItem(d) >= s.countItem(d))
	    {
		d++;
		checked += countItem(d);
		sChecked += s.countItem(d);
	    }

	if (d > maxValue && d > s.maxValue)
	    return true;
	else if (d > maxValue)
	    return (s.numItems - sChecked == 0); // does s have more items?
	else if (d > s.maxValue)
	    return (numItems - checked == 0); // does this have more items?
	else
	    return false;
    }

    /**
     * Determines if this multiset is equal to the given object.
     *
     * @param o the object to compare to
     * @return true iff <CODE>o</CODE> is a <CODE>Multiset</CODE> and
     * contains exactly the same items as this set
     */

    public boolean equals(Object o)
    {
	if (o instanceof Multiset)
	    return equals((Multiset)o);
	else
	    return false;
    }

    /**
     * Determines if this multiset is equal to the given multiset.
     *
     * @param s the multiset to compare to
     * @return true iff this set contains exactly the same elements as
     * the given set
     */

    public boolean equals(Multiset s)
    {
	boolean result =  (subset(s) && s.subset(this));
       	return result;
    }

    /**
     * Returns a hashcode for this multiset.
     *
     * @return a hashcode for this multiset
     */

    public int hashCode()
    {
	final int BITS_PER_INT = 32;

	// this is written to work well specifically for <CODE>DiceRoll</CODE>
	// -- each element in the set will take 3 bits, there are 5 total,
	// so only 15 bits are used

	int hash = 0;
	int shift = 0;

	Iterator i = iterator();
	while (i.hasNext())
	    {
		int elt = ((Integer)(i.next())).intValue();
		hash ^= elt << shift;
		shift += 3;
		if (shift >= BITS_PER_INT)
		    shift = 0;
	    }

	return hash;
    }

    /**
     * Returns the difference between this set and the given set.
     * The resulting set has the same type as this one.
     *
     * @param s a subset of this multiset
     * @return the difference between this set and the given set
     */

    public Multiset difference(Multiset s)
    {
	Multiset result = (Multiset)clone();

	for (int d = 0; d <= s.range(); d++)
	    result.removeItems(d, s.countItem(d));

	return result;
    }

    /**
     * Makes this multiset empty.
     *
     * @return a reference to this set
     */

    public Multiset makeEmpty()
    {
	numItems = 0;
	nonZeroD = -1;

	for (int d = 0; d <= maxValue; d++)
	    distribution[d] = 0;

	return this;
    }

    /**
     * Returns this multiset as an array.  The result will be sorted from
     * smallest to biggest.
     */

    public int[] toArray()
    {
	int[] result = new int[numItems];
	int j = 0;

	for (int d = 0; d < distribution.length; d++)
	    if (distribution[d] > 0)
		for (int i = 0; i < distribution[d]; i++, j++)
		    result[j] = d;

	return result;
    }

    /**
     * Returns this multiset as a list.  The result will be sorted from
     * smallest to biggest.
     *
     * @return this multiset as a list
     */

    public List toList()
    {
	List result = new LinkedList();

	for (int d = 0; d < distribution.length; d++)
	    for (int i = 0; i < distribution[d]; i++)
		result.add(new Integer(d));

	return result;
    }

    /**
     * Returns an iterator over the items in this multiset.  Items will
     * be returned by the iterator in order from smallest to biggest.
     *
     * @return an iterator over the items in this multiset
     */

    public Iterator iterator()
    {
	return new ElementIterator();
    }

    /**
     * Returns a new iterator over the subsets of this multiset.  The
     * iterator will be positioned before the empty subset.
     *
     * @return an iterator over this multiset's subsets
     */

    public Iterator subsetIterator()
    {
	return new SubsetIterator(this);
    }

    /**
     * Returns a new iterator over the subsets of this multiset.  The
     * iterator will be positioned before subset of the smallest
     * <CODE>k</CODE> elements.
     *
     * @param k an integer between 0 and the size of this multiset, inclusive
     * @return an iterator over this multiset's size <CODE>k</CODE> subsets
     */

    public Iterator subsetIterator(int k)
    {
	return new SubsetIterator(this, k);
    }

    /**
     * Changes this multiset to be the next subset of the given multiset.
     * If there is no such next subset, this set gets changed to something
     * that is not a subset.
     *
     * @param max the set to find the next subset of
     */

    private void goNextSubset(Multiset max)
    {
	// find lowest d s.t. this has fewer d's than max

	int d = 0;
	while (d <= maxValue && distribution[d] >= max.distribution[d])
	    d++;
	
	if (d > maxValue)
	    {
		// increase # of item 0 as marker that we're at the end
		
		addItem(0);
	    }
	else
	    {
		// increase count of d's
		
		addItem(d);
		
		// zero out everything lower
		
		try
		    {
			for (int i = 0; i < d; i++)
			    removeItems(i, countItem(i));
		    }
		catch (SetException cantHappen)
		    {
		    }
	    }
    }

    // Not sure why this was made public instead of using a method
    // to return an iterator over a multiset.  Methods to do that
    // have now been added but the nested class remains public in order
    // to avoid breaking old code.

    public static class SubsetIterator implements Iterator
    {
	/**
	 * The next multiset to be returned by this iterator, as an
	 * array of item counts.
	 */

	private Multiset next;

	/**
	 * The maximum count for each item.
	 */

	private Multiset max;

	/**
	 * The maximum number of elements in a subset.
	 */

	private int maxElements;

	/**
	 * The minimum number of elements in a subset.
	 */

	private int minElements;

	/**
	 * Constructs a new iterator over the subsets of the given multiset.
	 *
	 * @param s the set to iterate over the subsets of
	 */

	public SubsetIterator(Multiset s)
	{
	    next = (Multiset)(s.clone());
	    next.makeEmpty();

	    // set up maximum set

	    max = (Multiset)(s.clone());

	    maxElements = s.size();
	    minElements = 0;
	}

	public SubsetIterator(Multiset s, int k)
	{
	    this(s);

	    maxElements = k;
	    minElements = k;

	    // set up next to be 1st k-element subset of s

	    int e = 0;
	    while (k > 0)
		{
		    if (s.countItem(e) > 0)
			{
			    next.addItems(e, Math.min(k, s.countItem(e)));
			    k -= next.countItem(e);
			}

		    e++;
		}
	}

	/**
	 * Determines if this iterator has a next item.
	 *
	 * @return true iff this iterator has a next item
	 */

	public boolean hasNext()
	{
	    return (next.size() <= maxElements && max.subset(next));
	}

	/**
	 * Returns the next mulitset.
	 *
	 * @return the next multiset
	 */

	public Object next()
	{
	    // get the next multiset

	    Multiset result = (Multiset)(next.clone());

	    do
		{
		    next.goNextSubset(max);
		}
	    while (next.size() < minElements || (next.size() > maxElements
						 && max.subset(next)));

	    return result;
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
    }

    /**
     * An iterator over the elements of this multiset.  These iterators
     * will return the items in this multiset from smallest to biggest.
     */

    private class ElementIterator implements Iterator
    {
	/**
	 * The next element to be returned by this iterator, or -1.
	 */

	int next;

	/**
	 * The number of times the next element has already been
	 * returned by this iterator.
	 */

	int count;

	/**
	 * Constructs a new iterator over the elements of this multiset.
	 */

	public ElementIterator()
	{
	    next = nonZeroD;
	    count = 0;
	}

	/**
	 * Determines if there is another element to iterate to.
	 *
	 * @return true iff there is a next element
	 */

	public boolean hasNext()
	{
	    return (next != -1);
	}

	/**
	 * Returns the next item in the set.
	 *
	 * @return the next item
	 */

	public Object next()
	{
	    int result = next;

	    count++;

	    if (count == countItem(result))
		{
		    // about to return the last one of the current item,
		    // so find the next one

		    count = 0;
		    next++;

		    while (next <= maxValue && countItem(next) == 0)
			next++;

		    if (next > maxValue)
			{
			    // no more items!

			    next = -1;
			}
		}

	    return new Integer(result);
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
    }

    /**
     * Test driver for Multisets.  Displays all sets
     * of 5 digits chosen from {0, 1, 2, 3} the norm of such numbers,
     * and the number of such numbers.
     *
     * @param args ignored
     */

    public static void main(String[] args)
    {
	Multiset dd = new Multiset(5, 3);

	while (dd.hasNext())
	    {
		dd = dd.next();
		System.out.println(dd);
		
		Iterator i = dd.subsetIterator();
		while (i.hasNext())
		    System.out.println(i.next());

		System.out.println();
	    }
    }
}
