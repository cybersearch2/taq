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
package au.com.cybersearch2.taq.pattern;

import java.util.ArrayList;
import java.util.List;

import au.com.cybersearch2.taq.helper.Blank;
import au.com.cybersearch2.taq.interfaces.AxiomContainer;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.operator.OperatorTerm;
import au.com.cybersearch2.taq.provider.CallHandler;

/***
 * Call evaluator to perform select operation
 */
public class SelectCallEvaluator extends CallHandler {

	/** Performs selection operation */
	private final Choice choice;
	/** Template containing select */
	private final Template template;
	/** Template with select criterion */
	private final Template choiceTemplate;
	/** Sets receiver with call result */
	private ReceiverHandler receiverHandler;
	/** Archetype of axiom which returns a select result */
	private AxiomArchetype axiomArchetype;

	/**
	 * Construct SelectCallEvaluator object
	 * @param choice Performs selection operation
	 * @param template Template containing select
	 * @param choiceTemplate Template with select criterion
	 */
	public SelectCallEvaluator(Choice choice, Template template, Template choiceTemplate) {
		super(choice.getName(), OperandType.TERM);
		this.choice = choice;
		this.template = template;
		this.choiceTemplate = choiceTemplate;
		axiomArchetype = new AxiomArchetype(choice.getQualifiedAxiomName());
		List<Operand> variableList = choice.getVariableList();
		variableList.forEach(variable -> axiomArchetype.addTermName(variable.getName()));
		context = choice.getExecutionContext();
	}

	public Choice getChoice() {
		return choice;
	}

	public void setReceiverHandler(ReceiverHandler receiverHandler) {
		this.receiverHandler = receiverHandler;
	}
	
	/**
	 * Set axiom container to dynamically receive results
	 * @param axiomContainer Axiom container
	 */
	@Override
	public void setAxiomContainer(AxiomContainer axiomContainer) {
		super.setAxiomContainer(axiomContainer);
		if (receiverHandler != null)
			receiverHandler.setAxiomContainer(axiomContainer);
	}
	
	/**
     * Perform select operation
     * @param argumentList List of terms, the first being the value to select on
     * @return Flag set true if evaluation successful
     */
	@Override
    public boolean evaluate(List<Term> argumentList) {
		choice.backup(0);
		if (choice.getSelection() != Choice.NO_MATCH)
			choiceTemplate.backup(true);
    	boolean success = choice.completeSolution(choiceTemplate, argumentList.get(0).getValue(), template.getId(), context);
    	Axiom selection;
        if (success)
        { 
        	List<Parameter> termList = new ArrayList<>();
         	choice.getVariableList().forEach(variable -> termList.add(new OperatorTerm(variable.getName(), variable.getValue(), variable.getOperator())));
        	QualifiedName qname = new QualifiedName(choice.getName(), axiomArchetype.getQualifiedName());
        	selection = new Axiom(qname.toString(), termList.toArray(new Parameter[termList.size()]));
        	onNextAxiom(qname, selection, choice.getTermOperand().getOperator().getTrait().getLocale());
        }
        else
        {   // Return axiom containing blanks if no match found 
        	selection = new Axiom(axiomArchetype);
        	List<String> nameList = axiomArchetype.getTermNameList();
        	for (int i = 0; i < nameList.size(); ++i) {
        		String name = nameList.get(i);
        		selection.addTerm(new Parameter(name, new Blank()));
        	}
        	onNextAxiom(axiomArchetype.getQualifiedName(), selection, choice.getTermOperand().getOperator().getTrait().getLocale());
            // Only backup local changes
        	choiceTemplate.backup(true);
        }
        choice.setValue(selection);
       	if (success &&(receiverHandler != null))
    		receiverHandler.setReceiver(selection);
    	return success;
    }
    
    @Override
	public void backup(int id)  {
    	choice.backup(id);
        if (receiverHandler != null)
        	receiverHandler.backup();
    }

}
