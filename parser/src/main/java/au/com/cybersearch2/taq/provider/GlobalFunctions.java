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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import au.com.cybersearch2.taq.expression.ExpressionException;
import au.com.cybersearch2.taq.language.BigDecimalTerm;
import au.com.cybersearch2.taq.language.TaqLiteral;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.operator.OperatorTerm;

/**
 * Global functions available by object interface
 *
 */
public class GlobalFunctions {

    static final String RANDOM_BAD_BOUND = "random bound must be positive";
    
    private static Set<String> methods;
    private static List<String> printCaptureList;

    static {
		methods = new TreeSet<>();
		methods.add("now");
		methods.add("print");
		methods.add("reveal");
		methods.add("random");
		methods.add(TaqLiteral.decimal.name());
    }
    
	public GlobalFunctions() {
	}

	public String reveal(Term... terms) {
		StringBuilder builder = new StringBuilder();
		Arrays.asList(terms).forEach(term -> {
	    	if (term instanceof OperatorTerm) {
	    		OperatorTerm operatorTerm = (OperatorTerm)term;
	    		String value = operatorTerm.getOperator().getTrait().formatValue(term.getValue());
	    		builder.append(unescapeJavaString(value));
	    	} else
	    		builder.append(unescapeJavaString(term.getValue().toString()));
		});
    	return builder.toString();
	}

	public String print(Term... terms) {
		String text = reveal(terms);
    	if (printCaptureList != null)
    		printCaptureList.add(text);
    	else
            System.out.println(text);
        return text;
	}
	
	/**
	 * Returns a pseudo random, uniformly distributed {@code int} value
     * between 0 (inclusive) and the specified value (exclusive), drawn from
     * this random number generator's sequence.  
     * @param bound the upper bound (exclusive).  Must be positive.
     * @return the next pseudo random, uniformly distributed {@code int}
     *         value between zero (inclusive) and {@code bound} (exclusive)
     *         from this random number generator's sequence
     * @throws ExpressionException if bound is not positive
	 */
    public long random(Long bound) {
        if (bound <= 0)
            throw new ExpressionException(RANDOM_BAD_BOUND);
        long value = (long)new Random().nextInt(bound.intValue());
        return value;
    }

    public Instant now() {
    	return Instant.now();
    }
 
    public BigDecimalTerm decimal(String value) {
    	return new BigDecimalTerm(value);
    }
    
    public static boolean isMethod(String method) {
    	return methods.contains(method);
    }
    
    public static List<String> printCapture() {
     	printCaptureList = new ArrayList<>();
    	return printCaptureList;
    }
    
    /**
     * From https://gist.github.com/uklimaschewski/6741769
     *
     * Unescapes a string that contains standard Java escape sequences.
     * <ul>
     * <li><strong>\b \f \n \r \t \" \'</strong> :
     * BS, FF, NL, CR, TAB, double and single quote.</li>
     * <li><strong>\X \XX \XXX</strong> : Octal character
     * specification (0 - 377, 0x00 - 0xFF).</li>
     * <li><strong>\\uXXXX</strong> : Hexadecimal based Unicode character.</li>
     * </ul>
     * 
     * @param st
     *            A string optionally containing standard java escape sequences.
     * @return The translated string.
     */
    private String unescapeJavaString(String st) {

        StringBuilder sb = new StringBuilder(st.length());

        for (int i = 0; i < st.length(); i++) {
            char ch = st.charAt(i);
            if (ch == '\\') {
                char nextChar = (i == st.length() - 1) ? '\\' : st
                        .charAt(i + 1);
                // Octal escape?
                if (nextChar >= '0' && nextChar <= '7') {
                    String code = "" + nextChar;
                    i++;
                    if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                            && st.charAt(i + 1) <= '7') {
                        code += st.charAt(i + 1);
                        i++;
                        if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                                && st.charAt(i + 1) <= '7') {
                            code += st.charAt(i + 1);
                            i++;
                        }
                    }
                    sb.append((char) Integer.parseInt(code, 8));
                    continue;
                }
                switch (nextChar) {
                case '\\':
                    ch = '\\';
                    break;
                case 'b':
                    ch = '\b';
                    break;
                case 'f':
                    ch = '\f';
                    break;
                case 'n':
                    ch = '\n';
                    break;
                case 'r':
                    ch = '\r';
                    break;
                case 't':
                    ch = '\t';
                    break;
                case '\"':
                    ch = '\"';
                    break;
                case '\'':
                    ch = '\'';
                    break;
                // Hex Unicode: u????
                case 'u':
                    if (i >= st.length() - 5) {
                        ch = 'u';
                        break;
                    }
                    int code = Integer.parseInt(
                            "" + st.charAt(i + 2) + st.charAt(i + 3)
                                    + st.charAt(i + 4) + st.charAt(i + 5), 16);
                    sb.append(Character.toChars(code));
                    i += 5;
                    continue;
                }
                i++;
            }
            sb.append(ch);
        }
        return sb.toString();
    }
}
