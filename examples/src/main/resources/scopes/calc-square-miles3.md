# calc-square-miles3.taq

$ java -jar taq.jar calc-square-miles3
```
Running query au_surface_area_query in global scope 
country_area(country=Australia, surface_area=7741220.0, units=km2)
country_area(country=United States, surface_area=9831510.0, units=km2)

Running query us_surface_area_query in global scope 
country_area(country=Australia, surface_area=2988885.042, units=mi2)
country_area(country=United States, surface_area=3795946.011, units=mi2)

Running query xx_surface_area_query in global scope 
country_area(country=Australia, surface_area=7741220.0, units=km2)
country_area(country=United States, surface_area=9831510.0, units=km2)
```

### Description

calc-square-miles3.taq demonstrates usage of a scope property and how the absence of 
a property is handled. There are 3 queries which all display the surface area of both 
the USA and Australia. A "location" scope property is read to determine if the normal 
unit of area, square kilometers, should be changed to imperial square miles. The latter 
case applies if location = "United States", Comparing the "au_surface_area_query" for 
Australia to "us_surface_area_query" for USA in the console, the results are as expected.

The "xx_surface_area_query" is in the global scope which does not have a "location" 
property. This is handled gracefully and results are given in square kilometers. When 
a non-existent scope variable is read, a blank is returned. A blank can be detected 
by matching it to an empty string. Providing a default value for a nissing scope 
property is therefore easy as a blank is allowed to be updated to a value of any type. 
.
