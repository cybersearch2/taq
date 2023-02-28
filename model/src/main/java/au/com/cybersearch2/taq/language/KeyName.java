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
package au.com.cybersearch2.taq.language;

/**
 * KeyName
 * Axiom key and template name to pair for query
 * @author Andrew Bowley
 * 29 Dec 2014
 */
public class KeyName 
{
    public static QualifiedName EMPTY_QNAME;
    
	/** Axiom key - actually name of axiom */
	protected QualifiedName axiomKey;
	/** Template name */
	protected QualifiedName templateName;

	static
	{
	    EMPTY_QNAME = new QualifiedName("");
	}
	
	/**
	 * Construct KeyName object
	 * @param axiomKey Axiom key
	 * @param templateName Template name
	 */
	public KeyName(String axiomKey, String templateName) 
	{
		this.axiomKey = QualifiedName.parseName(axiomKey);
		this.templateName = QualifiedName.parseTemplateName(templateName);
	}

   /**
     * Construct KeyName object with empty axiom key
     * @param templateName Qualified name of template
     */
    public KeyName(QualifiedName templateName) 
    {
        this.axiomKey = EMPTY_QNAME;
        this.templateName = templateName;
    }

    /**
     * Construct KeyName object
     * @param axiomKey Axiom key qualified name
     * @param templateName Template qualified name
     */
    public KeyName(QualifiedName axiomKey, QualifiedName templateName) 
    {
        this.axiomKey = axiomKey;
        this.templateName = templateName;
    }

	/**
	 * Returns axiom key
	 * @return String
	 */
	public QualifiedName getAxiomKey() 
	{
		return axiomKey;
	}

	/**
	 * Returns template name
	 * @return String
	 */
	public QualifiedName getTemplateName() 
	{
		return templateName;
	}

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        if (axiomKey.equals(EMPTY_QNAME))
            return templateName.toString();
        StringBuilder builder = new StringBuilder(axiomKey.toString());
        builder.append(':').append(templateName.toString());
        return builder.toString();
    }

}
