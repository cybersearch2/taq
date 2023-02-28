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

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Schedules supervisory timeout task for excessive duration in program loop.
 * The tasks being scheduled are expected to run only rarely, if ever.
 *
 */
public class MonitorTimer {

	/** Scheduler */
	private class TimerWorker implements Runnable {

		private LoopTracker loopTracker;
		private Semaphore semaphore;

		public TimerWorker() {
			semaphore = new Semaphore(1);
			semaphore.acquireUninterruptibly();
		}
		
		@Override
		public void run() {
			while (true) {
				// Wait for task to be scheduled
				try {
			        semaphore.acquire();
				} catch (InterruptedException e) {
					return;
				}
				// Wait for timeout
				try {
					// Keep waiting while permit is acquired
					while (semaphore.tryAcquire(loopTracker.getDelay(), TimeUnit.MILLISECONDS) == true) {
					}
					// The task will exit if state has changed from SCHEDULED back to IDLE
					loopTracker.run();
				} catch (InterruptedException e) {
					return;
				}
			}
			
		}

		/**
		 * Schedule given LoopTracker to run
		 * @param loopTracker LoopTracker object
		 */
		public void schedule(LoopTracker loopTracker) {
			this.loopTracker = loopTracker;
			semaphore.release();
		}
		
	}
	
    /** Timer thread */
    private final Thread thread;
	/** Scheduler */
    private final TimerWorker timerWorker;

    /**
     * Construct MonitorTimer object
     */
    public MonitorTimer() {
    	timerWorker = new TimerWorker();
    	thread = new Thread(timerWorker);
    }

	/**
	 * Schedule given LoopTracker to run
	 * @param loopTracker LoopTracker object
	 */
	public void schedule(LoopTracker loopTracker) {
		if (!thread.isAlive())
			thread.start();
		timerWorker.schedule(loopTracker);
	}

}
