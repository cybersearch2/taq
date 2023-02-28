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
package au.com.cybersearch2.taq.expression;

import java.util.Collections;
import java.util.List;

import com.j256.simplelogging.Logger;

import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.CallEvaluator;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.log.LogManager;
import au.com.cybersearch2.taq.pattern.SelectCallEvaluator;
import au.com.cybersearch2.taq.pattern.Template;

/**
 * CallOperand Variable which is set using a supplied function object and
 * parameters contained in a template.
 * 
 * @author Andrew Bowley 7 Aug 2015
 */
public class CallOperand extends Variable {
	protected static List<Term> EMPTY_TERM_LIST = Collections.emptyList();
	/** Template containing parameters or null for no arguments */
	private final Template template;
	/** Injected call evaluator */
	private final CallEvaluator callEvaluator;
	/** Logger */
	private final Logger logger;

	/**
	 * Construct a CallOperand object
	 * 
	 * @param qname         Qualified name
	 * @param template      Template containing parameters or null for no arguments
	 * @param callEvaluator Evaluates function
	 */
	public CallOperand(QualifiedName qname, Template template, CallEvaluator callEvaluator) {
		super(qname);
		this.callEvaluator = callEvaluator;
		this.template = template;
		logger = LogManager.getLogger(this.getClass());
	}

	/**
	 * Execute operation for expression
	 * 
	 * @param id Identity of caller, which must be provided for backup()
	 * @return EvaluationStatus
	 */
	public EvaluationStatus evaluate(int id) {
		List<Term> termList;
		if (template != null) {
			template.evaluate(context);
			termList = template.toArray();
		} else
			termList = EMPTY_TERM_LIST;
		// The left operand is set if a query call without a receiver is unresolved
		Operand operand = getLeftOperand();
		if (operand != null)
			super.evaluate(id);
		else
			doCall(termList);
		this.id = id;
		return EvaluationStatus.COMPLETE;
	}

	protected boolean doCall(List<Term> termList) {
		boolean success = callEvaluator.evaluate(termList);
		setValue(callEvaluator.getValue());
		// Exclude logging of select evaluation failure as this is expected  
		if (!success && !(callEvaluator instanceof SelectCallEvaluator))
			logger.warn(String.format("Function %s failed", callEvaluator.getName()));
		return success;
	}

	/**
	 * Backup to initial state if given id matches id assigned on unification or
	 * given id = 0.
	 * 
	 * @param id Identity of caller.
	 * @return boolean true if backup occurred
	 * @see au.com.cybersearch2.taq.language.Parameter#unify(Term otherParam, int
	 *      id)
	 */
	@Override
	public boolean backup(int id) {
		if (template != null)
			template.backup(id != 0);
		callEvaluator.backup(id);
		Operand rightOperand = getRightOperand();
		if (rightOperand != null)
			rightOperand.backup(id);
		return super.backup(id);
	}

	/**
	 * @see au.com.cybersearch2.taq.expression.Variable#toString()
	 */
	@Override
	public String toString() {
		if (empty) {
			StringBuilder builder = new StringBuilder(qname.toString());
			builder.append('(');
			if (template != null) {
				Term op1 = template.getTermByIndex(0);
				builder.append(op1.toString());
				int count = template.getTermCount();
				if (count > 1) {
					Term op2 = template.getTermByIndex(count - 1);
					builder.append(" ... ").append(op2.toString());
				}
			}
			builder.append(')');
			return builder.toString();
		}
		return super.toString();
	}

}
