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
package au.com.cybersearch2.taq.complex;

import au.com.cybersearch2.taq.expression.ComplexOperand;
import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.operator.ComplexOperator;

/**
 * Object class for ComplexOperand. Provides methods that can be called
 * using TAQ's object interface, for example "'reciprocal' x" where x
 * is a complex number.
 * Complex number values are stored and passed between Complex operands as a double array.
 */
public class Complex {

	public static int re = 0;
	public static int im = 1;

	/** Operand wrapped by this object. */
	private final ComplexOperand operand;

	/**
	 * Construct Complex object for given real and imaginary parts 
	 * @param real Real part
	 * @param imaginary Imaginary part
	 */
	public Complex(double real, double imaginary) {
		double[] value = new double[] {real, imaginary};
		operand = new ComplexOperand(QualifiedName.ANONYMOUS, value);
	}

	/**
	 * Construct Complex object as operand wrapper
	 * @param operand Operand to wrap 
	 */
	public Complex(ComplexOperand operand) {
		this.operand = operand;
	}

	/**
	 * Return real part
	 * @return
	 */
    public double re() { 
    	double[] a = toArray();
    	return a[re]; 
    }

	/**
	 * Return imaginary part
	 * @return
	 */
    public double im() { 
    	double[] a = toArray();
    	return a[im]; 
    }

    /**
	 * Returns abs/modulus/magnitude
	 * @return double
	 */
	public double abs() {
    	double[] a = toArray();
    	// Square root of (re*re + im*im)
        return Math.hypot(a[re], a[im]);
	}
	
	/**
	 * Returns  angle/phase/argument, normalized to be between -pi and pi
	 * @return double
	 */
	public double phase() {
    	double[] a = toArray();
        // Returns the angle theta from the conversion of rectangular
        // coordinates ({@code x},&nbsp;{@code y}) to polar
        // coordinates (r,&nbsp;<i>theta</i>).
        return Math.atan2(a[re], a[im]);
	}

	/**
	 * Returns conjugate of this
	 * @return double[]
	 */
	public double[] conjugate() {
    	double[] a = toArray();
        return new double[] { a[re], -a[im]};
	}
	
	/**
	 * Returns reciprocal of complex number
	 * @return double[]
	 */
	public double[] reciprocal() {
    	double[] a = toArray();
        double scale = a[re]*a[re] + a[im]*a[im];
        return new double[]{a[re] / scale, -a[im] / scale};
	}
	
	/**
	 * Returns the complex exponential of this
	 * @return double[]
	 */
    public double[] exp() {
    	double[] a = toArray();
        return new double[] {Math.exp(a[re]) * Math.cos(a[im]), Math.exp(a[re]) * Math.sin(a[im])};
    }

	/**
	 * Returns the complex sine of this
	 * @return double[]
	 */
   public double[] sin() {
    	double[] a = toArray();
        return new double[] {Math.sin(a[re]) * Math.cosh(a[im]), Math.cos(a[re]) * Math.sinh(a[im])};
    }

	/**
	 * Returns the complex cosine of this
	 * @return double[]
	 */
    public double[] cos() {
    	double[] a = toArray();
        return new double[] {Math.cos(a[re]) * Math.cosh(a[im]), -Math.sin(a[re]) * Math.sinh(a[im])};
    }

 	/**
	 * Returns the complex tangent of this
	 * @return double[]
	 */
    public double[] tan() {
        return ((ComplexOperator)operand.getOperator()).divides(sin(), cos());
    }

    public double[] toArray() {
		if (operand.isEmpty())
			return new double[] {0.0,0.0};
		double[] value = (double[])operand.getValue();
		return value;
	}
	
	public static Complex arrayToComplex(double[]  array) {
		if (array.length != 2)
			throw new ExpressionException("Invalid array length = " + array.length);
		return new Complex(array[re], array[im]); 
	}
}
