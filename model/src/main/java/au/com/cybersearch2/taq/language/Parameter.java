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

/**
 * Parameter is a simple Term. It may be annonymous or named. It may be empty or assigned a value, which may be null.
 * Sub classes control Parameter construction and the value Java type. 
 * @author Andrew Bowley
 * @since 28/09/2010
 */
public class Parameter implements Term
{
	protected static final String INVALID_NAME_MESSAGE = "Parameter \"name\" is null";
	/** Value of Parameter is null by default */
    protected Object value;
    /** Name of parameter is BLANK by default, which means it's annonymous until a name is assigned */
	protected String name;
	/** The Parameter is empty until a value is assign. Beware - null is a VALID value */
	protected boolean empty;
	/** Identity - assigned when performing unification - see backup() for application */
	protected int id;

	/**
	 * Construct a non-empty named Parameter object
	 * @param name String
	 * @param value Value 
	 */
	public Parameter(String name, Object value)
	{
		if (name == null)
			throw new IllegalArgumentException(INVALID_NAME_MESSAGE);
		this.name = name;
		id = 0;
		setValue(value);
	}

    /**
     * Construct a non-empty named Parameter object containing an integer
     * @param name String
     * @param value Value 
     */
    public Parameter(String name, int value)
    {
        if (name == null)
            throw new IllegalArgumentException(INVALID_NAME_MESSAGE);
        this.name = name;
        id = 0;
        setValue((long)value);
    }

    /**
	 * Construct an empty named Parameter object
	 * @param name String
	 */
	public Parameter(String name)
	{
		if (name == null)
			throw new IllegalArgumentException(INVALID_NAME_MESSAGE);
		this.name = name;
		if (!empty)
		    clearValue();
	}

    /**
     * Set Parameter name. 
     * This means the Term is annoymous until a name is assigned to it.
     * @param name String
	 * @throws IllegalStateException if name has already been assigned
     */
	@Override
	public void setName(String name)
    {
		if (name == null)
			throw new IllegalArgumentException(INVALID_NAME_MESSAGE);
		if (!this.name.equals(ANONYMOUS))
			throw new IllegalStateException("Assigning name \"" + name + "\" to Term already named \"" + this.name + "\" not allowed");
		this.name = name;
    }
	
    /**
     * Returns Parameter name
     * @return String
     */
	@Override
	public String getName()
    {
    	return name;
    }

	/**
	 * Returns true if no value has been assigned to this Parameter
	 * @return boolean true if empty
	 */
	@Override
	public boolean isEmpty()
    {
    	return empty;
    }

	/**
	 * Returns Parameter value or null if not assigned
	 * @return Object
	 */
	@Override
	public Object getValue()
	{
		return value;
	}

	/**
	 * Returns id
	 * @return int
	 */
    @Override
	public int getId() 
	{
		return id;
	}

    /**
     * Set id
	 * @param id Identity of caller
     */
    public void setId(int id)
    {
        this.id = id;
    }
    
	/**
	 * Perform unification with other Term. If successful, two terms will be equivalent.
	 * Determines Term ordering and then delegates to evaluate()  
	 * @param otherTerm Term with which to unify
	 * @param id Identity of caller, which must be provided for backup()
	 * @return Identity passed in param "id" or zero if unification failed
	 */
	@Override
	public int unifyTerm(Term otherTerm, int id)
    {
		if (isEmpty() && !otherTerm.isEmpty())
		{
			return unify(otherTerm, id);
		}
		else if (!isEmpty() && otherTerm.isEmpty())
		{
			return otherTerm.unifyTerm(this, id);
		}
		return 0;
    }

	/**
	 * Returns Parameter value class or Null.class if value is null
	 * @return Class object
	 */
	@Override
    public Class<?> getValueClass()
    {
	    return value.getClass();
    }

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		if (!name.isEmpty())
			return name + "=" + (empty ? "<empty>" : value.toString());
		return  value.toString();
	}

	/**
	 * Equals matches on value. If either or both Parameters have null value, equals() returns false
	 * @see java.lang.Object#equals(Object obj)
	 */
	@Override
    public boolean equals(Object obj) 
	{
		if (obj == null)
			return false;
		if (obj instanceof Parameter)
		{
			Parameter other =  (Parameter)obj;
			if (other.value == null)
				return value == null;
			return (value != null) && this.value.equals(other.value) && (this.name.equals(other.name));
		}
		return false;
	}

	/**
	 * Delegate to perform actual unification with other Term. If successful, two terms will be equivalent. 
	 * @param otherTerm Term with which to unify
	 * @param id Identity of caller, which must be provided for backup()
	 * @return Identity passed in param "id" or zero if unification failed
	 */
	public int unify(Term otherTerm, int id)
	{
		this.id = id;
		setValue(otherTerm.getValue());
		return this.id;
    }

	/**
	 * Set Parameter value
	 * @param value Value
	 */
	@Override
	public void setValue(Object value)
	{
		if (value == null)
			this.value = new Null();
		else
		{
			this.value = value;
			this.empty = false;
		}
	}

	/**
	 * Set value to null, mark Parameter as empty and set id to 0
	 */
	public void clearValue()
	{
		empty = true;
		// Set value to avoid NPE on accidental access despite empty flag being set true
		value = new Null();
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() 
	{
		return empty ? super.hashCode() : value.hashCode();
	}

}
