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

import au.com.cybersearch2.taq.compile.ParserAssembler;
import au.com.cybersearch2.taq.expression.AppenderVariable;
import au.com.cybersearch2.taq.expression.Variable;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.ListItemSpec;
import au.com.cybersearch2.taq.interfaces.ParserRunner;
import au.com.cybersearch2.taq.language.QualifiedName;

/**
 * Applies appender created by an AppederFactory to target n list item  
 */
public class ListItemAppender extends Variable implements ParserRunner {

	private final AppenderSpec appenderSpec;
	
	private AppenderVariable appenderVariable;

	/**
	 * Construct ListItemAppender object
	 * @param appenderSpec Specification to build a list appender
	 * @param indexDataArray List item reference
	 * @param parserAssembler Parser assembler
	 */
	public ListItemAppender(AppenderSpec appenderSpec, ListItemSpec[] indexDataArray, ParserAssembler parserAssembler) {
		super(getVariableQname(appenderSpec, indexDataArray[0].getSuffix(), parserAssembler), appenderSpec.getListName().getName());
		this.appenderSpec = appenderSpec;
	}

	@Override
	public void run(ParserAssembler parserAssembler) {
		AppenderFactory appenderFactory = new AppenderFactory(appenderSpec, parserAssembler);
		appenderFactory.assembleAppender(parserAssembler);
		appenderVariable = (AppenderVariable)appenderFactory.createAppender();
		setLeftOperand(appenderVariable);
	}

	@Override
	public EvaluationStatus evaluate(int id) {
		AppenderVariable variable = getAppenderVariable(id);
    	if (variable != null) {
        		variable.setExecutionContext(context);
    		if (variable.evaluate(id) == EvaluationStatus.COMPLETE) {
    		    setValue(variable.getValue());
		        this.id = id;
    		}
    	}
		return EvaluationStatus.COMPLETE;
	}

	private AppenderVariable getAppenderVariable(int id) {
		return appenderVariable;
	}
	
	private static QualifiedName getVariableQname(AppenderSpec appenderSpec, String suffix, ParserAssembler parserAssembler) {
		return parserAssembler.getContextName(appenderSpec.getListName().getName() + "_" + suffix);
	}

}
