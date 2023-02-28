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
package au.com.cybersearch2.taq.operator;

import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.interfaces.OperatorFactory;

/**
 * DelegateType
 * Classifies all cases where a value is assigned to a variable. 
 * If value is not one of 7 specific types, is it classified as ASSIGN_ONLY.
 * @author Andrew Bowley
 * 29Apr.,2017
 */
public enum DelegateType
{
    STRING,
    INTEGER,
    DOUBLE,
    DECIMAL,
    BOOLEAN,
    AXIOM, 
    NULL,
    ASSIGN_ONLY,
    LIST,
    CURSOR;
 
    /** Creates an Operator instance specific to one DelegateType */
    private OperatorFactory operatorFactory;
 
    /**
     * Returns operator factory for delegate type
     * @return OperatorFactory object
     */
    public OperatorFactory getOperatorFactory()
    {
        return operatorFactory;
    }

    static
    {
        STRING.operatorFactory = new OperatorFactory()
        {
            @Override
            public Operator delegate()
            {
                return new StringOperator();
            }
        };

        INTEGER.operatorFactory = new OperatorFactory()
        {
            @Override
            public Operator delegate()
            {
                return new IntegerOperator();
            }
        };
        
        DOUBLE.operatorFactory = new OperatorFactory()
        {
            @Override
            public Operator delegate()
            {
                return new DoubleOperator();
            }
        };
        
        DECIMAL.operatorFactory = new OperatorFactory()
        {
            @Override
            public Operator delegate()
            {
                return new BigDecimalOperator();
            }
        };
        
        BOOLEAN.operatorFactory = new OperatorFactory()
        {
            @Override
            public Operator delegate()
            {
                return new BooleanOperator();
            }
        };
        
        AXIOM.operatorFactory = new OperatorFactory()
                {
            @Override
            public Operator delegate()
            {
                return new AxiomOperator();
            }
        };
        
        ASSIGN_ONLY.operatorFactory = new OperatorFactory()
        {
            @Override
            public Operator delegate()
            {
                return new AssignOnlyOperator();
            }
        };
        
        NULL.operatorFactory = new OperatorFactory()
        {
            @Override
            public Operator delegate()
            {
                return new NullOperator();
            }
        };

        CURSOR.operatorFactory = new OperatorFactory()
        {
            @Override
            public Operator delegate()
            {
                return new CursorOperator();
            }
        };

        LIST.operatorFactory = new OperatorFactory()
        {
        	@Override
		    public Operator delegate()
		    {
		        return new ListOperator();
		    }
		};

    }
}
