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
package au.com.cybersearch2.taq.pattern;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.helper.Blank;
import au.com.cybersearch2.taq.language.Null;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.language.Unknown;
import au.com.cybersearch2.taq.terms.TermStore;


/**
 * Axiom
 * Factual information packaged as a structure containing a sequence of terms  
 * @author Andrew Bowley
 *
 * @since 06/10/2010
 */
public class Axiom extends TermList<Term>
{
	private static final long serialVersionUID = 2741521667825735668L;
    private static final ObjectStreamField[] serialPersistentFields =
    {
        new ObjectStreamField("name", String.class),
    };

    private String name;
	
    /**
     * Returns the name of the Structure    
     * @return String
     */
    public String getName() 
    {
        return name;
    }
    
    /**
     * Construct an empty or parameter-supplied self-mangaged Axiom. Use addTerm() to add terms. 
     * @param name Name
     * @param params Parameters
     */
    public Axiom(String name, Parameter... params)
    {
        super(new AxiomArchetype(QualifiedName.parseGlobalName(name)));
        this.name = name;
        if ((params != null)&& (params.length > 0))
        {
            if (archetype.isMutable())
            {
                setTerms(params);
                archetype.clearMutable();
            }
            for (Term term: params)
                addTerm(term);
        }
    }

	/**
	 * Construct an empty Axiom. Use addTerm() to add terms. 
	 * @param axiomArchetype Axion archetype
	 */
	public Axiom(AxiomArchetype axiomArchetype)
	{
        super(axiomArchetype);
		name = axiomArchetype.getName();
	}

	/**
	 * Construct an Axiom from a variable Object argument
	 * @param axiomArchetype Axion archetype
	 * @param data Objects to add. 
	 */
	public Axiom(AxiomArchetype axiomArchetype, Object... data)
	{
        this(axiomArchetype);
        if ((data != null)&& (data.length > 0))
        {
            List<Term> terms = new ArrayList<Term>(data.length);
            for (Object datum: data)
            {
                if (datum instanceof Term)
                    terms.add((Term) datum);
                else if (datum instanceof Integer)
                    terms.add(new Parameter(Term.ANONYMOUS, ((Integer)datum).longValue()));
                else
                    terms.add(new Parameter(Term.ANONYMOUS, datum));
            }
    		if (archetype.isMutable())
    		{
    		    setTerms(terms.toArray(new Term[terms.size()]));
                archetype.clearMutable();
   	     	}
        }
	}

	/**
	 * Construct an Axiom from a variable Term argument
	 * @param axiomArchetype Axion archetype
	 * @param terms Terms to add
	 */
	public Axiom(AxiomArchetype axiomArchetype, Term... terms)
	{
        this(axiomArchetype);
        if ((terms != null)&& (terms.length > 0))
        {
            if (archetype.isMutable())
            {
                setTerms(terms);
                archetype.clearMutable();
            }
            for (Term term: terms)
                addTerm(term);
        }
	}

	/**
	 * Construct an Axiom from a list of terms
	 * @param axiomArchetype Axion archetype
	 * @param terms List of Term objects
	 */
	public Axiom(AxiomArchetype axiomArchetype, List<Term> terms)
	{
        this(axiomArchetype);
        if ((terms != null)&& (terms.size() > 0))
        {
            if (archetype.isMutable())
            {
                setTerms(terms.toArray(new Term[terms.size()]));
                archetype.clearMutable();
            }
            for (Term term: terms)
                addTerm(term);
         }
 	}

	/**
	 * Axiom copy constructor with name change
	 * @param name Name of copied axiom
	 * @param axiom Axiom to copy
	 */
	public Axiom(String name, Axiom axiom) {
		this((AxiomArchetype)axiom.archetype);
		this.name = name;
		setTermList(axiom.termList);
	}

	public void forEach(Consumer<? super Term> action) {
        Objects.requireNonNull(action);
        for (Term term : termList) {
            action.accept(term);
        }
    }

    /**
     * Add Term
     * @param term Term object
     */
	@Override
    public void addTerm(Term term)
    {
	    if (!term.getName().isEmpty())
	    {
            int position = archetype.getIndexForName(term.getName());
            if ((position != -1) && (position != termCount))
                throw new ExpressionException(name + " term \"" + term.getName() + "\" not allowed at index = " + termCount);
	    }
        super.addTerm(term);
    }
    
    @Override
	public boolean isFact() {
    	// An axiom containing a Blank term is not a fact.
    	// A template ignores Blank terms as CursorSentinalOperand always has a Blank value.
        if (termList.isEmpty())
            return false;
        for (Term param: termList)
            if (param.isEmpty() || 
            	(param.getValueClass() == Unknown.class) || 
            	(param.getValueClass() == Null.class) || 
            	(param.getValueClass() == Blank.class))
                return false;
        return true;
	}

	private void writeObject(ObjectOutputStream oos)
            throws IOException 
    {
        oos.writeObject(archetype);
        oos.writeObject(name);
        // termList size
        oos.writeInt(termList.size());
        // terms
        for (Term term: termList)
            oos.writeObject(new TermStore(term));
    }
    
    private void readObject(ObjectInputStream ois)
            throws IOException, ClassNotFoundException  
    {
        archetype = (AxiomArchetype)ois.readObject();
        name = (String)ois.readObject();
        // termList size
        Term[] termArray = new Term[ois.readInt()];
        // terms
        for (int i = 0; i < termArray.length; i++)
        {
            TermStore termStore = (TermStore) ois.readObject();
            Parameter param = new Parameter(termStore.getName(), termStore.getValue());
            param.setId(termStore.getId());
            termArray[i] = param;
        }
        termList = new ArrayList<Term>();
        setTerms(termArray);
        archetype.clearMutable();
        for (Term term: termArray)
            addTerm(term);
    }
}
