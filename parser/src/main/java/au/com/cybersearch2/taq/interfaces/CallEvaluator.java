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
package au.com.cybersearch2.taq.interfaces;

import java.util.List;

import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.provider.FunctionException;

/**
 * Performs evaluation for a function, query or select call
 *
 */
public interface CallEvaluator {

	static final String RETURN_TYPE_UNDUPPORTED = "Function '%s' does not support return type %s";

    /**
     * Returns name of function. Must be unique in context.
     * @return String
     */
    String getName();

	/**
     * Perform function 
     * @param argumentList List of terms
     * @return Flag set true if evaluation successful
     */
    boolean evaluate(List<Term> argumentList);

    /**
     * Returns evaluated value
     * @return Object
     */
    Object getValue();
    
    /**
     * Backup to initial state if given id matches modification or given id = 0. 
     * @param id Identity of caller. 
     */
	void backup(int id);

    /**
    * Set declared return type. Override to enforce type correctness. 
    * @param returnType
    * @return flag set true if return type is supported
    */
    default boolean setReturnType(OperandType returnType) {
	   	return false;
    }

    /**
    * Set declared list return type. Override to enforce type correctness. 
    * @param listType
    * @return flag set true if return type is supported
    */
    default boolean setListReturnType(OperandType listType) {
	   	return false;
    }

	/**
     * Apply declared list return type 
     * @param listType List type
     * @throws FunctionException if type not supported
     */
    default boolean applyReturnType(OperandType returnType) throws FunctionException {
    	if (!setReturnType(returnType))
    		throw new FunctionException(String.format(RETURN_TYPE_UNDUPPORTED, getName(), returnType.name().toLowerCase()));
    	return true;
    }
    
	/**
     * Apply declared list return type 
     * @param listType List type
     * @throws FunctionException if type not supported
     */
    default boolean applyListReturnType(OperandType listType) throws FunctionException {
    	if (!setListReturnType(listType))
    		throw new FunctionException(String.format(RETURN_TYPE_UNDUPPORTED, getName(), listType.name().toLowerCase()));
    	return true;
    }

    /**
     * Override to provide real execution context, if available
     * @return ExecutionContext object 
     */
    default ExecutionContext getExecutionContext() {
    	return new ExecutionContext();
    }
}
