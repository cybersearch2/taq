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

import au.com.cybersearch2.taq.expression.Variable;
import au.com.cybersearch2.taq.helper.Blank;
import au.com.cybersearch2.taq.helper.QualifiedTemplateName;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.OperandVisitor;
import au.com.cybersearch2.taq.interfaces.Trait;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.operator.OperatorTerm;
import au.com.cybersearch2.taq.query.Solution;

/**
 * SolutionPairer
 * Attempts to pair an operand to a solution term, and if successful, unifies them
 * @author Andrew Bowley
 * 20 Dec 2014
 */
public class SolutionPairer implements OperandVisitor 
{
    /** Map of Axioms selectable by Axiom name */
	protected Solution solution;
    /** Template id for unification */
    protected int id;
    /** Template qualified name */
    protected QualifiedName contextName;
	
	/**
	 * Construct SolutionPairer object
     * @param solution Contains query solution up to current stage
     * @param id Template id for unification
     * @param contextName Template qualified name
	 */
	public SolutionPairer(Solution solution, int id, QualifiedName contextName) 
	{
        this.solution = solution;
        this.id = id;
        this.contextName = contextName;
	}

	/**
	 * Set solution
	 * @param solution Contains query solution up to current stage
	 */
	public void setSolution(Solution solution)
	{
		this.solution = solution;
	}

	@Override
	public boolean next(Operand operand, int depth) 
	{
	    // Solution pairing only applies to operands with two or three part names
	    QualifiedName qname = operand.getQualifiedName();
	    if (qname.getTemplate().isEmpty())
	        return false;
	    // It is possible to unify an entire solution axiom to a variable with a template qualifier
	    boolean isAxiomMatch = false;
	    if (qname.getName().isEmpty()) {
	    	// The operand has a template qualifier if the template part is non-empty and the name is empty
	    	isAxiomMatch = operand.isEmpty() &&
	    			       (operand instanceof Variable);
	    	if (!isAxiomMatch)
	    		return false;
	    }
	    int stackSize = solution.getStackSize();
	    if (stackSize < 1)
	    	return false;
	    // Get keys from solution stack
	    String[] keys = solution.getStack();
	    // Determine if operand in same name space as template context. 
	    // Inner templates and replicates have 2 context names.
        boolean inSameSpace = (keys[0] != null);
        if (inSameSpace ) {
        	int pos = keys[0].indexOf(".");
        	if (pos != -1)
        		inSameSpace = contextName.getScope().equals(keys[0].substring(0, pos));
        }
        boolean skipFirstKey = false;
	    if (!inSameSpace)	
	    {
	    	inSameSpace = contextName.inSameSpace(operand.getQualifiedName());
		    if (contextName.inSameSpace(operand.getQualifiedName())) 
		    {
		        // A 2-part key indicates the solution template is in a non-global scope, so namespace match is required
		        if (!contextName.toString().equals(keys[0]))
		        	skipFirstKey = true;
		    }
		    else
		    {   // An operand belonging to another template can pair by template name and scope, which can be the operand's scope or the context scope, if not global.
		        String contextScope = contextName.getScope();
	            String operandKey = QualifiedTemplateName.toString(qname.getScope(), qname.getTemplate());
	 	        if (contextScope.isEmpty())
	 	            keys[0] = operandKey;
	 	        else {
	 	        	String[] keys2 = new String[stackSize + 1];
		            keys2[0] = QualifiedTemplateName.toString(contextScope, qname.getTemplate());
		            keys2[1] = operandKey;
		            if (stackSize > 1)
		            	System.arraycopy(keys, 1, keys2, 2, stackSize -1);
		            keys = keys2;
		        }
		    }
	    }
	    int count = 0;
	    for (String key: keys)
	    {
	    	++count;
	        if ((key == null) || key.isEmpty())
	            break;
	        if (count == 1) {
	        	if (skipFirstKey)
	        		continue;
	        } else if (key.indexOf(".")  != -1) {
	        	if (!((count == 2) && (keys.length != stackSize)))
	        	    continue;
	        }
    	    Axiom axiom = solution.getAxiom(key);
    		if (axiom != null) {
    	    	if (isAxiomMatch) {
   	                operand.unifyTerm(new Parameter(Term.ANONYMOUS, axiom), id);
    	    		return true;
    	    	} else {
	    			Term otherTerm = axiom.getTermByName(qname.getName());
	    			if (otherTerm instanceof OperatorTerm) {
	                    OperatorTerm operatorTerm = (OperatorTerm)otherTerm;
	                    return processKey(operand, operatorTerm, inSameSpace);
	    			}
    	    	}
    		}
	    }
		return false;
	}

	/**
	 * Process operand paired to solution term
	 * @param operand The operand to unify
	 * @param term The solution term to unify, which unlike a normal parameter, has the operator too
	 * @param inSameSpace Flag set true if operand belongs to same name space as template context
	 * @return boolean
	 */
	private boolean processKey(Operand operand, OperatorTerm term, boolean inSameSpace)
    {   
        if (operand.isEmpty()) {
            // Unify with Solution term
            operand.unifyTerm(term, id);
            Trait trait = term.getOperator().getTrait();
            if (trait.getOperandType() != OperandType.UNKNOWN)
                operand.getOperator().setTrait(trait);
            return true;
        } else if (operand.getValueClass() == Blank.class) {
            // Unify with Solution term
            operand.unify(term, id);
            Trait trait = term.getOperator().getTrait();
            if (trait.getOperandType() != OperandType.UNKNOWN)
                operand.getOperator().setTrait(trait);
            return true;
        } else if (inSameSpace)
            // Terminate this iteration if operand and term have mis-matched values
            return operand.getValue().equals(term.getValue());
        return false;
    }

}
