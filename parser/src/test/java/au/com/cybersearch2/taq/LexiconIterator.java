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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import au.com.cybersearch2.taq.language.StringTerm;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Archetype;
import au.com.cybersearch2.taq.pattern.Axiom;

/**
 * LexiconIterator
 * @author Andrew Bowley
 * 21 Dec 2014
 */
public class LexiconIterator implements Iterator<Axiom> 
{
    protected BufferedReader reader;
	protected Archetype<Axiom, Term> archetype;
    String[] strings;

	public LexiconIterator(Archetype<Axiom, Term> archetype) 
	{
		this.archetype = archetype;
		File dictionaryFile = ResourceHelper.getTestResourceFile("definitions.txt");
		
        try 
        {
			reader = new BufferedReader(new FileReader(dictionaryFile));
		} 
        catch (FileNotFoundException e) 
        {
			e.printStackTrace();
		}
	}        
			
	@Override
	public boolean hasNext() 
	{
		if (reader == null)
			return false;
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
                strings = line.split("-");
            } while (strings.length < 2);

		} 
        catch (IOException e) 
        {
        	reader = null;
			e.printStackTrace();
		}
        return true;
	}

	@Override
	public Axiom next() 
	{
		StringTerm word = new StringTerm(strings[0].trim());
		List<String> axiomTermNameList = archetype.getTermNameList();
		word.setName(axiomTermNameList.get(0));
		StringTerm definition = new StringTerm(strings[1].trim());
		definition.setName(axiomTermNameList.get(1));
		List<Term> terms = new ArrayList<Term>(2);
		terms.add(word);
		terms.add(definition);
		Axiom axiom = archetype.itemInstance(terms);
		return axiom;
	}
}
