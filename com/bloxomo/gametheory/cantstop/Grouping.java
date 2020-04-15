package com.bloxomo.gametheory.cantstop;

import com.bloxomo.gametheory.*;
import com.sirmapsalot.combinatorics.Multiset;

/**
 * Models ways to group the dice.  A grouping distinguishes between groups
 * that are being used and those that are not.  It may be illegal to
 * not use certain groups; that is accounted for elsewhere.
 */

public class Grouping implements Comparable
{
    private Multiset usedTotals;
    private Multiset unusedTotals;
    
    public Grouping(Multiset used, Multiset unused)
    {
	usedTotals = used;
	unusedTotals = unused;
    }
    
    public Multiset getUsed()
    {
	return usedTotals;
    }
    
    public Multiset getUnused()
    {
	return unusedTotals;
    }
    
    public boolean equals(Object o)
    {
	return (o instanceof Grouping
		&& ((Grouping)o).usedTotals.equals(usedTotals)
		&& ((Grouping)o).unusedTotals.equals(unusedTotals));
    }
    
    public int hashCode()
    {
	return usedTotals.hashCode() ^ unusedTotals.hashCode();
    }
    
    public int compareTo(Object o)
    {
	Grouping other = (Grouping)o;
	
	if (usedTotals.compareTo(other.usedTotals) != 0)
	    return usedTotals.compareTo(other.usedTotals);
	else
	    return unusedTotals.compareTo(other.unusedTotals);
    }
    
    public String toUglyString()
    {
	return usedTotals.toString() + "-" + unusedTotals.toString();
    }

    public String toString()
    {
	StringBuffer result = new StringBuffer();

	for (int tot = 2; tot <= 12; tot++)
	    {
		for (int i = 0; i < usedTotals.countItem(tot); i++)
		    result.append("" + tot + " ");
	    }

	result.append("/");

	for (int tot = 2; tot <= 12; tot++)
	    {
		for (int i = 0; i < unusedTotals.countItem(tot); i++)
		    result.append("" + tot + " ");
	    }

	return result.toString();
    }
}
