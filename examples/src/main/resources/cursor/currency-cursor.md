# currency-cursor.taq

$ java -jar taq.jar currency-cursor

    Running query parse_amounts in global scope 
    14567.89
    14197.52
    590.00
    amounts(items=[14567.89, 14197.52, 590.00], total="29.355,41 EUR")

currency-cursor.taq demonstrates a currency cursor assigned to the Euro currency locale
designated by the country code "EUR"

> cursor<currency.EUR> euro_amount(euro_amounts)

The query axiom source is a list named "euro_amounts". Values are presented in text format 
and the cursor converts each one to a currency type as it traverses the list. The cursor sums 
the individual amounts in a sideways evaluation flow. This would not be possible if 
the cursor did not perform type conversion on the fly

Note that because the exported "amounts" term list references the "amount_list" list, it gets 
exported to as a side effect.


