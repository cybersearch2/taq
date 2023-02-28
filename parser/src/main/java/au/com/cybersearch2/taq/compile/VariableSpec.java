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
package au.com.cybersearch2.taq.compile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.IOperand;
import au.com.cybersearch2.taq.language.ITemplate;
import au.com.cybersearch2.taq.language.IVariableSpec;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.OperatorEnum;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.TaqLiteral;
import au.com.cybersearch2.taq.pattern.Template;

/**
 * Contains parameters used to create a new variable
 */
public class VariableSpec implements IVariableSpec {

    /** Type of variable */
	private OperandType operandType;

    /** Axiom key property, which allows an axiom list to 
     *  contain axioms with a different name to that of the list */
    private QualifiedName axiomKey;
    /** Operand expression - sets the intial value of the operand */
    private Operand expression;
    /** Templates for list initialization */
    private List<ITemplate> templateList;
    /** Post-list-initialization template */
    private Template template;
    /** Locale of currency */
    private Locale locale;
    /** Currency property */
    private Currency currency;
    /** Currency country evaluation operand property */
    private Operand qualifierOperand;
    /** Literal operand property - evaluation not required */
    private Operand literal;
     /** List export flag */
    private boolean isExport;
    /** Reflex operator enum */
    private OperatorEnum reflexOp;
    /** Variable declaration */
    private String source;
    
	public VariableSpec(OperandType operandType) {
		this.operandType = operandType;
	}

	@Override
	public OperandType getOperandType() {
		return operandType;
	}

	@Override
	public void setUnknownType() {
		operandType = OperandType.UNKNOWN;
	}

	@Override
	public boolean isAxiom() { 
		return operandType == OperandType.AXIOM;
	}
	
	@Override
	public boolean isSetList() {
		return operandType == OperandType.SET_LIST;
	}
	
	@Override
	public boolean isTerm() {
		return operandType == OperandType.TERM;
	}

	@Override
	public boolean axiomKeyExists() {
		return axiomKey != null;
	}

	@Override
	public boolean existsLocale() {
		return locale != null;
	}
	
	@Override
	public boolean existsCurrency() {
		return currency != null;
	}
	
	@Override
	public boolean isListUnitialized() {
		return (template == null) &&
	        	((templateList == null) || 
	        	(((ITemplate) templateList.get(0)).getTermCount() == 0));
	}
	
	@Override
	public void setAxiomKey(QualifiedName axiomKey) {
		this.axiomKey = axiomKey;
	}

	public void setExpression(Operand expression) {
		this.expression = expression;
	}

	@Override
	public void setTemplateList(List<ITemplate> templateList) {
		this.templateList = templateList;
	}

	@Override
	public void setTemplate(ITemplate template) {
		this.template = (Template) template;
	}

	@Override
	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	@Override
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	@Override
	public void setQualifierOperand(IOperand qualifierOperand) {
		this.qualifierOperand = (Operand)qualifierOperand;
	}

	public void setLiteral(Operand literal) {
		this.literal = literal;
	}

	@Override
	public void setExport(boolean isExport) {
		this.isExport = isExport;
	}

	@Override
	public void setReflexOp(OperatorEnum reflexOp) {
		this.reflexOp = reflexOp;
	}

	@Override
	public QualifiedName getAxiomKey() {
		return axiomKey;
	}

	public Operand getExpression() {
		return expression;
	}

	public List<Template> getTemplateList() {
    	if ((templateList != null) && !templateList.isEmpty()) {
    		List<Template> initializeList = new ArrayList<>(templateList.size());
    		templateList.forEach(template -> initializeList.add((Template)template));
    		return initializeList;
     	} else
		    return Collections.emptyList();
	}

	public Template getTemplate() {
		return template;
	}

	@Override
	public Currency getCurrency() {
		return currency;
	}

	public Operand getQualifierOperand() {
		return qualifierOperand;
	}

	@Override
	public Locale getLocale() {
		return locale;
	}

	public Operand getLiteral() {
		return literal;
	}

	@Override
	public boolean isExport() {
		return isExport;
	}

	@Override
	public OperatorEnum getReflexOp() {
		return reflexOp;
	}

	@Override
	public String getSource() {
		return source;
	}

	@Override
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * Convert TAQ literal to variable type
	 * @param literal TAQ literal enum
	 * @return VariableType object
	 */
	public static VariableSpec variableSpec(TaqLiteral literal) {
    	switch (literal) {
    	case axiom: return new VariableSpec(OperandType.AXIOM);
    	case integer: return new VariableSpec(OperandType.INTEGER);
    	case taq_boolean: return new VariableSpec(OperandType.BOOLEAN);
    	case taq_double: return new VariableSpec(OperandType.DOUBLE);
    	case decimal: return new VariableSpec(OperandType.DECIMAL);
    	case term: return new VariableSpec(OperandType.TERM);
    	case currency: return new VariableSpec(OperandType.CURRENCY);
    	case complex: return new VariableSpec(OperandType.COMPLEX);
    	// Return string type by default, but this is the only remaining valid option
    	default: return new VariableSpec(OperandType.STRING);
    	}
    }
}
