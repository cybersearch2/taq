/** Copyright 2023 Andrew J Bowley

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. */
package au.com.cybersearch2.taq.service;

public class LoopTracker {

	interface InterruptHandler {
		void interrupt(LoopTracker loopTracker, Thread loopThread);
		void reschedule(LoopTracker loopTracker);
	}

	private static final long SLACK_THRESHOLD = 100L;

	// Identification and console output code can be uncommented for tracing 
	//private static int idSource;
	
    /** Task waiting to be scheduled */
    static final int IDLE = 0;
    /** Task is scheduled for execution */
    static final int SCHEDULED   = 1;
    /** Task is monitoring loop iterations */
    static final int MONITOR = 2;
    /** Task waiting to be rescheduled */
    static final int QUIESCED = 3;
 
    /** Object is used to control access to the LoopTracker internals */
    private final Object lock;
    /** State of this task either IDLE or CCHEDULED */
    private int state;
    // Id for tracing
	//private final int id;

    /**
     * Next execution time for this task in the format returned by
     * System.currentTimeMillis, assuming this task is scheduled for execution.
     */
    private long nextExecutionTime;
    /** Timeout value in milliseconds */
	private long delay;
	/** Allowance in milliseconds for monitoring the next nested loop */
	private long slack;
	/** Loop iteration count */
	private int ticks;
	/** Start date of monitoring */
	private long start;
	/** Thread being monitored */
	private Thread loopThread;
	/** Loop monitor managing all tracker instances */
	private final InterruptHandler interruptHandler;

	/**
	 * Construct LoopTracker object
	 * @param interruptHandler Loop monitor managing all tracker instances
	 */
	public LoopTracker(InterruptHandler interruptHandler) {
		this.interruptHandler = interruptHandler;
		state = IDLE;
		lock = new Object();
		//id = ++idSource;
	}
	
	//public int getId() {
	//	return id;
	//}

	/**
	 * Sets timeout value in milliseconds. A value of 0 is a reset.
	 * Sets the maximum number of loop iterations threshold
	 * @param delay Timeout value
	 * @param ticks Iteration threshold
	 */
	public void start(long delay, int  ticks) {
		this.delay = delay;
		this.ticks = ticks;
	    loopThread = Thread.currentThread();
	    start = System.currentTimeMillis();
		if (delay > 0) {
		    nextExecutionTime = start + delay;
			synchronized(lock) {
				state = SCHEDULED;
			}
		} else {
			nextExecutionTime = 0; 
			synchronized(lock) {
				state = MONITOR;
			}
		}
	}

	/**
	 * Reset to idle state
	 */
	public void reset() {
		delay = 0;
		ticks = 0;
		resetSlack();
		synchronized(lock) {
			state = IDLE;
		}
	}

	/**
	 * Returns timeout value in milliseconds
	 * @return long
	 */
	public long getDelay() {
		return delay;
	}

	/**
	 * Increment iteration counter. Trigger interrupt if threshold reached.
	 * @return flag set true if max iteration threshold not yet reached
	 */
	public boolean tick() {
		boolean ok = true;
		if ((ticks <= 0) || (--ticks <= 0)) {
			if (isAlive()) {
			    reset();
				interruptHandler.interrupt(this, loopThread);
			}
			ok = false;
		}
		return ok;
	}

	/**
	 * Returns number of milliseconds since Date epoch
	 * @return long
	 */
	public long getStart() {
		return start;
	}

	/**
	 * Returns flag set true if interrupt has not been triggered
	 * @return boolean
	 */
	public boolean isAlive() {
		return (delay > 0) || (ticks > 0);
	}

	/**
	 * Add to allowance in milliseconds for monitoring the next nested loop
	 * @param value Slack duration in milliseconds
	 */
	public void addSlack(long value) {
		slack += value;
	}

	/**
	 * Returns current loop iteration count
	 * @return int
	 */
	public int getTicks() {
		return ticks;
	}

	/**
	 * Scheduled task
	 */
	public void run() {
		if (state == SCHEDULED) 
			interruptHandler.interrupt(this, loopThread);
	}

	/**
	 * Returns flag set true if in scheduled state
	 * @return boolean
	 */
	public boolean isScheduled() {
		return state == SCHEDULED;
	}

	/**
	 * Returns flag set true if in idle state
	 * @return boolean
	 */
	public boolean isIdle() {
		return state == IDLE;
	}

	/**
	 * Returns flag set true if in quiesced state
	 * @return boolean
	 */
	public boolean isQuiesced() {
		return state == QUIESCED;
	}

	public void quiesce() {
		synchronized(lock) {
			state = QUIESCED;
		}
	}
	
	/**
	 * Reschedule timeout
	 * @return flag set true if timeout initiated
	 */
	public boolean reschedule() {
		if (state == QUIESCED)
			synchronized(lock) {
				state = SCHEDULED;
			}
		if (isAlive()) {
			delay = slack + nextExecutionTime - System.currentTimeMillis();
			slack = 0;
			if (delay > SLACK_THRESHOLD) {
			    start = System.currentTimeMillis();
			    nextExecutionTime = start + delay;
			    interruptHandler.reschedule(this);
			    return true;
			}
			//System.out.println("Timer " + getId() + " run at " + Instant.now().toString());
	        reset();
		}
		return false;
	}
	
	private void resetSlack() {
		slack = 0;
	}


}
