package com.bloxomo.synchronization;

/**
 * A barrier.  A barrier is used to synchonize steps between many threads.
 * No thread will exit <CODE>cross</CODE> until all threads associated
 * with this barrier have called <CODE>cross</CODE>.
 *
 * @author Jim Glenn
 * @version 0.1 1/26/2004
 */

public class Barrier
{
    /**
     * The number of threads that must cross this barrier.
     */

    private int numThreads;

    /**
     * The number of threads that have begun to cross this barrier.
     */

    private int crossing = 0;

    /**
     * Creates a barrier to synchronize the given number of threads.
     *
     * @param n the number of threads that must cross this barrier
     */

    public Barrier(int n)
    {
	numThreads = n;
	crossing = 0;
    }

    /**
     * Attempts to cross this barrier.  A call to this method will not
     * return until all threads have called it.
     */

    public synchronized void cross()
    {
	if (crossing == numThreads - 1)
	    {
		crossing = 0; // reset for repeated use
		notifyAll(); // wake up all blocked threads
	    }
	else
	    {
		try
		    {
			crossing++;
			wait();
		    }
		catch (InterruptedException stupid)
		    {
		    }
	    }
    }
}
    
