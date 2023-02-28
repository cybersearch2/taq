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

import java.util.List;

import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * LiteralListOperand
 * Compares a value to a list of literals to match on and short circuits if no match found
 * @author Andrew Bowley
 * 8 Sep 2015
 */
public class LiteralListOperand extends Variable
{
    /** Parameter list containing values to match on */
    protected List<Parameter> literalList;
    /**  Flag set true if match logic is reversed */
    protected boolean isNot;

    /**
     * Construct LiteralListOperand object
     * @param qname Qualified name of this operand
     * @param literalList Parameter list containing values to match on
     * @param isNot Flag set true if match logic is reversed
     */
    public LiteralListOperand(QualifiedName qname, List<Parameter> literalList, boolean isNot)
    {
        super(qname);
        this.literalList = literalList;
        this.isNot = isNot;
    }

    /**
     * Evaluate value by trying to match the target value to one of the literal list values.
     * @param id Identity of caller, which must be provided for backup()
     * @return EvaluationStatus
     */
    @Override
    public EvaluationStatus evaluate(int id) 
    {
        boolean match = false;
        if (!isEmpty())
        {
            for (Parameter param: literalList)
                if (param.getValue().equals(value))
                {
                    match = true;
                    break;
                }
        }
        this.id = id;
        if (isNot)
            match = !match;
        return match ? EvaluationStatus.COMPLETE : EvaluationStatus.SHORT_CIRCUIT;
    }

    /**
     * toString()
     * @see au.com.cybersearch2.taq.expression.Variable#toString()
     */
    @Override
    public String toString()
    {
        String string = super.toString();
        if (empty)
        {
            StringBuilder builder = new StringBuilder(string);
            builder.append(' ');
            if (isNot)
                builder.append('!');
            builder.append('{');
            boolean firstTime = true;
            for (Parameter param: literalList)
            {
                if (firstTime)
                    firstTime = false;
                else
                    builder.append(',');
                builder.append(param.getValue().toString());
            }
            builder.append('}');
            string = builder.toString();
        }
        return string;
    }
}
