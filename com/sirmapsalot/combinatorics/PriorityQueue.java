package com.sirmapsalot.combinatorics;

import java.util.*;

/**
 * A priority queue.  When items are added, they are given priorities.
 * Only the item with lowest priority can be removed.  Priorities of
 * items on the queue can be changed.
 *
 * @author Jim Glenn
 * @version 0.1 2/12/2003
 */

public class PriorityQueue
{
    /**
     * Maps items to entries in the itemTable vector.
     */

    private HashMap itemMap;

    /**
     * Keeps track of an item's location in the heap.
     * <CODE>itemTable[itemTable[itemMap.get(o)]</CODE>
     * (an abuse of [] notation) should be the heap element
     * corresponding to <CODE>o</CODE>.
     */

    private Vector itemTable;

    /**
     * The heap used to implement this priority queue.
     */

    private Vector itemHeap;

    /**
     * The number of items in this priority queue.
     */

    private int size;

    /**
     * Information necessary to store on the heap: 
     * its priority and a pointer back into the <CODE>heapIndex</CODE>
     * array.
     */

    private class HeapEntry
    {
	public HeapEntry(double pri, int index)
	{
	    priority = pri;
	    tableIndex = index;
	}

	public String toString()
	{
	    return "(" + priority + " " + tableIndex + ")";
	}

	private double priority;
	private int tableIndex;
    }

    /**
     * Information necessary to store in the table:
     * an item and its location in the heap.
     */

    private class TableEntry
    {
	public TableEntry(Object it, int index)
	{
	    item = it;
	    heapIndex = index;
	}

	public String toString()
	{
	    return "(" + item + ", " + heapIndex + ")";
	}

	private Object item;
	private int heapIndex;
    }

    /**
     * Constructs an empty queue.
     */

    public PriorityQueue()
    {
	itemMap = new HashMap();
	itemHeap = new Vector();
	itemTable = new Vector();
	size = 0;
    }

    /**
     * Returns the size of this priority queue.
     *
     * @return the size of this queue
     */

    public int getSize()
    {
	return size;
    }

    /**
     * Adds an item to this queue.
     *
     * @param item the item to add
     * @param pri the new item's priority
     */

    public void addItem(Object item, double pri)
    {
	int index = itemHeap.size();

	itemMap.put(item, new Integer(index));
	itemHeap.add(new HeapEntry(pri, index));
	itemTable.add(new TableEntry(item, index));
	size++;

	reheapUp(index);
    }

    /**
     * Returns the lowest priority on this queue.
     *
     * @return the lowest priority
     */

    public double peekPriority()
    {
	HeapEntry heapTop = (HeapEntry)(itemHeap.elementAt(0));
	return heapTop.priority;
    }

    /**
     * Returns the item with the lowest priority on this queue.
     *
     * @return the item with the lowest priority
     */

    public Object peekTop()
    {
	HeapEntry heapTop = (HeapEntry)(itemHeap.elementAt(0));
	return (TableEntry)(itemTable.elementAt(heapTop.tableIndex));
    }

    /**
     * Removes the item with the lowest priority from this queue.
     *
     * @return the item with the lowest priority
     */

    public Object removeItem()
    {
	HeapEntry heapTop = (HeapEntry)(itemHeap.elementAt(0));
	Object result = ((TableEntry)(itemTable.elementAt(heapTop.tableIndex))).item;

	// move last elt to top of heap and remove old top

	swap(0, size - 1);
	itemHeap.removeElementAt(size - 1);

	// fix table

	TableEntry temp = (TableEntry)(itemTable.elementAt(size - 1));
	itemTable.removeElementAt(size - 1);
	if (heapTop.tableIndex != size - 1)
	    {
		itemTable.setElementAt(temp, heapTop.tableIndex);
		((HeapEntry)(itemHeap.elementAt(temp.heapIndex))).tableIndex = heapTop.tableIndex;
	    }


	// fix hash

	itemMap.remove(result);
	itemMap.put(temp.item, new Integer(heapTop.tableIndex));

	// restore heap order

	size--;
	reheapDown(0);

	return result;
    }

    /**
     * Restores the heap property upwards starting from the given index.
     *
     * @param index an index of a node in the heap
     */

    private void reheapUp(int index)
    {
	int parent = (index - 1) / 2;
	while (index > 0 && getPriority(index) < getPriority(parent))
	    {
		swap(index, parent);
		index = parent;
		parent = (index - 1) / 2;
	    }
    }

    /**
     * Restores the heap order property downwards starting from the
     * given index.
     *
     * @param index an index of a node in the heap
     */

    private void reheapDown(int index)
    {
	int leftChild = index * 2 + 1;
	int rightChild = leftChild + 1;

	while ((leftChild < size
		&& getPriority(leftChild) < getPriority(index))
	       || (rightChild < size
		   && getPriority(rightChild) < getPriority(index)))
	    {
		if (rightChild >= size || getPriority(leftChild) < getPriority(rightChild))
		    {
			swap(index, leftChild);
			index = leftChild;
		    }
		else
		    {
			swap(index, rightChild);
			index = rightChild;
		    }

		leftChild = index * 2 + 1;
		rightChild = leftChild + 1;
	    }
    }

    /**
     * Swaps items at the given locations in the heap.
     *
     * @param i the index of one item to swap
     * @param j the index of the other item to swap
     */
    
    private void swap(int i, int j)
    {
	// swap entries in the heap

	HeapEntry one = (HeapEntry)(itemHeap.elementAt(i));
	HeapEntry two = (HeapEntry)(itemHeap.elementAt(j));
	itemHeap.setElementAt(two, i);
	itemHeap.setElementAt(one, j);

	// fix entries in the table

	((TableEntry)(itemTable.elementAt(one.tableIndex))).heapIndex = j;
	((TableEntry)(itemTable.elementAt(two.tableIndex))).heapIndex = i;
    }

    /**
     * Returns the priority of the given heap element.
     *
     * @param index the index in the heap to examine
     * @return the priority of the item there
     */

    private double getPriority(int index)
    {
	return ((HeapEntry)(itemHeap.elementAt(index))).priority;
    }

    /**
     * Returns the priority of the given item.
     *
     * @param item an item on this queue
     * @return the priority of that item
     */

    private double getPriority(Object item)
    {
	int tableLoc = ((Integer)(itemMap.get(item))).intValue();
	int heapLoc = ((TableEntry)(itemTable.elementAt(tableLoc))).heapIndex;
	return ((HeapEntry)(itemHeap.elementAt(heapLoc))).priority;
    }

    /**
     * Changes the priority of the given item on this priority queue.
     *
     * @param item the item to change the priority of
     * @param pri the new priority of that item
     */

    public void changePriority(Object item, double pri)
    {
	int tableLoc = ((Integer)(itemMap.get(item))).intValue();
	int heapLoc = ((TableEntry)(itemTable.elementAt(tableLoc))).heapIndex;
	double oldPriority = ((HeapEntry)(itemHeap.elementAt(heapLoc))).priority;
	((HeapEntry)(itemHeap.elementAt(heapLoc))).priority = pri;
	if (pri < oldPriority)
	    reheapUp(heapLoc);
	else
	    reheapDown(heapLoc);
    }

    /**
     * Changes the priority of the given item in this queue
     * if the new priority is less than the current priority.
     * If the item is not in this queue it is added.
     *
     * @param item an item in this queue
     * @param pri the new priority of that item
     * @return true iff the new priority was lower than the old of the item is new
     */

    public boolean decreasePriority(Object item, double pri)
    {
	if (itemMap.containsKey(item))
	    {
		double oldPriority = getPriority(item);
		
		if (pri < oldPriority)
		    changePriority(item, pri);
		
		return (pri < oldPriority);
	    }
	else
	    {
		addItem(item, pri);
		return true;
	    }
    }

    public String toString()
    {
	return "table: " + itemTable + "\nheap: " + itemHeap;
    }
}
    
