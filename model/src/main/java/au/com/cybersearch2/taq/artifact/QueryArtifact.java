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
package au.com.cybersearch2.taq.artifact;

import java.util.List;

import au.com.cybersearch2.taq.language.IOperand;
import au.com.cybersearch2.taq.language.KeyName;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.QuerySpec;
import au.com.cybersearch2.taq.language.TaqLiteral;
import au.com.cybersearch2.taq.language.Term;

/**
 * A query is a statement which launches an TAQ program
 */
public interface QueryArtifact {

	/**
     * Process a QueryChain production
     * @param querySpec Query specification
     * @param literal Solution type or null if no solution provided
     */
    void queryChain(QuerySpec querySpec, TaqLiteral literal);
 
	/**
	 * Returns a query specification
	 * @param name Query name
	 * @param isHeadQuery Flag set true if head query specification
	 * @return QuerySpec object
	 */
    QuerySpec createQuerySpec(String name, boolean isHeadQuery);

    /**
     * Process a KeyName production
     * @param querySpec Query specification
     * @param name1 Name first part
     * @param name2 Name second part
     * @return KeyName object
     */
    KeyName keyName(QuerySpec querySpec, QualifiedName name1, QualifiedName name2);

    /**
     * Process a QueryDelaration production
     * @param querySpec Query specification
     * @param firstKeyname First keyname in query
     * @param keynameCount Number of keynames in query
     * @param termList Additional parameters
     * @return QuerySpec object
     */
    QuerySpec queryDeclaration(QuerySpec querySpec, KeyName firstKeyname, int keynameCount, List<Term> termList);

	QualifiedName wrapList(QualifiedName name1, IOperand listOperand);
 
}
