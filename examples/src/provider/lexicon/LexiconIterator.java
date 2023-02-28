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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.language.StringTerm;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Archetype;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.AxiomArchetype;

/**
 * LexiconIterator
 * @author Andrew Bowley
 * 21 Dec 2014
 */
public class LexiconIterator implements Iterator<Axiom> 
{
    private BufferedReader reader;
    private Archetype<Axiom, Term> archetype;
    private String filter;
    private boolean hasNext;
    private String[] strings;

	public LexiconIterator(Archetype<Axiom, Term> archetype) 
	{
		this.archetype = archetype;
        archetype.clearMutable();
        URL url = LexiconIterator.class.getClassLoader().getResource("i-words.taq");
		File dictionaryFile = null;
		try {
			dictionaryFile = new File(url.toURI());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}// ResourceHelper.getResourceFile("i-words.taq");
        try 
        {
			reader = new BufferedReader(new FileReader(dictionaryFile));
		} 
        catch (FileNotFoundException e) 
        {
			e.printStackTrace();
		}
	}        
			
	public void setFilter(String filter) {
		this.filter = filter;
	}

	@Override
	public boolean hasNext() 
	{
		if (reader == null)
			return false;
		if (hasNext)
			return true;
		int bracket1 = -1;
        try 
        {
        	do
            {
                String line = reader.readLine();
                if (line == null)
                {
                	reader.close();
                	reader = null;
                	return false;
                }
                bracket1 = line.indexOf(" { ");
                if (bracket1 != -1) {
                	String terms = line.substring(3, line.length() - 2);
                    strings = terms.split(",");
                    strings[0] = strings[0].substring(1, strings[0].length() - 1);
                    if (filter != null) {
        				Matcher matcher = getMatcher(strings[0], "^" + filter + "[^ ]+");
        				if (!matcher.find()) 
        					bracket1 = -1;
                    } else
                        strings[1] = strings[1].substring(1, strings[1].length() - 1);
                }
            } while (bracket1 == -1);

		} 
        catch (IOException e) 
        {
        	reader = null;
			e.printStackTrace();
		}
        hasNext = true;
        return true;
	}

	@Override
	public Axiom next() 
	{
		if (!hasNext && !hasNext())
			throw new IllegalStateException("Lexicon Iterator expired when next() called");
		StringTerm word = new StringTerm(strings[0]);
		List<String> axiomTermNameList = archetype.getTermNameList();
		word.setName(axiomTermNameList.get(0));
		StringTerm definition = new StringTerm(strings[1]);
		definition.setName(axiomTermNameList.get(1));
		Axiom axiom = new Axiom((AxiomArchetype) archetype);
		axiom.addTerm(word);
		axiom.addTerm(definition);
		hasNext = false;
		return axiom;
	}

	protected Matcher getMatcher(String input, String scanPattern)
	{
		Pattern pattern = null;
        try
        {
            pattern = Pattern.compile(scanPattern, 0);
        }
        catch(PatternSyntaxException e)
        {
            throw new ExpressionException("Error in regular expression", e);
        }
        // Retain value on match
        return pattern.matcher(input);
	}
}
