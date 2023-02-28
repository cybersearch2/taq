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
package au.com.cybersearch2.taq.provider;

import java.util.ArrayList;
import java.util.List;

import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.pattern.Template;

public class ObjectSpec {

    private final String methodName;
    private final List<Object> parameters;
    private final Template parametersTemplate;
    private final Operand operand;
    
    private Object object;
    private Class<?> objectClass;
    private boolean isReflexive;

	public ObjectSpec(String methodName, List<Object> parameters, Operand operand) {
        this.methodName = methodName;
        this.parameters = parameters;
        this.operand = operand;
        parametersTemplate = null;
	}

	public ObjectSpec(String methodName, Template parametersTemplate) {
		this(methodName, parametersTemplate, null);
	}
	
	public ObjectSpec(String methodName, Template parametersTemplate, Operand operand) {
        this.methodName = methodName;
		this.parametersTemplate = parametersTemplate;
		this.operand = operand;
        parameters = null;
        operand = null;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public Class<?> getObjectClass() {
		return objectClass;
	}

	public void setObjectClass(Class<?> objectClass) {
		this.objectClass = objectClass;
	}

	public Operand getOperand() {
		return operand;
	}

	public boolean hasTemplate() {
		return parametersTemplate != null;
	}
	
	public boolean isReflexive() {
		return isReflexive && (operand != null);
	}

	public void setReflexive(boolean isReflexive) {
		this.isReflexive = isReflexive;
	}

	public String getMethodName() {
		return methodName;
	}

	public List<Object> getParameters() {
		return parameters != null ? parameters : new ArrayList<>(); 
	}

	public Template getParametersTemplate() {
		return parametersTemplate;
	}

}
