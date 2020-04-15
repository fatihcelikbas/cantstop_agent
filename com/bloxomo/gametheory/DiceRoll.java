package com.bloxomo.gametheory;

import com.sirmapsalot.combinatorics.*;
import java.util.*;

/**
 * The outcome of rolling k n-sided dice.
 *
 * @author Jim Glenn
 * @version 0.1 8/27/2003
 */
 
public class DiceRoll implements Cloneable, Comparable
{
    /**
     * The multiset that models this roll.  The number of 0's in the
     * multiset corresponds to the number of 1's in the roll, and so
     * on.
     */

    private Multiset dice;

    /**
     * An index that is unique among all rolls with the same
     * number of dice with the same number of sides.  This is here
     * to improve the efficiency of hashCode and equals when many
     * rolls of the same parameters (number of sides and dice) are
     * added to the same map.
     */

    private int index;

    /**
     * Constructs a roll of the given number of dice that all show 1 pip.
     *
     * @param k the number of dice in this new roll
     * @param max the maximum number of pips
     */

    public DiceRoll(int k, int max)
    {
	dice = new Multiset(k, max - 1);
	computeIndex();
    }

    /**
     * Constructs a roll of dice that show the number
     * of pips given by the string parameter.  The maximum value of the dice
     * is given by the integer parameter.
     *
     * @param max the maximum value of the dice
     * @param diceString a string of digits
     */

    public DiceRoll(int max, String diceString)
    {
	dice = new Multiset(max - 1);

	for (int d = 0; d < diceString.length(); d++)
	    {
		int pips = Character.getNumericValue(diceString.charAt(d));

		if (pips > max)
		    throw new IllegalArgumentException("die > " + max);

		if (pips < 1)
		    throw new IllegalArgumentException("invalid digit: " + diceString.charAt(d));

		dice.addItem(pips - 1);
	    }

	computeIndex();
    }

    /**
     * Constructs a copy of the given roll.
     *
     * @param toCopy the roll to copy
     */

    public DiceRoll(DiceRoll toCopy)
    {
	dice = new Multiset(toCopy.dice);

	index = toCopy.index;
    }

    /**
     * Constructs a roll using the given set as the count of dice.
     * The number of 0's in the set will be the number of 1's in the roll,
     * and so on.
     *
     * @param s a multiset
     */

    private DiceRoll(Multiset s)
    {
	dice = s;

	computeIndex();
    }

    public Object clone() throws CloneNotSupportedException
    {
	DiceRoll result = (DiceRoll)(super.clone());

	result.dice = new Multiset(dice);
	result.index = index;

	return result;
    }
    
    /**
     * Returns the number of sides on the dice in this roll
     *
     * @return the number of sides on the dice in this roll
     */

    public int countSides()
    {
	return dice.range() + 1;
    }

    /**
     * Returns the total dice in this roll.
     *
     * @return the total number of dice in this roll
     */

    public int countDice()
    {
	return dice.size();
    }

    /**
     * Returns the number of dice in this roll showing the given number.
     *
     * @return the number of dice showing the given number
     */

    public int countDice(int n)
    {
	// the multiset starts at 0; we thus need the -1

	return dice.countItem(n - 1);
    }

    /**
     * Computes the probability of this roll.  The probability is computed
     * assuming we roll the same number of dice as are in this roll and that
     * the dice are independent and fair.
     *
     * @return the probability of this roll
     */

    public double probability()
    {
	return dice.count().intValue() * Math.pow(1.0 / (dice.range() + 1), size());
    }

    /**
     * Randomly rolls all the dice in this roll.
     */

    public void roll()
    {
	int numDice = dice.size();

	dice.removeAll();

	for (int i = 0; i < numDice; i++)
	    dice.addItem((int)(Math.random() * (dice.range() + 1)));

	computeIndex();
    }

    /**
     * Determines if this roll is a subroll of the given roll.  One roll
     * is a subroll of another iff all dice in the first are also present
     * in at least the same quantity in the second.
     *
     * @param r a roll
     * @return true iff this roll is a subroll of the given roll
     */

    public boolean isSubroll(DiceRoll s)
    {
	return s.dice.subset(dice);
    }

    /**
     * Compares this roll to the given object.
     *
     * @param o an object
     * @return -1, 0, or 1 depending on whether this roll is less than, equal
     * to, or greater than the given object.
     * @throws ClassCastException if <CODE>o</CODE> is not a <CODE>DiceRoll</CODE>
     */

    public int compareTo(Object o)
    {
	// compare rolls based on index

	if (o instanceof DiceRoll)
	    {
		DiceRoll other = (DiceRoll)o;

		if (countSides() == other.countSides()
		    && countDice() == other.countDice())
		    {
			return index - other.index;
		    }
		else if (countDice() < other.countDice())
		    return -1;
		else if (countDice() > other.countDice())
		    return 1;
		else if (countSides() < other.countSides())
		    return -1;
		else
		    return 1;
	    }
	else
	    throw new ClassCastException("comparing DiceRoll " + toString() + " to " + o.getClass().getName() + " " + o);
    }

    /**
     * Returns a string representation of this roll.  The string returned
     * is enclosed in square brackets and lists the dice in increasing
     * order.
     *
     * @return a string representation of this roll
     */

    public String toString()
    {
	StringBuffer result = new StringBuffer("[");

	for (int i = 1; i <= countSides(); i++)
	    for (int j = 0; j < countDice(i); j++)
		{
		    /*
		    if (result.length() > 1)
			result.append(" ");
		    */
		    result.append(i);
		}

	result.append("]");

	return result.toString();
    }


    /**
     * Parses the given string as a dice roll with the given number of sides.
     *
     * @param s the string to parse
     * @param sides the number of sides on the dice
     * @return the parsed roll
     */

    public static DiceRoll parseRoll(String s, int sides)
    {
	StringTokenizer tok = new StringTokenizer(s);

	DiceRoll result = new DiceRoll(0, sides);

	while (tok.hasMoreTokens())
	    {
		result.dice.addItem(Integer.parseInt(tok.nextToken()) - 1);
	    }

	result.computeIndex();

	return result;
    }

    /**
     * Creates a roll consisting of the given number of dice showing the
     * given number of pips.
     *
     * @param pips the number of pips showing on all dice in the new roll
     * @param howMany the number of dice in the new roll
     * @param numSides the number of sides on the dice in the new roll
     * @return a new roll containing dice showing the same number of pips
     */

    public static DiceRoll makeNKind(int pips, int howMany, int numSides)
    {
	DiceRoll result = new DiceRoll(0, numSides);

	for (int i = 0; i < howMany; i++)
	    result.dice.addItem(pips - 1);

	result.computeIndex();

	return result;
    }

    /**
     * Creates a roll consisting of one die of each number in the given range.
     *
     * @param start the lowest die in the new roll
     * @param end the highest die in the new roll
     * @param numSides the number of sides no the dice in the new roll
     * @return a new roll containing dice forming a straight
     */

    public static DiceRoll makeStraight(int start, int end, int numSides)
    {
	DiceRoll result = new DiceRoll(0, numSides);

	for (int i = start; i <= end; i++)
	    result.dice.addItem(i - 1);

	result.computeIndex();

	return result;
    }	

    /**
     * Counts the most frequently occurring number of pips in this roll.
     * For example, if the roll is 2 2 2 2 3, the return value is 4
     * because there are 4 2's.
     *
     * @return the count of the most frequently occurring item
     */

    public int countMajority()
    {
	return dice.countMajority();
    }


    /**
     * Counts the least frequently occurring number of pips in this roll.
     * For example, if the roll is 2 2 2 2 3, the return value is 1
     * because there is 1 3.
     *
     * @return the count of the least frequently occurring number of pips
     */

    public int countMinority()
    {
	return dice.countMinority();
    }

    /**
     * Returns the most frequently occurring number of pips in this roll.
     * Ties are broken arbitrarily.
     * For example, if the roll is 2 2 2 2 3, the return value is 2
     * because there are more 2's than anything else.
     *
     * @return the most frequently occurring item
     */

    public int getMajority()
    {
	return dice.getMajority() + 1;
    }

    public int getHighestPair()
    {
	for (int pips = countSides(); pips >= 1; pips--)
	    if (countDice(pips) >= 2)
		return pips;
	return 0;
    }

    public int getLowestPair()
    {
	for (int pips = 1; pips <= countSides(); pips++)
	    if (countDice(pips) >= 2)
		return pips;
	return 0;
    }

    /**
     * Returns the least frequently occurring number of pips in this roll.
     * Ties are broken arbitrarily.  If this roll has no dice, returns 0.
     * For example, if the roll is 2 2 2 2 3, the return value is 3
     * because there are fewer 3's than anything else.
     *
     * @return the least frequently occurring item
     */

    public int getMinority()
    {
	return dice.getMinority() + 1;
    }

    /**
     * Totals the pips on all dice in this roll.
     *
     * @return the total pips on all dice on this roll
     */

    public int total()
    {
	int tot = 0;
	for (int pips = 1; pips <= countSides(); pips++)
	    tot += pips * countDice(pips);

	return tot;
    }

    /**
     * Returns the number of dice in this roll.
     *
     * @return the number of dice in this roll
     */

    public int size()
    {
	return dice.size();
    }

    /**
     * Determines if all the dice in this roll are consecutive singletons.
     *
     * @return if this roll is consecutive singletons
     */

    public boolean isStraight()
    {
	return (countMajority() == 1
		&& getMax() - getMin() == countDice() - 1);
    }

    public int getMax()
    {
	int pips = countSides();
	while (pips >= 1 && countDice(pips) == 0)
	    pips--;

	return pips;
    }

    public int getMin()
    {
	int pips = 1;
	while (pips <= countSides() && countDice(pips) == 0)
	    pips++;

	return pips;
    }

    /**
     * Returns a new roll that contains the dice from this roll and
     * the given roll.  For example, if this roll is 2 3 and the
     * roll passed as an argument is 1 3 4 then the result
     * is 1 2 3 3 4.
     *
     * @param r the roll to add elements from
     * @return the resulting roll
     */

    public DiceRoll add(DiceRoll r)
    {
	try
	    {
		DiceRoll result = (DiceRoll)clone();
		
		for (int d = 1; d <= r.countSides(); d++)
		    result.dice.addItems(d - 1, r.countDice(d));
		
		result.computeIndex();

		return result;
	    }
	catch (CloneNotSupportedException e)
	    {
		return null;
	    }
    }

    /**
     * Returns a new roll containing this dice from this roll with the
     * given number of dice of the given value added in.
     *
     * @param number a number between 1 and the max on this roll
     * @param howMany a nonnegative integer
     */

    public DiceRoll add(int number, int howMany)
    {
	try
	    {
		DiceRoll result = (DiceRoll)clone();
		
		result.dice.addItems(number - 1, howMany);
		result.computeIndex();

		return result;
	    }
	catch (CloneNotSupportedException e)
	    {
		return null;
	    }
    }

    /**
     * Returns the dice in this roll in an array.  The dice will be in
     * increasing order in the array.
     *
     * @return an array containing the numbers showing on the dice in
     * this roll
     */

    public int[] toArray()
    {
	int[] result = dice.toArray();

	// convert from multiset elements (0, ..., sides-1) to dice
	// (1, ..., sides)

	for (int i = 0; i < result.length; i++)
	    result[i]++;

	return result;
    }

    /**
     * Returns an iterator over the subrolls of this roll.  The iterator
     * will be positioned before the empty subroll.
     *
     * @return an iterator over the subrolls of this roll
     */

    public Iterator subrollIterator()
    {
	return new SubrollIterator();
    }

    /**
     * Returns an iterator over the size-<CODE>k</CODE>
     * subrolls of this roll.  The iterator
     * will be positioned before the empty subroll.
     *
     * @param k an integer between 0 and the number of dice in this roll,
     * inclusive
     * @return an iterator over the subrolls of this roll
     */

    public Iterator subrollIterator(int k)
    {
	return new SubrollIterator(k);
    }

    private class SubrollIterator implements Iterator
    {
	private Iterator i;

	public SubrollIterator()
	{
	    i = dice.subsetIterator();
	}

	public SubrollIterator(int k)
	{
	    i = dice.subsetIterator(k);
	}

	public boolean hasNext()
	{
	    return i.hasNext();
	}
	
	public Object next()
	{
	    try
		{
		    DiceRoll result = (DiceRoll)(DiceRoll.this.clone());

		    result.dice = (Multiset)(i.next());

		    result.computeIndex();
		    
		    return result;
		}
	    catch (CloneNotSupportedException e)
		{
		    return null;
		}
	}

	public void remove()
	{
	    throw new UnsupportedOperationException();
	}
    }

    public DiceRoll next()
    {
	try
	    {
		DiceRoll result = (DiceRoll)clone();
		
		result.dice = result.dice.next();

		result.computeIndex();
		
		return result;
	    }
	catch (CloneNotSupportedException e)
	    {
		return null;
	    }
    }

    public DiceRoll difference(DiceRoll other)
    {
	try
	    {
		DiceRoll result = (DiceRoll)clone();
		
		result.dice = result.dice.difference(other.dice);

		result.computeIndex();
		
		return result;
	    }
	catch (CloneNotSupportedException e)
	    {
		return null;
	    }
    }

    public boolean hasNext()
    {
	return dice.hasNext();
    }

    /**
     * Computes an index that is unique among all rolls with the same
     * number of dice of the same number of sides.
     */

    private int computeIndex()
    {
	index = 0;
	int placeValue = 1;

	for (int pips = 1; pips <= countSides(); pips++)
	    {
		index += countDice(pips) * placeValue;
		placeValue *= (countDice() + 1);
	    }

	return index;
    }

    public int hashCode()
    {
	return index;
    }

    public boolean equals(Object o)
    {
	if (o instanceof DiceRoll)
	    {
		DiceRoll other = (DiceRoll)o;

		if (countDice() != other.countDice()
		    || countSides() != other.countSides())
		    return false;
		else
		    return index == other.index;
	    }
	else
	    return false;
    }

    /**
     * Test driver for <CODE>DiceRoll</CODE>. Displays all the possible
     * rolls of 5 6-sided dice, the probability of obtaining those rolls,
     * and the cumulative probability (which should be 1).
     *
     * @param args ignored
     */

    public static void main(String[] args)
    {
	DiceRoll roll = new DiceRoll(1, 6);

	double cum = roll.probability();
	System.out.println(roll + " " + cum);
	while (roll.hasNext())
	    {
		roll = (DiceRoll)(roll.next());
		System.out.println(roll + " " + roll.probability() + " " + roll.hashCode());
		cum += roll.probability();

		// output all subrolls
		
		Iterator i = roll.subrollIterator();
		while (i.hasNext())
		    {
			DiceRoll subroll = (DiceRoll)(i.next());

			System.out.println("\t" + subroll + " " + roll.difference(subroll));
		    }
	    }
	System.out.println(cum);
    }
}

