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
package au.com.cybersearch2.taq.compile;

import java.util.HashMap;
import java.util.Map;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.language.IVariableSpec;
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * ParserContext
 * Aggregates variables required while parsing 
 * @author Andrew Bowley
 * 6Apr.,2017
 */
public class ParserContext
{
    private final QueryProgram queryProgram;
    /** TAQ document */
    private String sourceDocument;
    
    /** Current scope */
    private Scope scope;
    /** Current parserAssembler */
    private ParserAssembler parserAssembler;
    /** Current operand map */
    private OperandMap operandMap;
    /** Map context list name to type information */
    private Map<String,VariableSpec> contextListMap;
    /**
     * Construct ParserContext with empty source document path
     * @param queryProgram Main query object
     */
    public ParserContext(QueryProgram queryProgram)
    {
    	this(queryProgram, "");
    }
    
//
    /**
     * Contruct ParserContext with empty source document path
     * @param queryProgram Main query object
     * @param sourceDocument TAQ document
     */
    public ParserContext(QueryProgram queryProgram, String sourceDocument)
    {
        this.queryProgram = queryProgram;
        this.sourceDocument = sourceDocument;
        contextListMap = new HashMap<>();
        resetScope();
    }
    
    /**
     * Set current scope - call {@link #resetScope()}resetScope on exit from this scope
     * @param scope Scope
     */
    public void setScope(Scope scope)
    {
        this.scope = scope;
        parserAssembler = scope.getParserAssembler();
        operandMap = parserAssembler.getOperandMap();
    }

    /**
     * Switch to global scope
     */
    public void resetScope()
    {
        setScope(queryProgram.getGlobalScope());
    }

    /**
     * Returns current scope
     * @return Scope object
     */
    public Scope getScope()
    {
        return scope;
    }
 
    /**
     * @return the outerTemplate
     */
    public QualifiedName getTemplateName()
    {
    	QualifiedName qname = scope.getOuterTemplate();
    	if (qname.getTemplate().equals(Scope.SCOPE) && (parserAssembler.getTemplateAssembler().getTemplate(qname) == null)) {
            parserAssembler.getTemplateAssembler().createTemplate(qname, TemplateType.calculator); 
    	}
    	return qname;
    }

	/**
     * @param outerTemplate the outerTemplate to set
     */
    public void setTemplateName(QualifiedName outerTemplate)
    {
        scope.setOuterTemplate(outerTemplate);
    }

    /**
     * @return the parserAssembler
     */
    public ParserAssembler getParserAssembler()
    {
        return parserAssembler;
    }

    /**
     * Returns current operand map
     * @return OperandMap object
     */
    public OperandMap getOperandMap()
    {
        return operandMap;
    }

    /**
     * Returns qualified name of current context
     * @return QualifiedName object
     */
    public QualifiedName getContextName()
    {
        return parserAssembler.getQualifiedContextname();
    }

    /**
     * Set current context name
     * @param qualifiedName Qualified name
     */
    public void setContextName(QualifiedName qualifiedName)
    {
        parserAssembler.setQualifiedContextname(qualifiedName);
    }

    public void putContextList(String name, VariableSpec varSpec) {
    	contextListMap.put(name, varSpec);
    }
    
    public IVariableSpec getVariableSpec(String name) {
    	return contextListMap.get(name);
    }
    
    /**
     * @param parserAssembler the parserAssembler to set
     */
    public void setParserAssembler(ParserAssembler parserAssembler)
    {
        this.parserAssembler = parserAssembler;
    }

    /**
     * Returns Query Program
     * @return QueryProgram object
     */
    public QueryProgram getQueryProgram()
    {
        return queryProgram;
    }

    
    public String getSourceDocument() {
		return sourceDocument;
	}

}
