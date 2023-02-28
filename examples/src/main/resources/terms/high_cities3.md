@ high_cities3.taq

$ java -jar taq.jar high_cities3

```
Running query high_cities in global scope 
high_city(addis ababa, 8000)
high_city(denver, 5280)
high_city(flagstaff, 6970)
high_city(leadville, 10200)
```

### Description

high_cities3.taq demonstrates how to pass anonymous terms to the solution.
This is achieved using the **term** keyword which has the effect of making
the term both anonymous and non-private, 

The "high_cities" query feeds an axiom list containing city names and elevations to 
the "high_city" template to create a new "high_cities" axiom list containing only
those cities which are above 5,000 feet elevation. The incoming axioms have terms
named "city" and "elevation". These are made private using the period '**.**', 
operator. These variables are then repeated but prefaced by the  keyword 
**term**, so they can be displayed as anonymous terms.

> **term** city, **term** elevation



