# high-cities-sorted2.taq

$ java -jar taq.jar high-cities-sorted2

    Running query high_cities in global scope 
    high_cities(name=denver, altitude=5280)
    high_cities(name=flagstaff, altitude=6970)
    high_cities(name=addis ababa, altitude=8000)
    high_cities(name=leadville, altitude=10200)

### Description
    
high-cities-sorted2.taq shows a cursor working to perform an insertion sort, 
The "high_cities" query produces a list of high cities sorted by elevation.
The most notable aspect of cursor usage here is the referencing of the underlying 
list by relative index

> city[1] = city--

The assignment here shuffles the city at the current location to one place higher 
on the list and then decrements the cursor index. This outcome shows the index `[]`
notation is taken as being relative to the current cursor index. 

The bracket notation appears again at the end where, post shuffle, the last city read from 
the axiom source is inserted in the right location

> city[1] = inserted

This works even if the cursor has decremented beyond the start of the list, in which 
case, the value of it's index is = -1.
