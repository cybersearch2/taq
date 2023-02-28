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
package lexicon;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.AxiomArchetype;
import au.com.cybersearch2.taq.provider.ResourceMonitor;

/**
 * LexiconResourceProvider
 * @author Andrew Bowley
 * 17 Mar 2015
 */
public class LexiconResourceProvider extends ResourceMonitor 
{
	private static final class LexiconAxiomListener implements LocaleAxiomListener {

		@Override
		public boolean onNextAxiom(QualifiedName qname, Axiom axiom, Locale locale) 
		{
		    System.out.println(axiom.toString());
			return true;
		}
	}

	public static final String LEXICON = "lexicon";
	public static final String FILTER = "filter";
	
    private String filter;
    private LexiconAxiomListener lexiconAxiomListener;
 
    public LexiconResourceProvider() {
    	super();
    	lexiconAxiomListener = new LexiconAxiomListener();
    }
    
	@Override
	public String getName() 
	{
		return LEXICON;
	}

    @Override
	public void open() 
	{
    	Map<String, Object> properties = getConnectionProperties();
    	if (properties.containsKey(FILTER))
    		setProperty(FILTER, properties.get(FILTER));
        onOpen();
	}

	@Override
	public boolean setProperty(String key, Object value) {
		if (FILTER.equals(key)) {
	    	if (value != null) {
	    		filter = value.toString();
		    	return true;
	    	}
		}
		return false;
	}

	@Override
	public Iterator<Axiom> iterator(AxiomArchetype archetype) 
	{
		LexiconIterator iterator = new LexiconIterator(archetype);
		if (filter != null)
			iterator.setFilter(filter);
		return iterator;
	}

	@Override
	public boolean isEmpty() 
	{
		return false;
	}

	@Override
	public LocaleAxiomListener getAxiomListener(String name) 
	{   // Listener writes to console
		if (isListenerChainEmpty())
			return lexiconAxiomListener;
		else
			return super.listenerChainInstance();
	}

	@Override
	public boolean chainAxiomListener(LocaleAxiomListener axiomListener) {
		if (isListenerChainEmpty())
		    super.chainAxiomListener(lexiconAxiomListener);
		return super.chainAxiomListener(axiomListener);
   }
}
