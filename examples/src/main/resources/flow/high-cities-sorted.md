# high-cities-sorted.taq

$ java -jar taq.jar high-cities-sorted

    Running query high_cities in global scope 
    high_cities(name=denver, altitude=5280)
    high_cities(name=flagstaff, altitude=6970)
    high_cities(name=addis ababa, altitude=8000)
    high_cities(name=leadville, altitude=10200)

### Description

high-cities-sorted.taq shows a non-trivial loop with 2 exit criteria. The loop performs 
an insert sort on a list of cities so they are ordered by ascending altitude. The start
of the loop is marked by this comment

> // Shuffle list until sort order restored

Early exit from the loop is triggered by sort order being achieved. Another exit 
occurs when there are no more cities to sort. The loop contains a shuffle step which
moves a city one place down the list

>  high_cities[i + 1] = high_cities[i]

The last city is detected by loop variable 'i' decrementing to zero. 
