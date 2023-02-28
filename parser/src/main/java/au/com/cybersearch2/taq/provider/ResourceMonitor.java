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
package au.com.cybersearch2.taq.provider;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.interfaces.ResourceProvider;

/**
 * Resource provider which can notify it's clients of open and close events
 *
 */
public abstract class ResourceMonitor implements ResourceProvider {

	public static interface EventHandler {
		
		void onOpen();
		void onClose();
	}
	
	private final List<EventHandler> eventHandlers;
	
	private boolean isOpen;
    private Deque<LocaleAxiomListener> listenerChain;
    private Map<String,Object> connectionProperties;

	/**
	 * Construct ResourceMonitor object
	 */
	public ResourceMonitor() {
		
		eventHandlers = new LinkedList<>();
		connectionProperties = Collections.emptyMap();
	}

	public void addHandler(EventHandler handler) {
		eventHandlers.add(handler);
		if (isOpen)
			handler.onOpen();
	}
	
	/**
	 * Returns connection properties
	 * @return properties map
	 */
	public Map<String,Object> getConnectionProperties() {
		return connectionProperties;
	}

	/**
	 * Set connection properties
	 * @param properties Properties map
	 */
	public void setConnectionProperties(Map<String, Object> properties) {
		if (connectionProperties.isEmpty())
			connectionProperties = new HashMap<>();
		connectionProperties.putAll(properties);
	}
	
    @Override
    public void close()
    {
    	onClose();
    }

    @Override
	public boolean chainAxiomListener(LocaleAxiomListener axiomListener) {
		if (listenerChain == null)
		    listenerChain = new ArrayDeque<>();
	    listenerChain.push(axiomListener);
	    return true;
    }

	protected void onOpen() {
		isOpen = true;
		eventHandlers.forEach(handler -> handler.onOpen());
	}
	
	protected void onClose() {
		isOpen = false;
		eventHandlers.forEach(handler -> handler.onClose());
	}

	protected boolean isListenerChainEmpty() {
		return (listenerChain == null) || listenerChain.isEmpty();
	}

	protected LocaleAxiomListener listenerChainInstance() {
		return new ListenerChain(listenerChain);
	}

}
