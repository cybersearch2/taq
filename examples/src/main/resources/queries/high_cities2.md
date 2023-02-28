# high_cities2.taq

$ java -jar taq.jar high_cities2
    
    Running query high_cities in global scope 
    high_city(city=addis ababa, altitude=8000)
    high_city(city=denver, altitude=5280)
    high_city(city=flagstaff, altitude=6970)
    high_city(city=leadville, altitude=10200)

### Description

high_cities2.taq demonstrates query paraemterization.The "high_cities" query 
returns a list of cities which are above 5,000 feet elevation. The height 
threshold is set as a query parameter appended to the query declaration.
This makes it easier to change the threshold at a later date.

   