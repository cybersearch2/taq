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
package au.com.cybersearch2.taq.compiler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.artifact.ResourceArtifact;
import au.com.cybersearch2.taq.compile.ListAssembler;
import au.com.cybersearch2.taq.compile.OperandMap;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.compile.ParserTask;
import au.com.cybersearch2.taq.db.CustomDatabaseProvider;
import au.com.cybersearch2.taq.db.CustomDatabaseProvider.ArtifactType;
import au.com.cybersearch2.taq.db.DatabaseProvider;
import au.com.cybersearch2.taq.expression.AxiomOperand;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.expression.ResourceOperand;
import au.com.cybersearch2.taq.interfaces.ListType;
import au.com.cybersearch2.taq.interfaces.LocaleAxiomListener;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.interfaces.ParserRunner;
import au.com.cybersearch2.taq.interfaces.ResourceProvider;
import au.com.cybersearch2.taq.language.ITemplate;
import au.com.cybersearch2.taq.language.InitialProperties;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.pattern.Template;

/**
 * Parser helper collects resource declaration production
 */
public class ParserResource implements ResourceArtifact {

	/** Binds resource provider to import and export connectors */
	private class BindingTask implements ParserRunner {

		@Override
		public void run(ParserAssembler parserAssembler) {
			QualifiedName qualifiedBindingName = getFirstBindingName();
			if (hasList) 
				bindResource(resourceName, qualifiedListName, parserAssembler);
			else
				parserAssembler.bindResource(resourceName, qualifiedBindingName);
			if (hasCollectorAxiom) {
				// Handle bi-directional resource
				if (hasTemplate) 
					parserAssembler.bindResource(resourceName, qualifiedTemplateName);
				else if (hasList)
					bindResource(resourceName, qualifiedListName, parserAssembler);
				QualifiedName qualifiedAxiomName = getCollectorAxiomName();
	    	    if (!QueryProgram.GLOBAL_SCOPE.equals(qualifiedAxiomName.getScope())) {
				    Scope scope = parserAssembler.getScope().getScope(qualifiedAxiomName.getScope());
				    scope.getParserAssembler().putResourceName(qualifiedAxiomName, resourceName);
	    	    }
		    }
            if (hasTemplate()) {
	    	    QualifiedName qualifiedTempateName = getQualifiedTemplateName();
	    	    if (!QueryProgram.GLOBAL_SCOPE.equals(qualifiedTempateName.getScope())) {
				    Scope scope = parserAssembler.getScope().getScope(qualifiedTempateName.getScope());
				    scope.getParserAssembler().putResourceName(qualifiedTempateName, resourceName);
	    	    }
            }
		}
		
		private QualifiedName getFirstBindingName() {
			if (hasCollectorAxiom())
				return collectorAxiomName;
			else if (hasTemplate) {
				return qualifiedTemplateName;
			} else {
				return qualifiedListName;
			}
		}

	}
	
	/** Creates artifacts from parser productions */
	private final Compiler compiler;
	/** Resource identifier persisted as qualified name in global scope. */
	private final QualifiedName qualifiedName;
	
	/** Declared name of resource. Also provider name if system name not set. */
	private String resourceName;
    /** Name allocated to resource by provider */
	private String systemName;
	/** Name of collector class, if included in declaration */
	private String collectorClass;
	/** Name of emitter class, if included in declaration */
	private String emitterClass;
	/** Flag set true if declaration includes collector axiom specification */
	private boolean hasCollectorAxiom;
	/** Flag set true if declaration includes emitter axiom specification */
	private boolean hasEmitterAxiom;
	/** Flag set true if resource consumes template solution */
	private boolean hasTemplate;
	/** Flag set true if resource consumes an axiom list */
	private boolean hasList;
	/** Resource data-source role qualified name or null if not specified */ 
	private QualifiedName collectorAxiomName;
	/** Resource emit function role qualified name or null if not specified */ 
	private QualifiedName emitterAxiomName;
	/** Resource data-consumer role qualified name or null if not specified */
	private QualifiedName qualifiedTemplateName;
	/** Resource export list qualified name or null if not specified */
	private QualifiedName qualifiedListName;
	/** Resource properties - empty if not specified */
	private Map<String, Object> properties;

	/**
	 * Construct ParserResource object
	 * @param name Resource name
	 */
	public ParserResource(Compiler compiler, String name) {
		this.compiler = compiler;
	   	qualifiedName = new QualifiedName(name);
	   	// This is name allocated to resource by provider unless system name overrides it
		resourceName = name;
	}

	public QualifiedName getCollectorAxiomName() {
		return collectorAxiomName;
	}

	public QualifiedName getEmitterAxiomName() {
		return emitterAxiomName;
	}

	public QualifiedName getQualifiedTemplateName() {
		return qualifiedTemplateName;
	}

	public QualifiedName getQualifiedListName() {
		return qualifiedListName;
	}

	public Map<String, Object> getProperties() {
		if (properties == null)
			properties = new HashMap<>();
		return properties;
	}

	public String getSystemName() {
 		return systemName != null ? systemName : resourceName;
	}
	
	public ParserRunner getParserRunner() {
		return new BindingTask();
 	}

	@Override
	public void setQualifiedListName(QualifiedName qualifiedListName) {
		this.qualifiedListName = qualifiedListName;
	}
	
	@Override
	public void setQualifiedTemplateName(QualifiedName qualifiedName) {
		qualifiedTemplateName = qualifiedName; 
	}

	@Override
	public boolean hasList() {
		return hasList;
	}

	@Override
	public void setHasList(boolean hasList) {
		this.hasList = hasList;
	}

	@Override
	public boolean hasCollectorAxiom() {
		return hasCollectorAxiom;
	}

	@Override
	public void setHasCollectorAxiom(boolean hasAxiom) {
		this.hasCollectorAxiom = hasAxiom;
	}

	@Override
	public void setCollectorAxiomName(QualifiedName qualifiedAxiomName) {
		this.collectorAxiomName = qualifiedAxiomName;
	}

	@Override
	public boolean hasEmitterAxiom() {
		return hasEmitterAxiom;
	}

	@Override
	public void setHasEmitterAxiom(boolean hasAxiom) {
		this.hasEmitterAxiom = hasAxiom;
	}

	@Override
	public void setEmitterAxiomName(QualifiedName qualifiedAxiomName) {
		this.emitterAxiomName = qualifiedAxiomName;
	}

	@Override
	public boolean hasTemplate() {
		return hasTemplate;
	}

	@Override
	public void setHasTemplate(boolean hasTemplate) {
		this.hasTemplate = hasTemplate;
	}

	@Override
	public QualifiedName getQualifiedName() {
		return qualifiedName;
	}

	@Override
	public void setProperties(InitialProperties resourceProperties) {
		this.properties = resourceProperties.getProperties();
	}

	@Override
	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}

	/**
	 * Process Resource body
	 * @param template Initialization template
	 */
	@Override
    public void resourceBody(ITemplate template) {
		compiler.getQueryProgram().addInitTemplate((Template)template);
    }

	@Override
	public void setCollectorClass(String collectorClass) {
		this.collectorClass = collectorClass;
	}

	@Override
	public void setEmitterClass(String emitterClass) {
		this.emitterClass = emitterClass;
	}
	
	/**
	 * Process ResourceDeclaration production
	 */
	@SuppressWarnings("rawtypes")
	@Override
    public ResourceOperand resourceDeclaration() {
		ParserAssembler parserAssembler = compiler.getParserAssembler();
		// If mapping of qualified role name to resource name required
		// save configuration in global scope as this may be needed in the first apss
        if (hasCollectorAxiom()) {
        	String resourceName = getQualifiedName().getName();
            QualifiedName qualifiedAxiomName = getCollectorAxiomName();
            parserAssembler.getScope().getGlobalParserAssembler().putResourceName(qualifiedAxiomName, resourceName);
        }
        if (hasTemplate()) {
    	    String resourceName = getQualifiedName().getName();
    	    QualifiedName qualifiedTempateName = getQualifiedTemplateName();
            parserAssembler.getScope().getGlobalParserAssembler().putResourceName(qualifiedTempateName, resourceName);
        }
		ResourceOperand resourceOperand = 
				compiler.getQueryProgram().createResourceOperand(this, getProperties());
		parserAssembler.getOperandMap().addOperand(resourceOperand);
		// Add declared collectors and emitters unless already configured by application
    	if (hasCollectorAxiom() || hasTemplate() || hasList()) {
    		//throw new CompilerException(String.format("Resource %s must have at least one role qualifier", getSystemName() , null));
	        ParserTask parserTask = parserAssembler.addPending(getParserRunner());
	        parserTask.setPriority(ParserTask.Priority.fix.ordinal());
	        ResourceProvider resourceProvider = resourceOperand. getProvider();
	        DatabaseProvider databaseProvider = null;
	        if (resourceProvider instanceof DatabaseProvider) 
	        	databaseProvider = (DatabaseProvider) resourceProvider;;
	        if (databaseProvider instanceof CustomDatabaseProvider) {
	        	CustomDatabaseProvider customDProvider = (CustomDatabaseProvider)resourceProvider;
	        	String axiomName = collectorAxiomName.toString();
	        	if (customDProvider.requiredArtifactTypes().contains(ArtifactType.collector) &&
	        		!databaseProvider.hasCollector(axiomName))
	        		customDProvider.addCArtifact(ArtifactType.collector, axiomName);
	        	if (customDProvider.requiredArtifactTypes().contains(ArtifactType.emitter)) {
		        	if (hasTemplate)
		        		axiomName =  qualifiedTemplateName.toString();
		        	else if (hasList)
		        		axiomName = qualifiedListName.toString();
		        	else
		        	    axiomName = emitterAxiomName.toString();
		        	if (!databaseProvider.hasCollector(axiomName))
	        		    customDProvider.addCArtifact(ArtifactType.emitter, axiomName.toString());
	        	}
	        } 
	        if ((databaseProvider != null) && (emitterClass != null)) {
	        	String axiomName;
	        	if (hasTemplate)
	        		axiomName =  qualifiedTemplateName.toString();
	        	else if (hasList)
	        		axiomName = qualifiedListName.toString();
	        	else
	        	    axiomName = emitterAxiomName.toString();
	        	if (!databaseProvider.hasEmitter(axiomName))
	        	    databaseProvider.addEmitterEntity(axiomName, emitterClass);
	        } 
	        if (((databaseProvider != null) && collectorClass != null)) {
	        	String axiomName = collectorAxiomName.toString();
	        	if (!databaseProvider.hasCollector(axiomName))
	        	    databaseProvider.addCollectorEntity(collectorAxiomName.toString(), collectorClass);
	        }
    	}
        return resourceOperand; 
   }
 
	private ResourceProvider getResourceProvider(ParserAssembler parserAssembler) {
        ResourceProvider resourceProvider = parserAssembler.getResourceProvider(resourceName);
        if (resourceProvider == null) 
            throw new ExpressionException("Axiom provider \"" + resourceName + "\" not found");
        return resourceProvider;
	}
	
	private void bindResource(String resourceName, QualifiedName bindingName, ParserAssembler parserAssembler) {
		// Check if binding to a dynamic axiom list
        String listScopeName = bindingName.getScope();
        Scope listScope = parserAssembler.getScope();
        ListAssembler listAssembler = parserAssembler.getListAssembler();
        if (!listAssembler.existsKey(ListType.axiom_dynamic, bindingName)) 
        	throw new ExpressionException(String.format("Axiom list %s not found", bindingName.toString()));
        OperandMap operandMap = parserAssembler.getOperandMap();
        if (!listScopeName.isEmpty() && !parserAssembler.getScope().getAlias().equals(listScopeName))
        {
            listScope = parserAssembler.getScope().findScope(listScopeName);
            if (listScope != null) {
                listAssembler = listScope.getParserAssembler().getListAssembler();
                operandMap = listScope.getParserAssembler().getOperandMap();
            }
        }
	    Operand operand = operandMap.getOperand(bindingName);
	    if (!(operand instanceof AxiomOperand))
        	throw new ExpressionException(String.format("Axiom list %s not available", bindingName.toString()));
        ResourceProvider resourceProvider = getResourceProvider(parserAssembler);
        parserAssembler.getScope().addQueryResultCallback(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				LocaleAxiomListener axiomListener = resourceProvider.getAxiomListener(bindingName.toString());
				if (axiomListener == null)
					throw new ExpressionException("Emitter not found: " + bindingName.toString());
		    	((AxiomOperand)operand).copyList(axiomListener);
				return null;
			}});
 	}

}
