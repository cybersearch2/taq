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

import com.j256.simplelogging.Logger;

import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.ITemplate;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.log.LogManager;
import au.com.cybersearch2.taq.pattern.Template;
import au.com.cybersearch2.taq.service.LoopMonitor;

/**
 * TemplateOperand
 * Operand which performs evaluation a sequence of operands. 
 * A short circuit causes a return to the start (ie. expression prefixed with '?' evaluates to 'false') 
 * On each iteration, a backup clears the results of the previous evaluations. 
 * A 2 second timer operates to break an infinite loop.
 * This is a NullOperand as any result will be returned in an already declared parameter. 
 * @author Andrew Bowley
 */
public class TemplateOperand extends BooleanOperand
{
    private final static Logger logger = LogManager.getLogger(TemplateOperand.class);

	/** The Operand sequence to be evaluated is contained in a template */
	private final Template template;
	/** Flag whether run once or loop */
	private final  boolean runOnce;

	/**
	 * Construct a TemplateOperand object
	 * @param template Container for the Operand sequence to be evaluated
	 */
	public TemplateOperand(ITemplate template) 
	{
		this(template, false);
	}

    /**
     * Construct a TemplateOperand object with given template
     * Sets this operand to make a selection.
     * @param template Template
     * @param runOnce Flag set true if template is evaluated once only
     */
	public TemplateOperand(ITemplate template, boolean runOnce) 
	{
		super(new QualifiedName((template.getQualifiedName().getTemplate()) + (runOnce ? "_run_once" : "_loop"), template.getQualifiedName()));
		this.template = (Template)template;
		this.runOnce = runOnce; 
	}

	/**
	 * Returns value of first non-empty term of template
	 * @return Object
	 */
    public Object getSelection()
    {
        for (int i = 0; i < template.getTermCount(); ++i)
        {
            Operand operand = template.getTermByIndex(i);
            if (!operand.isEmpty())
                return operand.getValue();
        }
        return null;
    }

	/**
	 * Evaluate loop
	 * @param id Not used as evaluation and backup are local only
	 * @return Flag set true
	 */
	@Override
	public EvaluationStatus evaluate(int id)
	{
		if (context == null)
			throw new IllegalStateException(ExecutionContext.CONTEXT_NOT_SET);
	    this.id = id;
	    boolean isTemplateScope = getQualifiedName().isTemplateScope();
	    if (runOnce) {
			EvaluationStatus evaluationStatus = template.evaluate(context);
            template.backup(isTemplateScope);
            Template next = template.getNext();
            while (next != null) {
            	if (!next.isBackedUped())
            	    next.backup(isTemplateScope);
            	next = next.getNext();
            }
            // Value indicates successful completion
			setValue(Boolean.valueOf(evaluationStatus == EvaluationStatus.COMPLETE)); 
			return evaluationStatus;
	    }
	    else {
		    LoopMonitor monitor = context.getLoopMonitor();
		    monitor.push();
		    try {
				while (true)
				{
					EvaluationStatus evaluationStatus = template.evaluate(context);
		            // Only backup local changes unless terminating in outermost loop
		            boolean terminate = evaluationStatus == EvaluationStatus.SHORT_CIRCUIT;
		            boolean partial = !(terminate && !isTemplateScope);
		            template.backup(partial);
		            Template next = template.getNext();
		            while (next != null) {
		            	if (!next.isBackedUped())
		            	    next.backup(partial);
		            	next = next.getNext();
		            }
		            if ((evaluationStatus != EvaluationStatus.FAIL) && !monitor.tick()) {
				    	logger.error(String.format("Loop monitor terminated template %s", template.getName()));
				    	evaluationStatus = EvaluationStatus.FAIL;
					} else if (terminate) {
						setValue(Boolean.TRUE); // Value indicates successful completion
						return EvaluationStatus.COMPLETE;
					} 
					if (evaluationStatus == EvaluationStatus.FAIL) {
					    setValue(Boolean.FALSE); // Value indicates successful completion
					    return EvaluationStatus.FAIL;
					}
				}
		    } finally {
		    	monitor.pop();
		    }
	    }
	}

	/**
	 * Backup to initial state if given id matches id assigned on unification or given id = 0. 
	 * @param id Not used. 
	 * @return Flag set true
	 */
	@Override
	public boolean backup(int id)
	{   
		super.backup(id);
		// Changes managed locally
		if (id != template.getId())
		    template.backup(id);
		return template.backup(true);
	}

	/**
	 * @see au.com.cybersearch2.taq.expression.ExpressionOperand#toString()
	 */
	@Override
	public String toString() 
	{
	    StringBuilder builder = new StringBuilder(template.getName());
	    int termCount = template.getTermCount();
        if ( termCount > 0)
        {
            builder.append('(');
            boolean firstTime = true;
            for (int i = 0; i < termCount; ++i)
            {
                Term param = template.getTermByIndex(i);
                if (firstTime)
                {
                    firstTime = false;
                    builder.append(param.toString());
                }
                else if (i == termCount - 1)
                {
                    builder.append(" ... ").append(param.toString());
                }
            }
            builder.append(')');
        }
        else
            builder.append("()");
        return builder.toString();
	}
}
