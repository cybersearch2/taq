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
package au.com.cybersearch2.taq.query;

/**
 * QueryExecutionException
 * @author Andrew Bowley
 * 30 Dec 2014
 */
public class QueryExecutionException extends RuntimeException 
{

	private static final long serialVersionUID = 1L;

	/**
	 * Construct QueryExecutionException object
	 * @param message Message
	 */
	public QueryExecutionException(String message) 
	{
		super(message);

	}

	/**
	 * Construct QueryExecutionException object
	 * @param message Message
	 * @param cause Throwable object
	 */
	public QueryExecutionException(String message, Throwable cause) 
	{
		super(message, cause);

	}

}
