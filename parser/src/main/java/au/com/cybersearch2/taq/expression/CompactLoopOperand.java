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
package au.com.cybersearch2.taq.expression;

import com.j256.simplelogging.Logger;

import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.log.LogManager;
import au.com.cybersearch2.taq.operator.BooleanOperator;
import au.com.cybersearch2.taq.pattern.Template;
import au.com.cybersearch2.taq.service.LoopMonitor;

/**
 * Contains both a criteria and an expression to conditionally evaluate
 * in a single operand. Supports both branch and loop execution.
 */
public class CompactLoopOperand extends Operand {

    private final static Logger logger = LogManager.getLogger(CompactLoopOperand.class);

	/** Expression to conditionally evaluate */
	private final Operand executeExpression;
	/** Boolean operator */
	private final Operator operator;
	/** Unique id to identify this operand */
	private final int modificationdId;
	/** Flag set true if a branch ie. only executes once */
	private final boolean runOnce;
    
    /**
     * Creates CompactLoopOperand object
     * @param factExpression Expression subject to fact check
     * @param executeExpression Expression to execute
     * @param runOnce Flag set true if only to run once
      */
	public CompactLoopOperand(Operand factExpression, Operand executeExpression, boolean runOnce) {
		super(Term.ANONYMOUS);
		this.executeExpression = executeExpression;
		this.runOnce = runOnce;
		modificationdId = Template.getUniqueId();
		operator = new BooleanOperator();
		setLeftOperand(factExpression);
		// Do not pass boolean value of this operand to query solution
		setPrivate(true);
	}

	@Override
	public void assign(Parameter parameter) {
	}

	@Override
	public QualifiedName getQualifiedName() {
		return QualifiedName.ANONYMOUS;
	}

	@Override
	public Operand getRightOperand() {
		return executeExpression;
	}

	@Override
	public Operator getOperator() {
		return operator;
	}

	@Override
	public EvaluationStatus evaluate(int id) {
		EvaluationStatus status = EvaluationStatus.COMPLETE;
		Operand factExpression = getLeftOperand();
   		if (!factExpression.isEmpty())  {
			if (FactOperand.analyseValue(factExpression.getValue())) {
				executeExpression.setExecutionContext(context);
				status = executeExpression.evaluate(modificationdId);
			}
		} else {
			factExpression.setExecutionContext(context);
			executeExpression.setExecutionContext(context);
			LoopMonitor loopMonitor = null;
			if (!runOnce) {
				loopMonitor = context.getLoopMonitor();
				loopMonitor.startLoopMonitor();
			}
			while (status == EvaluationStatus.COMPLETE) {
				if (!factExpression.isEmpty())
				    factExpression.backup(modificationdId);
				if (!executeExpression.isEmpty())
					executeExpression.backup(modificationdId);
				status = factExpression.evaluate(modificationdId);
				if (status == EvaluationStatus.COMPLETE) {
					Object toTest = factExpression.getValue();
					boolean evaluate;
					if (toTest instanceof Boolean)
						evaluate = ((Boolean)toTest).booleanValue();
					else 
						evaluate = FactOperand.analyseValue(toTest);
					if (evaluate)
						status = executeExpression.evaluate(modificationdId);
					else
						break;
				} else
					break;
				if (!runOnce) {
					if (!loopMonitor.tick()) {
				    	logger.error("Loop monitor terminated compact loop execution");
				    	break;
					}
				} else
					break;
			}
			if (!runOnce)
				loopMonitor.stopLoopMonitor();
		}
		setValue(status == EvaluationStatus.COMPLETE);
		setId(id);
		return status;
	}

	@Override
	public boolean backup(int id) {
		int backupId = id == 0 ? 0 : modificationdId;
		Operand factExpression = getLeftOperand();
		factExpression.backup(backupId);
		executeExpression.backup(backupId);
		super.backup(id);
		return true;
	}

}
