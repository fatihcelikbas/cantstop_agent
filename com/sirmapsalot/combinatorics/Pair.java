package com.sirmapsalot.combinatorics;

/**
 * An ordered pair of objects.
 *
 * @author Jim Glenn
 * @version 0.1 8/8/2003
 */

public class Pair < T1, T2 >
{
    /**
     * The two objects in this pair.
     */

    T1 x;
    T2 y;

    /**
     * Constructs the given ordered pair.
     *
     * @param first the first component of the new pair     
     * @param second the second component of the new pair
     */

    public Pair(T1 first, T2 second)
    {
	x = first;
	y = second;
    }

    /**
     * Returns the first component of this pair.
     *
     * @return the first component of this pair
     */

    public T1 getFirst()
    {
	return x;
    }

    /**
     * Returns the second component of this pair.
     *
     * @return the second component of this pair
     */

    public T2 getSecond()
    {
	return y;
    }

    /**
     * Determines if this pair is equal to the given object.
     *
     * @param o the object to compare this pair to
     * @return true iff <CODE>o</CODE> is a <CODE>Pair</CODE> whose components
     * match those of this pair
     * @throws ClassCastException if the given object is not a <CODE>Pair</CODE>
     */

    public boolean equals(Object o)
    {
	return equals((Pair< T1, T2 >)o);
    }

    /**
     * Sets the first component of this pair to the given value.
     *
     * @param obj the new first component
     */

    public void setFirst(T1 obj)
    {
	x = obj;
    }

    /**
     * Sets the second component of this pair to the given value.
     *
     * @param obj the new second component
     */

    public void setSecond(T2 obj)
    {
	y = obj;
    }

    /**
     * Determines if this pair is equal to the given pair.  Two pairs are
     * equal iff their first components are equal (according to their
     * <CODE>equals</CODE> methods) and their second components are equal
     * (again, according to their <CODE>equals</CODE> methods).
     *
     * @param p the pair to compare to
     * @return true iff the two pairs are equal
     */

    public boolean equals(Pair< T1, T2 > p)
    {
	if (x == null && y == null)
	    {
		return p.x == null && p.y == null;
	    }
	else if (x == null)
	    {
		return p.x == null && y.equals(p.y);
	    }
	else if (y == null)
	    {
		return p.y == null && x.equals(p.x);
	    }
	else
	    {
		return x.equals(p.x) && y.equals(p.y);
	    }
    }

    /**
     * Returns a hashcode for this pair.
     *
     * @return a hashcode for this pair
     */

    public int hashCode()
    {
	int hash =  x.hashCode() ^ y.hashCode();
	return hash;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object.
     */

    public String toString()
    {
	StringBuffer result = new StringBuffer("(");
	
	if (x != null)
	    result.append(x.toString());
	else
	    result.append("null");
	
	result.append(", ");

	if (y != null)
	    result.append(y.toString());
	else
	    result.append("null");

	result.append(")");

	return result.toString();
    }
}

