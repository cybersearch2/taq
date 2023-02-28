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
package au.com.cybersearch2.taq.expression;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import au.com.cybersearch2.taq.compiler.CompilerException;
import au.com.cybersearch2.taq.helper.EvaluationStatus;
import au.com.cybersearch2.taq.interfaces.Operand;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.pattern.Template;
import au.com.cybersearch2.taq.provider.ObjectSpec;

/**
 * ObjectOperand
 * Invokes a method on the object of an operand
 * @author Andrew Bowley
 */
public class ObjectOperand extends DelegateOperand
{
    private final String methodName;
    
    private List<Object> parameters;
    private List<Term> termList;

    private Template parametersTemplate;
    
    private Object object;
    private Class<?> objectClass;
    private Method method;
    private Operand target;
    
    private Operand rightOperand;
    private boolean useSingleTerm;
    private boolean useTerms;

    /**
     * Construct ObjectOperand - non-reflexive
     * @param objectSpec Object specification
     */
    public ObjectOperand(ObjectSpec objectSpec)
    {
        super(QualifiedName.ANONYMOUS, Term.ANONYMOUS);
        objectClass = objectSpec.getObjectClass();
        object = objectSpec.getObject();
        methodName = objectSpec.getMethodName();
        parameters = objectSpec.getParameters();
        target = objectSpec.getOperand();
        if (objectSpec.hasTemplate() && 
        	(objectSpec.getParametersTemplate().getTermCount() > 0))
        	parametersTemplate = objectSpec.getParametersTemplate();
        else
        	parametersTemplate = null;
        if (objectSpec.isReflexive())
            setRightOperand(target);
        if ((objectClass != null) && !objectSpec.hasTemplate())
            createMethod (objectClass, target, methodName);
    }

	/**
     * 
     * @see au.com.cybersearch2.taq.interfaces.Operand#getQualifiedName()
     */
    @Override
    public QualifiedName getQualifiedName()
    {
        return QualifiedName.ANONYMOUS;
    }

    @Override
	public EvaluationStatus evaluate(int id) {
        if (parametersTemplate != null)
        {
        	parametersTemplate.evaluate(context);
            termList = parametersTemplate.toArray();
            if (!useTerms && !useSingleTerm) {
                parameters.clear();
                termList.forEach(term -> parameters.add(term.getValue()));
            }
        }
        if (objectClass == null) {
		    objectClass = target.getOperator().getObjectClass();
            if (objectClass == null) // Object interface not supported
        	    throw new CompilerException(String.format("Variable \"%s\" is not an object",  name.toString()));
            createMethod (objectClass, target, methodName);
        } else if (method == null)
            createMethod (objectClass, target, methodName);
    	try {
    		if (useTerms) 
			    setValue(method.invoke(object, (Object)termList.toArray(new Term[termList.size()])));
    		else if (useSingleTerm) 
			    setValue(method.invoke(object, termList.get(0)));
    		else 
			    setValue(method.invoke(object, parameters.toArray()));
			setId(id);
			if (rightOperand != null) {
				rightOperand.setValue(getValue());
				rightOperand.setId(id);
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new ExpressionException(String.format("Error invoking method %s of class %s", method.getName(), object.getClass().getName()), e);
		}
    	setId(id);
    	return EvaluationStatus.COMPLETE;
	}

	@Override
	public boolean backup(int id) {
		if (leftOperand != null)
			leftOperand.backup(id);
        if (parametersTemplate != null) {
        	int backupId = parametersTemplate.getId();
        	if (id == getId())
		        parametersTemplate.backup(backupId);
        	else if (id == 0)
		        parametersTemplate.backup(0);
        }
		return super.backup(id);
	}

	/**
     * 
     * @see au.com.cybersearch2.taq.interfaces.Operand#assign(au.com.cybersearch2.taq.language.Parameter)
     */
    @Override
    public void assign(Parameter parameter)
    {
    }

    /**
     * 
     * @see au.com.cybersearch2.taq.interfaces.Operand#getRightOperand()
     */
    @Override
    public Operand getRightOperand()
    {
        return rightOperand;
    }

    @Override
	public String toString() {
    	String objectName = target != null ? target.getName() : "object";
		return objectName + (empty ? (methodName + "()") : ("=" + value.toString()));
	}

	private void setRightOperand(Operand rightOperand) {
		this.rightOperand = rightOperand;
	}

    private void createMethod(Class<?> objectClass, Operand target, String methodName) {
    	Class<?>[] classArray = new Class<?>[parameters.size()];;
    	int index = 0;
        for (Object parameter: parameters) {
        	Class<?> clazz = parameter.getClass();
        	//if (!clazz.isPrimitive())
        	//	clazz = Integer.TYPE;
        	classArray[index++] = clazz;
        }
        Method reflected = null;
        try {
        	reflected = objectClass.getMethod(methodName, classArray);
        	method = reflected;
        } catch(Throwable e) {
		}
        if ((reflected == null) && (classArray.length == 1)) {
        	classArray[0] = Term.class;
            try {
            	reflected = objectClass.getMethod(methodName, classArray);
            	method = reflected;
            	useSingleTerm = true;
            } catch(Throwable e) {
    		}
        }
        if ((reflected == null) && (classArray.length > 0)) {
        	classArray = new Class<?>[] {Term[].class};
            try {
            	reflected = objectClass.getMethod(methodName, classArray);
            	method = reflected;
            	useTerms = true;
            } catch(Throwable e) {
    		}
        }
        if (reflected == null)
    	    throw new CompilerException(String.format("Method \"%s\" does not exist in class %s", methodName, objectClass.getName()));
    	try {
        	if (target != null) {
        		Constructor<?> constructor = objectClass.getConstructor(target.getClass());
        		object = constructor.newInstance(target);
        	}
		} catch (Throwable e) {
			throw new CompilerException("Reflection error", e);
		} 
    }

}
