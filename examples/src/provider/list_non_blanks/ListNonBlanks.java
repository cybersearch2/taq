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
package list_non_blanks;

import java.util.List;

import au.com.cybersearch2.taq.compile.VariableFactory;
import au.com.cybersearch2.taq.compile.VariableSpec;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.interfaces.FunctionProvider;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.operator.ListOperator;
import au.com.cybersearch2.taq.operator.OperatorTerm;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.provider.CallHandler;

public class ListNonBlanks implements FunctionProvider {

	private final static class FunctionCallHandler extends CallHandler {

		public static final String IDENTIFIER = "list_non_blanks";
		
		protected FunctionCallHandler(String name) {
			super(name, OperandType.STRING);
		}

		@Override
		public boolean setListReturnType(OperandType listType) {
			return (listType == returnType);
		}

		@Override
		public boolean evaluate(List<Term> argumentList) {
			VariableSpec variableSpec = new VariableSpec(OperandType.STRING);
			VariableFactory variableFactory = new VariableFactory(variableSpec);
			QualifiedName qname = new QualifiedName(getName(), IDENTIFIER);
			@SuppressWarnings("unchecked")
			ItemList<String> returnList = (ItemList<String>) variableFactory.getItemListInstance(qname, true);
			Object value = argumentList.get(0).getValue();
			if (!(value instanceof Axiom))
				throw new ExpressionException("Parammeter " + value.toString() + " is not an Axiom");
			Axiom axiom = (Axiom)value;
        	for (int i = 0; i < axiom.getTermCount(); ++i) {
        		Term term = axiom.getTermByIndex(i);
        		if (!term.getValue().toString().isEmpty()) {
        			returnList.append(term.toString());
        		}
        	}
			OperatorTerm param = new OperatorTerm(qname.getName(), returnList, new ListOperator());
            Term term = solutionAxiom.getTermByIndex(0);
            if (term != null)
            	term.setValue(param);
            else
            	solutionAxiom.addTerm(param);
            return true;
		}
	}
	
	@Override
	public String getName() {
		return "blank";
	}

	@Override
	public CallHandler getCallEvaluator(String identifier) {
        if (FunctionCallHandler.IDENTIFIER.equals(identifier))
			return new FunctionCallHandler(getName());
	    throw new ExpressionException("Unknown function identifier: " + identifier);
    }
}
