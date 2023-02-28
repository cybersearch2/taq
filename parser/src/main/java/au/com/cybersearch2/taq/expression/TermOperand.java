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

import au.com.cybersearch2.taq.compile.AxiomTermListEvaluator;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.list.AxiomTermList;
import au.com.cybersearch2.taq.operator.TermOperator;

/**
 * TermOperand
 * Contains an AxiomList value. 
 * Concatenation operation causes contents of right operand to be appended to the this operand.
 * Assignment is only other operation allowed.
 * @author Andrew Bowley
 * 5 Aug 2015
 */
public class TermOperand extends ExpressionOperand<AxiomTermList>
{
    /** Axiom key to use when an empty list is created */
    private final QualifiedName axiomKey;
    /** Defines operations that an Operand performs with other operands. To be set by super. */
    private final TermOperator operator;
    /** Creates an AxiomList object on evaluation */
    protected AxiomTermListEvaluator axiomTermListEvaluator;

    /**
     * Construct Axiom term list variable with evaluator to initialize it
     * @param axiomTermListEvaluator Creates an AxiomList object on evaluation
     */
    public TermOperand(AxiomTermListEvaluator axiomTermListEvaluator) 
    {
        super(axiomTermListEvaluator.getQualifiedName());
        this.axiomKey = axiomTermListEvaluator.getAxiomKey();
        this.axiomTermListEvaluator = axiomTermListEvaluator;
        operator = new TermOperator();
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
        // Perform static initialization to a list of axioms
        if (axiomTermListEvaluator != null)
        {
            // Only set id if axiom term list is created non-empty
            setValue(axiomTermListEvaluator.evaluate(id, context));
            if (axiomTermListEvaluator.size() > 0)
                 this.id = id;
            if (isEmpty())
                // If an error occurs populate with an empty list for graceful handling
                setValue(new AxiomTermList(qname, axiomKey));
        }
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
        if (axiomTermListEvaluator != null)
        {
            axiomTermListEvaluator.backup(id);
            return super.backup(id);
        }
        return false;
    }
    
    /**
     * Override toString() to incorporate initialization list
     * @see au.com.cybersearch2.taq.language.Parameter#toString()
     */
    @Override
    public String toString()
    {
        if (empty)
            return axiomTermListEvaluator.toString();
        return super.toString();
    }

    @Override
    public Operator getOperator()
    {
        return operator;
    }
}
