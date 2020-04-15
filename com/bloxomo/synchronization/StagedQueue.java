package com.bloxomo.synchronization;

import java.util.*;

/**
 * A synchronized queue.  Creates multiple consumer threads and one
 * producer thread.  <CODE>dequeue</CODE> blocks for consumer threads.
 * Furthermore, the
 * producer thread is expected to call <CODE>endStage</CODE>
 * at the end of each stage of production, which
 * will block until the queue is empty and all consumer threads have
 * blocked in <CODE>dequeue</CODE>.  Because of this synchronization
 * characteristic, the queue must be told how many consumer threads
 * there are when it is constructed.  Implementations should override
 * <CODE>createConsumer</CODE> and <CODE>createProducer</CODE> to
 * return instances of the appropriate threads.
 * 
 * @author Jim Glenn
 * @version 0.1 10/22/2003
 */

public abstract class StagedQueue
{
    /**
     * The number of consumer threads.
     */

    private int consumers;

    /**
     * A list of objects on this queue.  The head of the queue is the
     * beginning of the list.
     */

    private LinkedList q;

    /**
     * The number of consumers that are blocked in <CODE>dequeue</CODE>.
     */

    private int consumersWaiting;

    /**
     * The producer thread for this queue.
     */

    private Thread producer;

    /**
     * The current state of the producer thread.
     */

    private int producerState;

    /**
     * Constants for producer state.
     */

    private static final int PRODUCING = 0;
    private static final int WAITING = 1;
    private static final int FINISHED = 2;
    private static final int SLEEPING = 3;

    /**
     * Maximum length of the queue
     */

    private static final int MAX_LENGTH = 1024;
    private static final int WAKEUP_THRESHHOLD = 512;
    

    /**
     * Constructs an empty queue served by the given number of consumer
     * threads.  The consumer threads will be created by this constructor.
     *
     * @param n the number of consumer threads to create
     */

    public StagedQueue (int n)
    {
	consumers = n;
	consumersWaiting = 0;
	producerState = PRODUCING;
	q = new LinkedList();
    }

    /**
     * Starts the consumer and producer threads for this queue.
     */

    public synchronized void start()
    {
	producer = createProducer();
	producer.start();

	for (int t = 0; t < consumers; t++)
	    {
		Thread consumer = createConsumer();
		consumer.start();
	    }
    }

    /**
     * Adds the given item to this queue.  The item must not be
     * <CODE>null</CODE>.  This method must not be called by the
     * consumer threads.
     *
     * @param item the object to add
     */

    public synchronized void enqueue(Object item)
    {
	if (Thread.currentThread() != producer)
	    throw new IllegalStateException("enqueue called by non-producer");

	while (q.size() == MAX_LENGTH)
	    {
		producerState = SLEEPING;
		try
		    {
			wait();
		    }
		catch (InterruptedException e)
		    {
		    }
	    }

	q.addLast(item);

	if (consumersWaiting > 0)
	    {
		consumersWaiting--;
		notify();
	    }
    }

    /**
     * Blocks the producer until the queue is empty and all consumers
     * are blocked in <CODE>dequeue</CODE>.  Must not be called by
     * the consumers.
     */

    public synchronized void endStage()
    {
	if (Thread.currentThread() != producer)
	    throw new IllegalStateException("endStage called by non-producer");

	if (consumersWaiting < consumers)
	    {
		producerState = WAITING;
		try
		    {
			wait();
		    }
		catch (InterruptedException thisIsDumb)
		    {
		    }
	    }
    }

    /**
     * Notifies all consumers that the producers are finished.  The
     * consumers will then get <CODE>null</CODE> from their
     * next <CODE>dequeue</CODE>.  Must notbe called by the consumers.
     */

    public synchronized void finish()
    {
	if (Thread.currentThread() != producer)
	    throw new IllegalStateException("enqueue called by non-producer");

	producerState = FINISHED;
	consumersWaiting = 0;
	notifyAll();
    }

    /**
     * Removes and returns an item from this queue.  If there are no items
     * to remove then the calling thread is blocked until it gets an item
     * or the producer finishes, in which case <CODE>null</CODE> is
     * returned.
     *
     * @return the item removed from this queue
     */

    public synchronized Object dequeue()
    {
	while (q.size() == 0 && producerState != FINISHED)
	    {
		consumersWaiting++;

		if (producerState == PRODUCING
		    || (consumersWaiting < consumers
			&& producerState == WAITING))
		    {
			try
			    {
				wait();
			    }
			catch (InterruptedException thisIsDumb)
			    {
			    }
		    }
		else if (consumersWaiting == consumers
			 && producerState == WAITING)
		    {
			producerState = PRODUCING;
			consumersWaiting = 1;
			notifyAll();
			try
			    {
				wait();
			    }
			catch (InterruptedException thisIsDumb)
			    {
			    }
		    }
	    }

	// either something is on the queue or we were notified that
	// the producer is finished
	
	Object result;
	
	if (q.size() > 0)
	    {
		result = q.removeFirst();

		if (producerState == SLEEPING && q.size() <= WAKEUP_THRESHHOLD)
		    {
			producerState = PRODUCING;
			notifyAll();
		    }
	    }
	else
	    result = null;
	
	return result;
	
    }

    /**
     * Returns a producer thread.
     */

    protected abstract Thread createProducer();

    /**
     * Returns a consumer thread.
     */

    protected abstract Thread createConsumer();
}

