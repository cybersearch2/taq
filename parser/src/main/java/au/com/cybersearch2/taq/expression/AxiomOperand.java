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

import au.com.cybersearch2.taq.compile.AxiomListEvaluator;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.AxiomListListener;
import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.list.AxiomList;
import au.com.cybersearch2.taq.operator.AxiomOperator;

/**
 * AxiomOperand
 * Contains an AxiomList value. 
 * Concatenation operation causes contents of right operand to be appended to the this operand.
 * Assignment is only other operation allowed.
 * @author Andrew Bowley
 * 5 Aug 2015
 */
public class AxiomOperand extends ExpressionOperand<AxiomList>
{
    /** Axiom key to use when an empty list is created */
    protected QualifiedName axiomKey;
    /** Creates an AxiomList object on evaluation */
    protected AxiomListEvaluator axiomListEvaluator;
    /** Axiom listener to notify when an axiom list is created/assigned */
    protected AxiomListListener axiomListListener;
    /** Defines operations that an Operand performs with other operands. To be set by super. */
    protected AxiomOperator operator;
    /** The axiom list - reference kept to recover from partial backup */
    protected AxiomList axiomList;
    
    /**
     * Construct AxiomOperand object as Axiom list variable
     * @param qname Qualified name
     * @param axiomKey Axiom key to use when an empty list is created
     * @param axiomListListener Axiom listener to notify when an axiom list is created/assigned or null
     */
    public AxiomOperand(AxiomList axiomList, AxiomListListener axiomListListener) 
    {
        super(axiomList.getQualifiedName());
        this.axiomList = axiomList;
        this.axiomListListener = axiomListListener;
        setValue(axiomList);
        init();
    }

    /**
     * Construct AxiomOperand object as Axiom list literal
     * @param qname Qualified name
     * @param axiomList Axiom list literal value
     */
    public AxiomOperand(QualifiedName qname, AxiomList axiomList) 
    {
        super(qname, axiomList);
        this.axiomList = axiomList;
        init();
    }

    /**
     * Construct AxiomOperand object as Axiom List
     * @param axiomListEvaluator Evaluates to create an AxiomList from an initialization list
     * @param axiomListListener Axiom listener to notify when an axiom list is created
     */
    public AxiomOperand(AxiomListEvaluator axiomListEvaluator, AxiomListListener axiomListListener) 
    {
        super(axiomListEvaluator.getQualifiedName());
        this.axiomList = axiomListEvaluator.getAxiomList();
        this.axiomListEvaluator = axiomListEvaluator;
        this.axiomListListener = axiomListListener;
        init();
    }

    /**
     * Copy all axioms to given axiom listener
     * @param axiomListener
     * @return number of axioms copied
     */
	public int copyList(LocaleAxiomListener axiomListener) {
		axiomList.forEach(axiom -> {
		    axiomListener.onNextAxiom(
			    	axiom.getArchetype().getQualifiedName(), 
			    	axiom,
			    	getOperator().getTrait().getLocale());
		});
		return axiomList.getLength();
	}
	
    /**
     * Execute operation for expression
     * @param id Identity of caller, which must be provided for backup()
     * @return Flag set true if evaluation is to continue
     */
    @Override
    public EvaluationStatus evaluate(int id)
    {
        EvaluationStatus status = EvaluationStatus.COMPLETE;
        if (empty && (axiomListEvaluator != null))
        {   // Create list if not set or parameters have changed
            if ((axiomList.isEmpty()) || ((axiomListEvaluator.size() > 0) && axiomListEvaluator.isEmpty()))
            {
            	axiomListEvaluator.setExecutionContext(context);
                axiomList = axiomListEvaluator.evaluate(id);
                // Do not set id if list is created empty
                if (axiomListEvaluator.size() > 0)
                    this.id = id;
                if (axiomListListener != null)
                    axiomListListener.addAxiomList(qname, axiomList);
            }
            setValue(axiomList);
        }
        if (isEmpty())
            // If an error occurs populate with an empty list for graceful handling
            setValue(new AxiomList(qname, axiomKey));
        return status;
    }

    /**
     * Backup to initial state if given id matches id assigned on unification or given id = 0. 
     * @param id Identity of caller. 
     * @return boolean true if backup occurred
     * @see au.com.cybersearch2.taq.language.Parameter#unify(Term otherParam, int id)
     */
    @Override
    public boolean backup(int id)
    {
        if (axiomListEvaluator != null)
            axiomListEvaluator.backup(id);
        return super.backup(id);
    }
    
    /**
     * Assign a value to this Operand derived from a parameter 
     * @param parameter Parameter containing non-null value
     */
    @Override
    public void assign(Parameter parameter)
    {
        super.assign(parameter);
        axiomList = (AxiomList)parameter.getValue();
        if (axiomListListener != null)
            axiomListListener.addAxiomList(qname, axiomList);
    }

    /**
     * Override toString() to incorporate intialization list
     * @see au.com.cybersearch2.taq.language.Parameter#toString()
     */
    @Override
    public String toString()
    {
        if (axiomListEvaluator != null)
        {
            StringBuilder builder = new StringBuilder("list<axiom> ");
            builder.append(qname.toString());
            int length = empty ? axiomListEvaluator.size() : ((AxiomList)getValue()).getLength();
            builder.append('[').append(Integer.toString(length)).append(']');
            return builder.toString();
        }
        else
            return super.toString();
    }

    /**
     * getOperator
     * @see au.com.cybersearch2.taq.interfaces.Operand#getOperator()
     */
    @Override
    public Operator getOperator()
    {
        return operator;
    }

    /**
     * Set operator
     */
    private void init()
    {
        operator = new AxiomOperator();
    }

}
