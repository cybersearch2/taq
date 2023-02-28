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
package system;

import java.util.Date;
import java.util.List;

import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.interfaces.FunctionProvider;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.provider.CallHandler;

public class SystemFunctionProvider implements FunctionProvider {

	private final static class TimestampHandler extends CallHandler {

		public static final String TIMESTAMP = "timestamp";
		
		protected TimestampHandler(String name) {
			super(name, OperandType.UNKNOWN);
		}

        @Override
		public boolean setReturnType(OperandType returnType) {
			if ((returnType != OperandType.UNKNOWN) && (returnType != OperandType.STRING))
				return false;
			this.returnType = returnType;
			return true;
		}

        @Override
        public boolean evaluate(List<Term> argumentList)
        {
            Date date = new Date();
            Object value = returnType == OperandType.UNKNOWN ?
            		date : date.toString();
            Term term = solutionAxiom.getTermByIndex(0);
            if (term != null)
            	term.setValue(value);
            else
            	solutionAxiom.addTerm(new Parameter(Term.ANONYMOUS, value));
            return true;
        }
	}		

	private final static class SimpleNameHandler extends CallHandler {

		public static final String SIMPLE_NAME = "simple_name";

		protected SimpleNameHandler(String name) {
			super(name, OperandType.STRING);
		}

       @Override
		public boolean setReturnType(OperandType returnType) {
			return returnType == this.returnType;
		}

        @Override
        public boolean evaluate(List<Term> argumentList)
        {
        	if (argumentList.isEmpty())
        		return false;
        	Term arg1 = argumentList.get(0);
        	String value = arg1.getValueClass().getSimpleName();
            Term term = solutionAxiom.getTermByIndex(0);
            if (term != null)
            	term.setValue(value);
            else
            	solutionAxiom.addTerm(new Parameter(Term.ANONYMOUS, value));
            return true;
        }
	}

    @Override
    public String getName()
    {
        return "system";
    }

    @Override
    public CallHandler getCallEvaluator(String identifier)
    {
        if (TimestampHandler.TIMESTAMP.equals(identifier))
            return new TimestampHandler(identifier);
        else if (SimpleNameHandler.SIMPLE_NAME.equals(identifier))
        	return new SimpleNameHandler(identifier);
        throw new ExpressionException("Unknown function identifier: " + identifier);
    }
}
