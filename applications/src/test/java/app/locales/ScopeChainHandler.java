package app.locales;

import java.util.List;

import app.AppCompletionHandler;

public class ScopeChainHandler extends AppCompletionHandler {

	String[] expected = new String[] { 
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

	@Override
	public void onAppComplete(List<String> lineBuffer) {
		compareArray(expected, lineBuffer);
	}
}
