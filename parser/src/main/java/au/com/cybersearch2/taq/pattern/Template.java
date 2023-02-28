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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import au.com.cybersearch2.taq.compile.TemplateType;
import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.expression.Variable;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.helper.QualifiedTemplateName;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.OperandVisitor;
import au.com.cybersearch2.taq.language.IOperand;
import au.com.cybersearch2.taq.language.ITemplate;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.operator.OperatorTerm;
import au.com.cybersearch2.taq.query.Solution;

/**
 * Template A collection of operands which evaluate and produce a solution
 * axiom. A template performs unification which it supports by analyzing the
 * archetypes of axioms with which it is paired. Template variants are inner
 * template, choice and replicate which support particular language features
 * Every Template has a unique ID to facilitate partial backup.
 * 
 * @author Andrew Bowley 30 Nov 2014
 */
public class Template extends TermList<Operand> implements ITemplate {
	private static final long serialVersionUID = -3549624322416667887L;

	private static final ObjectStreamField[] serialPersistentFields = { 
			// Template inherits Serializable from TermList but should not be serialized. Three fields
			// defined for tracing purposes.
			new ObjectStreamField("qname", QualifiedName.class), new ObjectStreamField("key", String.class),
			new ObjectStreamField("id", Integer.class) };

	public static List<String> EMPTY_NAMES_LIST;
	public static List<Operand> EMPTY_OPERAND_LIST;
	public static List<Term> EMPTY_TERM_LIST;

	/** Unique identity generator */
	static protected AtomicInteger referenceCount;

	static {
		referenceCount = new AtomicInteger();
		EMPTY_OPERAND_LIST = Collections.emptyList();
		EMPTY_NAMES_LIST = Collections.emptyList();
		EMPTY_TERM_LIST = Collections.emptyList();
	}

	/** Qualified name of template */
	private QualifiedName qname;
	/** Key to match with Axiom name for unification */
	private String key;
	/** Identity used in backup to allow partial backup to last unifying agent */
	private int id;
	/** Parent identity or 0 if none */
	private int parentId;
	/** Initialization data (optional) */
	private transient TemplateProperties properties;
	/** Link to next Template in chain. Used by Calculator. */
	private Template next;
	/** Flag true if template declared a calculator */
	private boolean isCalculator;
	/** Flag true if template used to make selection. Used by Calculator. */
	private boolean isChoice;
	/** Flag true if inner template */
	private boolean isInnerTemplate;
	/** Unique id if replicate, otherwise 0 */
	private int replicateId;
	/** Head of call stack */
	private transient CallContext headCallContext;
	/** Tail of call stack */
	private transient CallContext tailCallContext;
	/** Template archetype - attribute avoids casting to get super archetype */
	private TemplateArchetype templateArchetype;

	/**
	 * Construct a replicate Template object. The new template has a unique id and
	 * specified qualified name
	 * 
	 * @param master Template object to replicate
	 * @param qname  New Template qualified name.
	 */
	public Template(Template master, QualifiedName qname) {
		// Replicates share the master template archetype
		this(master.getTemplateArchetype());
		// Context name is now set as qualified name of master template.
		// This template qualified name must be set to given value.
		// Note this is the only case where template qualified name and
		// context name happen to be in different scopes.
		this.qname = qname;
		key = master.getKey();
		this.isCalculator = master.isCalculator();
		this.isChoice = master.isChoice();
		this.isInnerTemplate = master.isInnerTemplate();
		replicateId = id;
		id = master.getId();
		parentId = master.getParentId();
		// Replicates share the terms of the master template
		master.forEach(operand -> termList.add(operand));
		termCount = termList.size();
		properties = new TemplateProperties(master.getProperties());
		Template masterNext = master.getNext();
		if (masterNext != null)
			setNext(masterNext);
	}

	/**
	 * Construct Template object from archetype
	 * 
	 * @param templateArchetype Template archetype
	 */
	public Template(TemplateArchetype templateArchetype) {
		super(templateArchetype);
		this.templateArchetype = templateArchetype;
		qname = templateArchetype.getQualifiedName();
		key = qname.getTemplate();
		id =  getUniqueId();
		properties = new TemplateProperties();
	}

	/**
	 * Construct Template object from archetype with given key
	 * 
	 * @param key               Axiom name to unify with
	 * @param templateArchetype Template archetype
	 */
	public Template(String key, TemplateArchetype templateArchetype) {
		this(templateArchetype);
		this.key = key;
	}

	/**
	 * Construct Template object from archetype with given term list
	 * 
	 * @param templateArchetype Template archetype
	 * @param terms             One or more Variables
	 */
	public Template(TemplateArchetype templateArchetype, List<Operand> terms) {
		this(templateArchetype);
		if (terms != null) {
			if (terms.size() == 0)
				throw new IllegalArgumentException("Parameter \"terms\" is empty");
			for (Operand term : terms)
				addTerm(term);
		}
	}

	/**
	 * Construct Template object from archetype with given key and term list
	 * 
	 * @param key               Axiom name to unify with
	 * @param templateArchetype Template archetype
	 * @param termList          One or more Variables
	 */
	public Template(String key, TemplateArchetype templateArchetype, List<Operand> termList) {
		this(templateArchetype, termList);
		this.key = key;
	}

	/**
	 * Construct Template object from archetype with given array of terms
	 * 
	 * @param templateArchetype Template archetype
	 * @param terms             One or more Variables
	 */
	public Template(TemplateArchetype templateArchetype, Operand... terms) {
		this(templateArchetype);
		if ((terms != null) && (terms.length > 0)) {
			for (Operand term : terms)
				addTerm(term);
		} else
			throw new IllegalArgumentException("Parameter \"terms\" is empty");
	}

	/**
	 * Construct Template object from archetype with given key and array of terms
	 * 
	 * @param key               Axiom name to unify with
	 * @param templateArchetype Template archetype
	 * @param terms             One or more Variables
	 */
	public Template(String key, TemplateArchetype templateArchetype, Operand... terms) {
		this(templateArchetype, terms);
		this.key = key;
	}

	/**
	 * Sets flag to indicate this object is used as a Calculator
	 * 
	 * @param isCalculator Flag set true if this is a calculator
	 */
	public void setCalculator(boolean isCalculator) {
		this.isCalculator = isCalculator;
	}

	/**
	 * Set flag to indicate this object is used as a Choice
	 * 
	 * @param isChoice Choice flag
	 */
	public void setChoice(boolean isChoice) {
		this.isChoice = isChoice;
	}

	/**
	 * Returns identity of this structure
	 * 
	 * @return int
	 */
	public int getId() {
		return id;
	}

	public TemplateProperties getProperties() {
		return properties;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	/**
	 * Evaluate Terms of this Template
	 * 
	 * @param executionContext Execution context - may be null
	 * @return EvaluationStatus
	 */
	public EvaluationStatus evaluate(ExecutionContext executionContext) {
		if (executionContext == null)
			throw new IllegalArgumentException("Parameter executionContext is null");
		boolean isDebug = executionContext.isDebug();
		if (!isEmpty()) {
			for (Operand term : termList) {
				if (!term.isEmpty() && (term.getId() != id) && !term.isShadow())
					continue;
				if (isDebug)
					executionContext.beforeEvaluate(term);
				term .setExecutionContext(executionContext);
				EvaluationStatus evaluationStatus = term.evaluate(id);
				if (evaluationStatus != EvaluationStatus.COMPLETE)
					return evaluationStatus;
			}
		}
		return EvaluationStatus.COMPLETE;
	}

	/**
	 * Evaluate Terms of this Template and then set their modification ids to 0
	 * 
	 * @param executionContext Execution context - may be null
	 * @return EvaluationStatus
	 */
	public EvaluationStatus globalEvaluate(ExecutionContext context) {
		EvaluationStatus status = evaluate(context);
		forEach(term -> term.setId(0));
		return status;
	}

	/**
	 * Evaluate Terms of this Template until status COMPLETE is returned
	 * 
	 * @param executionContext Execution context - may be null
	 * @param termOperand      Term operand
	 * @param choice           Choice
	 * @param parentId         Parent id
	 * @return Position of first term selected or Choice.NO_MATCH if evaluation does
	 *         not return status COMPLETE
	 */
	public int select(ExecutionContext executionContext, Operand termOperand, Choice choice, int parentId) {
		int position = 0;
		if (!isEmpty()) { 
			// The backup at the end of the loop below clears any variable set by
			// unification. The term containing the selection value is set using a
			// parameter assignment so it will not be cleared.
			Parameter selectParam = null;
			if (termOperand != null) {
				selectParam = new Parameter(termOperand.getName(), termOperand.getValue());
			}
			for (Operand term : termList) {
				Operand leftOperand = term.getLeftOperand();
				if (leftOperand != null) {
					term.getRightOperand().unifyTerm(choice.getTermOperand(), id);
					leftOperand = leftOperand.getLeftOperand();
					if ((leftOperand instanceof Variable) && (selectParam != null))
						leftOperand.assign(selectParam);
				}
				if ((executionContext != null) && executionContext.isDebug())
					executionContext.beforeEvaluate(term);
				if (term.evaluate(id) == EvaluationStatus.COMPLETE) {
					return position;
				}
				term.backup(id);
				++position;
			}
		}
		// NO Default
		return Choice.NO_MATCH;
	}

	/**
	 * Backup from last unification.
	 * 
	 * @param partial Flag to indicate backup to before previous unification or
	 *                backup to start
	 * @return Flag to indicate if this Structure is ready to continue unification
	 */
	public boolean backup(boolean partial) {
		boolean isMutable = false;
		if (!isEmpty())
			for (Operand term : termList) {
				boolean backupPerformed = (partial ? term.backup(id) : term.backup(0));
				if (backupPerformed)
					isMutable = true;
			}
		return isMutable;
	}

	public boolean isBackedUped() {
		if (!isEmpty())
			for (Operand term : termList)
				if (!term.isEmpty())
					return false;
		return true;
	}

	/**
	 * Backup from last unification.
	 * 
	 * @param modificationId Modification id
	 * @return Flag to indicate if this Structure is ready to continue unification
	 */
	public boolean backup(int modificationId) {
		boolean isMutable = false;
		if (!isEmpty())
			for (Operand term : termList) {
				boolean backupPerformed = term.backup(modificationId);
				if (backupPerformed)
					isMutable = true;
			}
		return isMutable;
	}

	/**
	 * Unify given Axiom with this Template, pairing Terms of the Axiom with those
	 * of this Template. Terms are matched by name except when all terms are
	 * anonymous, in which case, terms are paired in list order. When both terms of
	 * a pairing have a value and values do not match, unification is skipped.
	 * Unification of all terms is also skipped if this Axiom and the other Template
	 * have different names.
	 * 
	 * @param axiom    Axiom with which to unify as TermList object
	 * @param solution Contains result of previous unify-evaluation steps
	 * @return Flag set true if unification completed successfully
	 */
	public boolean unify(TermList<Term> axiom, Solution solution) {
		return unify(axiom, solution, templateArchetype.getTermMapping(axiom.getArchetype()), false);
	}

	/**
	 * Unify given Axiom with this Template using case-insensitive matching.
	 * 
	 * @param axiom    Axiom with which to unify as TermList object
	 * @param solution Contains result of previous unify-evaluation steps
	 * @return Flag set true if unification completed successfully
	 */
	public boolean unifyCaseInsensitive(TermList<Term> axiom, Solution solution) {
		return unify(axiom, solution, templateArchetype.getTermMapping(axiom.getArchetype(), true), true);
	}

	/**
	 * Returns solution pairer to perform unification with this template
	 * 
	 * @param solution The query solution up to this stage
	 * @return SolutionPairer object
	 */
	public SolutionPairer getSolutionPairer(Solution solution) {
		return new SolutionPairer(solution, id, qname);
	}

	/**
	 * Returns flag set true if given operand is in same namespace as this template
	 * 
	 * @param operand Operand object
	 * @return boolean
	 */
	public boolean isInSameSpace(Operand operand) {
		return qname.inSameSpace(operand.getQualifiedName());

	}

	/**
	 * Returns an axiom containing the values of this template and having same key
	 * and name as this template's name.
	 * 
	 * @return Axiom
	 */
	public Axiom toAxiom() {
		Axiom axiom = new Axiom(qname.getTemplate());
		@SuppressWarnings("unchecked")
		Archetype<Axiom, Term> archetype = (Archetype<Axiom, Term>) axiom.getArchetype();
		for (Term term : termList) {
			Operand operand = (Operand) term;
			String operandName = operand.getName();
			if (!operand.isEmpty() && !operand.isPrivate()
					&& (isInSameSpace(operand) || (isReplicate() && !operand.getQualifiedName().isTemplateScope()))
					&& (operandName.isEmpty() || (archetype.getIndexForName(operandName) == -1))) {
				String termName = operand.getName();
				OperatorTerm param = new OperatorTerm(termName, operand.getValue(), operand.getOperator());
				axiom.addTerm(param);
			}
		}
		return axiom;
	}

	/**
	 * Returns all term values as a list. Unlike toAxiom(), there is no filtering
	 * eg. no honoring "private" flag
	 * 
	 * @return Term list
	 */
	public List<Term> toArray() {
		List<Term> arrayList = new ArrayList<Term>();
		for (Operand operand : termList) {
			OperatorTerm param = new OperatorTerm(operand.getName(), operand.getValue(), operand.getOperator());
			arrayList.add(param);
		}
		return arrayList;
	}

	/**
	 * Returns an OperandWalker object for navigating this template
	 * 
	 * @return OperandWalker
	 */
	public OperandWalker getOperandWalker() {
		if (!isEmpty())
			return new OperandWalker(termList);
		return new OperandWalker(EMPTY_OPERAND_LIST);
	}

	/**
	 * Walk this and enclosed templates with given visitor
	 * 
	 * @param templateList   List of enclosed templates recorded by
	 *                       TemplateAssembler
	 * @param operandVisitor Operand visitor
	 */
	public void traverseLeaf(List<Template> templateList, OperandVisitor operandVisitor) {
		OperandWalker walker = getOperandWalker();
		walker.setAllNodes(true);
		walker.visitAllNodes(operandVisitor);
		Iterator<Template> iterator = templateList.iterator();
		while (iterator.hasNext()) {
			walker.reset(iterator.next());
			walker.visitAllNodes(operandVisitor);
		}
	}

	/**
	 * For each term, clear the value and set as empty
	 */
	public void reset() {
		Template template = this;
		while (template != null) {
			if (!template.isEmpty())
				template.forEach(term -> term.backup(0));
			template = template.getNext();
		}
	}

	/**
	 * Returns next template in chain
	 * 
	 * @return Template object or null
	 */
	public Template getNext() {
		return next;
	}

	/**
	 * Set new template object in chain
	 * 
	 * @param nextTemplate Template object
	 */
	public void setNext(Template nextTemplate) {
		setNext(nextTemplate, 0);
	}

	/**
	 * Set new template object in chain
	 * 
	 * @param nextTemplate Template object
	 */
	public void setNext(Template nextTemplate, int parentId) {
		Template template = this;
		// Add inner template to outer template so it will be included in unification
		while (template.getNext() != null) {
			template = template.getNext();
			if (template == nextTemplate)
				return;
		}
		template.next = nextTemplate;
		nextTemplate.setParentId(parentId);
	}

	/**
	 * Add Operand term.
	 * 
	 * @param operand Operand object
	 */
	@Override
	public void addTerm(IOperand operand) {
		addTerm((Operand) operand);
	}

	/**
	 * Returns Term referenced by name
	 * 
	 * @param name String
	 * @return Term object or null if not found
	 */
	@Override
	public Operand getTermByName(String name) {
		Operand term = super.getTermByName(name);
		if (term == null) {
			OperandFinder operandFinder = new OperandFinder(termList, name);
			term = operandFinder.findNode();
		}
		return term;
	}

	@Override
	public void reverse() {
		int termCount = getTermCount();
		List<Operand> reverseList = new ArrayList<>(termCount);
		for (int i = termCount - 1; i > -1; --i) {
			reverseList.add(termList.get(i));
		}
		termList.clear();
		termList = reverseList;
	}

	/**
	 * Returns the name of the Structure
	 * 
	 * @return String
	 */
	@Override
	public String getName() {
		return qname.getTemplate();
	}

	/**
	 * Returns key to match with Axiom name for unification
	 * 
	 * @return String
	 */
	@Override
	public String getKey() {
		return key;
	}

	/**
	 * Sets axiom key
	 * 
	 * @param value Value
	 */
	@Override
	public void setKey(String value) {
		key = value;
	}

	/**
	 * Returns flag set true if this object is used as a Calculator
	 * 
	 * @return boolean
	 */
	@Override
	public boolean isCalculator() {
		return isCalculator;
	}

	/**
	 * Returns flag set true if this object is used as a Choice
	 * 
	 * @return boolean
	 */
	@Override
	public boolean isChoice() {
		return isChoice;
	}

	/**
	 * Returns flag set true if this is an inner template
	 * 
	 * @return boolean
	 */
	@Override
	public boolean isInnerTemplate() {
		return isInnerTemplate;
	}

	/**
	 * Returns flag set true if this si a replicate template
	 * 
	 * @return boolean
	 */
	@Override
	public boolean isReplicate() {
		return replicateId != 0;
	}

	/**
	 * Returns template qualified name
	 * 
	 * @return QualifiedName object
	 */
	@Override
	public QualifiedName getQualifiedName() {
		return qname;
	}

	/**
	 * Returns query template instance
	 * 
	 * @param name         Query name to be appended to template qualified name
	 * @param templateType Template type - template or choice
	 * @param master       Master choice template - required only when template type
	 *                     is choice
	 * @return Template object
	 */
	public Template innerTemplateInstance(String name, TemplateType templateType, Template master) {
		boolean isChoice = templateType == TemplateType.choice;
		TemplateArchetype masterArchetype = null;
		if (isChoice) {
			masterArchetype = master.getTemplateArchetype();
			masterArchetype.clearMutable();
		}
		QualifiedName innerTemplateName;
		innerTemplateName = getInnerTemplateQname();
		TemplateArchetype newTemplateArchetype = masterArchetype == null ? new TemplateArchetype(innerTemplateName)
				: masterArchetype;
		Template newTemplate = isChoice ? new Template(key, newTemplateArchetype) : new Template(newTemplateArchetype);
		if (isChoice)
			newTemplate.setChoice(true);
		newTemplate.isInnerTemplate = true;
		setNext(newTemplate, id);
		return newTemplate;
	}

	/**
	 * Returns query template instance
	 * 
	 * @param name Query name to be appended to template qualified name
	 * @return Template object
	 */
	public Template receiverTemplateInstance(String name) {
		QualifiedName innerTemplateName;
		innerTemplateName = getInnerTemplateQname();
		TemplateArchetype newTemplateArchetype = new TemplateArchetype(innerTemplateName);
		Template newTemplate = new Template(newTemplateArchetype);
		newTemplate.isInnerTemplate = true;
		setNext(newTemplate, id);
		return newTemplate;
	}

	/**
	 * Returns inner template instance chained to this template
	 * 
	 * @param templateType Template type
	 * @return Template object
	 */
	public Template innerTemplateInstance(TemplateType templateType) {
		return innerTemplateInstance(null, templateType, null);
	}

	/**
	 * Create a choice template instance as an inner template of this template,
	 * 
	 * @param master Global scope template containing the choice operands which are
	 *               copied to the inner template.
	 * @return Template object
	 */
	public Template choiceInstance(Template master) {
		Template choiceTemplate = innerTemplateInstance(null, TemplateType.choice, master);
		master.forEach(operand -> choiceTemplate.addTerm(operand));
		return choiceTemplate;
	}

	/**
	 * Push template operand values on stack
	 */
	public void push() {
		CallContext newCallContext = new CallContext(this);
		if (headCallContext == null) {
			headCallContext = newCallContext;
			tailCallContext = newCallContext;
		} else {
			tailCallContext.setNext(newCallContext);
			tailCallContext = newCallContext;
		}
		Template template = getNext();
		// Do full backup of chain templates so parameters can be re-evaluatoed
		while (template != null) {
			template.backup(false);
			template = template.getNext();
		}
	}

	/**
	 * Pop template terms off stack
	 */
	public void pop() {
		if (tailCallContext != null) {
			tailCallContext.restoreContext();
			if (headCallContext == tailCallContext) {
				headCallContext = null;
				tailCallContext = null;
			} else {
				CallContext newTail = headCallContext;
				while (newTail.getNext() != tailCallContext)
					newTail = newTail.getNext();
				tailCallContext = newTail;
			}
		}
	}

	public void forEach(Consumer<? super Operand> action) {
		Objects.requireNonNull(action);
		for (Operand term : termList) {
			action.accept(term);
		}
	}

	/**
	 * Return the archetype with it's actual type
	 * 
	 * @return TemplateArchetype object
	 */
	public TemplateArchetype getTemplateArchetype() {
		return templateArchetype;
	}

	public static int getUniqueId() {
		return referenceCount.incrementAndGet();
	}
	
	/**
	 * Unify template using given axiom and solution
	 * 
	 * @param axiom           Axiom with which to unify as TermList object
	 * @param solution        Contains result of previous unify-evaluation steps
	 * @param termMapping     Maps operands to axiom terms
	 * @param caseInsensitive
	 * @return Flag set true if unification completed successfully
	 */
	protected boolean unify(TermList<Term> axiom, Solution solution, int[] termMapping, boolean caseInsensitive) {
		OperandWalker walker = new OperandWalker(termList);
		// Create list of term pairs to unify
		Unifier unifier = new Unifier(this, axiom, termMapping, solution);
		unifier.setCaseInsensitive(caseInsensitive);
		return walker.visitAllNodes(unifier);
	}

	private int getParentId() {
		return parentId;
	}

	/**
	 * Serial I/O
	 * 
	 * @param ois ObjectInputStream
	 * @throws IOException            if IO error occurs
	 * @throws ClassNotFoundException if class not found
	 */
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		archetype = new TemplateArchetype(qname);
	}

	private QualifiedName getInnerTemplateQname() {
		String templateName = qname.getTemplate() + Integer.toString(qname.incrementReferenceCount() + 1);
		String scope = qname.getTemplateScope();
		return new QualifiedTemplateName(scope, templateName);
	}

}
