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
package au.com.cybersearch2.taq.language;

import java.math.BigDecimal;

/**
 * BigDecimalTerm
 * @author Andrew Bowley
 * 19 Jan 2015
 */
public class BigDecimalTerm extends GenericParameter<BigDecimal> implements Literal
{
    /**
     * Construct a BigDecimalTerm object
     * @param value String representation of {@code BigDecimal}
     */
	public BigDecimalTerm(String value) 
	{
		super(Term.ANONYMOUS, new BigDecimal(value));
	}

    @Override
    public LiteralType getLiteralType()
    {
        return LiteralType.decimal;
    }
}
