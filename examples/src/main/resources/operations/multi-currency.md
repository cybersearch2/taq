## multi-currency.taq

```
$ java -jar taq.jar multi-currency

    Running query price_query in global scope 
    format_total(amount=10682.12, total_text=MY Total + gst: MYR 10,682.12)
    format_total(amount=545.81, total_text=QA Total + gst: QAR ٥٤٥٫٨١)
    format_total(amount=510, total_text=IS Total + gst: 510 ISK)
    format_total(amount=5362.50, total_text=FI Total + gst: 5 362,50 EUR)
    format_total(amount=3061.34, total_text=MT Total + gst: EUR3,061.34)
    format_total(amount=1390.41, total_text=CH Total + gst: 1’390.41 CHF)
    format_total(amount=4355.05, total_text=BE Total + gst: 4.355,05 EUR)
    format_total(amount=10505.13, total_text=SA Total + gst: SAR ١٠٬٥٠٥٫١٣)
    format_total(amount=2774.226, total_text=IQ Total + gst: IQD ۲٬۷۷۴٫۲۲۶)
    format_total(amount=3490.46, total_text=PR Total + gst: USD3,490.46)
    format_total(amount=9607, total_text=CL Total + gst: CLP9.607)
    ... (104 items in total)
    format_total(amount=1750.14, total_text=FR Total + gst: 1 750,14 EUR)
```

### Description

multi-currency.taq shows the capability for TAQ to deal with amounts in various currencies. 
The "price_query" query imports file "world_currency.taq" which declares an axiom list 
with each axiom containing a 2-character country code and an amount expressed
in the country's decimal format. Here is how the list starts:

    axiom list price (country, amount)
        {"MY", 9711.02} 
        {"QA", 496.19} 
        {"IS", 464} 
        {"FI", 4875.00} 
        {"MT", 2783.04} 
        {"CH", 1264.01} 
        {"BE", 3959.14} 
        {"SA", 9550.12} 
        {"IQ", 2522.024} 
        {"PR", 3173.15} 
        {"CL", 8734} 
        
The decimal amount is applied to a currency type variable with a locale qualifier which unifies 
with the "country" term fed to the query.

> **currency** @ country amount

The amount is locale-sensitive formated using an operation available to all numeric 
types

> amount.*format()*
