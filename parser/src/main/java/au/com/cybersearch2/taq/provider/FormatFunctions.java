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
package au.com.cybersearch2.taq.provider;

import au.com.cybersearch2.taq.expression.BigDecimalOperand;
import au.com.cybersearch2.taq.expression.BooleanOperand;
import au.com.cybersearch2.taq.expression.ChoiceOperand;
import au.com.cybersearch2.taq.expression.DoubleOperand;
import au.com.cybersearch2.taq.expression.IntegerOperand;
import au.com.cybersearch2.taq.expression.Variable;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.Null;
import au.com.cybersearch2.taq.language.Unknown;

/**
 * Format functions available by object interface
 *
 */
public class FormatFunctions {

	public final Operand variable;
	
	public FormatFunctions(BigDecimalOperand variable) {
		this.variable = variable;
	}
	
	public FormatFunctions(DoubleOperand variable) {
		this.variable = variable;
	}
	
	public FormatFunctions(IntegerOperand variable) {
		this.variable = variable;
	}
	
	public FormatFunctions(BooleanOperand variable) {
		this.variable = variable;
	}
	
	public FormatFunctions(Variable variable) {
		this.variable = variable;
	}
	
	public FormatFunctions(ChoiceOperand choiceOperand) {
		this.variable = choiceOperand;
	}
	
	public String format() {
	    Object expressionValue = variable.getValue();
	    String formatValue;
	    if ((expressionValue instanceof Unknown) || 
	        (expressionValue instanceof Null) ||
	        (expressionValue instanceof Double && ((Double)expressionValue).isNaN()))
            formatValue = Unknown.UNKNOWN;
        else
            try
    	    {
            	if (variable.isShadow())
                    formatValue= variable.getHead().getOperator().getTrait().formatValue(expressionValue);
            	else
                    formatValue= variable.getOperator().getTrait().formatValue(expressionValue);
    	    }
    	    catch(IllegalArgumentException e)
    	    {
                formatValue = Unknown.UNKNOWN;
    	    }
	    return formatValue;
	}
}
