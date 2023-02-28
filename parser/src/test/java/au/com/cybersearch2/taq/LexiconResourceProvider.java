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
package au.com.cybersearch2.taq;

import java.util.Iterator;
import java.util.Locale;

import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.interfaces.ResourceProvider;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.AxiomArchetype;

/**
 * LexiconResourceProvider
 * @author Andrew Bowley
 * 17 Mar 2015
 */
public class LexiconResourceProvider implements ResourceProvider 
{

	@Override
	public String getName() 
	{
		return "lexicon";
	}

	@Override
	public void open() 
	{
	}

	@Override
	public Iterator<Axiom> iterator(AxiomArchetype archetype) 
	{
		return new LexiconIterator(archetype);
	}

	@Override
	public boolean isEmpty() 
	{
		return false;
	}

	@Override
	public LocaleAxiomListener getAxiomListener(String name) 
	{   // Listener writes to console
		return new LocaleAxiomListener()
		{
			@Override
			public boolean onNextAxiom(QualifiedName qname, Axiom axiom, Locale locale) 
			{
			    System.out.println(axiom.toString());
				return true;
			}
		};
	}

    @Override
    public void close()
    {
    }
}
