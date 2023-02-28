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
package select;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.compile.AxiomAssembler;
import au.com.cybersearch2.taq.compile.ListAssembler;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.AxiomArchetype;

/**
Demonstrates the bank account queries are unaffected with the  addition
of an invalid account.
*/
public class DefaultAccount extends BankAccounts {

	static String QUERY_LIST[] = { "account", "bank_details" };
	
	public DefaultAccount(String query) {
		super(query);
	}
	
	@Override
    protected QueryProgram getQueryProgram() {
		QueryProgram queryProgram = super.getQueryProgram();
		Scope scope = queryProgram.getScope(QueryProgram.GLOBAL_SCOPE);
		ListAssembler listAssembler = scope.getParserAssembler().getListAssembler();
		AxiomAssembler axiomAssembler = scope.getParserAssembler().getAxiomAssembler();
		QualifiedName qualifiedAxiomName = new QualifiedName("prefix_account");
       	List<Axiom> axiomList = listAssembler.getAxiomItems(qualifiedAxiomName);
        AxiomArchetype axiomArchetype = axiomAssembler.getAxiomArchetype(qualifiedAxiomName);
        List<Term> terms = new ArrayList<>();
        terms.add(new Parameter("prefix", "999999"));
        terms.add(new Parameter("account", 9L));
        Axiom bogusAccount = axiomArchetype.itemInstance(terms);
        axiomList.add(bogusAccount);	
		return queryProgram;
	}
	
    public static void main(String[] args)
    {
    	for (String query: QUERY_LIST) {
    		System.out.println(String.format("\nQuery \"%s\"\n", query));
	        try 
	        {
	        	DefaultAccount bankAccounts = new DefaultAccount(query);
	            Iterator<Axiom> iterator = bankAccounts.checkChoices(query);
	            while (iterator.hasNext())
	                System.out.println(iterator.next().toString());
	        } 
			catch (Throwable e) 
			{
				e.printStackTrace();
				System.exit(1);
			}
    	}
        System.exit(0);
    }
}
