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
package au.com.cybersearch2.taq.debug;

import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.service.LoopMonitor;

/**
 * ExecutionContext
 * @author Andrew Bowley
 * 17May,2017
 */
public class ExecutionContext
{
	public static final String DEBUG = "DEBUG";
	public static final String CONTEXT_NOT_SET = "Execution context not set";

	private final LoopMonitor loopMonitor;
	private boolean isDebug;
    private boolean caseInsensitiveNameMatch;
    private int loopTimeout;
    private int loopThreshold;
    	
    public ExecutionContext()
    {
    	this(false, false);
    }
    
    public ExecutionContext(boolean isDebug, boolean caseInsensitiveNameMatch)
    {
    	this.isDebug = isDebug;
    	this.caseInsensitiveNameMatch = caseInsensitiveNameMatch;
    	loopMonitor = new LoopMonitor();
    }
    
    public ExecutionContext(ExecutionContext contextToCopy)
    {
    	this(contextToCopy.isDebug, contextToCopy.caseInsensitiveNameMatch);
    }
    
    public void beforeEvaluate(Operand operand)
    {
    }

	public boolean isDebug() {
		return isDebug;
	}

	public boolean isCaseInsensitiveNameMatch() {
		return caseInsensitiveNameMatch;
	}
 
	public void setDebug(boolean isDebug) {
		this.isDebug = isDebug;
	}

	public void setCaseInsensitiveNameMatch(boolean caseInsensitiveNameMatch) {
		this.caseInsensitiveNameMatch = caseInsensitiveNameMatch;
	}

	public int getLoopTimeout() {
		return loopTimeout;
	}

	public void setLoopTimeout(int loopTimeout) {
		this.loopTimeout = loopTimeout;
	}

	public int getLoopThreshold() {
		return loopThreshold;
	}

	public void setLoopThreshold(int loopThreshold) {
		this.loopThreshold = loopThreshold;
	}

	public LoopMonitor getLoopMonitor() {
		if (loopTimeout > 0)
			loopMonitor.setTimeoutSecs(loopTimeout);
		if (loopThreshold > 0)
			loopMonitor.setThreshold(loopThreshold);
		return loopMonitor;
	}
	
	public static boolean isDebugMode() {
		String isDebugMode = System.getProperty(DEBUG);
		return isDebugMode != null && isDebugMode.equalsIgnoreCase("true");
	}

}
