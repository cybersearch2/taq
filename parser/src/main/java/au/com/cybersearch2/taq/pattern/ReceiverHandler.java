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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import au.com.cybersearch2.taq.artifact.ChoiceArtifact;
import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.expression.ListOperand;
import au.com.cybersearch2.taq.interfaces.AxiomContainer;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.OperandVisitor;
import au.com.cybersearch2.taq.interfaces.ParserRunner;
import au.com.cybersearch2.taq.language.Null;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.list.AxiomTermList;
import au.com.cybersearch2.taq.operator.OperatorTerm;

/**
 * Evaluates receiver template. This includes unification with the values
 * return by the function. 
 *
 */
public class ReceiverHandler {

	/** Template to receive the the result of a function */
	private final Template receiverTemplate;
	/** Axiom term list which stores the template solution */
	private final AxiomTermList receiverTermList;
	/** Locale */
	private final Locale locale;
	/** Execution context */
	private final ExecutionContext context;
	
	/** List of templates enclosed by the receiver template. May be empty. */
	private List<Template> receiverLeaf;
	/** List Operand for returning axiom list result */
	private ListOperand<?> listOperand;
	/** Function axiom or term return list or null if not applicable */
	private AxiomContainer axiomContainer;
	/** Flag set true if axiom container exists */
	private boolean hasAxiomContainer;
	/** Flag set true if function returns axiom term list */
	private boolean returnsTermList;
	/** Name of function */
	private String functionName;

	/**
	 * Construct ReceiverHandler object
	 * @param receiverTemplate Template to receive the the result of a function
	 * @param receiverTermList Axiom term list which stores the template solution 
	 */
	public ReceiverHandler(Template receiverTemplate, AxiomTermList receiverTermList, Locale locale, ExecutionContext context) {
		this.receiverTemplate = receiverTemplate;
     	this.receiverTermList = receiverTermList;
		this.locale = locale;
		this.context = context;
     	// Default to no enclosed templates
     	receiverLeaf = Collections.emptyList();
     	locale = Locale.getDefault();
     	// Derive the function name from the template key
     	String[] parts = receiverTemplate.getKey().split("\\.");
     	functionName = parts.length > 0 ? parts[parts.length -1] : receiverTemplate.getKey();
		if (functionName.startsWith(ChoiceArtifact.CHOICE_PREFIX)) 
			// A select operation is named with a prefix to avoid ambiguity with the declaration.
			functionName = functionName.substring(ChoiceArtifact.CHOICE_PREFIX.length());
	}

	/**
	 * Returns the qualified name of the embedded template
	 * @return Qualified name
	 */
	public QualifiedName getQualifiedName() {
		return receiverTemplate.getQualifiedName();
	}

	/**
	 * Returns flag set true if an an axiom container has been provided to return the function result.
	 * @return boolean
	 */
	public boolean hasAxiomContainer() {
		return hasAxiomContainer;
	}

	/**
	 * Sets list of templates enclosed by the receiver template
	 * @param receiverLeaf Template list, may be empty
	 */
	public void setReceiverLeaf(List<Template> receiverLeaf) {
		this.receiverLeaf = receiverLeaf;
	}

	/**
	 * Sets axiom container to return the function result, The default is
	 * use the provider solution axiom. Note the function name is updated to 
	 * the item list name of the axiom container. This is an override mechanism.
	 * @param axiomContainer AxiomContainer object (AxiomList or AxiomTermList)
	 */
	@SuppressWarnings("unchecked")
	public void setAxiomContainer(AxiomContainer axiomContainer) {
		this.axiomContainer = axiomContainer;
		returnsTermList = axiomContainer.getOperandType() == OperandType.TERM;
		if (returnsTermList) {
		    ItemList<Term> itemList = (ItemList<Term>)axiomContainer;
		    listOperand = new ListOperand<Term>(itemList);
		    functionName = itemList.getName();
		} else {
		    ItemList<Axiom> itemList = (ItemList<Axiom>)axiomContainer;
		    listOperand = new ListOperand<Axiom>(itemList);
		    functionName = itemList.getName();
    	}
		if (functionName.startsWith(ChoiceArtifact.CHOICE_PREFIX)) 
			functionName = functionName.substring(ChoiceArtifact.CHOICE_PREFIX.length());
		hasAxiomContainer = true;
	}

	/**
	 * Performs receiver template unification using returned function values
	 * @param solutionAxiom Solution axiom - empty if axiom container set for axiom list
	 */
	public void setReceiver(Axiom solutionAxiom) {
		// The solution is obtained by unifying the axiom with a receiver followed by evaluation
    	receiverLeaf.forEach(template -> template.backup(true));
	    // Perform a simple unification based on matching term names
        Set<String> termNames = new HashSet<>();
        if (returnsTermList)
        	termNames.addAll(axiomContainer.getAxiomTermNameList());
        else
            termNames.addAll(solutionAxiom.getArchetype().getTermNameList());
        if (hasAxiomContainer || !returnsTermList) 
            termNames.add(functionName);
        Axiom pairAxiom = solutionAxiom;
        OperandVisitor operandVisitor = new OperandVisitor() {

			@Override
			public boolean next(Operand operand, int depth) {
				String name = operand.getName();
				// Filter on name and operand attributes to exclude operands that should not
				// participate. They are be set only by evaluation.
				if (operand.isEmpty() && !name.isEmpty() &&
				    termNames.contains(name) && 
				    !(operand instanceof ParserRunner)) {
					Parameter param = null;
					boolean listOperandMatch = hasAxiomContainer && functionName.equals(name) && (operand instanceof ListOperand);
					if (listOperandMatch) {
						param = new OperatorTerm(operand.getName(), listOperand, operand.getOperator());
					} else {
						if ((pairAxiom.getTermCount() == 1) && functionName.equals(name)) {
							if (pairAxiom.isFact())
								param = getParam(pairAxiom.getTermByIndex(0), operand);
							else {
						  	    param = new OperatorTerm(operand.getName(), new Null(), operand.getOperator());
							}
						} else {
						    Term item = pairAxiom.getTermByName(name);
						    if (item != null)
						        param = new OperatorTerm(operand.getName(), item.getValue(), operand.getOperator());
						}
					}
					if (param != null) {
					    param.setId(receiverTemplate.getId());
					    operand.assign(param);
					}
				}
				return true;
			}};
		receiverTemplate.traverseLeaf(receiverLeaf, operandVisitor);
        // Evaluate to complete the solution
        receiverTemplate.evaluate(context);
        Axiom axiom = toAxiom(receiverTemplate);
        if (axiom.getTermCount() > 0) 
        	receiverTermList.getAxiomListener().onNextAxiom(receiverTermList.getQualifiedName(), axiom, locale);	
	}

	/**
	 * Backup the template fully
	 */
	public void backup() {
		receiverTemplate.backup(false);	
	}
	
    protected Parameter getParam(Term term, Operand operand) {
		Parameter param;
		Object value = term.getValue();
  	    param = new OperatorTerm(operand.getName(), value, operand.getOperator());
		return param;
	}

	/**
	 * Returns an axiom containing the values of this template and 
	 * having same key and name as this template's name.
	 * @return Axiom
	 */
	private Axiom toAxiom(Template template)
	{
        Axiom axiom = new Axiom(template.getQualifiedName().getTemplate());
		for (int i = 0; i < template.getTermCount(); ++i)
		{
			Term term = template.getTermByIndex(i);
		    Operand operand = (Operand)term;
            if (!operand.isEmpty() && !operand.isPrivate())
			{
                String termName = operand.getName();
				OperatorTerm param = new OperatorTerm(termName, operand.getValue(), operand.getOperator());
				axiom.addTerm(param);
			}
		}
		return axiom;
	}

}
