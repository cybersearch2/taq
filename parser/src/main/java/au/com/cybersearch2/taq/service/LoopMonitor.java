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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.j256.simplelogging.Logger;

import au.com.cybersearch2.taq.log.LogManager;

/**
 * Monitors for an infinite loop caused by a scripted short circuit condition failure.
 * There is a timeout trigger (default 2 seconds) and a loop count trigger (default 10,000).
 * Both are configurable to allow for tuning to meet special cases.
 * When loops are nested, each is tracked separately.
 */
public class LoopMonitor implements LoopTracker.InterruptHandler {

	/** Continuous loop timeout */
	public static int DEFAULT_TIMEOUT = 2;
	/** Excessive loop count threshold */
	public static int DEFAULT_TRESHOLD = 10000;
    /** Maximum error trace level */
	private static final int MAX_TRACE_COUNT = 120;
	/** Logger */
	private static final Logger logger = LogManager.getLogger(LoopMonitor.class);
	
	/** Nested loop stack */
	private final Deque<LoopTracker> trackerStack;

	/** Current loop tracker subject to timeout monitoring */
	private LoopTracker loopTracker;
	/** Dedicated timer */
	private MonitorTimer timer;
	/** Timeout value in seconds */
	private int timeoutSecs;
	/** Max iteration threshold */
	private int threshold;
	/** Pool of tracker objects */
	private List<LoopTracker> trackerPool;
	
	/** 
	 * Construct LoopMonitor object
	 */
	public LoopMonitor() {
		trackerStack = new ArrayDeque<>();
		timeoutSecs = DEFAULT_TIMEOUT;
		threshold = DEFAULT_TRESHOLD;
		trackerPool = new ArrayList<>();
	}

	public void setTimeoutSecs(int timeoutSecs) {
		this.timeoutSecs = timeoutSecs;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	/**
	 * Start monitoring at start of program loop, which may be
	 * nested inside another loop
	 */
	public void push() {
		enterMonitor(false);
	}
	
	/**
	 * Start monitoring at start of program loop, which may be
	 * nested inside another loop, but only track loop iteration count.
	 */
	public void startLoopMonitor() {
		enterMonitor(true);
	}
	
	/**
	 * Stop monitoring track loop iteration count at exit of program loop
	 */
	public void stopLoopMonitor() {
		pop();
	}
	
	/**
	 * Go back to monitoring previous loop or stop monitoring if not required
	 */
	public void pop() {
		if (!trackerStack.isEmpty()) {
			trackerStack.pop();
			LoopTracker nextLoopTracker = null;
			if (!trackerStack.isEmpty())
				nextLoopTracker = trackerPool.get(trackerStack.size() - 1);
			// Add time taken in loop just exited to next reschedule
			long slack = System.currentTimeMillis() - loopTracker.getStart();
			if (nextLoopTracker != null) {
				if (loopTracker.isScheduled())
					loopTracker.quiesce();
				trackerStack.forEach(tracker -> tracker.addSlack(slack));
				loopTracker = nextLoopTracker;
				if (loopTracker.isScheduled() || loopTracker.isQuiesced())
				    loopTracker.reschedule();
			} else
				trackerStack.forEach(tracker -> {
					tracker.reset(); 
				});
		}
	}

	/**
	 * Increment loop iteration count and return flag set true if the maximum
	 * threshold has been reached
	 * @return boolean
	 */
	public boolean tick() {
		return loopTracker.tick();
	}

	/**
	 * Returns flag set true if a monitor interrupt has occurred
	 * @return boolean
	 */
	public boolean isInterrupted() {
		return loopTracker.isAlive();
	}
	
	@Override
	public void interrupt(LoopTracker loopTracker, Thread loopThread) {
		// Loop monitor has fired, so cancel all trackers
		trackerStack.forEach(tracker -> {
			tracker.reset(); 
		});
		StackTraceElement[] stackTrace = loopThread.getStackTrace();
		int index = 0;
		// Capture top of stack trace of thread being interrupted
		StringBuilder builder = new StringBuilder("Timeout while in program loop");
		for (StackTraceElement element: stackTrace) {
			if (index++ > 0)
				builder.append(',');
			String trace = element.toString();
			builder.append(trace);
			if (trace.contains("TemplateOperand"))
				break;
			if (index == MAX_TRACE_COUNT)
				break;
		}
		logger.error(builder.toString());
	}

	@Override
	public void reschedule(LoopTracker loopTracker) {
		//System.out.println("Timer " + loopTracker.getId() + " rescheduled");
		timer.schedule(loopTracker);
	}

	private void startMonitor() {
		if (timer == null)
			timer = new MonitorTimer();
		//System.out.println("Timer " + loopTracker.getId() + " started");
		timer.schedule(loopTracker);
	}

	/**
	 * Start monitoring at start of program loop, which may be
	 * nested inside another loop
	 * @param noSchedule Flag set true if only monitoring loop iteration
	 */
	private void enterMonitor(boolean noSchedule) {
		if ((loopTracker != null) && loopTracker.isScheduled() && noSchedule)
			loopTracker.quiesce();
		// If first time entry to new nest depth, add a new
		// loop tracker to the pool for these objects
		int nestDepth = trackerStack.size();
		if (nestDepth >= trackerPool.size()) {
	         loopTracker = new LoopTracker(this);
			 trackerPool.add(loopTracker);
		} else			
			 loopTracker = trackerPool.get(nestDepth);
		trackerStack.push(loopTracker);
		// A scheduled loop tracker will resume waiting for a loop exit 
		if ((loopTracker.isScheduled() || (loopTracker.isQuiesced())))
			loopTracker.reschedule();
		else {
			if (noSchedule)
				loopTracker.start(0, threshold);
			else {
			    loopTracker.start(timeoutSecs * 1000L, threshold);
	            startMonitor();
			}
		}
	}

}
