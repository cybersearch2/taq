# black-is-white.taq

$ java -jar taq.jar black-is-white

Running query color_query in global scope 
color_query((name="white", red=255, green=255, blue=255))

Running query dyna_query in global scope 
dyna_query(dyna_list(name=white, red=255, green=255, blue=255))

Running query list_query in global scope 
list_query(axiom_list(name="white", red=255, green=255, blue=255))

### Description

black-is-white.taq shows updates of term values in three different contexts.
Each demonstration changes the color "black" into "white". The color is 
specified by both name and red-green-blue components. 

The "color_query" query selects the "inverse" template to transform axiom "color_axiom". 
The white color values are returned as expected. 

The "dyna_query" query selects the "dyna_inverse" template to change the first item of 
dynamic axiom list "dyna_list". he white color values are returned as expected. 

The "list_query" query selects the "list_inverse" template to change the first item 
of the "axiom_list" axiom list.  The white color values are returned as expected. 
