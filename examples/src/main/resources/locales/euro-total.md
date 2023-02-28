# euro-total.taq

$ java -jar taq.jar euro-total  amount="12.345,67 €"
```
Running query be_fr_item_query in global scope 
Parameters [euro-total, amount=12.345,67 €]
totals(Total=le total 13 703,69 EUR, Tax=11% impôt, Locale=fr_BE)

Running query de_item_query in global scope 
Parameters [euro-total, amount=12.345,67 €]
totals(Total=Gesamtkosten 14.567,89 EUR, Tax=18% Steuer, Locale=de_DE)

Running query fr_item_query in global scope 
Parameters [euro-total, amount=12.345,67 €]
totals(Total=le total 14 197,52 EUR, Tax=15% impôt, Locale=fr_FR)
```
### Description

euro-total.taq demonstrates the role scopes can play in providing information about 
locale. Three queries are each assigned a specific locale to create a total 
amount invoice with language and regional adaptions. Here is how the French scope 
is declared

> **scope** french (language="fr", region="FR") 

Note that language and region are attributes that all scopes have and are accessed using 
a 2-part name notation that is distinct from that used for properties 

> **scope**.region,\
> **scope**.language,

The total amount includes a sales tax applicable to the region and 
is stated using the locale language. 
