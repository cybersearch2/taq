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

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * QualifiedName
 * Three-part name consisting of scope, template and name.
 * @see NameParser
 * @author Andrew Bowley
 * 22 Aug 2015
 */
public class QualifiedName implements Comparable<QualifiedName>, Serializable
{
    /** scope literal */
    //static final public String SCOPE = "scope";
	/** The global scope is accessible from all scopes */
    public static final String GLOBAL_SCOPE = "global";

    private static final long serialVersionUID = 3872140142266578675L;
    
    public static String EMPTY;
    public static QualifiedName ANONYMOUS;
    public static QualifiedName SCOPE;
    
    static 
    {
        EMPTY = "";
        ANONYMOUS = new QualifiedName(EMPTY, EMPTY, Term.ANONYMOUS);
        SCOPE = new QualifiedName(EMPTY, EMPTY, "scope");
    }
    
    protected int name;
    protected int scope;
    protected int template;
    protected String[] parts;
    transient protected String source;
    transient protected AtomicInteger referenceCount;

    /**
     * Construct name-only QualifiedName in global namespace
     * @param name Name
     */
    public QualifiedName(String name)
    {
        this(EMPTY, EMPTY, name);
    }

    /**
     * Construct QualifiedName object from name using context name to provide defaults for empty parts 
     * @param name Formatted qualified name
     * @param contextName Context qualified name
     */
    public QualifiedName(String name, QualifiedName contextName)
    {
        NameParser nameParser = new NameParser(name);
        String scope = nameParser.getScope();
        if (scope.isEmpty() && !contextName.getScope().isEmpty())
            scope = contextName.getScope();
        String template = nameParser.getTemplate(); 
        if (template.isEmpty() && !contextName.getTemplate().isEmpty())
            template = contextName.getTemplate();
        setParts(scope, template, nameParser.getName());
        // Preserve source name for post construction analysis
        source = nameParser.toString();
    }

    /**
     * Construct QualifiedName object from namee and scope parts
     * @param scope Scope
     * @param name Name
     */
    public QualifiedName(String scope, String name)
    {
        this(scope, EMPTY, name);
    }

    /**
     * Construct QualifiedName object from separate components
     * @param scope Scope
     * @param template Template
     * @param name Name
     */
    public QualifiedName(String scope, String template, String name)
    {
        setParts(scope, template, name);
        source = toString();
    }
    
    /**
     * Construct QualifiedName object copy
     * @param qname Qualified name to copy
     */
    public QualifiedName(QualifiedName qname)
    {
        if (qname.parts ==null)
        {
            this.template = -1;
            this.scope = -1;
            this.name = -1;
            return;
        }
        parts = new String[qname.parts.length];
        System.arraycopy(qname.parts, 0, parts, 0, qname.parts.length);
        this.template = qname.template;
        this.scope = qname.scope;
        this.name = qname.name;
    }
    
    /**
     * Returns scope
     * @return String
     */
    public String getScope()
    {
        return scope == -1 ? EMPTY : parts[scope];
    }

    /**
     * Returns scope.template combination
     * @return String
     */
    public String getTemplateScope()
    {
        String part1 = scope == -1 ? GLOBAL_SCOPE : parts[scope];
        String part2 = template == -1 ? EMPTY : parts[template];
        return part2.isEmpty() ? part1 : part1 + "@" + part2;
    }

    /**
     * Returns flag set true if this name is in a template scope
     * @return
     */
    public boolean isTemplateScope() {
    	return getScope().contains("@");
    }
    
    /**
     * Set scope part to specied name. Create space slot first if none available.
     * @param scopeName Scope name
     */
    public void setScope(String scopeName)
    {
        if (scope == -1)
        {
            String[] newParts = new String[parts.length + 1];
            System.arraycopy(parts, 0, newParts, 1, parts.length);
            parts = newParts;
            scope = 0;
            if (template != -1)
                template += 1;
            if (name != -1)
                name += 1;
        }
        parts[scope] = scopeName;
    }
    
    /**
     * Returns template
     * @return String
     */
    public String getTemplate()
    {
        return template == -1 ? EMPTY : parts[template];
    }

    /**
     * Returns name
     * @return String
     */
    public String getName()
    {
        return name == -1 ? EMPTY : parts[name];
    }

    /**
     * Returns flag set true if this name is global
     * @return boolean
     */
    public boolean isGlobalName()
    {
    	if ((scope != -1) && !getScope().isEmpty())
    		return false;
        return template == -1;
    }

    /**
     * Convert this scope name to template name.
     * @return this QualifiedName
     */
    public QualifiedName toTemplateName()
    {
        if (template != -1)
            scope = template;
        if (name != -1)
        {
            template = name;
            name = -1;
        }
        return this;
    }

    /**
     * Convert this template name to scope name.
     * @return this QualifiedName
     */
    public QualifiedName toScopeName()
    {
        if (template != -1)
        {
            if (scope != -1)
                parts[template] = EMPTY;
            scope = template;
            template = -1;
        }
        return this;
    }
    
    /**
     * Convert this name to context name in which template is empty and scope is set to "scope" 
     */
    public void toContextName()
    {
        switch (parts.length)
        {
        case 3:
            parts[0] = "scope";
            parts[1] = EMPTY;
            template = -1;
            break;
        case 2:
            parts[0] = "scope";
            scope = 0;
            template = -1;
            break;
        case 1:
        {   // Create slot for scope
            name = 1;
            String name = parts[0];
            parts = new String[2];
            parts[1] = name;
            parts[0] = "scope";
            scope = 0;
            break;
        }
        default:
        }
    }
    
    /** 
     * Clear template component so qualified name is changed to scope namespace
     */
    public void clearTemplate()
    {
        if (template != -1)
            parts[template] = EMPTY;
        template = -1;
    }
    
    /** 
     * Clear scope component so qualified name is changed to global scope namespace
     */
    public void clearScope()
    {
        if (scope != -1)
            parts[scope] = EMPTY;
        scope = -1;
    }
 
    /**
     * @return the name in part format used to construct this object 
     */
    public String getSource()
    {
        return source;
    }

    @Override
    public int compareTo(QualifiedName anotherQualifiedName)
    {
        int scopeComp = scope - anotherQualifiedName.scope;
        if ((scopeComp == 0) && (scope != -1)) 
            scopeComp = getScope().compareTo(anotherQualifiedName.getScope());
        int templateComp = template - anotherQualifiedName.template;
        if ((templateComp == 0) && (template != -1)) 
            templateComp = getTemplate().compareTo(anotherQualifiedName.getTemplate());
        if (scopeComp == 0)
        {
            if (templateComp == 0)
               return getName().compareTo(anotherQualifiedName.getName());
            return templateComp;
        }
        return scopeComp;
    }

    @Override
    public int hashCode()
    {
        return getScope().hashCode() ^ getTemplate().hashCode() ^ getName().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;
        if (!(obj instanceof QualifiedName))
            return false;
        QualifiedName qualifiedName = (QualifiedName)obj;
        return compareTo(qualifiedName) == 0;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(EMPTY);
        if (scope != -1)
            builder.append(getScope()).append('.');
        if (template != -1)
        {
            builder.append(getTemplate());
            if (!getName().isEmpty())
                builder.append('.');
        }
        builder.append(getName());
        return builder.toString();
    }

    /**
     * Construct QualifiedName object from separate components
     * @param scope Scope
     * @param template Tempplate
     * @param name Name
     */
    protected void setParts(String scope, String template, String name)
    {
       if (QualifiedName.GLOBAL_SCOPE.equals(scope) || (scope == null) || scope.isEmpty())
            this.scope = 0;
        else
            this.scope = 1;
        if ((template == null) || template.isEmpty())
            this.template = 0;
        else
            this.template = 1;
        if ((name == null) || name.isEmpty())
            this.name = 0;
        else
            this.name = 1;
        int length = this.name + this.scope + this.template;
        if (length > 0)
            parts = new String[length];
        switch (length)
        {
        case 3:
            this.scope = 0;
            parts[this.scope] = scope;
            this.template = 1;
            parts[this.template] = template;
            this.name = 2;
            parts[this.name] = name;
            break;
        case 2:
        case 1:
        { 
            int index = 0;
            if (this.scope == 1)
            {
                this.scope = index++;
                parts[this.scope] = scope;
            }
            else
                this.scope = -1;
            if (this.template == 1)
            {
                this.template = index++;
                parts[this.template] = template;
            }
            else
                this.template = -1;
            if (this.name == 1)
            {
                this.name = index;
                parts[this.name] = name;
            }
            else
                this.name = -1;
            break;
        }
        default:
            this.template = -1;
            this.scope = -1;
            this.name = -1;
        }
    }

    /**
     * Converts text to QualifiedName. Two part names are placed in scope namespace.  
     * @param text Text
     * @return QualifiedName object
     */
    public static QualifiedName parseName(String text)
    {
        String[] parts = text.split("\\.");
        if (parts.length > 3)
            throw new SyntaxException("Qualified name \"" + text + "\" is invalid");
        final String name = parts[parts.length - 1];
        if (parts.length == 1)
            return new QualifiedName(name, QualifiedName.ANONYMOUS);
        else if (parts.length == 3)
            return new QualifiedName(parts[0], parts[1], name);
        return new QualifiedName(parts[0], QualifiedName.EMPTY, name);
    }

    /**
     * Returns QualifiedName for 1 or 2-part name in template namespace
     * Use parseName() for 3-part name
     * @param text 1 or 2-part template name name expected
     * @return QualifiedName object
     */
    public static QualifiedName parseTemplateName(String text)
    {
        String[] parts = text.split("\\.");
        if (parts.length > 3)
            throw new SyntaxException("Qualified name \"" + text + "\" is invalid");
        final String name = parts[parts.length - 1];
        if (parts.length == 1)
            return new QualifiedName(QualifiedName.EMPTY, name, QualifiedName.EMPTY);
        else if (parts.length == 3)
            return new QualifiedName(parts[0], parts[1], name);
        return new QualifiedName(parts[0], name, QualifiedName.EMPTY );
    }
 
    /**
     * Returns QualifiedName for 1 or 2-part name in global scope
     * Use parseName() for 3-part name
     * @param text 1 or 2-part global scope name expected
     * @return QualifiedName object
     */
    public static QualifiedName parseGlobalName(String text)
    {
        if (text.startsWith(".") || text.endsWith("."))
            throw new SyntaxException("Qualified name \"" + text + "\" missing part");
        String[] parts = text.split("\\.");
        if (parts.length == 0)
        {
            // Make parts valid
            parts = new String[1];
            parts[0] = text;
        }
        if (parts.length > 3)
            throw new SyntaxException("Qualified name \"" + text + "\" is invalid");
        final String name = parts[parts.length - 1];
        if (parts.length == 1)
            return new QualifiedName(QualifiedName.EMPTY, QualifiedName.EMPTY, name);
        else if (parts.length == 3)
            return new QualifiedName(parts[0], parts[1], name);
        return new QualifiedName(QualifiedName.EMPTY , parts[0], name);
    }

    /**
     * Returns QualifiedName for text using context qualified name to supply missing parts
     * @param text 1 or 2-part name in text format
     * @param qualifiedContextname Context qualified name supplying scope and template parts of new qualified name
     * @return QualifiedName object
     */
    public static QualifiedName parseName(String text, QualifiedName qualifiedContextname)
    {
    	boolean isTemplateName;
        String actualScope = null;
		int at = qualifiedContextname.getScope().indexOf('@');
		if (at != -1)
			actualScope = qualifiedContextname.getScope().substring(0, at);
		else
			actualScope = qualifiedContextname.getScope();
        // If in template context, assume 2-part name is template name
		isTemplateName = qualifiedContextname.template != -1;
		if (actualScope.isEmpty())
			actualScope = QualifiedName.GLOBAL_SCOPE;
        QualifiedName qname = isTemplateName ? parseGlobalName(text) : parseName(text);
        String newScope = null;
        String newTemplate = null;
        boolean replaceTemplate = (qname.template == -1) && (!qualifiedContextname.getTemplate().isEmpty());
        if (qname.scope == -1) {
        	// Supply scope unless context scope is global
    		if ((at == -1)  || !replaceTemplate) {
    			if (!actualScope.equals(QualifiedName.GLOBAL_SCOPE))
    	    		newScope = actualScope;
    		} else // Use template scope
                newScope = qualifiedContextname.getScope();
        }
        if (replaceTemplate)
            newTemplate = qualifiedContextname.getTemplate();
        boolean replace = false;
        if (newScope != null)
            replace = true;
        else
            newScope = qname.getScope();
        if (newTemplate != null)
            replace = true;
        else
            newTemplate = qname.getTemplate();
        return replace ? new QualifiedName(newScope, newTemplate, qname.getName()) : qname;
    }

    /**
     * Returns qualified name as axiom version of supplied template name
     * @param templateName Qualified template name
     * @return QualifiedName object
     */
    public static QualifiedName axiomFromTemplate(QualifiedName templateName)
    {
        return new QualifiedName(templateName.getScope(),  templateName.getTemplate());
    }

    /**
     * Returns qualified name as template version of supplied axiom name
     * @param axiomName Qualified axiom name
     * @return QualifiedName object
     */
    public static QualifiedName templateFromAxiom(QualifiedName axiomName)
    {
        return new QualifiedName(
        		QualifiedName.GLOBAL_SCOPE.equals(axiomName.getScope()) ? 
        		EMPTY : axiomName.getScope(), 
        		axiomName.getName(), EMPTY);
    }

    /**
     * Returns flag set true if scope is empty
     * @return boolean
     */
    public boolean isScopeEmpty()
    {
        return scope == -1;
    }
    
    /**
     * Returns flag set true if template is empty
     * @return boolean
     */
    public boolean isTemplateEmpty()
    {
        return template == -1;
    }
    
    /**
     * Returns flag set true if name is empty
     * @return boolean
     */
   public boolean isNameEmpty()
    {
        return name == -1;
    }
    
    /**
     * Returns flag set true if supplied specified qualified name is in same namespace as this one
     * @param qname Context qualified name - only scope and template parts are relevant.
     * @return boolean
     */
    public boolean inSameSpace(QualifiedName qname)
    {
        if (qname == null)
            throw new IllegalArgumentException("Parameter qname is null");
        // Unqualified name is always in same space
        if (qname.scope == -1 && qname.template == -1)
            return true;
        if (qname.getTemplate().equals(getTemplate()) || (qname.template == -1))
            return qname.getScope().equals(getScope());
        return false;
    }

    /**
     * Returns reference count value prior to increment 
     * @return positive number
     */
    public int incrementReferenceCount()
    {
        if (referenceCount == null)
            referenceCount = new AtomicInteger();
        return referenceCount.getAndIncrement();
    }
}
