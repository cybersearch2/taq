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

import au.com.cybersearch2.taq.QueryParams;

/**
 * TestChainQueryExecuter
 * @author Andrew Bowley
 * 13Jan.,2017
 */
public class TestChainQueryExecuter extends ChainQueryExecuter
{

    public TestChainQueryExecuter(QueryParams queryParams)
    {
        super(queryParams);
    }

    /**
     * Set initial solution
     * @param solution Solution object - usually empty but can contain initial axioms
     */
    public void setSolution(Solution solution)
    {
        super.setSolution(solution);
    }

    /**
     * Returns solution
     * @return Collection of axioms referenced by name. The axioms reference the templates supplied to the query.
     */
    public Solution getSolution() 
    {
        return super.getSolution();
    }
}
