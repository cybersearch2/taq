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
package au.com.cybersearch2.taq.interfaces;

import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.language.IOperand;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.operator.DelegateOperator;

/**
 * Operand A Term which evaluates binary and unary expressions
 * 
 * @author Andrew Bowley 1 Dec 2014
 */
public abstract class Operand extends Parameter implements IOperand, ExecutionTracker {
	/** Dependent Operand to evaluate first if not null */
	protected Operand leftOperand;
	/** Head of shadow operand chain or null if none */
	protected Operand head;
	/** ExecutionContext */
	protected ExecutionContext context;
	/** Flag set true if operand not visible in solution */
	private boolean isPrivate;
	/** Identity of container which registered the archetype index */
	private int archetypeId;
	/** Index of this Operand in the registered archetype */
	private int archetypeIndex;
	/** Next operand in shadow chain or null if none */
	private Operand next;
	/** Flag to prevent backup recursion */
	private boolean isInBackup;
	/** Delegate operator for when operand type is initially unknown */
	private DelegateOperator delegateOperator;

	/**
	 * Construct empty Operand object
	 * 
	 * @param name Name
	 */
	protected Operand(String name) {
		super(name);
		archetypeIndex = -1;
	}

	/**
	 * Construct empty Operand object with given delegate operator
	 * 
	 * @param name             Name
	 * @param delegateOperator Delegate operator providing proxy support
	 */
	protected Operand(String name, DelegateOperator delegateOperator) {
		this(name);
		this.delegateOperator = delegateOperator;
	}

	/**
	 * Construct Operand object with integer value
	 * 
	 * @param name  Name
	 * @param value Value
	 */
	protected Operand(String name, int value) {
		super(name, value);
		archetypeIndex = -1;
	}

	/**
	 * Construct Operand object with given object value
	 * 
	 * @param name  Name
	 * @param value Value
	 */
	protected Operand(String name, Object value) {
		super(name, value);
		archetypeIndex = -1;
	}

	/**
	 * Returns qualified name
	 * 
	 * @return QualifiedName object
	 */
	public abstract QualifiedName getQualifiedName();

	/**
	 * Returns right child of Operand
	 * 
	 * @return Operand object or null if there is no child
	 */
	public abstract Operand getRightOperand();

	/**
	 * Returns object which defines operations that an Operand performs with other
	 * operands
	 * 
	 * @return Operator object
	 */
	public abstract Operator getOperator();

	/**
	 * Returns delegate operator providing proxy support
	 * 
	 * @return DelegateOperator object
	 * @throws java.lang.IllegalStateException if there is no delegate operator set
	 */
	public DelegateOperator getDelegateOperator() {
		if (delegateOperator == null)
			throw new IllegalStateException("Operand type cannot be delegated");
		return delegateOperator;
	}

	/**
	 * Returns branch1 of Operand
	 * 
	 * @return Operand object or null if there is no branch
	 */
	public Operand getBranch1() {
		return null;
	}

	/**
	 * Set Delegate operator providing proxy support
	 * 
	 * @param delegateOperator Delegate operator
	 */
	public void setDelegateOperator(DelegateOperator delegateOperator) {
		this.delegateOperator = delegateOperator;
	}

	/**
	 * Returns branch2 of Operand
	 * 
	 * @return Operand object or null if there is no child
	 */
	public Operand getBranch2() {
		return null;
	}

	/**
	 * Returns next shadow operand
	 * 
	 * @return Operand object or null there is no next operand
	 */
	public Operand getNext() {
		return next;
	}

	/**
	 * Add shadow operand to chain
	 * 
	 * @param shadow Shadow operand
	 */
	public void addShadow(Operand shadow) {
		shadow.head = this;
		if (next == null) {
			next = shadow;
		} else {
			Operand operand = next;
			while (operand.next != null)
				operand = operand.next;
			operand.next = shadow;
		}
	}

	/**
	 * Set given value on shadow chain. Only valid if this is head object..
	 * 
	 * @param value Object to set
	 * @param id    Modification id
	 */
	public void castShadow(Object value, int id) {
		setValue(value);
		castShadow(id);
		setId(id);
	}

	/**
	 * Set current value on shadow chain. Only valid if this is head object..
	 */
	public void castShadow() {
		castShadow(id);
	}

	/**
	 * Returns left child of Operand
	 * 
	 * @return Operand object or null if there is no child
	 */
	public Operand getLeftOperand() {
		return leftOperand;
	}

	public void setLeftOperand(Operand leftOperand) {
		this.leftOperand = leftOperand;
	}

	/**
	 * Evaluate value using data gathered during unification.
	 * 
	 * @param id Identity of caller, which must be provided for backup()
	 * @return EvaluationStatus
	 */
	public EvaluationStatus evaluate(int id) {
		return EvaluationStatus.COMPLETE;
	}

	/**
	 * Set operand empty
	 */
	public void empty() {
		empty = true;
	}

	/**
	 * Backup to initial state if given id matches id assigned on unification or
	 * given id = 0.
	 * 
	 * @param id Identity of caller.
	 * @return boolean true if backup occurred
	 * @see #unify(Term otherParam, int id)
	 * @see #evaluate(int id)
	 */
	public boolean backup(int id) {
		if ((this.id == 0) || ((id != 0) && (this.id != id)))
			return false;
		if (!empty)
			super.clearValue();
		if (isHead())
			backupHead(id);
		return true;
	}

	/**
	 * Set this operand private - not visible in solution
	 * 
	 * @param isPrivate Flag set true if operand not visible in solution
	 */
	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	/**
	 * Returns flag set true if this operand is private
	 * 
	 * @return boolean
	 */
	public boolean isPrivate() {
		if (isPrivate)
			return true;
		return head == null ? false : head.isPrivate();
	}

	/**
	 * setArchetypeId
	 * 
	 * @param archetypeId Archetype id
	 */
	public void setArchetypeId(int archetypeId) {
		this.archetypeId = archetypeId;
	}

	/**
	 * getArchetypeId
	 * 
	 * @return archetype id
	 */
	public int getArchetypeId() {
		return archetypeId;
	}

	/**
	 * Sets index of this Operand in the archetype of it's containing template
	 * 
	 * @param archetypeIndex int value
	 */
	public void setArchetypeIndex(int archetypeIndex) {
		this.archetypeIndex = archetypeIndex;
	}

	/**
	 * Returns index of this Operand in the archetype of it's containing template.
	 * 
	 * @return non-negative number, if set, otherwise -1
	 */
	public int getArchetypeIndex() {
		return archetypeIndex;
	}

	public Operand getHead() {
		return head;
	}

	public boolean isShadow() {
		return (head != null) && (head != this);
	}

	public boolean isHead() {
		return (next != null) && (head == null);
	}

	@Override
	public boolean isEmpty() {
		if (super.isEmpty())
			return true;
		if ((next != null) && (leftOperand != null) && leftOperand.isEmpty()) {
			empty = true;
			return true;
		}
		return false;
	}

	/**
	 * Delegate to perform actual unification with other Term. If successful, two
	 * terms will be equivalent.
	 * 
	 * @param otherTerm Term with which to unify
	 * @param id        Identity of caller, which must be provided for backup()
	 * @return Identity passed in param "id" or zero if unification failed
	 * @see #backup(int id)
	 */
	@Override
	public int unify(Term otherTerm, int id) {
		int unifyId = super.unify(otherTerm, id);
		if (isHead())
			castShadow(id);
		if (isShadow())
			head.castShadow(getValue(), id);
		return unifyId;
	}

	@Override
	public void setValue(Object value) {
		super.setValue(value);
		if (delegateOperator != null) {
			if (value != null)
				delegateOperator.setDelegate(getValueClass());
			if (isHead())
				castShadow();
		} else if (isHead())
			castShadow(0);
	}

	@Override
	public Parameter toParameter() {
		return (Parameter) this;
	}

	@Override
	public void setExecutionContext(ExecutionContext context) {
		this.context = context;
	}

	protected void castShadow(int id) {
		Operand operand = next;
		while (operand != null) {
			operand.setValue(value);
			if (id > 0)
				operand.setId(id);
			operand = operand.next;
		}
	}

	/**
	 * Back up head, avoiding recursion
	 */
	private void backupHead(int id) {
		if (!isInBackup) {
			isInBackup = true;
			Operand operand = next;
			while (operand != null) {
				if (!operand.isEmpty())
					// If partial backup, ensure all shadows backup by using onn id
					operand.backup(id == 0 ? 0 : operand.getId());
				operand = operand.next;
			}
			isInBackup = false;
		}
	}

}
