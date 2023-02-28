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

import au.com.cybersearch2.taq.Scope;
import au.com.cybersearch2.taq.complex.Complex;
import au.com.cybersearch2.taq.expression.DoubleOperand;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.interfaces.LocaleListener;
import au.com.cybersearch2.taq.interfaces.Operator;
import au.com.cybersearch2.taq.interfaces.Trait;
import au.com.cybersearch2.taq.language.OperatorEnum;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.trait.ComplexTrait;

/**
 * DoubleOperator
 * @see DelegateType#DOUBLE
 * @see DoubleOperand
 * @author Andrew Bowley
 * 28Apr.,2017
 */
public class ComplexOperator implements Operator, LocaleListener
{
    private static final int re = 0;
    private static final int im = 1;
    /** Behaviors for localization and specialization of Complex operands */
    private ComplexTrait complexTrait = new ComplexTrait();
    
    @Override
    public Trait getTrait()
    {
        return complexTrait;
    }
    
    @Override
    public void setTrait(Trait trait)
    {
        if (!ComplexTrait.class.isAssignableFrom(trait.getClass()))
            return; //throw new ExpressionException(trait.getClass().getSimpleName() + " is not a compatible Trait");
        complexTrait = (ComplexTrait) trait;
    }
    
    @Override
    public boolean onScopeChange(Scope scope)
    {
    	if (!complexTrait.getLocale().equals(scope.getLocale())) {
    	    complexTrait.setLocale(scope.getLocale());
    	    return true;
    	}
    	return false;
    }

    /**
     * Returns object class associated with operands that use this operator
     * @return Class
     */
    @Override
	public Class<?> getObjectClass() {
		return Complex.class;
	}

    @Override
    public OperatorEnum[] getRightBinaryOps() 
    {
        return  new OperatorEnum[]
        { 
            OperatorEnum.ASSIGN,
            OperatorEnum.PLUS,
            OperatorEnum.MINUS,
            OperatorEnum.STAR,
            OperatorEnum.SLASH,
            OperatorEnum.PLUSASSIGN,
            OperatorEnum.MINUSASSIGN,
            OperatorEnum.STARASSIGN,
            OperatorEnum.SLASHASSIGN
        };
    }

	@Override
	public OperatorEnum[] getRightUnaryOps() {
        return  new OperatorEnum[]
        { 
            OperatorEnum.PLUS,
            OperatorEnum.MINUS
    };
	}

    @Override
    public OperatorEnum[] getLeftBinaryOps() 
    {
        return  new OperatorEnum[]
        { 
                OperatorEnum.ASSIGN,
                OperatorEnum.PLUS,
                OperatorEnum.MINUS,
                OperatorEnum.STAR,
                OperatorEnum.SLASH,
                OperatorEnum.PLUSASSIGN,
                OperatorEnum.MINUSASSIGN,
                OperatorEnum.STARASSIGN,
                OperatorEnum.SLASHASSIGN
        };
    }

	@Override
	public OperatorEnum[] getLeftUnaryOps() {
        return EMPTY_OPERAND_OPS;
	}

	@Override
     public OperatorEnum[] getConcatenateOps()
     {
         return EMPTY_OPERAND_OPS;
     }

    @Override
    public double[] numberEvaluation(OperatorEnum operatorEnum2, Term rightTerm) 
    {
        return new double[] {0.0,0.0};
    }

    @Override
    public double[] numberEvaluation(Term leftTerm, OperatorEnum operatorEnum2, Term rightTerm) 
    {
    	Object leftValue = leftTerm.getValue();
    	Object rightValue = rightTerm.getValue();
    	boolean leftIsComplex = leftValue instanceof double[];
    	boolean rightIsComplex = rightValue instanceof double[];
    	if (operatorEnum2 == OperatorEnum.STAR) {
	    	if (!leftIsComplex) {
	    		if (rightIsComplex && (leftValue instanceof Double))
	    			return scale((double[]) rightValue, (double)leftValue);
	    	} else if (!rightIsComplex) {
	    		if (leftIsComplex && (rightValue instanceof Double))
	    			return scale((double[]) leftValue, (double)rightValue);
	    	}
    	}
    	if (!leftIsComplex || !rightIsComplex)
    		throw new ExpressionException(
    			String.format("Cannot evaluate %s %s %s", 
    				leftValue.toString(), 
    				operatorEnum2.name(), 
    				rightValue.toString()));
    	double[] a = (double[]) leftValue;
    	double[] b = (double[]) rightValue;
    	switch(operatorEnum2) {
        case PLUSASSIGN: // "+="
        case PLUS: 
        {
            double real = a[re] + b[re];
            double imag = a[im] + b[im];
            return new double[] {real, imag};
        }
        case MINUSASSIGN: // "-="
        case MINUS: 
        {
            double real = a[re] - b[re];
            double imag = a[im] - b[im];
            return new double[] {real, imag};
        }
        case SLASHASSIGN: // "/="
        case SLASH:
        	return divides(a,b);
        case STARASSIGN: // "*="
    	case STAR: 
    	{
            double real = a[re] * b[re] - a[im] * b[im];
            double imag = a[re] * b[im] + a[im] * b[re];
            return new double[] {real, imag};
    	}
    	default:
    	}
        return new double[] {0.0,0.0};
    }

    public double[] divides(double[] a, double[] b) {   
    	// Get reciprocal of b
        double scale = b[re]*b[re] + b[im]*b[im];
        double[] _b = new double[] {b[re] / scale,-b[im] / scale};
        double real = a[re] * _b[re] - a[im] * _b[im];
        double imag = a[re] * _b[im] + a[im] * _b[re];
        return new double[] {real, imag};
	}

	/**
	 * Returns (this * alpha)
	 * @return double[]
	 */
	public double[] scale(double[] a, double alpha) {
        return new double[] {alpha * a[re], alpha * a[im]};
	}
	

	@Override
    public Boolean booleanEvaluation(Term leftTerm, OperatorEnum operatorEnum2, Term rightTerm) 
    {
    	return Boolean.FALSE;
    }

}
