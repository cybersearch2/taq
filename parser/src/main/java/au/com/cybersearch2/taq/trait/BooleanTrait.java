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
package au.com.cybersearch2.taq.trait;

import java.util.Locale;

import au.com.cybersearch2.taq.artifact.LiteralArtifact;
import au.com.cybersearch2.taq.expression.BooleanOperand;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.StringCloneable;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.Term;

/**
 * BooleanTrait
 * Behaviors for localization and specialization of Boolean operands
  */
public class BooleanTrait extends DefaultTrait implements StringCloneable {

    /**
     * Construct BooleanTrait object
     */
    public BooleanTrait()
    {
        super(OperandType.BOOLEAN);
    }

    /**
     * parseValue
     */
    public Boolean parseValue(String string)
    {
        return LiteralArtifact.isMatch(Boolean.TRUE, string);
    }

	@Override
	public Operand cloneFromOperand(Operand stringOperand) {
        BooleanOperand clone = 
            stringOperand.getLeftOperand() == null ? 
            new BooleanOperand(stringOperand.getQualifiedName(), false) :
            new BooleanOperand(stringOperand.getQualifiedName(), stringOperand.getLeftOperand());
        Parameter param = new Parameter(Term.ANONYMOUS, stringOperand.getValue().toString());
        param.setId(stringOperand.getId());
        Locale locale = stringOperand.getOperator().getTrait().getLocale();
        clone.getOperator().getTrait().setLocale(locale);
        clone.assign(param);
        clone.setArchetypeId(stringOperand.getArchetypeId());
        clone.setArchetypeIndex(stringOperand.getArchetypeIndex());
        clone.setId(stringOperand.getId());
        return clone;
	}

}
