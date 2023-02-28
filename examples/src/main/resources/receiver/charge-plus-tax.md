# charge-plus-tax.taq

$ java -jar taq.jar charge-plus-tax amount="12.345,67 €"
```
Running query be_fr_item_query in global scope 
Parameters [charge-plus-tax, amount=12.345,67 €]
euro_total(Total=le total 13.703,69 EUR, Tax=11% impôt, Locale=fr_BE)

Running query be_nl_item_query in global scope 
Parameters [charge-plus-tax, amount=12.345,67 €]
euro_total(Total=totale kosten 13.703,69 EUR, Tax=11% belasting, Locale=nl_BE)

Running query de_item_query in global scope 
Parameters [charge-plus-tax, amount=12.345,67 €]
euro_total(Total=Gesamtkosten 14.567,89 EUR, Tax=18% Steuer, Locale=de_DE)

Running query fr_item_query in global scope 
Parameters [charge-plus-tax, amount=12.345,67 €]
euro_total(Total=le total 14 197,52 EUR, Tax=15% impôt, Locale=fr_FR)
```
### Description

charge-plus-tax.taq demonstrates a select receiver template with a map nested inside. 
The "charge_plus_tax" flow produces an invoice total which is locale-specific. The 
"district" select sets locale variables "language"and "region". A receiver attached
to the select maps "region" to "percent" sales tax. The dependency of percent on 
the region is made emphatic by this arrangement

> flow district(region_code) { 

>> percent = map region { ... }

The "lexicon" select also has a receiver attached to it which formats the invoice. 
Inside the receiver template, select variables "total" and "tax" are avaiable without 
having to be declared which is one advantage of using a receiver.