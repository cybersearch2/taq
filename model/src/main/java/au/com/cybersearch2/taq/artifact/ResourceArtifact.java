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
package au.com.cybersearch2.taq.artifact;

import au.com.cybersearch2.taq.language.IOperand;
import au.com.cybersearch2.taq.language.ITemplate;
import au.com.cybersearch2.taq.language.InitialProperties;
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * A resource is a data source and/or data consumer
 * ResourceArtifact
 */
public interface ResourceArtifact {

	/** 
	 * Returns resource identifier 
	 * @return QualifiedName object
	 */
	QualifiedName getQualifiedName();
	
	/**
	 * Returns flag set true if resource has a data source
	 * @return boolean
	 */
	boolean hasCollectorAxiom();
	
	/**
	 * Returns flag set true if resource data consumer is specified as a template
	 * @return boolean
	 */
	boolean hasTemplate();
	
	/**
	 * Returns flag set true if resource data consumer is specified as a list
	 * @return boolean
	 */
	boolean hasList();
	
	/**
	 * Set resource has a data source flag
	 * @param hasAxiom Value
	 */
	void setHasCollectorAxiom(boolean hasAxiom);
	
	/**
	 * Set resource data consumer is specified as a list flag
	 * @param hasTemplate Value
	 */
	void setHasTemplate(boolean hasTemplate);
	
	/**
	 * Set resource data consumer is specified as a template flag
	 * @param hasList Value
	 */
	void setHasList(boolean hasList);
	
	/**
	 * Set name allocated to resource by provider
	 * @param resourceName Name
	 */
	void setSystemName(String resourceName);
	
	/**
	 * Set resource data-source role qualified name or null if not specified
	 * @param qualifiedAxiomName Qualified name
	 */
	void setCollectorAxiomName(QualifiedName qualifiedAxiomName);
	
	/**
	 * Set resource data-consumer role or null if not specified
	 * @param qualifiedName Qualified name
	 */
	void setQualifiedTemplateName(QualifiedName qualifiedName);
	
	/**
	 * Set resource export list qualified name or null if not specified
	 * @param qualifiedListName Qualified name
	 */
	void setQualifiedListName(QualifiedName qualifiedListName);
	
	/**
	 * Set properties
	 * @param properties Initial properties
	 */
	void setProperties(InitialProperties properties);

	/**
	 * Process ResourceDeclaration production
	 */
    IOperand resourceDeclaration();
 
	/**
	 * Process Resource body
	 * @param template Initialization template
	 */
    void resourceBody(ITemplate template);

	void setCollectorClass(String collectorClass);

	void setEmitterClass(String emitterClass);

	boolean hasEmitterAxiom();

	void setHasEmitterAxiom(boolean hasAxiom);

	void setEmitterAxiomName(QualifiedName qualifiedAxiomName);
}
