/** Copyright 2022 Andrew J Bowley

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. */
package au.com.cybersearch2.taq.db;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.provider.ResourceMonitor;
import au.com.cybersearch2.taq.service.WorkerService;

/**
 * Relational database resource provider 
 *
 * @param <C> Entity collector customized for database implementation
 * @param <E> Entity emitter customized for database implementation
 */
public abstract class DatabaseProvider<C extends EntityCollector<?>,E extends EntityEmitter<?>> 
                                      extends ResourceMonitor implements EntityAgent<C,E> {
	/**
	 * Drop all tables that can be created by this provider and recreate them
	 */
	public abstract void dropAllTables();

	/** 
	 * Returns path to database file 
	 */
	public abstract String getDatabasePath();

	/**
	 * Turns on monitoring records written to the database, sending them to the console 
	 */
	public abstract void logToConsole();

	/**
	 * Returns connection profile
	 * @return ConnectionProfile object
	 */
	public abstract ConnectionProfile getConnectionProfile();

	/**
	 * Returns fixed thread pool service to allow the emitter to operate asynchronously
	 */
	public WorkerService getWorkerService() {
		return QueryProgram.getWorkerService();
	}

}
