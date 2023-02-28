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

import java.util.List;

import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.interfaces.AxiomContainer;
import au.com.cybersearch2.taq.interfaces.CallEvaluator;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.ReceiverHandler;

/**
 * Evaluates function, and if receiver template attached, evaluates it
 */
public class FunctionCallEvaluator implements CallEvaluator {

	/** Function call handler */
	private final CallHandler callHandler;
    /** Receiver template handler - may be null */
	private ReceiverHandler receiverHandler;

	/**
	 * Construct FunctionCallEvaluator object
	 * @param callHandler Function call handler
	 */
	public FunctionCallEvaluator(CallHandler callHandler) {
		this.callHandler = callHandler;
	}

	/**
	 * Set receiver handler to process each call handler solution
	 * @param receiverHandler ReceiverHandler object
	 */
    public void setReceiverHandler(ReceiverHandler receiverHandler) {
		this.receiverHandler = receiverHandler;
		AxiomContainer axiomContainer = callHandler.getAxiomContainer();
		if (axiomContainer != null) 
		    receiverHandler.setAxiomContainer(axiomContainer);
	}

    @Override
	public boolean evaluate(List<Term> argumentList) {
		boolean success = callHandler.evaluate(argumentList);
		if (success && (receiverHandler != null)) 
	    	receiverHandler.setReceiver(callHandler.getSolution());
		return success;
	}

	@Override
	public Object getValue() {
		return callHandler.getValue();
	}

	@Override
	public void backup(int id) {
		callHandler.backup(id);
		if (receiverHandler!= null)
			receiverHandler.backup();
	}

	@Override
	public String getName() {
		return callHandler.getName();
	}

	@Override
	public ExecutionContext getExecutionContext() {
		return callHandler.getExectionContext();
	}

}
