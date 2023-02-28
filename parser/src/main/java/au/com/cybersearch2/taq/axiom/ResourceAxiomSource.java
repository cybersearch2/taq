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
package au.com.cybersearch2.taq.axiom;

import java.util.Iterator;

import au.com.cybersearch2.taq.debug.ExecutionContext;
import au.com.cybersearch2.taq.interfaces.AxiomSource;
import au.com.cybersearch2.taq.interfaces.ResourceProvider;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Archetype;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.AxiomArchetype;

/**
 * ResourceAxiomSource
 * @author Andrew Bowley
 * 6Sep.,2017
 */
public class ResourceAxiomSource implements AxiomSource
{
    protected AxiomArchetype archetype;
    protected ResourceProvider resourceProvider;
    
    /**
     * Construct ResourceAxiomSource object
     * @param resourceProvider Resource provider
     * @param archetype Axiom archetype
     */
    public ResourceAxiomSource(ResourceProvider resourceProvider, AxiomArchetype archetype)
    {
        this.resourceProvider = resourceProvider;
        this.archetype = archetype;
    }

    @Override
    public Iterator<Axiom> iterator(ExecutionContext context)
    {
        return resourceProvider.iterator(archetype);
    }

    @Override
    public Archetype<Axiom, Term> getArchetype()
    {
        return archetype;
    }
}
