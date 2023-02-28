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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * NameParser
 * Utility class to parse qualified name in text format
 * @author Andrew Bowley
 * 7 Jun 2015
 */
public class NameParser 
{
	/** The global scope is accessible from all scopes */
	static public final String GLOBAL_SCOPE = "global";
	
	private final static char DOT = '.';
	private final static String IDENTIFIER = "[a-zA-Z0-9][a-zA-Z_0-9]*";
	private final static String NAME_REGEX = "^(" + IDENTIFIER + ")(\\.)?(" + IDENTIFIER + ")?$" ;
	private final static String NULL_KEY_MESSAGE = "Null key passed to name parser";

    /** Name ends with '@' */
    private final boolean isDefaultGlobal;

    /** Scope part */
    private String scope;
    /** Template part */
    private String template;
    /** Name part */
    private String name;

    /**
     * Construct NameParser instance
     * @param name Qualified name in text format
     */
    public NameParser(String name)
    {
        isDefaultGlobal = name.endsWith("@");
        this.scope = isDefaultGlobal ? GLOBAL_SCOPE : QualifiedName.EMPTY;
        this.template = QualifiedName.EMPTY;
        this.name = QualifiedName.EMPTY;
        if (name.isEmpty())
            return;
        if (name.startsWith("@")) {
        	// Template name: @identifier.indentifier
        	int index = name.indexOf(DOT);
        	if ((index == -1) || (name.indexOf(DOT, index + 1) != -1))	
                throw new SyntaxException("Name \"" + name + "\" myst have 2 parts to be valid");
            this.scope = name.substring(1, index);
            this.template =  name.substring(index + 1);
            return;
        }
        String[] fragments = name.split("@");
        if ((fragments.length == 2) && isDefaultGlobal)
            throw new SyntaxException("Name \"" + name + "\" with more than one \"@\" is invalid");
        if ((fragments.length > 2))
        	parseTemplateName(fragments);
        // Parse artifact name
        Pattern pattern = null;
        try
        {
            pattern = Pattern.compile(NAME_REGEX, 0);
        }
        catch(PatternSyntaxException e)
        {   // This is not expected
            throw new SyntaxException("Error in regular expression", e);
        }
        if ((fragments.length < 2) && !isDefaultGlobal)
        {   // Parse part name
            parseGlobalName(fragments[0]);
            return;
        }
        // Use pattern to decompose name fragments each side of "@" into parts
        String[] groupValues1 = new String[0];
        Matcher matcher = pattern.matcher(fragments[0]);
        if (matcher.find())
            groupValues1 = getGroups(matcher);
        if (groupValues1.length == 0)
            throw new SyntaxException("Name \"" + name + "\" is invalid");
        String[] groupValues2 = new String[0];
        if (fragments.length == 2)
        {
            matcher = pattern.matcher(fragments[1]);
            if (matcher.find())
                groupValues2 = getGroups(matcher);
            if (groupValues2.length == 0)
                throw new SyntaxException("Name \"" + name + "\" is invalid after \"@\"");
        }
        if ((groupValues1.length == 4) && (groupValues2.length == 4))
            throw new SyntaxException("Name \"" + name + "\" with more than 3 parts is invalid");
        String templatePart = null;
        String scopePart = null;
        // With @, so name comes first
        String namePart = groupValues1[1];
        if (groupValues1.length == 4)
        {
            templatePart = groupValues1[1];
            namePart = groupValues1[3];
            if (groupValues2.length > 1)
                scopePart = groupValues2[1];
        }
        else if (groupValues2.length == 4)
        {
            templatePart = groupValues2[1];
            scopePart = groupValues2[3];
        }
        else if (groupValues2.length > 0)
            scopePart = groupValues2[1];
        if (templatePart == null)
            templatePart = QualifiedName.EMPTY;
        if (scopePart == null)
            scopePart = QualifiedName.EMPTY;
        this.scope = scopePart;
        this.template = templatePart;
        this.name = namePart;
    }

	/**
     * Returns Qualified name from parsed name
     * @return QualifiedName object
     */
    public QualifiedName getQualifiedName()
    {
        return new QualifiedName(scope, template, name);
    }
    
    /**
     * @return the scope
     */
    public String getScope()
    {
        return scope;
    }

    /**
     * @return the template
     */
    public String getTemplate()
    {
        return template;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * toString - Display qualified name with non-empty parts separated with dot character
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(QualifiedName.EMPTY);
        if (!scope.isEmpty())
            builder.append(scope).append('.');
        if (!template.isEmpty())
        {
            builder.append(template);
            if (!getName().isEmpty())
                builder.append('.');
        }
        builder.append(name);
        return builder.toString();
    }

    public static boolean hasThreeParts(String name) {
    	int dotCount = 0;
    	int index = name.indexOf(DOT);
    	while (index != -1) {
    		++dotCount;
    		index = name.indexOf(DOT, index + 1);
    	}
    	return dotCount == 2 || ((dotCount == 1) && name.contains("@"));
    }
    
    /**
     * Decompose part name, assuming 2-part name is template name in global scope
     * @param text Formated text
     */
    private void parseGlobalName(String text)
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
        this.name = parts[parts.length - 1];
        if (parts.length == 3)
        {
            this.scope = parts[0];
            this.template= parts[1];
        }
        else if (parts.length == 2)
            this.template= parts[0];
    }

    /**
     * Returns values for group 0 and above as array
     * @param matcher Matcher object in matched state
     * @return String array
     */
    protected static String[] getGroups(Matcher matcher)
    {
        List<String> groupList = new ArrayList<String>(matcher.groupCount() + 1);
        for (int i = 0; i < matcher.groupCount() + 1; i++)
        {
            String group = matcher.group(i);
            if (group != null)
                groupList.add(group);
        }
        return groupList.toArray(new String[groupList.size()]);
    }

    /**
     * Returns name part of key
     * @param key Key
     * @return String
     */
    public static String getNamePart(String key)
    {
        if (key == null)
            throw new IllegalArgumentException(NULL_KEY_MESSAGE);
        int dot = key.lastIndexOf(DOT);
        if (dot != -1)
            return key.substring(dot + 1);
        return key;
    }

    /**
     * Returns scope part of key or name of Global Scope if not in key
     * @param key Key
     * @return String
     */
    public static String getScopePart(String key)
    {
        if (key == null)
            throw new IllegalArgumentException(NULL_KEY_MESSAGE);
        int dot = key.indexOf(DOT);
        if (dot != -1)
            return key.substring(0, dot);
        return QualifiedName.GLOBAL_SCOPE;
    }

    /**
     * Scope part contains 'scope@template' path
     * @param fragments The path split by "@" separator
     */
    private void parseTemplateName(String[] fragments) {
        // Parse artifact name
        try
        {
            Pattern identifierPattern = Pattern.compile(IDENTIFIER, 0);
            Matcher matcher = identifierPattern.matcher(fragments[0]);
            if (!matcher.find())
                throw new SyntaxException("Scope \"" + fragments[0] + "\" is invalid");
            // First fragment is always an actual scope and "global' used for global scope
            scope = fragments[0];
            // Last fragment expected to contain 'template.name'
            int endPos = fragments.length - 1;
            boolean isNameValid = false;
            String[] parts = fragments[endPos].split(".");
            if (parts.length == 2)
            {
                name = parts[0];
                template = parts[1];
            	matcher = identifierPattern.matcher(name);
            	if (matcher.find())
            	{
                	matcher = identifierPattern.matcher(template);
                	isNameValid = matcher.find();
            	}
            } else if (parts.length < 2) // Last fragment is just a template
            {
                template = fragments[endPos];
            	matcher = identifierPattern.matcher(template);
            	isNameValid = matcher.find();
            }
            if (!isNameValid)
                throw new SyntaxException("Name \"" + fragments[endPos] + "\" is invalid");
            // Any fragments between first and last belong to the scope
       	    for (int i = 1; i <  endPos; ++i)
       	    {
	        	matcher = identifierPattern.matcher(fragments[i]);
	        	if (!matcher.find())
	                throw new SyntaxException("Template \"" + fragments[i] + "\" is invalid");
	        	scope += "@" + fragments[i];
      		}
        }
        catch(PatternSyntaxException e)
        {   // This is not expected
            throw new SyntaxException("Error in regular expression", e);
        }
		
	}

}
