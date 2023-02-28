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
package au.com.cybersearch2.taq.compiler;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.artifact.FunctionArtifact;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.interfaces.CallEvaluator;
import au.com.cybersearch2.taq.language.ITemplate;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.pattern.ReceiverHandler;
import au.com.cybersearch2.taq.pattern.Template;

/**
 * Parser helper to collect function details
 *
 */
public class ParserFunction implements FunctionArtifact {
	
    /** Function name parts - max 2 parts */
	private String[] nameParts;

	/** Qualified function name */
	private QualifiedName qname;
	/** Operand arguments packaged in an inner template or null for no arguments */
	private Template parametersTemplate;
	/** Receiver template or null if none */
	private Template receiver;
	/** Call handler retained in case a receiver is attached */
	private CallEvaluator callEvaluator;
	/** Receiver handler retained, if created, for final access at end of function compilation */
	private ReceiverHandler receiverHandler;

	/**
	 * Construct ParserFunction object
	 * @param qname Function qualified name
	 * @param sourceName Function name as it appears in the soure
	 */
	public ParserFunction(QualifiedName qname, String sourceName) {
		this.qname = qname;
		if (sourceName.indexOf('.') != -1)
		    nameParts = sourceName.split("\\.");
		else
			nameParts = new String[] {sourceName};
		if (nameParts.length > 2) {
			if ((nameParts.length == 3) && !qname.isScopeEmpty() && !qname.isTemplateEmpty()) {
				nameParts = new String[] { nameParts[1], nameParts[2] }; 
			} else
			    throw new ExpressionException(String.format("Function name %s is invalid", sourceName));
		}
	}

	public String getLibrary() {
		return nameParts.length > 1 ? nameParts[0] :QueryProgram.GLOBAL_SCOPE ;
	}
	
	public String getFunctionName() {
		return  nameParts.length > 1 ? nameParts[1] : nameParts[0];
	}
	
	public Template getParametersTemplate() {
		return parametersTemplate;
	}

	public boolean hasReceiver() {
		return receiver != null;
	}

	public CallEvaluator getCallEvaluator() {
		return callEvaluator;
	}

	public void setCallEvaluator(CallEvaluator callEvaluator) {
		this.callEvaluator = callEvaluator;
	}

	public ReceiverHandler getReceiverHandler() {
		return receiverHandler;
	}

	public void setReceiverHandler(ReceiverHandler receiverHandler) {
		this.receiverHandler = receiverHandler;
	}
	
	@Override
	public QualifiedName getName() {
		return qname;
	}

	@Override
	public void setParametersTemplate(ITemplate parametersTemplate) {
		this.parametersTemplate = (Template)parametersTemplate;
	}

	@Override
	public Template getReceiver() {
		return receiver;
	}

	@Override
	public void setReceiver(ITemplate receiver) {
		this.receiver = (Template)receiver;
	}

	/**
	 * Future feature not currently supported
	 */
	@Override
	public void setQuote(String quote) {
		throw new UnsupportedOperationException();
	}

	protected void setName(QualifiedName qname) {
		this.qname = qname;
	}

}
