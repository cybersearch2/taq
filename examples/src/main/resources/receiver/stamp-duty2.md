# stamp-duty2.taq

java -jar taq.jar stamp-duty2

```
Running query stamp_duty_query in global scope 
stamp_duty_payable(100077, USD3,789.00, bracket=0, payable=USD20.00)
stamp_duty_payable(100078, USD123,458.00, bracket=5, payable=USD3,768.32)
stamp_duty_payable(100079, USD55,876.33, bracket=6, payable=USD1,285.67)
stamp_duty_payable(100080, USD1,245,890.00, bracket=1, payable=USD62,353.95)
```

### Description

stamp-duty2.taq demonstrates a select receiver template used with a skip on default 
strategy. The "stamp_duty_query" query calculates stamp duty (tax on a real estate purchase)
according to sale amount and the bracket it falls into. There is a 
threshold below which a flat rate applies and no calculation is required. 

The bracket range is open at the top (amount > 500000) and closed at the bottom 
(amount > 5000), so it is amounts under $5,000 to which the default strategy applies. 
The default condition simply results in the sideways execution into the receiver being 
skipped leaving the "duty" variable unchanged from it's initial setting.

Note that the select variables are accessed directly in the receiver and thus 
improving readability

> duty = base + (amount - threshold) * (percent / 100)