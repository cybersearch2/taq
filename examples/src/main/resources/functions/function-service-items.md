# function-service-items.taq

$ java -jar taq.jar function-service-items

    Running query scan_service_items in global scope 
    charges(Service=83057, Amount=USD60.00)
    charges(Service=10800, Amount=USD30.00)
    charges(Service=10661, Amount=USD45.00)
    charges(Service=78587, Amount=USD15.00)
    charges(Service=99585, Amount=USD10.00)
    charges(Service=99900, Amount=USD5.00)
    accumulator(total=165.00)
    
### Description

function-service-items.taq demonstrates a function which returns a term list declared as a "service_amount"
template archetype. This list contains a "service" term to identify the service 
and an "amount" term. Both values are extracted from a line of text passed to the function.

> $ **template\<term\>** service_amount (string service, string amount)
    
Note how the provider is declared is in the "service' scope and the provider Java class is specified 
as a  "provider" scope parameter.

> **scope** service(provider = "service.ServiceProvider") {
>
>> **axiom\<service_amount\>** **function** amount(**string** item)
>
> }

