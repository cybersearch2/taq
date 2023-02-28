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
 * GenericParameter
 * Parameter with value of generic type. 
 * @author Andrew Bowley
 * 16 Nov 2014
 */
public  class GenericParameter<T> extends Parameter  
{

	/**
	 * Construct a non-empty named GenericParameter object
	 * @param name String
	 * @param value Object of generic type T
	 */
	public GenericParameter(String name, T value) 
	{
		super(name, value);

	}

	/**
	 * Construct an empty named Parameter object
	 * @param name String
	 */
	protected GenericParameter(String name) 
	{
		super(name);
	}

	/**
	 * Set value type safe
	 * @param value Object of generic type T
	 */
	public void setTypeValue(T value)
	{
		if (value == null)
			this.value = new Null();
		else
			this.value = value;
	    this.empty = false;
	}

	/**
	 * Returns value
	 * @return Object of generic type T
	 */
	@SuppressWarnings("unchecked")
    @Override
    public T getValue()
    {
	    return (T) value;
    }

}
