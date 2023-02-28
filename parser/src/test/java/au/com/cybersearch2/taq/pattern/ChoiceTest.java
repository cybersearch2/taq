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
package au.com.cybersearch2.taq.pattern;

import org.junit.Test;

import au.com.cybersearch2.taq.QueryProgram;
import au.com.cybersearch2.taq.interfaces.SolutionHandler;
import au.com.cybersearch2.taq.query.Solution;

import static org.assertj.core.api.Assertions.*;

/**
 * ChoiceTest
 * @author Andrew Bowley
 * 6 Sep 2015
 */
public class ChoiceTest
{
    static final String CHOICE_COLORS =
        "axiom list shades (name) {\"aqua\"} {\"blue\"} {\"orange\"}\n" +
        "select swatch\n" +
        "  ( color,     red, green, blue)\n" +
        "{\n" +
        "  ? \"aqua\":  0,   255,   255\n" +
        "  ? \"black\": 0,   0,     0 \n" +
        "  ? \"blue\":  0,   0,     255\n" +
        "  ? \"white\": 255, 255,   255\n" +
        "}\n" +
        "flow shader\n" +
        "(\n" +
        "  color = name,\n" +   
        "  red, green, blue,\n" +
        ". flow swatch(name),\n" +
        "  index = swatch.index()\n" +
        ")\n" +
        "query color_query (shades : shader)\n";

    static final String[] CHOICE_COLORS_LIST =
    {
        "shader(color=aqua, red=0, green=255, blue=255, index=0)",
        "shader(color=blue, red=0, green=0, blue=255, index=2)",
        "shader(color=orange, index=-1)"
    };

    static final long[] CHOICE_SELECTION_LIST = { 0, 2, -1 };
    
    static final String STAMP_DUTY =
            "axiom list transacton_amount (amount)\n" +
            "{123458.00}\n" +
            "{55876.33}\n" +
            "{1245890.00}\n" +
            "select bracket " +
            "( amount,           threshold, base, percent)\n" +
            "{\n" +
            "    ? <  12000:      0,     0.00, 1.00\n" +
            "    ? <  30000:  12000,   120.00, 2.00\n" +
            "    ? <  50000:  30000,   480.00, 3.00\n" +
            "    ? < 100000:  50000,  1080.00, 3.50\n" +
            "    ? < 200000: 100000,  2830.00, 4.00\n" +
            "    ? < 250000: 200000,  6830.00, 4.25\n" +
            "    ? < 300000: 250000,  8955.00, 4.75\n" +
            "    ? < 500000: 300000, 11330.00, 5.00\n" +
            "    ? > 500000: 500000, 21330.00, 5.50\n" +
            "}\n" +
            "flow stamp_duty_payable(\n" +
            "  currency amount,\n" +
            ". bracket(amount),\n" +
            "  index = bracket.index(),\n" +
            "  currency duty = base + (amount - threshold) * (percent / 100),\n" +
            "  string display = duty.format()\n" +
            ")\n" +
            "query stamp_duty_query (transacton_amount : stamp_duty_payable)\n";

    static final String[] STAMP_DUTY_LIST =
    {
        "stamp_duty_payable(amount=123458.0, index=4, duty=3768.320, display=AUD3,768.32)",
        "stamp_duty_payable(amount=55876.33, index=3, duty=1285.67155, display=AUD1,285.67)",
        "stamp_duty_payable(amount=1245890.0, index=8, duty=62353.9500, display=AUD62,353.95)"
    };
    
    @Test
    public void test_stamp_duty()
    {
        QueryProgram queryProgram = new QueryProgram();
        queryProgram.parseScript(STAMP_DUTY);
        int[] index = new int[] {0};
        queryProgram.executeQuery("stamp_duty_query", new SolutionHandler(){
            @Override
            public boolean onSolution(Solution solution)
            {
                //System.out.println(solution.getAxiom("stamp_duty_payable").toString());
                assertThat(solution.getAxiom("stamp_duty_payable").toString()).isEqualTo(STAMP_DUTY_LIST[index[0]++]);
                return true;
            }});
        assertThat(index[0]).isEqualTo(3);
    }

    @Test
    public void test_choice_colors()
    {
        QueryProgram queryProgram = new QueryProgram();
        queryProgram.parseScript(CHOICE_COLORS);
        // Create QueryParams object for Global scope and query "stamp_duty_query"
        queryProgram.executeQuery("color_query", new SolutionHandler(){
            int index = 0;
            @Override
            public boolean onSolution(Solution solution) {
                //System.out.println(solution.getAxiom("shader").toString());
                assertThat(solution.getAxiom("shader").toString()).isEqualTo(CHOICE_COLORS_LIST[index]);
                ++index;
                return true;
            }});
     }

}
