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

import java.util.Locale;

import au.com.cybersearch2.taq.helper.CountryCode;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.interfaces.Trait;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.operator.DelegateType;
import au.com.cybersearch2.taq.trait.CurrencyTrait;

/**
 * CountryOperand
 * @author Andrew Bowley
 * 8 Mar 2015
 */
public class CountryOperand extends ExpressionOperand<String> 
{
    /** Trait of operand to be set by this operand */
    protected Trait targetTrait;
    /** Current locale setting */
    protected Locale locale;
    protected Operator operator;
    
	/**
	 * Construct CountryOperand object for specified locale
     * @param qname Qualified name
     * @param targetTrait Trait of operand to be set by this operand
     * @param expression Operand assigned to evaluate country
	 */
	public CountryOperand(QualifiedName qname, Trait targetTrait, Operand expression) 
	{
		super(qname, expression);
		this.targetTrait = targetTrait;
		locale = targetTrait.getLocale();
		operator = DelegateType.ASSIGN_ONLY.getOperatorFactory().delegate();
	}

	/**
	 * Evaluate value using data gathered during unification.
	 * @param id Identity of caller, which must be provided for backup()
	 * @return Flag set true if evaluation is to continue
	 */
	@Override
	public EvaluationStatus evaluate(int id) 
	{
	    EvaluationStatus status = super.evaluate(id);
	    if ((status == EvaluationStatus.COMPLETE) && !empty)
	        updateLocale();
		return status;
	}

    @Override
    public Operator getOperator()
    {
        return operator;
    }
    
    /**
     * @see au.com.cybersearch2.taq.expression.ExpressionOperand#toString()
     */
    @Override
    public String toString()
    {
        return locale.toString();
    }

    protected void updateLocale()
    {
    	String localeString = getValue().toString();
    	if (!locale.toString().equals(localeString)) {
	    	String[] parts = localeString.split("_");
	    	if (parts.length == 2) {
		        locale = new Locale(parts[0], parts[1]);
		        targetTrait.setLocale(locale);
	    	} else if ((parts.length == 1) && (targetTrait instanceof CurrencyTrait)) {
	    		Locale countryLocale = CountryCode.getLocaleByCountryCode(localeString);
	    		if (locale == null)
	    	    	System.err.println("No locale found for country code " + localeString);
	    		else {
	    			locale = countryLocale;
	    			targetTrait.setLocale(locale);
	    		}
	    	}
	    }
    }

}
