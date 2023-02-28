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

import java.util.List;

import com.j256.simplelogging.Logger;

import au.com.cybersearch2.taq.artifact.ChoiceArtifact;
import au.com.cybersearch2.taq.compile.AxiomAssembler;
import au.com.cybersearch2.taq.compile.ListAssembler;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.log.LogManager;
import au.com.cybersearch2.taq.operator.NullOperator;
import au.com.cybersearch2.taq.provider.SelectionFunctions;
import au.com.cybersearch2.taq.query.Solution;

/**
 * Performs select or map operation according to choice specification. Extends
 * Operand for object interface integration.It can therefore be inserted in
 * OperandMap to support Select functions.
 * 
 * @author Andrew Bowley
 */
public class Choice extends Operand {
	private static class SelectOperator extends NullOperator {

		@Override
		public Class<?> getObjectClass() {
			return SelectionFunctions.class;
		}

	}

	/** Constant returned from Template select() when no match found */
	public static final int NO_MATCH = -1;

	/** Suffix used to name the list containing the selection axioms */
	public static final String AXiOM_SUFFIX = "_axiom";
	/** Suffix used to name the selection template */
	public static final String TEMPLATE_SUFFIX = "_template";
	/** Suffix used to name the dynamic axiom list */
	public static final String DYNAMIC_SUFFIX = "_dynamic";
	/** Logger */
	private static final Logger logger = LogManager.getLogger(Choice.class);

	/** Select operator */   
	private final Operator operator;
	/** Injected Choice Artifact */
	private final ChoiceArtifact choiceSpec;
	/** Injected Choice Parameters */
	private final ChoiceParameters choiceParameters;

	/** List of axioms, with each axiom representing one row of the choice */
	private List<Axiom> choiceAxiomList;
	/** Choice term names */
	private List<String> termNameList;
	/** Choice selection is row index or -1 for no match */
	private int selection;
	/** Operand containing the selection value */
	private Operand termOperand;

	/**
	 * Construct Choice object
	 * 
	 * @param choiceSpec       Choice specification
	 * @param choiceParameters Choice runtime parameters
	 */
	public Choice(ChoiceArtifact choiceSpec, ChoiceParameters choiceParameters) {
		super(choiceSpec.getName());
		this.choiceSpec = choiceSpec;
		this.choiceParameters = choiceParameters;
		operator = new SelectOperator();
		// Get axiom source for this Choice
		ParserAssembler parserAssembler = choiceParameters.getScope().getParserAssembler();
		ListAssembler listAssembler = parserAssembler.getListAssembler();
		AxiomAssembler axiomAssembler = parserAssembler.getAxiomAssembler();
		QualifiedName axiomName = choiceSpec.getQualifiedAxiomName();
		choiceAxiomList = listAssembler.getAxiomItems(axiomName);
		termOperand = choiceParameters.getVariableList().get(0);
		// Initialize selection to a safe value
		selection = NO_MATCH;
		termNameList = axiomAssembler.getTermNameList(choiceSpec.getQualifiedAxiomName());
	}

	/**
	 * Returns operand containing the selection value
	 * 
	 * @return Operand object
	 */
	public Term getSelectionTerm() {
		if (choiceSpec.isMap()) {
			return choiceParameters.getVariableList().get(1);
		}
		return termOperand;
	}

	public Operand getTermOperand() {
		return termOperand;
	}

	public String getName() {
		return choiceSpec.getName();
	}

	/**
	 * Returns qualified name of axiom list containing available choices
	 * 
	 * @return QualifiedName object
	 */
	QualifiedName getQualifiedAxiomName() {
		return choiceSpec.getQualifiedAxiomName();
	}

	/**
	 * Returns selection index. -1 = 'no match'
	 * 
	 * @return int
	 */
	public int getSelection() {
		return selection;
	}

	/**
	 * Returns the list of choice axioms
	 * 
	 * @return Axiom list
	 */
	public List<Axiom> getChoiceAxiomList() {
		return choiceAxiomList;
	}

	public List<Operand> getVariableList() {
		return choiceParameters.getVariableList();
	}

	/**
	 * Returns the selected choice axiom
	 * 
	 * @return Axiom
	 */
	public Axiom getChoiceAxiom() {
		return selection != -1 ? choiceAxiomList.get(selection) : null;
	}

	ExecutionContext getExecutionContext() {
		return choiceParameters.getScope().getExecutionContext();
	}
	
	/**
	 * Completes ChoiceOperand selection. The operand references a choice
	 * declaration.
	 * 
	 * @param choiceTemplate Template used to calculate choice
	 * @param id             Evaluation id
	 * @param value          Value
	 * @param context        context Execution context
	 * @return Flag set true if selection match was found
	 */
	public boolean completeSolution(Template choiceTemplate, Object value, int id, ExecutionContext context) {
		setExecutionContext(context);
		// The term operand must be set and have the outer template evaluation id
		if ((value != null) && termOperand.isEmpty()) {
			// For non-alias case, the term operand may need to be set from the given value
			Parameter termParam = new Parameter(termOperand.getName(), value);
			termParam.setId(id);
			termOperand.assign(termParam);
		}
		if (choiceParameters.hasParameterTemplate()) {
			setParameters(choiceTemplate);
		}
		if (choiceParameters.hasScopeOperand()) {
			// Choice declared in a scope, so local selection value must be set on scope
			// operand
			Parameter termParam = new Parameter(termOperand.getName(), termOperand.getValue());
			choiceParameters.getScopeOperand().assign(termParam);
		}
		// Delegate selection operation to the choice template
		selection = choiceTemplate.select(context, termOperand, this, id);
		if (selection != NO_MATCH) {
			handleSelection(id);
			choiceTemplate.getTermByIndex(selection).backup(choiceTemplate.getId());
		}
		return selection != NO_MATCH;
	}

	/**
	 * Backup Choice variables
	 * 
	 * @param id Modification id
	 */
	@Override
	public boolean backup(int id) {
		for (Operand operand : choiceParameters.getVariableList())
			operand.backup(id);
		return true;
	}

	@Override
	public QualifiedName getQualifiedName() {
		return new QualifiedName(choiceSpec.getName(), choiceSpec.getQualifiedName());
	}

	@Override
	public void assign(Parameter parameter) {
	}

	@Override
	public Operand getRightOperand() {
		return null;
	}

	@Override
	public Operator getOperator() {
		return operator;
	}

	/**
	 * Unify template with solution
	 * 
	 * @param template Template
	 * @param solution Solution
	 */
	private void unify(Template template, Solution solution) {
		OperandWalker walker = template.getOperandWalker();
		walker.setAllNodes(true);
		SolutionPairer pairer = template.getSolutionPairer(solution);
		walker.visitAllNodes(pairer);
	}

	/**
	 * Set template parameters
	 * 
	 * @param choiceTemplate
	 */
	private void setParameters(Template choiceTemplate) {
		QualifiedName templateName = choiceTemplate.getQualifiedName();
		AxiomArchetype archetype = new AxiomArchetype(
				new QualifiedName(templateName.getScope(), templateName.getTemplate()));
		Axiom axiom = new Axiom(archetype);
		Template parameterTemplate = choiceParameters.getParameterTemplate();
		parameterTemplate.backup(true);
		List<Term> termList;
		parameterTemplate.evaluate(context);
		termList = parameterTemplate.toArray();
		int item = 0;
		for (Term term : termList) {
			if (item == 0)
				axiom.addTerm(term);
			else {
				String termName = term.getName();
				// Parameters override variables
				int index = !termName.isEmpty() ? -1 : item;
				for (int i = 1; i < termNameList.size(); ++i)
					if (termName.equals(termNameList.get(i))) {
						index = i;
						break;
					}
				if (index != -1) {
					Operand selectOperand = choiceParameters.getVariableList().get(index);
					selectOperand.setValue(term.getValue());
				} else
					logger.warn(String.format("No match for %s select parameter %s", choiceSpec.getName(), termName));
			}
			if (++item == termNameList.size())
				break;
		}
		if (axiom.getTermCount() > 0) {
			Solution solution = new Solution(termOperand.getOperator().getTrait().getLocale());
			solution.put(choiceParameters.getContextName().toString(), axiom);
			unify(choiceTemplate, solution);
		}
	}

	private void handleSelection(int id) {
		// Finalize solution using selected axiom
		Axiom choiceAxiom = choiceAxiomList.get(selection);
		int termCount = choiceAxiom.getTermCount();
		if (termCount > 1)
			setVariables(choiceAxiom, id, termCount);
	}

	private void setVariables(Axiom choiceAxiom, int id, int termCount) {
		// The solution is contained in a set of variables, one per axiom term
		int index = 1;
		int listSize = choiceParameters.getVariableList().size();
		while ((index < termCount) && (index < listSize)) {
			setOperand(choiceAxiom, index, id);
			++index;
		}
		if ((index == 1) && (listSize == 1)) { // Special case selection term is also the selection variable
			setOperand(choiceAxiom, 0, id);
		}
	}

	private void setOperand(Axiom choiceAxiom, int index, int id) {
		Operand operand = choiceParameters.getVariableList().get(index);
		if (index == 0)
			index = 1;
		operand.assign((Parameter) choiceAxiom.getTermByIndex(index));
		operand.setId(id);
	}

}
