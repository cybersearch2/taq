# euro-total2.taq

$ java -jar taq.jar euro-total2 amount="12.345,67 €"

```
Running query item_query in belgium_fr scope 
Parameters [euro-total2, amount=12.345,67 €]
item_query(fr_BE, total=13703.69, total_text=le total + impôt: 13 703,69 EUR)

Running query item_query in french scope 
item_query(fr_FR, total=14197.52, total_text=le total + impôt: 14 197,52 EUR)

Running query item_query in german scope 
item_query(
 de_DE,
 total=14567.89,
 total_text=Gesamtkosten + Steuer: 14.567,89 EUR
)

Running query item_query in global scope 
item_query(
 de_DE,
 total=14567.89,
 total_text=Gesamtkosten + Steuer: 14.567,89 EUR
)
item_query(fr_BE, total=13703.69, total_text=le total + impôt: 13 703,69 EUR)
item_query(fr_FR, total=14197.52, total_text=le total + impôt: 14 197,52 EUR)```
```

### Description

euro-total2.taq demonstrates using an axiom list in a scope to apply locale-specific 
data to a query. In addition, each scope has a query for just itself. 

The "charge_plus_tax" template has the benefit of not needing to reference the current 
scope to derive parameters that are locale=specific.
