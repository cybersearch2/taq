# foreign-total.taq

$ java -jar taq.jar foreign-total amount="12.345,67 €"

```
Running query item_query in french scope 
Parameters [foreign-total, amount=12.345,67 €]
item_query(total_text=le total + tax: 13 580,24 EUR)

Running query item_query in german scope 
Parameters [foreign-total, amount=12.345,67 €]
item_query(total_text=Gesamtkosten + tax: 13.580,24 EUR)
```

### Description

foreign-total.taq demonstrates using a context list to perform language translation. 
This is a simple case of translating the word "total" into French "le total" or German 
"Gesamtkosten".

The context list is named "lexicon"

> $ **list\<term\>** lexicon

There are 2 foreign scopes "german" and "french", the names reflecting the languages 
to be translated.  The "item_query" query of each scope creates a statement which begins with the 
foreign word for "total". Each scope declares a "lexicon" axiom

```
axiom german.lexicon 
  ( Total)
  {"Gesamtkosten"}
```

The translation is just a matter of referencing the context list of the current sxope

> lexicon->Total

