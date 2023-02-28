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

import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.pattern.Template;
import au.com.cybersearch2.taq.query.Solution;

/**
 * FindSolution
 * @author Andrew Bowley
 * 12 Jan 2015
 */
public interface SolutionFinder 
{
	/**
	 * Find a solution for specified template
	 * @param solution Resolution of current query managed by LogicQueryExecuter up to this point  
	 * @param template Template used on each iteration
	 * @param context Execution context
	 * @return Flag to indicate if another solution may be available
	 */
	boolean iterate(Solution solution, Template template, ExecutionContext context);

	/**
	 * Set axiom listener to receive each solution as it is produced
	 * @param axiomListener The axiom listener object
	 */
	void setAxiomListener(LocaleAxiomListener axiomListener);
}
