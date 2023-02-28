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
package au.com.cybersearch2.taq.terms;

import java.math.BigDecimal;

import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.language.Literal;
import au.com.cybersearch2.taq.language.LiteralType;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.language.Unknown;

/**
 * TermMetaData
 * Term attributes
 * @author Andrew Bowley
 * 3May,2017
 */
public class TermMetaData implements Comparable<TermMetaData>
{
    /** Literal type - classifies term according to type */
    protected LiteralType literalType;
    /** Flag set true is name is empty */
    protected boolean isAnonymous;
    /** Term name */
    protected String name;
    /** Position of term in metadata list */
    protected int index;

    /**
     * Construct TermMetaData object
     * @param literalType Term type
     * @param name Term name
     * @param index Position in metadata list
     */
    public TermMetaData(LiteralType literalType, String name, int index)
    {
        this.literalType = literalType;
        this.name = name;
        this.isAnonymous = name.isEmpty();
        this.index = index;
    }

    public TermMetaData(TermMetaData termMeta) {
		this(termMeta.literalType, termMeta.name, termMeta.index);
	}


    /**
     * Construct TermMetaData object for given term
     * @param term The term
     */
    public TermMetaData(Term term)
    {
        this(term, -1);
    }

    /**
     * Construct TermMetaData object for given term and list position
     * @param term The term
     * @param index The position
     */
    public TermMetaData(Term term, int index)
    {
    	if (term == null)
    	    throw new ExpressionException("Term parameter is null");
        if (term instanceof Literal)
            // Literal terms have intrinsic type
            literalType = ((Literal)term).getLiteralType();
        else if (term instanceof Operand)
        {   // Operands have operand type which is mapped to literal type.
            // Unknown type is specific to term populated with same-named type,
            // otherwise, generic "object" type is assigned
        	literalType = getLiteralType((Operand)term);
        }
        else if (!term.isEmpty())
        {   // If not operand, then type is inferred from content
            if (term.getValueClass() == Long.class)
                literalType = LiteralType.integer;
            else if (term.getValueClass() == Boolean.class)
                literalType = LiteralType.taq_boolean;
            else if (term.getValueClass() == Double.class)
                literalType = LiteralType.taq_double;
            else if (term.getValueClass() == String.class)
                literalType = LiteralType.string;
            else if (term.getValueClass() == BigDecimal.class)
                literalType = LiteralType.decimal;
            else if (ItemList.class.isAssignableFrom(term.getValueClass()))
                literalType = LiteralType.list;
            else if (term.getValueClass() == Unknown.class)
                literalType = LiteralType.unknown;
            else
                literalType = LiteralType.unspecified;
        }
        else // An empty term has unspecified type, which can be updated
            literalType = LiteralType.unspecified;
        this.name = term.getName();
        this.isAnonymous = name.isEmpty();
        this.index = index;
    }

	/**
     * compareTo
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(TermMetaData other)
    {
        if (literalType != other.literalType)
        {   // Ignore type if at least one item has type unspecified 
            if (!((literalType == LiteralType.unspecified) || (other.literalType == LiteralType.unspecified)))
                return literalType.ordinal() - other.literalType.ordinal();
        }
        if (isAnonymous)
            return other.isAnonymous ? index - other.index : 1; 
        return name.compareTo(other.name);
     }

    /**
     * @param index the index to set
     */
    public void setIndex(int index)
    {
        this.index = index;
    }

    /**
     * @return the index
     */
    public int getIndex()
    {
        return index;
    }

    /**
     * Set literal type
     * @param literalType Literal type
     */
    public void setLiteralType(LiteralType literalType)
    {
        this.literalType = literalType;
    }
    
    /**
     * @return the literalType
     */
    public LiteralType getLiteralType()
    {
        return literalType;
    }

    /**
     * @return the isAnonymous
     */
    public boolean isAnonymous()
    {
        return isAnonymous;
    }

    /**
     * Set name if currently anonymous
     * @param name The name
     * @return flag set true if name changed
     */
    public boolean setName(String name)
    {
        if (isAnonymous)
        {
            this.name = name;
            isAnonymous = false;
            return true;
        }
        return false;
    }
    
    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        if (isAnonymous)
            return literalType.toString() + " " + index;
        return name;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return literalType.hashCode() ^ name.hashCode() ^ index;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if ((obj == null) || !(obj instanceof TermMetaData))
            return false;
        TermMetaData other = (TermMetaData)obj; 
        return compareTo(other) == 0;//(literalType == other.literalType) && name.equals(other.name) && (index == other.index);
    }

    public static LiteralType getLiteralType(Operand term) {   
    	// Operands have operand type which is mapped to literal type.
        // Unknown type is specific to term populated with same-named type,
        // otherwise, generic "object" type is assigned
    	LiteralType literalType = LiteralType.unspecified;
    	Operator operator = ((Operand)term).getOperator();
    	if (operator != null) {
            OperandType operandType = operator.getTrait().getOperandType();
            switch(operandType)
            {
            case INTEGER: literalType = LiteralType.integer; break;
            case BOOLEAN: literalType = LiteralType.taq_boolean; break;
            case DOUBLE: literalType = LiteralType.taq_double; break;
            case STRING: literalType = LiteralType.string; break;
            case CURRENCY:
            case DECIMAL: literalType = LiteralType.decimal; break;
            case TERM_LIST: literalType = LiteralType.list; break;
            case UNKNOWN:
                if (!term.isEmpty() && (term.getValueClass() == Unknown.class))
                    literalType = LiteralType.unknown; 
                else
                    literalType = LiteralType.object;
                break;
            default:
            }
    	}
     	return literalType;
    }

}
