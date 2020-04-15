package com.sirmapsalot.combinatorics;

import java.math.*;
import java.util.*;

/**
 * Holder for static combinatoric functions.
 *
 * @author Jim Glenn
 * @version 0.1 2/11/2003
 */

public class Combinatorics
{
    /**
     * Memoized values from factorial.  <CODE>factMemo[n]</CODE> holds n!.
     */

    private static BigInteger[] factMemo = null;

    /**
     * Memoized values from choose.  <CODE>choose[n][k]</CODE> holds n choose k
     */

    private static BigInteger[][] chooseMemo = null;

    /**
     * Computes <CODE>n</CODE>!
     *
     * @param n an integer
     * @return <CODE>n</CODE>!
     */

    public static BigInteger factorial(int n)
    {
	if (factMemo == null || factMemo.length <= n)
	    {
		// memoize more values of fact

		int i = 1;
		BigInteger[] newFact;

		if (factMemo != null)
		    {
			// copy values from old array

			newFact = new BigInteger[Math.max(factMemo.length * 2, n + 1)];
			for (i = 0; i < factMemo.length; i++)
			    newFact[i] = factMemo[i];
		    }
		else
		    {
			// initialize new array

			newFact = new BigInteger[n + 1];
			newFact[0] = BigInteger.ONE;
		    }

		// fill in new values in array

		for (; i < newFact.length; i++)
		    newFact[i] = newFact[i - 1].multiply(BigInteger.valueOf(i));

		factMemo = newFact;
	    }

	return factMemo[n];
    }

    /**
     * Returns the number of ways to choose k things out of n.
     *
     * @param n the number of things to choose from
     * @param k the number of things to choose
     * @return the number of different ways to choose <CODE>k</CODE> from
     * <CODE>n</CODE>
     */

    public static BigInteger choose(int n, int k)
    {
	if (k > n || n < 0 || k < 0)
	    return BigInteger.ZERO;

	if (chooseMemo == null || chooseMemo.length <= n)
	    {
		int i;
		BigInteger[][] newChoose;
		
		// extend or create table

		if (chooseMemo != null)
		    {
			newChoose = new BigInteger[Math.max(chooseMemo.length * 2, n + 1)][1];
			for (i = 0; i < chooseMemo.length; i++)
			    newChoose[i] = chooseMemo[i];
		    }
		else
		    {
			newChoose = new BigInteger[n + 1][1];
			newChoose[0][0] = BigInteger.ONE;
			i = 1;
		    }
		
		// fill out rows i on

		for (; i < newChoose.length; i++)
		    {
			newChoose[i] = new BigInteger[i + 1];
			newChoose[i][0] = BigInteger.ONE;
			for (int j = 1; j < i; j++)
			    newChoose[i][j] = newChoose[i - 1][j].add(newChoose[i - 1][j - 1]);
			newChoose[i][i] = BigInteger.ONE;
		    }

		chooseMemo = newChoose;
	    }


	return chooseMemo[n][k];
    }

    /**
     * Returns the representation of <CODE>n</CODE> in the given radix
     * as an array of digits (least-sig in elt 0, etc).  This method
     * does not have the radix limitation that <CODE>Integer.toString</CODE>
     * does -- the radix can be as large as the maximum value of an
     * <CODE>int</CODE>. The most significant digit is guaranteed to
     * be non-zero as long as <CODE>n</CODE> is non-zero (that is,
     * just as many digits are are needed are used).
     *
     * @param n the number to convert
     * @param radix the radix to use
     * @return an array containing the digits in the representation of
     * <CODE>n</CODE> in the given radix
     */

    public static int[] toRadix(int n, int radix)
    {
	int numDigits;

	if (n == 0)
	    numDigits = 1;
	else
	    numDigits = (int)(Math.floor(Math.log(n) / Math.log(radix))) + 1;

	int[] result = new int[numDigits];

	for (int exp = numDigits - 1; exp >= 1; exp--)
	    {
		int value = (int)Math.pow(radix, exp);  // place value of digit
		int digit = n / (int)(Math.pow(radix, exp));  // value of digit

		result[exp] = digit;
		n = n - digit * value;
	    }

	result[0] = n;

	return result;
    }

    /**
     * Returns the representation of <CODE>n</CODE> in the given radix
     * as an array of digits (least-sig in elt 0, etc).  The digits
     * will be from -(d-1)/2 to +(d-1)/2.This method
     * does not have the radix limitation that <CODE>Integer.toString</CODE>
     * does -- the radix can be as large as the maximum value of an
     * <CODE>int</CODE>. The most significant digit is guaranteed to
     * be non-zero as long as <CODE>n</CODE> is non-zero (that is,
     * just as many digits are are needed are used).
     *
     * @param n the number to convert
     * @param radix an odd integer no less than 3
     * @return an array containing the digits in the representation of
     * <CODE>n</CODE> in the given radix
     */

    public static int[] toRadixNegativeDigits(int n, int radix)
    {
	int absN = Math.abs(n);

	int[] digits = toRadix(absN, radix);

	int carry = 0;
	for (int d = 0; d < digits.length; d++)
	    {
		digits[d] += carry;
		carry = 0; 
		if (digits[d] >= radix)
		    {
			carry = digits[d] / radix;
			digits[d] = digits[d] % radix;
		    }

		if (digits[d] > radix / 2)
		    {
			carry++;
			digits[d] = digits[d] - radix;
		    }
	    }

	// add another digit if necessary

	if (carry > 0)
	    {
		int[] newDigits = new int[digits.length + 1];
		for (int d = 0; d < digits.length; d++)
		    newDigits[d] = digits[d];
		newDigits[digits.length] = carry;
		digits = newDigits;
	    }

	// adjust for negative numbers

	if (n < 0)
	    {
		for (int d = 0; d < digits.length; d++)
		    digits[d] = -digits[d];
	    }

	return digits;
    }

    /**
     * Returns the representation of <CODE>n</CODE> in the given radix
     * as an array of digits (least-sig in elt 0, etc).  This method
     * does not have the radix limitation that <CODE>Integer.toString</CODE>
     * does -- the radix can be as large as the maximum value of an
     * <CODE>int</CODE>.  The representation will use the given
     * number of digits.  If this is not enough, the leading digit
     * will exceed maximum digit value for the radix.  If the number
     * of digits is too large, the most significant digits will be zero.
     *
     * @param n the number to convert
     * @param radix the radix to use
     * @param numDigits the number of digits to use
     * @return an array containing the digits in the representation of
     * <CODE>n</CODE> in the given radix
     */

    public static int[] toRadix(int n, int radix, int numDigits)
    {
	int[] result = new int[numDigits];

	for (int exp = numDigits - 1; exp >= 1; exp--)
	    {
		int value = (int)Math.pow(radix, exp);  // place value of digit
		int digit = n / (int)(Math.pow(radix, exp));  // value of digit

		result[exp] = digit;
		n = n - digit * value;
	    }

	result[0] = n;

	return result;
    }


    /**
     * Returns the representation of <CODE>n</CODE> in the given radix
     * as an array of digits (least-sig in elt 0, etc).  This method
     * does not have the radix limitation that <CODE>Integer.toString</CODE>
     * does -- the radix can be as large as the maximum value of an
     * <CODE>int</CODE>.  The most significant digit is guaranteed to
     * be non-zero as long as <CODE>n</CODE> is non-zero (that is,
     * just as many digits are are needed are used).
     *
     * @param n the number to convert
     * @param radix the radix to use
     * @return an array containing the digits in the representation of
     * <CODE>n</CODE> in the given radix
     */

    public static int[] toRadix(BigInteger n, int radix)
    {
	int numDig = numDigits(n, radix);

	int[] result = new int[numDig];

	for (int exp = numDig - 1; exp >= 1; exp--)
	    {
		BigInteger value = BigInteger.valueOf(radix).pow(exp);
		BigInteger digit = n.divide(value);

		result[exp] = digit.intValue();
		n = n.subtract(value.multiply(digit));
	    }

	result[0] = n.intValue();

	return result;
    }

    /**
     * Returns the representation of <CODE>n</CODE> in the given radix
     * as an array of digits (least-sig in elt 0, etc).  This method
     * does not have the radix limitation that <CODE>Integer.toString</CODE>
     * does -- the radix can be as large as the maximum value of an
     * <CODE>int</CODE>.  The representation will use the given
     * number of digits.  If this is not enough, the leading digit
     * will exceed maximum digit value for the radix.  If the number
     * of digits is too large, the most significant digits will be zero.
     *
     * @param n the number to convert
     * @param radix the radix to use
     * @param n the number of digits to use
     * @return an array containing the digits in the representation of
     * <CODE>n</CODE> in the given radix
     */

    public static int[] toRadix(BigInteger n, int radix, int numDigits)
    {
	int[] result = new int[numDigits];

	for (int exp = numDigits - 1; exp >= 1; exp--)
	    {
		BigInteger value = BigInteger.valueOf(radix).pow(exp);
		BigInteger digit = n.divide(value);

		result[exp] = digit.intValue();
		n = n.subtract(value.multiply(digit));
	    }

	result[0] = n.intValue();

	return result;
    }

    /**
     * Calculates the number represented by the digits in the given array using
     * the given radix.  The units digit should be in element 0 of the array.
     *
     * @param digits an array of integers
     * @param radix an integer no less than 2
     * @return the number represented by the digits in <CODE>digits</CODE> in
     * radix <CODE>radix</CODE>
     */

    public static BigInteger fromRadix(int[] digits, int radix)
    {
	BigInteger result = BigInteger.ZERO;
	BigInteger placeValue = BigInteger.ONE;
	BigInteger base = BigInteger.valueOf(radix);

	for (int i = 0; i < digits.length; i++)
	    {
		result = result.add(placeValue.multiply(BigInteger.valueOf(digits[i])));
		placeValue = placeValue.multiply(base);
	    }

	return result;
    }

    /**
     * Returns the number of digits required to represent <CODE>n</CODE> in
     * the given radix.
     *
     * @param n the number to count digits in
     * @param radix the radix to count digits in
     * @return the number of digits in the representation of <CODE>n</CODE>
     * in the given radix.
     */

    public static int numDigits(BigInteger n, int radix)
    {
	int numDigits = 1;
	BigInteger nextPlaceValue = BigInteger.valueOf(radix);

	while(nextPlaceValue.compareTo(n) <= 0)
	    {
		numDigits++;
		nextPlaceValue = nextPlaceValue.multiply(BigInteger.valueOf(radix));
	    }

	return numDigits;
    }

    /**
     * Test driver for <CODE>toRadix</CODE>.  Displays a number
     * given on command line in radix given on command line.
     *
     * @param args array with number and radix in first two elements
     */
    
    public static void main(String[] args)
    {
	/* Radix and choose tests
	if (args.length > 0)
	    {
		int n = Integer.parseInt(args[0]);
		int radix = Integer.parseInt(args[1]);
		int numDigits = Integer.parseInt(args[2]);
		
		int[] digits = Combinatorics.toRadix(n, radix, numDigits);
		
		for (int d = digits.length - 1; d >= 0; d--)
		    {
			System.out.print(digits[d]);
			if (radix > 10)
			    System.out.print('.');
		    }
		System.out.println();
	    }
	else
	    {
		for (int n = 0; n < 10; n++)
		    {
			for (int k = 0; k <= n; k++)
			    System.out.print(choose(n, k) + " ");
			System.out.println();
		    }
	    }
	*/
	
	int[] a = new int[args.length];
	for (int i = 0; i < args.length; i++)
	    {
		a[i] = Integer.parseInt(args[i]);
	    }
	System.out.println(findArrangements(a));
    }

    /**
     * Returns the sum or the squares of the numbers in the given array.
     *
     * @param a an array of integers
     * @return the sum of the squares of the elements in <CODE>a</CODE>
     */

    public static int sumSquares(int[] a)
    {
	int total = 0;

	for (int i = 0; i < a.length; i++)
	    total += a[i] * a[i];

	return total;
    }

    /**
     * Returns the sum of a geometric series with the given initial term
     * and multiplier.
     *
     * @param init the first term in the series
     * @param mult the multiplicative factor applied to each term to get
     * then next
     * @param len the number of terms to sum
     */

    public static BigInteger geometricSum(int init, int mult, int len)
    {
	BigInteger sum = BigInteger.ZERO;
	BigInteger term = BigInteger.valueOf(init);
	int i = 0;

	while (i < len)
	    {
		sum = sum.add(term);
		term = term.multiply(BigInteger.valueOf(mult));
		i++;
	    }

	return sum;

    }

    /**
     * Returns a list of the rearrangements of the objects given by count.
     *
     * @param counts a count of each object type
     * @return a complete list of arrangements of the objects
     */

    public static List< List < Integer > > findArrangements(int[] counts)
    {
	List< List < Integer > > l = new LinkedList< List< Integer > >();

	int tot = 0;
	for (int i = 0; i < counts.length; i++)
	    tot += counts[i];

	findArrangements(new ArrayList< Integer >(), counts, tot, l);

	return l;
    }

    /**
     * Adds to the given list all of the rearrangements that complete the
     * gien partial rearrangements with the number of each type of
     * object given in the array.
     *
     * @param partial a partial arrangement
     * @param counts the number of each item left to add to the
     * partial arrangement
     * @param totalCount the total of the values in counts
     * @param the list to add complete arrangements to
     */
    
    private static void findArrangements(List< Integer > partial, int[] counts, int totalCount, List< List< Integer > > l)
    {
	if (totalCount == 0)
	    l.add(new ArrayList< Integer >(partial));
	else
	    {
		for (int item = 0; item < counts.length; item++)
		    if (counts[item] > 0)
			{
			    partial.add(item);
			    counts[item]--;
			    totalCount--;

			    findArrangements(partial, counts, totalCount, l);

			    partial.remove(partial.size() - 1);
			    counts[item]++;
			    totalCount++;
			}
	    }
    }
}
