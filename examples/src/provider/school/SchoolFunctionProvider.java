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
package school;

import java.util.List;
import java.util.Locale;

import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.expression.StringOperand;
import au.com.cybersearch2.taq.interfaces.FunctionProvider;
import au.com.cybersearch2.taq.interfaces.Trait;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.operator.OperatorTerm;
import au.com.cybersearch2.taq.pattern.Template;
import au.com.cybersearch2.taq.pattern.TemplateArchetype;
import au.com.cybersearch2.taq.provider.CallHandler;

/**
 * SchoolFunctionProvider
 * @author Andrew Bowley
 * 17Aug.,2017
 */
public class SchoolFunctionProvider implements FunctionProvider
{

	private final static class FunctionCallHandler extends CallHandler {
		
		private static final String UNKNOWN_IDENTIFIER = "Unknown function identifier: ";
	
		static String[] MARKS =
	    {
	        "f-", "f", "f+", "e-", "e", "e+", "d-", "d", "d+", 
	        "c-", "c", "c+", "b-", "b", "b+", "a-", "a", "a+"
	    };
	 
	    static String SUBJECT = "subject";
	    static String MARK = "mark";
	    static String CONVERT_GRADES = "convert_grades";
	    static QualifiedName listName = QualifiedName.parseGlobalName("subjects");

	    protected FunctionCallHandler(String name) {
			super(name, OperandType.SET_LIST);
		}

        @Override
		public boolean setReturnType(OperandType returnType) {
        	return returnType == this.returnType;
		}

		@Override
		public boolean evaluate(List<Term> argumentList) {
        	Template solutionTemplate = getSolutionTemplate();
            if (solutionTemplate == null) {
            	solutionTemplate = new Template(CONVERT_GRADES, getTemplateArchetype(CONVERT_GRADES));
            }
            Template tempate = solutionTemplate;
        	argumentList.forEach(argument -> process(tempate, argument));
            return true;
        }
        
		private void process(Template solutionTemplate, Term argument) {
        	if (argument instanceof OperatorTerm) {
        		OperatorTerm operatorTerm = (OperatorTerm)argument;
        		Trait trait = operatorTerm.getOperator().getTrait();
        		if (trait.getOperandType() == OperandType.INTEGER)
        			doConversion(solutionTemplate, argument.getName(), ((Long)argument.getValue()).intValue(), trait.getLocale());
        		else {
        			// TODO - Handle invalid argument
        		}
        	}
        }
        
        private void doConversion(Template solutionTemplate, String subject, int grade, Locale locale) {
        	solutionTemplate.getTermByIndex(0).setValue(subject);
        	solutionTemplate.getTermByIndex(1).setValue(MARKS[grade - 1]);
            onNextAxiom(listName, solutionTemplate.toAxiom(), locale);
        }
        
        private TemplateArchetype getTemplateArchetype(String identifier) {
            if (!identifier.equals(CONVERT_GRADES)) 
                throw new ExpressionException(UNKNOWN_IDENTIFIER + identifier);
        	TemplateArchetype archetype = new TemplateArchetype(new QualifiedName(getName(), CONVERT_GRADES));
        	archetype.addTerm(archetype.analyseTerm(new StringOperand(new QualifiedName(SUBJECT)), 0));
        	archetype.addTerm(archetype.analyseTerm(new StringOperand(new QualifiedName(MARK)), 0));
        	return archetype;
        }
        
    }
	
     /**
     * @see au.com.cybersearch2.taq.interfaces.FunctionProvider#getName()
     */
    @Override
    public String getName()
    {
        return "school";
    }

    /**
     * @see au.com.cybersearch2.taq.interfaces.FunctionProvider#getCallEvaluator(java.lang.String)
     */
    @Override
    public CallHandler getCallEvaluator(String identifier)
    {
        if (!identifier.equals(FunctionCallHandler.CONVERT_GRADES)) 
            // Throw exception for unrecognized function name   
            throw new ExpressionException(FunctionCallHandler.UNKNOWN_IDENTIFIER + identifier);
        return new FunctionCallHandler(FunctionCallHandler.CONVERT_GRADES);
    }

}
