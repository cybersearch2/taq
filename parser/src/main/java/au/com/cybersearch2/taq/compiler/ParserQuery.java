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

import java.util.List;

import au.com.cybersearch2.taq.artifact.QueryArtifact;
import au.com.cybersearch2.taq.compile.ListAssembler;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.compile.ParserContext;
import au.com.cybersearch2.taq.compile.ProtoAxiom;
import au.com.cybersearch2.taq.compile.VariableFactory;
import au.com.cybersearch2.taq.compile.VariableSpec;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.language.IOperand;
import au.com.cybersearch2.taq.language.KeyName;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.QuerySpec;
import au.com.cybersearch2.taq.language.TaqLiteral;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.list.AxiomList;
import au.com.cybersearch2.taq.list.AxiomTermList;
import au.com.cybersearch2.taq.pattern.Axiom;
import au.com.cybersearch2.taq.pattern.Template;

/**
 * Parser helper for creating query artifacts
 */
public class ParserQuery implements QueryArtifact {

	/** Parser context */
	private final ParserContext parserContext;

	/**
	 * Construct ParserQuery object
	 * @param parserContext Parser context
	 */
	public ParserQuery(ParserContext parserContext) {
		this.parserContext = parserContext;
	}

	/**
     * Process a QueryChain production
     * @param querySpec Query specification
     * @param literal Solution type or null if no solution provided
     */
	@Override
    public void queryChain(QuerySpec querySpec, TaqLiteral literal) {
        parserContext.getScope().addQuerySpec(querySpec);
        OperandType operandType = null;
        if (literal != null) {
        	operandType = literal == TaqLiteral.axiom ? OperandType.AXIOM : OperandType.TERM;
        	querySpec.setOperandType(operandType);
        }
        if (operandType == null) 
          return;
        ParserAssembler parserAssembler = getParserAssembler();
        VariableSpec varSpec = new VariableSpec(operandType);
        varSpec.setAxiomKey(querySpec.getKey());
        VariableFactory variableFactory = new VariableFactory(varSpec);
        ItemList<?> itemList = variableFactory.getItemListInstance(querySpec.getName(), parserAssembler.getQualifiedContextname());
        if (operandType == OperandType.AXIOM)
        	parserAssembler.registerAxiomList((AxiomList) itemList);
        else
        	parserAssembler.registerAxiomTermList((AxiomTermList) itemList);
        itemList.setPublic(true);
        parserAssembler.getListAssembler().addItemList(itemList.getQualifiedName(), itemList);
    }
 
	/**
	 * Returns a query specification
	 * @param name Query name
	 * @param isHeadQuery Flag set true if head query specification
	 * @return QuerySpec object
	 */
	@Override
    public QuerySpec createQuerySpec(String name, boolean isHeadQuery) {
    	 return new QuerySpec(name, isHeadQuery);
    }

    /**
     * Process a KeyName production
     * @param querySpec Query specification
     * @param name1 Name first part or null if only 1 part
     * @param name2 Name second part
     * @return KeyName object
     */
	@Override
    public KeyName keyName(QuerySpec querySpec, QualifiedName name1, QualifiedName name2) {
        boolean isBinary = name1 != null;
        QualifiedName axiomKey = isBinary ? name1 : QualifiedName.ANONYMOUS;
        QualifiedName templateName = name2;
        KeyName keyname = new KeyName(axiomKey, templateName);
        querySpec.addKeyName(keyname);
        Template template = parserContext.getScope().getKeyTemplate(templateName);
        if (template == null)
        	throw new ExpressionException(String.format("Template %s not found", templateName.toString()));
        return keyname;
    }

	@Override
	public QualifiedName wrapList(QualifiedName name1, IOperand listOperand) {
        ParserAssembler parserAssembler = getParserAssembler();
        ListAssembler listAssembler = parserAssembler.getListAssembler();
        QualifiedName key = new QualifiedName(name1.getName() + "Key", name1);
        ProtoAxiom protoAxiom =  listAssembler.axiomInstance(key, false);
        protoAxiom.add(new Axiom(key.toString(), new Parameter(name1.getName(), listOperand.getValue())));
		return key;
	}
	
    /**
     * Process a QueryDelaration production
     * @param querySpec Query specification
     * @param firstKeyname First keyname in query
     * @param keynameCount Number of keynames in query
     * @param termList Additional parameters
     * @return QuerySpec object
     */
	@Override
    public QuerySpec queryDeclaration(QuerySpec querySpec, KeyName firstKeyname, int keynameCount, List<Term> termList) {
        return parserContext.getScope().buildQuerySpec(querySpec, firstKeyname, keynameCount, termList);
    }
 
	/**
	 * Returns parser assembler
	 * @return ParserAssembler object
	 */
	private ParserAssembler getParserAssembler() {
		return parserContext.getParserAssembler();
	}


}
