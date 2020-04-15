package com.sirmapsalot.combinatorics;

/**
 * An ordered pair of objects.
 *
 * @author Jim Glenn
 * @version 0.1 11/14/2008 based on Pair v0.1
 */

public class Triple < T1, T2, T3 >
{
    /**
     * The three objects in this triple.
     */

    private T1 x;
    private T2 y;
    private T3 z;

    /**
     * Constructs the given ordered triple.
     *
     * @param first the first component of the new triple     
     * @param second the second component of the new triple
     * @param third the third component of the new triple
     */

    public Triple(T1 first, T2 second, T3 third)
    {
	x = first;
	y = second;
	z = third;
    }

    /**
     * Returns the first component of this triple.
     *
     * @return the first component of this triple
     */

    public T1 getFirst()
    {
	return x;
    }

    /**
     * Returns the second component of this triple.
     *
     * @return the second component of this triple
     */

    public T2 getSecond()
    {
	return y;
    }

    /**
     * Returns the third component of this triple.
     *
     * @return the third component of this triple
     */

    public T3 getThird()
    {
	return z;
    }

    /**
     * Sets the first component of this triple to the given value.
     *
     * @param obj the new first component
     */

    public void setFirst(T1 obj)
    {
	x = obj;
    }

    /**
     * Sets the second component of this triple to the given value.
     *
     * @param obj the new second component
     */

    public void setSecond(T2 obj)
    {
	y = obj;
    }

    /**
     * Sets the third component of this triple to the given value.
     *
     * @param obj the new third component
     */

    public void setThird(T3 obj)
    {
	z = obj;
    }

    /**
     * Determines if this triple is equal to the given object.
     *
     * @param o the object to compare this triple to
     * @return true iff <CODE>o</CODE> is a <CODE>Triple</CODE> whose components
     * match those of this triple
     * @throws ClassCastException if the given object is not a <CODE>Triple</CODE>
     */

    public boolean equals(Object o)
    {
	if (o instanceof Triple)
	    return equals((Triple)o);
	else
	    return false;
    }

    /**
     * Determines if this triple is equal to the given triple.  Two triples are
     * equal iff their first components are equal (according to their
     * <CODE>equals</CODE> methods) and their second components are equal
     * (again, according to their <CODE>equals</CODE> methods).
     *
     * @param p the triple to compare to
     * @return true iff the two triples are equal
     */

    public boolean equals(Triple p)
    {
	boolean result =  (x.equals(p.x) && y.equals(p.y) && z.equals(p.z));
	return result;
    }

    /**
     * Returns a hascode for this triple.
     *
     * @return a hashcode for this triple
     */

    public int hashCode()
    {
	int hash =  x.hashCode() ^ y.hashCode() ^ z.hashCode();
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

	result.append(", ");

	if (z != null)
	    result.append(z.toString());
	else
	    result.append("null");

	result.append(")");

	return result.toString();
    }
}

