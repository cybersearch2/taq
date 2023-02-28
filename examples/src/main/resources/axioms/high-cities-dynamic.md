# high-cities-dynamic.taq

$ java -jar taq.jar high-cities-dynamic

    Running query cities in global scope 
    high_cities(name=addis ababa, altitude=8000)
    high_cities(name=denver, altitude=5280)
    high_cities(name=flagstaff, altitude=6970)
    high_cities(name=leadville, altitude=10200)

### Description

high-cities-dynamic.taq demonstrates exporting an axiom list as a means to returning 
a query result. This approach allows the axiom list to be accessed while processing 
the query. It also provides a channel to output additional information from a query 
such as statistics. However, this example just shows how to export a list.

The first term of the "high_city" is a dynamic axiom list declaration wuth an **export**
qualification

> **export list\<axiom\>** high_cities

It is progressively populated as each high city is encountered from 
the incoming "city" axiom list. The list is accessed from the query result using the 
full name of the list as the key, which in this case is "high_cdy.high_cities".
