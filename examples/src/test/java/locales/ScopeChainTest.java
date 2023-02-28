package locales;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.taq.helper.Taq;

public class ScopeChainTest {

	private static final String ITEM_QUERY = "item_query";

	private static final String[] FOREIGN_ITEMS = {
			"item(",
			" mug,",
			" charge_plus_tax(de_DE, Gesamtkosten 14.567,89 EUR, 18% Steuer)",
			" charge_plus_tax(fr_FR, le total 14 197,52 EUR, 15% impôt)",
			" charge_plus_tax(fr_BE, le total 13 703,69 EUR, 11% impôt)",
			" charge_plus_tax(nl_BE, totale kosten EUR 13.703,69, 11% belasting)",
			")",
			"item(",
			" cap,",
			" charge_plus_tax(de_DE, Gesamtkosten 10.735,65 EUR, 18% Steuer)",
			" charge_plus_tax(fr_FR, le total 10 462,71 EUR, 15% impôt)",
			" charge_plus_tax(fr_BE, le total 10 098,79 EUR, 11% impôt)",
			" charge_plus_tax(nl_BE, totale kosten EUR 10.098,79, 11% belasting)",
			")",
			"item(",
			" t-shirt,",
			" charge_plus_tax(de_DE, Gesamtkosten 659,61 EUR, 18% Steuer)",
			" charge_plus_tax(fr_FR, le total 642,84 EUR, 15% impôt)",
			" charge_plus_tax(fr_BE, le total 620,48 EUR, 11% impôt)",
			" charge_plus_tax(nl_BE, totale kosten EUR 620,48, 11% belasting)",
			")"
	};
	
	@Before
	public void setUp() throws IOException {
		Taq.initialize();
		Taq.setQuietMode();
	}
	
	@Test
	public void testScopeChain() throws IOException {
    	List<String> args = new ArrayList<>();
    	args.add("scope-chain");
        Taq taq = new Taq(args);
		taq.findFile();
		taq.compile();
		List<String> captureList = taq.getCaptureList();
		List<String> expected = Arrays.asList(FOREIGN_ITEMS);
		boolean success = false;
		if (!taq.execute(ITEM_QUERY)) {
			System.err.println(String.format("Query %s not found", ITEM_QUERY));
		} else {
			int index = 0;
			Iterator<String> iterator = captureList.iterator();
	        while (iterator.hasNext()) {
	        	checkSolution(expected.get(index++), iterator.next());
	        	success = true;
	        }
		}
        assertThat(success).isTrue();
	}
	
	private void checkSolution(String solution, String output)
    {
        assertThat(output).isEqualTo(solution);
    }

}
