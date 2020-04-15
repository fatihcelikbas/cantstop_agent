package com.bloxomo.gametheory;

import java.util.*;

public class ListOfLists implements Collection
{
    /**
     * None of the lists in this list may be empty.
     */

    private List lists;

    public ListOfLists()
    {
	lists = new LinkedList();
    }

    public boolean add(Object o)
    {
	if (!(o instanceof List))
	    throw new IllegalArgumentException("o must ba a List");

	if (((List)o).isEmpty())
	    {
		return false;
	    }
	else
	    {
		lists.add(o);
		return true;
	    }
    }

    public boolean addAll(Collection c)
    {
	throw new UnsupportedOperationException();
    }

    public void clear()
    {
	lists.clear();
    }

    public boolean contains(Object o)
    {
	throw new UnsupportedOperationException();
    }

    public boolean containsAll(Collection c)
    {
	throw new UnsupportedOperationException();
    }

    public boolean equals(Object o)
    {
	throw new UnsupportedOperationException();
    }

    public int hashCode()
    {
	int hashCode = 0;

	Iterator l = lists.iterator();
	while (l.hasNext())
	    hashCode = hashCode ^ l.next().hashCode();
    
	return hashCode;
    }
	
    public boolean isEmpty()
    {
	throw new UnsupportedOperationException();
    }

    public Iterator iterator()
    {
	return new ListOfListsIterator();
    }

    public boolean remove(Object o)
    {
	throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection c)
    {
	throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection c)
    {
	throw new UnsupportedOperationException();
    }

    public int size()
    {
	int total = 0;

	Iterator l = lists.iterator();
	while (l.hasNext())
	    total += ((List)(l.next())).size();
    
	return total;
    }
	
    public Object[] toArray()
    {
	throw new UnsupportedOperationException();
    }

    public Object[] toArray(Object[] a)
    {
	throw new UnsupportedOperationException();
    }

    private class ListOfListsIterator implements Iterator
    {
	private Iterator outer;
	private Iterator inner;
	private Object nextItem;

	private ListOfListsIterator()
	{
	    nextItem = null;

	    outer = lists.iterator();
	    inner = null;

	    advance();
	}

	public boolean hasNext()
	{
	    return (nextItem != null);
	}

	public Object next()
	{
	    Object result = nextItem;

	    advance();

	    return result;
	}

	public void remove()
	{
	    throw new UnsupportedOperationException();
	}

	private void advance()
	{
	    if (inner != null && inner.hasNext())
		{
		    nextItem = inner.next();
		}
	    else
		{
		    if (outer.hasNext())
			{
			    inner = ((List)(outer.next())).iterator();
			    advance();
			}
		    else
			nextItem = null;
		}
	}
    }


    public static void main(String[] args)
    {
	ListOfLists ll = new ListOfLists();

	ll.add(new LinkedList());

	LinkedList l = new LinkedList();
	l.add(1);
	l.add(2);

	ll.add(l);

	ll.add(new LinkedList());

	LinkedList l2 = new LinkedList();
	l2.add(3);
	l2.add(4);
	l2.add(5);

	ll.add(l2);

	ll.add(new LinkedList());

	Iterator i = ll.iterator();
	while (i.hasNext())
	    System.out.println(i.next());
    }
	
}

