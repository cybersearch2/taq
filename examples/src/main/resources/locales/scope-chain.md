# scope-chain.taq

$ java -jar taq.jar scope-chain

```
Running query item_query in global scope 
item(
 mug,
 charge_plus_tax(de_DE, Gesamtkosten 14.567,89 EUR, 18% Steuer)
 charge_plus_tax(fr_FR, le total 14 197,52 EUR, 15% impôt)
 charge_plus_tax(fr_BE, le total 13 703,69 EUR, 11% impôt)
 charge_plus_tax(nl_BE, totale kosten 13.703,69 EUR, 11% belasting)
)
item(
 cap,
 charge_plus_tax(de_DE, Gesamtkosten 10.735,65 EUR, 18% Steuer)
 charge_plus_tax(fr_FR, le total 10 462,71 EUR, 15% impôt)
 charge_plus_tax(fr_BE, le total 10 098,79 EUR, 11% impôt)
 charge_plus_tax(nl_BE, totale kosten 10.098,79 EUR, 11% belasting)
)
item(
 t-shirt,
 charge_plus_tax(de_DE, Gesamtkosten 659,61 EUR, 18% Steuer)
 charge_plus_tax(fr_FR, le total 642,84 EUR, 15% impôt)
 charge_plus_tax(fr_BE, le total 620,48 EUR, 11% impôt)
 charge_plus_tax(nl_BE, totale kosten 620,48 EUR, 11% belasting)
)
```

### Description

scope-chain.taq demonstrates a sequence of records being processed by a template 
in several scopes, This is achieved using a query chain where the final link
aggregates the output from the preceding links.

There are 4 european scopes declared and the "item_query" query invokes the
"charge_plus_tax" template for each one. A final "item" template collects each
solution axiom using a variable with a qualified template name, which is a 2-part
name with an at @ symbol placed in front

> term \@german.charge_plus_tax
