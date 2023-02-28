# circum.taq

$ java -jar taq.jar circum radius=1.425

    Running query circumference in global scope 
    Parameters [circum, radius=1.425]
    circumference(circumference=8.94900)

### Description

circum.taq demonstrates making a query to a template using a function call operation. 
The "x_by_factor" template belongs to the "math" scope and is designed to take 2 decimal 
numbers, multiply one by the other and return a decimal result in a term named "product"

> **decimal** product = x `*` factor

The template query has the format of a function call. The fact it performs a query 
is hidden. However, a template can only return a term list. When the list contains only a single term, as is the case 
here, it makes sense to reference the term immediately after the function call returns

> **decimal** pi_times_radius = 
>
>> math.x_by_factor(pi = **decimal** 3.14, radius)->product

This invokes a query to the "x_by_factor" template with parameters "pi" and "radius" 
and the "product" result is extracted from the returned term list. You will notice that
the terms of the "x_by_factor" template scope align with the parameters of the call.


