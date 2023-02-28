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

import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.pattern.Choice;
import au.com.cybersearch2.taq.pattern.Template;

/**
 * ChoiceOperand
 * @author Andrew Bowley
 * 5 Sep 2015
 */
public class ChoiceOperand extends DelegateOperand
{
	private final Template choiceTemplate;
    private final Choice choice;
    
    public ChoiceOperand(QualifiedName qname, Template choiceTemplate, Choice choice)
    {
        super(qname);
        this.choiceTemplate = choiceTemplate;
        this.choice = choice;
    }

    public ChoiceOperand(QualifiedName qname, Template choiceTemplate, Choice choice, Operand head)
    {
        super(new QualifiedName(qname.getName() + qname.incrementReferenceCount(), qname), qname.getName());
        this.choiceTemplate = choiceTemplate;
        this.choice = choice;
        this.head = head;
    }

    public Choice getChoice() {
		return choice;
	}

	public void setHead(Operand head) {
		this.head = head;
    }

	/**
     * Evaluate loop
     * @param id Evaluation id
     * @return Flag set true
     */
    @Override
    public EvaluationStatus evaluate(int id)
    {
		if (context == null)
			throw new IllegalStateException(ExecutionContext.CONTEXT_NOT_SET);
        this.id = id;
        Parameter param = null;
        Object value = null;
        if (leftOperand != null) {
        	if (leftOperand.isEmpty())
        		leftOperand.evaluate(id);
        	value = leftOperand.getValue();
        }
        if (choice.completeSolution(choiceTemplate, value, id, context))
        {
            param = (Parameter)choice.getSelectionTerm();
            param.setId(id);
            assign(param);
            if (head != null) 
		    	head.castShadow(param.getValue(), id);
         }
        else
        {    
            // Only backup local changes
        	choiceTemplate.backup(true);
        }
        return EvaluationStatus.COMPLETE;
    }

    /**
     * Backup to intial state if given id matches id assigned on unification or given id = 0. 
     * @param id Evaluation id
     * @return Flag set true
     */
    @Override
    public boolean backup(int id)
    {   
        super.backup(id);
        choice.backup(id);
        // Changes managed locally
        choiceTemplate.backup(id);
        if (choiceTemplate.getId() != id)
        	choiceTemplate.backup(id != 0);
        if (leftOperand != null)
        	leftOperand.backup(id);
        return true;
    }

    /**
     * @see au.com.cybersearch2.taq.expression.ExpressionOperand#toString()
     */
    @Override
    public String toString() 
    {
        return "select " + name;
    }

	@Override
	public Operand getRightOperand() {
		return null;
	}

}

