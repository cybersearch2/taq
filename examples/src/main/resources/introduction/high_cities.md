# high_cities.taq

$ java -jar taq.jar high_cities
    
    Running query high_cities in global scope 
    high_city(city=addis ababa, altitude=8000)
    high_city(city=denver, altitude=5280)
    high_city(city=flagstaff, altitude=6970)
    high_city(city=leadville, altitude=10200)

### Description

high_cities.taq demonstrates a simple TAQ query as an introduction to the TAQ language.
The goal of a query is to take the facts input to it and apply logic to arrive at new facts. 
A fact is packaged in a structure called an "axiom" and new facts are manufactured 
in a structure called a "template". 

The "high_cities" query feeds an axiom list containing city names and altitudes to 
the "high_city" template to create a new "high_cities" axiom list containing only
those cities which are above 5,000 feet elevation. The logic to select which cities 
are accepted is expressed in the first template term as:

> city ? altitude > 5000
   