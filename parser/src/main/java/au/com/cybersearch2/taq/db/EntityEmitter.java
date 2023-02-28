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
package au.com.cybersearch2.taq.db;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import au.com.cybersearch2.taq.axiom.AxiomReflection;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.AxiomArchetype;
import au.com.cybersearch2.taq.query.QueryExecutionException;

/**
 * EntityEmitter
 * Base class for executing database statements to insert rows into an entity table.
 * Abstract method internalEmitData() must be implemented using the chosen 
 * persistence technology to perform statements.
 * @param <E> Entity type
 * @author Andrew Bowley
 */
public abstract class EntityEmitter<E> extends StatementBase<E> implements LocaleAxiomListener {

	/** Translates Axioms to Java Beans and vice versa */
	private AxiomConverter axiomConverter;
	/** Pre-processes a data object to be emitted as an entity object */
	private DataEmitter<?,E> dataEmitter;

    /**
     * Construct an EntityCollector object for a specific entity class.
     * @param entityClass Class of entity to be collected
     */
    public EntityEmitter(Class<E> entityClass)
    {
        super(entityClass);
    }

    /**
     * Returns text to identify this emitter in messages 
     * @return description
     */
    abstract public String getDescription();
    
    /**
     * Returns object which inserts a database row mapped to an entity object
     * @return StatementRunner object
     * @throws ExecutionException
     */
    abstract public StatementRunner<E> getStatementRunner() throws ExecutionException;

	public void setDataEmitter(DataEmitter<?,E> dataEmitter) {
		this.dataEmitter = dataEmitter;
	}

	@Override
	public boolean onNextAxiom(QualifiedName qname, Axiom axiom, Locale locale) {
		if (axiomConverter == null)	{
			AxiomReflection axiomReflection = new AxiomReflection((AxiomArchetype)axiom.getArchetype());
			axiomConverter = new AxiomConverter(axiomReflection);
		}
		else if (!axiomConverter.getArchetype().getName().equals(axiom.getName()))
			throw new ExpressionException(
				String.format("Axiom named '%s' not emitted because '%s' expected", 
						      axiom.getName(), 
						      axiomConverter.getArchetype()
						          .getName()));
		try 
		{
			if (dataEmitter != null) {
				List<E> dataList = dataEmitter.emit(axiom, locale, axiomConverter);
				for (E data: dataList)
				    internalEmitData(data);
			} else {
			    E data = axiomConverter.getEntityFromAxiom(axiom, getEntityClass(), locale);
			    internalEmitData(data);
			}
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
			throw new QueryExecutionException(getDescription() + " failed", e.getCause());
		}
		return true;
	}

}
