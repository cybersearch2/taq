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

import java.math.BigDecimal;

import com.j256.simplelogging.Logger;

import au.com.cybersearch2.taq.artifact.LiteralArtifact;
import au.com.cybersearch2.taq.expression.Evaluator;
import au.com.cybersearch2.taq.helper.Blank;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.OperandVisitor;
import au.com.cybersearch2.taq.interfaces.TermListManager;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.log.LogManager;
import au.com.cybersearch2.taq.query.Solution;

/**
 * Unifier Attempts to unify one operand with a term. For an operand in the
 * template context, the term is selected from a supplied axiom, otherwise the
 * operand is unified with a term selected from a solution axiom, if available.
 * Supports variable initialization which has special rules.
 * 
 * @author Andrew Bowley 9May,2017
 */
public class Unifier implements OperandVisitor {
	
	/** Logger */
	private static final Logger logger = LogManager.getLogger(Unifier.class);

	/** The template containing operands to be unified */
	private final Template template;
	/** Int array mapping operand indexes to name=matched axiom indexes */
	private final int[] termMapping;
	/** Axiom reduced to a TermList object */
	private final TermList<Term> axiom;

	/** Optional solution pairer, used if solution keyset is non-empty */
	private SolutionPairer solutionPairer;
	/** ID applied upon unification of any operand */
	private int modificationId;
	/** Flag set true if unification uses case insensitive text comparison */
	private boolean caseInsensitive;

	/**
	 * Construct Unifier object
	 * 
	 * @param template    Template performing unification
	 * @param axiom       Axiom performing unification
	 * @param termMapping Int array mapping operand indexes to name-matched axiom
	 *                    indexes
	 * @param solution    Contains result of query up to this stage
	 */
	public Unifier(Template template, TermList<Term> axiom, int[] termMapping, Solution solution) {
		this.template = template;
		this.axiom = axiom;
		this.termMapping = termMapping;
		modificationId = template.getId();
		if ((solution != null) && (solution.keySet().size() > 0))
			solutionPairer = template.getSolutionPairer(solution);
	}

	public void setCaseInsensitive(boolean caseInsensitive) {
		this.caseInsensitive = caseInsensitive;
	}

	/**
	 * next
	 * 
	 * @see au.com.cybersearch2.taq.interfaces.OperandVisitor#next(au.com.cybersearch2.taq.interfaces.Operand,
	 *      int)
	 */
	@Override
	public boolean next(Operand operand, int depth) {
		if (!operand.getName().isEmpty() && !(operand instanceof Evaluator)) {
			int index;
			if (operand.getArchetypeId() == template.getId())
				index = operand.getArchetypeIndex();
			else {
				TermListManager archetype = template.getArchetype();
				index = archetype.getIndexForName(operand.getName(), caseInsensitive);
			}
			if (index != -1) { // Operand in template context
								// Pair by mapped index
				if (index >= termMapping.length) {
					// Paranoid check - This is not expected to happen
					logger.warn(
							String.format("Template index %d exceeds mapping index %d", index, termMapping.length - 1));
					return true;
				}
				int pairIndex = termMapping[index];
				if (pairIndex != -1) {
					int id = modificationId != 0 ? template.getId() : 0;
					Term axiomTerm = axiom.getTermByIndex(pairIndex);
					if (axiomTerm == null) {
						logger.warn(String.format("Term index %d mapping failed for operand %s", pairIndex,
								operand.getName()));
						return false;
					}
					if (pairTerms(operand, axiomTerm, id)) {
						if (axiomTerm.getValueClass() == Blank.class)
							pairSolution(operand);
						return true;
					} else
						return pairSolution(operand);
				} else if ((operand.getArchetypeIndex() != -1) && pairSolution(operand))
					// Operand in another template context and solution available for unification
					return true;
			} else if ((operand.getArchetypeIndex() != -1) && pairSolution(operand))
				// Operand in another template context and solution available for unification
				return true;
		}
		return true;
	}

	private boolean pairSolution(Operand operand) {
		if ((solutionPairer != null) &&
		// Operand in another template context and solution available for unification
				solutionPairer.next(operand, 0))
			return true;

		return false;
	}

	/**
	 * Unify operand with term
	 * 
	 * @param operand Operand to unify, if empty, else compare values
	 * @param term    Term to unify
	 * @return flag set true if unification succeeded
	 */
	private boolean pairTerms(Operand operand, Term term, int id) {
		// Pair first term to other term if first term is empty
		if (operand.isEmpty()) {
			// template.add(operand, otherTerm);
			operand.unifyTerm(term, id);
			return true;
		}
		// Check for exit case: terms in the same name space have different values
		else if (id == 0) {
			operand.setValue(term.getValue());
			return true;
		} else {
			Object operandValue = operand.getValue();
			Object termValue = term.getValue();
			boolean match = operandValue.equals(termValue);
			if (!match && ((operandValue instanceof Number) || (termValue instanceof Number)))
				match = isNumberMatch(operandValue, termValue);
			else if (!match && ((operandValue instanceof Boolean) || (termValue instanceof Boolean)))
				match = isBooleanMatch(operandValue, termValue);
			return match;
		}
	}

	private boolean isNumberMatch(Object operandValue, Object termValue) {
		// Convergence of numbers is problematic due to type conversion and string
		// formatting.
		// A match of values as strings handles simple cases
		boolean match = operandValue.toString().equals(termValue.toString());
		if (match)
			return true;
		// Do big decimal comparison if both values are Number types.
		// If one value is a String type, allow match if it contains at least
		// one digit. Additional explicit filtering is required to eliminate
		// false positives.
		BigDecimal v1 = null;
		BigDecimal v2 = null;

		if (operandValue instanceof BigDecimal)
			v1 = (BigDecimal) operandValue;
		else if (operandValue instanceof Double)
			v1 = new BigDecimal(((Double) operandValue).toString());
		else if (operandValue instanceof Long) {
			Long longValue = (Long) termValue;
			// Longs are formated as doubles to get '.0' appended
			v1 = new BigDecimal(Double.toString(longValue.doubleValue()));
		} else if (operandValue instanceof String)
			match = LiteralArtifact.isNumber(operandValue.toString());
		if (!match && (v1 != null)) {
			if (termValue instanceof BigDecimal)
				v2 = (BigDecimal) termValue;
			else if (termValue instanceof Double)
				v2 = new BigDecimal(((Double) termValue).toString());
			else if (termValue instanceof Long) {
				Long longValue = (Long) termValue;
				// Longs are formated as doubles to get '.0' appended
				v2 = new BigDecimal(Double.toString(longValue.doubleValue()));
			} else if (termValue instanceof String)
				match = LiteralArtifact.isNumber(termValue.toString());
			if (!match && (v2 != null))
				match = v1.equals(v2);
		}
		return match;
	}

	private boolean isBooleanMatch(Object operandValue, Object termValue) {
		boolean match = operandValue.toString().equals(termValue.toString());
		if (match)
			return true;
		if (operandValue instanceof Boolean)
			match = LiteralArtifact.isMatch((Boolean) operandValue, termValue.toString());
		else
			match = LiteralArtifact.isMatch((Boolean) termValue, operandValue.toString());
		return match;
	}

}
