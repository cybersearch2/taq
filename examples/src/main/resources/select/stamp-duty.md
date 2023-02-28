# stamp-duty.taq

$ java -jar taq.jar stamp-duty

    Running query stamp_duty_query in global scope 
    stamp_duty_payable(100077, USD3,789.00, bracket=0, payable=USD20.00)
    stamp_duty_payable(100078, USD123,458.00, bracket=5, payable=USD3,768.32)
    stamp_duty_payable(100079, USD55,876.33, bracket=6, payable=USD1,285.67)
    stamp_duty_payable(100080, USD1,245,890.00, bracket=1, payable=USD62,353.95)
    
### Description

stamp-duty.taq demonstrates a select default strategy where an execution step is skipped when
none of the available choices match.  The "stamp_duty_query" query calculates stamp 
duty, a form of tax applying to real estate, according to sale amount and the bracket
it falls into. There is a threshold below which a flat rate applies and no calculation is required. 

The bracket range is open at the top `amount > 500000` and closed at the bottom 
`amount > 5000`, so it is amounts under $5,000 to which the default strategy applies. 
The default condition is found checking the select index to see if it is the default 
value which equates to a bracket number of 0

> // Flat rate of $20 applies \
>    term flat = 0

> ? bracket != flat {

>> *calculate duty*... }

