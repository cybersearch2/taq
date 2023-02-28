# receive-service-items.taq

 java -jar taq.jar receive-service-items
 
    Running query scan_service_items in global scope 
    charges(Service=83057, Amount=USD60.00)
    charges(Service=10800, Amount=USD30.00)
    charges(Service=10661, Amount=USD45.00)
    charges(Service=78587, Amount=USD15.00)
    charges(Service=99585, Amount=USD10.00)
    charges(Service=99900, Amount=USD5.00)
    accumulator(total=165.00)
    
### Description

receive-service-items.taq demonstrates a receiver template attached to function call. 
The scan_service_items query uses function service.amount() to convert a list of service 
charges expressed in text format into records containing separate service identity 
and amount fields. These records are exported by the query along with a total amount for all 
services.

The advantage of using a receiver in this case is that the terms of the axiom returned 
by the function call are accessed directly. This is the archetype for the axiom

> $ **template\<term\>** service_amount (**string** service, **string** amount) 

The function is called without assignment to an axiom variable

>  **flow** service.amount(item++)

The receiver works with variables named "service" (not declared) and "amount" (declared 
as a currency type but not assigned a value). The latter declaration is for type conversion 
as amount is initially delivered as a string.