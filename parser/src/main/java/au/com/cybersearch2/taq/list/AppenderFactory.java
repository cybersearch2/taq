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

import au.com.cybersearch2.taq.compile.ListAssembler;
import au.com.cybersearch2.taq.compile.OperandMap;
import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.compiler.CompilerException;
import au.com.cybersearch2.taq.expression.AppenderVariable;
import au.com.cybersearch2.taq.expression.ListOperand;
import au.com.cybersearch2.taq.expression.Variable;
import au.com.cybersearch2.taq.interfaces.Appender;
import au.com.cybersearch2.taq.interfaces.ItemList;
import au.com.cybersearch2.taq.interfaces.ListItemDelegate;
import au.com.cybersearch2.taq.interfaces.ListType;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;

/**
 * Produces list appenders which also service for right hand side of assignment expressions
 */
public class AppenderFactory {

	/** Suffix added to list name to create appender name */
    public static final String APPENDER_SUFFIX = "_appender";

    /** Specification to build a list appender */
	private final AppenderSpec appenderSpec;
	/** The part shared by all appenders of a particular list */
	private ListItemVariable appender;

	/**
	 * Construct an AppenderFactory object
	 * @param parserAssembler Parser assembler of current scope
	 * @param parserAssembler Parser assembler
	 */
	public AppenderFactory(AppenderSpec appenderSpec, ParserAssembler parserAssembler) {
		this.appenderSpec = appenderSpec;
		assureAppenderExists(parserAssembler);
	}

	/**
	 * Returns qualified name of list to append
	 * @return QualifiedName object
	 */
	public QualifiedName getListName() {
		return appenderSpec.getListName();
	}

	/**
	 * Run task to bind the list to this factory immediately
	 * @param parserAssembler Parser assembler
	 */
	public void assembleAppender(ParserAssembler parserAssembler) {
		appender.run(parserAssembler);
		ListItemDelegate delegate = appender.getDelegate();
		if (delegate instanceof AxiomVariable)
			((AxiomVariable)delegate).setAppemderDelegate(true);
	}

	/**
	 * Delegate given list as appender target
	 * @param itemList Item list
	 */
	public void setItemList(ItemList<?> itemList) {
        appender.setDelegate(itemList);
	}

	/**
	 * Returns part shared by all appenders of delegated list
	 * @return appender object
	 */
	public Appender getAppender() {
		return appender;
	}

	/**
	 * Returns list appender variable with default name. The expression evaluates a value which is appended to a list.
     * @return Operand object
     * @throws CompilerException if operand found with appender name look up is not an appender
	 */
	public Operand createAppender() throws CompilerException
    {
		return createAppender(appenderSpec.getDefaultVariableName());
    }

	/**
	 * Create basic list appender variable. The expression evaluates a value which is appended to a list.
     * @param variableQname Qualified name of variable
     * @return Operand object
     * @throws CompilerException if operand found with appender name look up  is not an appender
	 */
	public Operand createAppender(QualifiedName variableQname) throws CompilerException {
	    QualifiedName qname = appenderSpec.getListName();
        String name = variableQname.getName().isEmpty() ? Term.ANONYMOUS : qname.getName();
        Variable var = new AppenderVariable(variableQname, name, appenderSpec);
        var.setRightOperand(appender);
        return var;
	}

	/**
	 * Look for existing Appender in the operand map and create one if it is not found
	 * @param parserAssembler Parser assembler
	 */
	private void assureAppenderExists(ParserAssembler parserAssembler) {
	    OperandMap operandMap = parserAssembler.getOperandMap();
	    QualifiedName qname = appenderSpec.getListName();
	    // The Appender is a singleton
	    String appenderName = qname.getName() + APPENDER_SUFFIX;
	    QualifiedName appenderQname = new QualifiedName(appenderName, qname);
	    // Check for existing appender operand
	    if (operandMap.existsName(appenderName)) {
		    Operand operand = null;
            operand = operandMap.getOperand(appenderQname);
            if (operand == null) {
            	appenderQname = new QualifiedName(qname.getScope(), appenderName);
                operand = operandMap.getOperand(appenderQname);
            }
            if (operand != null) {
            	if (!(operand instanceof Appender))
                    throw new CompilerException(qname.toString() + " is not an appender");
            	appender = (ListItemVariable) operand;
            }
 	    } 
        if (appender == null) 
        {   
            ListAssembler listAssembler = parserAssembler.getListAssembler();
            boolean isCursor = listAssembler.existsKey(ListType.cursor, qname);
            boolean isDynamicAxiomList = listAssembler.existsKey(ListType.axiom_dynamic, qname);
            if (isDynamicAxiomList) {
               QualifiedName targetName = listAssembler.getAxiomListMapping(qname);
               if (targetName.getScope().equals("scope"))
            	   qname = targetName;
            }
            ItemList<?> itemList = appenderSpec.getItemList();
            if (itemList != null) {
	        	// Create appender operand according to list type
	            if ((appenderSpec.getListType() == ListType.basic) && !isCursor)
	                appender = new ListItemVariable(appenderQname, itemList, appenderSpec.getIndexDataArray(), null);
	            else
	                appender = new AxiomListAppender(appenderQname, itemList, (ArrayIndex)appenderSpec.getIndexDataArray()[0]);
            } else {
	        	// Create appender operand according to list type
	            if ((appenderSpec.getListType() == ListType.basic) && !isCursor)
	                appender = new ListItemVariable(appenderQname, appenderSpec.getIndexDataArray(), null);
	            else
	                appender = new AxiomListAppender(appenderQname, (ArrayIndex)appenderSpec.getIndexDataArray()[0]);
            }
            Operand listOperand = parserAssembler.getOperandMap().get(qname);
            if (listOperand instanceof ListOperand)
            	appender.setRightOperand(listOperand);
            operandMap.addOperand(appender);
       }
	}

}
