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
package service;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.interfaces.FunctionProvider;
import au.com.cybersearch2.taq.interfaces.Trait;
import au.com.cybersearch2.taq.language.LiteralParameter;
import au.com.cybersearch2.taq.language.LiteralType;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.operator.OperatorTerm;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.provider.CallHandler;

public class ServiceProvider implements FunctionProvider {

	private static final class FunctionCallHandler extends CallHandler {

		private static final String servicePattern = "#([0-9]+)";
		private static final String amountPattern = "(\\$[0-9]+\\.[0-9]+)";
		private static final String scanPattern =   
				  "^Service "  + servicePattern + 
				  "\\s+" + amountPattern + "?$";
		
		/** Pre-compiled pattern */
		protected Pattern pattern;

		protected FunctionCallHandler(String name) {
			super(name, OperandType.TERM);
		}

        @Override
		public boolean setReturnType(OperandType returnType) {
        	return returnType == this.returnType;
		}

		@Override
		public boolean evaluate(List<Term> argumentList) {
			Term argument = argumentList.get(0);
			Locale locale;
        	if (argument instanceof OperatorTerm) {
        		OperatorTerm operatorTerm = (OperatorTerm)argument;
        		Trait trait = operatorTerm.getOperator().getTrait();
        		if (trait.getOperandType() != OperandType.STRING)
        			throw new ExpressionException(String.format("Invalid %s parameter type, expecting string" , trait.getOperandType().name().toLowerCase()));
        		locale = trait.getLocale();
        	} else
        		locale = Locale.getDefault();
			Matcher matcher = getMatcher(argument.getValue().toString());
			if (matcher.find()) {
				Axiom axiom = new Axiom(getName());
				axiom.addTerm(new LiteralParameter("service", matcher.group(1), LiteralType.string));
				axiom.addTerm(new LiteralParameter("amount", matcher.group(2), LiteralType.string));
                onNextAxiom(axiom.getArchetype().getQualifiedName(), axiom, locale);
				return true;
			}
			return false;
		}
		
		protected Matcher getMatcher(String input)
		{
	        try
	        {
	            pattern = Pattern.compile(scanPattern, 0);
	        }
	        catch(PatternSyntaxException e)
	        {
	            throw new ExpressionException("Error in regular expression", e);
	        }
	        // Retain value on match
	        return pattern.matcher(input);
		}
	}
	


	public ServiceProvider() {
	}

	@Override
	public String getName() {
		return "service";
	}

	@Override
	public CallHandler getCallEvaluator(String identifier) {
		return new FunctionCallHandler(getName());
	}

}
