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
package au.com.cybersearch2.taq.list;

import java.util.Iterator;
import java.util.List;

import au.com.cybersearch2.taq.engine.SourceItem;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.AxiomArchetype;
import au.com.cybersearch2.taq.provider.ResourceMonitor;
import au.com.cybersearch2.taq.provider.ResourceMonitor.EventHandler;

/**
 * ArrayList which is loaded with the resource content if non-iterative operation requested
 */
public class ResourceList extends AxiomList {

	private static final String READ_ONLY = "Resource %s is read-only";
	
	/** Provides external systems used to input and output data */
	private final ResourceMonitor provider;
	/** Axiom factory which type checks data passed to construct axioms */
	private final AxiomArchetype archetype;
	/** Resource provider data-source role list */
	private final List<Axiom> axiomList;

	/**
	 * Construct ResourceList object
	 * @param provider Provides external systems used to input and output data
	 * @param archetype Axiom factory which type checks data passed to construct axioms
	 * @param axiomList Resource provider data-source role list
	 */
	public ResourceList(ResourceMonitor provider, AxiomArchetype archetype, List<Axiom> axiomList) {
		super(archetype.getQualifiedName(), axiomList, archetype.getName());
		this.provider = provider;
		this.archetype = archetype;
		this.axiomList = axiomList;
	}

	public void addHandler(EventHandler handler) {
		provider.addHandler(handler);
	}
	
	@Override
	public Iterator<Axiom> iterator() {
		Iterator<Axiom> iterator = provider.iterator(archetype);
		return iterator;
	}

	@Override
	public void setSourceItem(SourceItem sourceItem) {
	}

	@Override
	public int getLength() {
		return getAxiomList().size();
	}

	@Override
	public String getName() {
		return archetype.getName();
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public QualifiedName getQualifiedName() {

		return archetype.getQualifiedName();
	}

	@Override
	public boolean isEmpty() {

		return provider.isEmpty();
	}

	@Override
	public void assignItem(ListIndex listIndex, Axiom value) {
		throw new ExpressionException(String.format(READ_ONLY, provider.getName()));
	}

	@Override
	public Axiom getItem(ListIndex listIndex) {
		Axiom item = getAxiomList().get(listIndex.getIndex());
		if (item == null)
			throw new ExpressionException(getName() + " item " + listIndex.getIndex() + " not found");
		return item;
	}

	@Override
	public OperandType getOperandType() {

		return OperandType.AXIOM;
	}

	@Override
	public boolean hasItem(ListIndex listIndex) {
		return (getAxiomList().size() > listIndex.getIndex()) && (listIndex.getIndex() >= 0);
	}

	@Override
	public Iterable<Axiom> getIterable() {

		return new Iterable<Axiom>() {

			@Override
			public Iterator<Axiom> iterator() {
				return ResourceList.this.iterator();
			}};
	}

	@Override
	public void clear() {
		axiomList.clear();
	}

	private List<Axiom> getAxiomList() {
		if (axiomList.isEmpty() && !provider.isEmpty()) {
			Iterator<Axiom> iterator = provider.iterator(archetype);
			iterator.forEachRemaining(axiom -> axiomList.add(axiom));
		}
		return axiomList;
	}
}
